/**
 * ExamSeat AI - Examination Management Logic
 */
const ExamsView = {
  examsList: [],
  allHalls: [],

  async load() {
    await this.fetchHalls();
    await this.fetchExams();
  },

  async fetchHalls() {
    try {
      const res = await Auth.fetchWithAuth('/api/halls/active');
      const json = await res.json();
      if (json.success) {
        this.allHalls = json.data;
      }
    } catch (e) {
      console.error(e);
    }
  },

  async fetchExams() {
    try {
      const res = await Auth.fetchWithAuth('/api/exams');
      const json = await res.json();
      if (json.success) {
        this.examsList = json.data;
        this.renderTable();
      }
    } catch (e) {
      App.showToast('Failed to load exams: ' + e.message, 'error');
    }
  },

  renderTable() {
    const tbody = document.getElementById('exams-table-body');
    if (!tbody) return;

    if (this.examsList.length === 0) {
      tbody.innerHTML = `<tr><td colspan="8" style="text-align:center; padding:2rem; color:var(--gray-500);">No examinations scheduled. Click "Schedule Exam" to create one.</td></tr>`;
      return;
    }

    const canEdit = Auth.isStaffOrAdmin();

    tbody.innerHTML = this.examsList.map(ex => {
      const depts = (ex.departments || []).map(d => `${d.department} (S${d.semester})`).join(', ');
      const halls = (ex.halls || []).map(h => h.hallName).join(', ');
      const isCapacityShort = ex.totalCapacity < ex.eligibleStudentsCount;

      return `
        <tr>
          <td><strong>${App.escapeHtml(ex.name)}</strong></td>
          <td>${ex.examDate} <span class="badge badge-draft">${ex.session}</span></td>
          <td>${ex.startTime.substring(0, 5)} - ${ex.endTime.substring(0, 5)}<br><small style="color:var(--gray-500)">Report: ${ex.reportingTime.substring(0, 5)}</small></td>
          <td><small>${App.escapeHtml(depts || 'None')}</small></td>
          <td><small>${App.escapeHtml(halls || 'None')}</small></td>
          <td>
            <strong>${ex.eligibleStudentsCount}</strong> students / 
            <span style="color:${isCapacityShort ? 'var(--danger-600)' : 'var(--primary-700)'}; font-weight:700;">${ex.totalCapacity} seats</span>
            ${isCapacityShort ? '<br><small style="color:var(--danger-600); font-weight:600;">⚠️ Insufficient seats</small>' : ''}
          </td>
          <td><span class="badge badge-${ex.status.toLowerCase()}">${ex.status.replace(/_/g, ' ')}</span></td>
          <td>
            <button class="btn btn-sm btn-accent" onclick="window.location.hash='seating'; SeatingView.selectExam('${ex.id}');" title="Go to seating arrangement">Seating</button>
            ${canEdit && ex.status !== 'FINALIZED' && ex.status !== 'CANCELLED' ? `
              <button class="btn btn-sm btn-secondary" onclick="ExamsView.editExam('${ex.id}')">Edit</button>
              <button class="btn btn-sm btn-outline-danger" onclick="ExamsView.cancelExam('${ex.id}', '${App.escapeHtml(ex.name)}')">Cancel</button>
            ` : ''}
          </td>
        </tr>
      `;
    }).join('');
  },

  openAddModal() {
    document.getElementById('exam-modal-title').textContent = 'Schedule New Examination';
    document.getElementById('exam-id').value = '';
    document.getElementById('exam-form').reset();
    this.renderHallsCheckboxes([]);
    this.renderDepartmentInputs([{ department: 'Computer Science', semester: 5 }]);
    App.openModal('exam-modal');
  },

  renderHallsCheckboxes(selectedHallIds = []) {
    const container = document.getElementById('exam-halls-checkboxes');
    if (!container) return;

    container.innerHTML = this.allHalls.map(h => `
      <label style="display:flex; align-items:center; gap:0.5rem; font-size:0.85rem; padding:4px 0;">
        <input type="checkbox" name="exam-hall-checkbox" value="${h.id}" ${selectedHallIds.includes(h.id) ? 'checked' : ''} onchange="ExamsView.updateCapacityIndicator()">
        <span>${App.escapeHtml(h.name)} (${h.capacity} seats)</span>
      </label>
    `).join('');

    this.updateCapacityIndicator();
  },

  renderDepartmentInputs(deptList = []) {
    const container = document.getElementById('exam-dept-rows-container');
    if (!container) return;

    container.innerHTML = deptList.map((d, idx) => `
      <div class="dept-input-row" style="display:flex; gap:0.5rem; margin-bottom:0.5rem;">
        <input type="text" class="form-control" name="dept-name" value="${App.escapeHtml(d.department)}" placeholder="Department (e.g. Computer Science)" required>
        <input type="number" class="form-control" name="dept-sem" value="${d.semester}" min="1" max="12" style="width:100px;" placeholder="Sem" required>
        <button type="button" class="btn btn-sm btn-secondary" onclick="this.parentElement.remove();">✕</button>
      </div>
    `).join('');
  },

  addDeptRow() {
    const container = document.getElementById('exam-dept-rows-container');
    const row = document.createElement('div');
    row.className = 'dept-input-row';
    row.style.cssText = 'display:flex; gap:0.5rem; margin-bottom:0.5rem;';
    row.innerHTML = `
      <input type="text" class="form-control" name="dept-name" placeholder="Department" required>
      <input type="number" class="form-control" name="dept-sem" value="5" min="1" max="12" style="width:100px;" placeholder="Sem" required>
      <button type="button" class="btn btn-sm btn-secondary" onclick="this.parentElement.remove();">✕</button>
    `;
    container.appendChild(row);
  },

  updateCapacityIndicator() {
    const checkboxes = document.querySelectorAll('input[name="exam-hall-checkbox"]:checked');
    let totalCap = 0;
    checkboxes.forEach(cb => {
      const hall = this.allHalls.find(h => h.id === cb.value);
      if (hall) totalCap += hall.capacity;
    });

    const indicator = document.getElementById('exam-capacity-indicator');
    if (indicator) {
      indicator.textContent = `Selected Total Hall Capacity: ${totalCap} seats`;
    }
  },

  async editExam(id) {
    try {
      const res = await Auth.fetchWithAuth(`/api/exams/${id}`);
      const json = await res.json();
      if (!json.success) throw new Error(json.message);

      const ex = json.data;
      document.getElementById('exam-modal-title').textContent = 'Edit Examination';
      document.getElementById('exam-id').value = ex.id;
      document.getElementById('exam-name').value = ex.name;
      document.getElementById('exam-date').value = ex.examDate;
      document.getElementById('exam-session').value = ex.session;
      document.getElementById('exam-start-time').value = ex.startTime.substring(0, 5);
      document.getElementById('exam-end-time').value = ex.endTime.substring(0, 5);
      document.getElementById('exam-reporting-time').value = ex.reportingTime.substring(0, 5);
      document.getElementById('exam-instructions').value = ex.instructions || '';

      const selectedHallIds = (ex.halls || []).map(h => h.hallId);
      this.renderHallsCheckboxes(selectedHallIds);
      this.renderDepartmentInputs(ex.departments || []);

      App.openModal('exam-modal');
    } catch (e) {
      App.showToast(e.message, 'error');
    }
  },

  async saveExam(e) {
    e.preventDefault();
    const id = document.getElementById('exam-id').value;

    const hallCheckboxes = document.querySelectorAll('input[name="exam-hall-checkbox"]:checked');
    const hallIds = Array.from(hallCheckboxes).map(cb => cb.value);

    if (hallIds.length === 0) {
      App.showToast('Please select at least one examination hall.', 'warning');
      return;
    }

    const deptRows = document.querySelectorAll('.dept-input-row');
    const departments = [];
    deptRows.forEach(row => {
      const deptName = row.querySelector('input[name="dept-name"]').value.trim();
      const sem = parseInt(row.querySelector('input[name="dept-sem"]').value);
      if (deptName && sem) {
        departments.push({ department: deptName, semester: sem });
      }
    });

    if (departments.length === 0) {
      App.showToast('Please add at least one department and semester cohort.', 'warning');
      return;
    }

    const payload = {
      name: document.getElementById('exam-name').value.trim(),
      examDate: document.getElementById('exam-date').value,
      session: document.getElementById('exam-session').value,
      startTime: document.getElementById('exam-start-time').value + ':00',
      endTime: document.getElementById('exam-end-time').value + ':00',
      reportingTime: document.getElementById('exam-reporting-time').value + ':00',
      instructions: document.getElementById('exam-instructions').value.trim(),
      departments: departments,
      hallIds: hallIds
    };

    try {
      const url = id ? `/api/exams/${id}` : '/api/exams';
      const method = id ? 'PUT' : 'POST';

      const res = await Auth.fetchWithAuth(url, {
        method: method,
        body: JSON.stringify(payload)
      });
      const json = await res.json();
      if (!res.ok || !json.success) throw new Error(json.message);

      App.showToast(json.message || 'Exam schedule saved successfully', 'success');
      App.closeModal('exam-modal');
      this.load();
    } catch (err) {
      App.showToast(err.message, 'error');
    }
  },

  async cancelExam(id, name) {
    if (!confirm(`Are you sure you want to cancel examination "${name}"? Existing seating allocations will be reset.`)) return;

    try {
      const res = await Auth.fetchWithAuth(`/api/exams/${id}/cancel`, { method: 'POST' });
      const json = await res.json();
      if (!res.ok || !json.success) throw new Error(json.message);

      App.showToast('Examination cancelled', 'success');
      this.load();
    } catch (err) {
      App.showToast(err.message, 'error');
    }
  }
};
