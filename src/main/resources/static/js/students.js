/**
 * ExamSeat AI - Student Management & CSV Import Logic
 */
const StudentsView = {
  currentPage: 0,
  pageSize: 15,
  totalPages: 1,
  selectedFile: null,

  async load() {
    await this.fetchStudents();
  },

  async fetchStudents(page = 0) {
    this.currentPage = page;
    const search = document.getElementById('student-search-input')?.value.trim() || '';
    const dept = document.getElementById('student-filter-dept')?.value || '';
    const sem = document.getElementById('student-filter-sem')?.value || '';

    let url = `/api/students?page=${this.currentPage}&size=${this.pageSize}&sortBy=rollNo&sortDir=asc`;
    if (search) url += `&search=${encodeURIComponent(search)}`;
    if (dept) url += `&department=${encodeURIComponent(dept)}`;
    if (sem) url += `&semester=${encodeURIComponent(sem)}`;

    try {
      const res = await Auth.fetchWithAuth(url);
      const json = await res.json();
      if (json.success) {
        this.renderTable(json.data);
      }
    } catch (e) {
      App.showToast('Failed to load students: ' + e.message, 'error');
    }
  },

  renderTable(pagedData) {
    const tbody = document.getElementById('students-table-body');
    if (!tbody) return;

    this.totalPages = pagedData.totalPages || 1;
    document.getElementById('students-page-info').textContent =
      `Page ${pagedData.page + 1} of ${Math.max(1, pagedData.totalPages)} (${pagedData.totalElements} students)`;

    document.getElementById('btn-student-prev').disabled = pagedData.page <= 0;
    document.getElementById('btn-student-next').disabled = pagedData.last;

    if (!pagedData.content || pagedData.content.length === 0) {
      tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; padding:2rem; color:var(--gray-500);">No students found matching current filters.</td></tr>`;
      return;
    }

    const canEdit = Auth.isStaffOrAdmin();

    tbody.innerHTML = pagedData.content.map(s => `
      <tr>
        <td><strong>${App.escapeHtml(s.rollNo)}</strong></td>
        <td>${App.escapeHtml(s.name)}</td>
        <td><span class="dept-tag ${this.getDeptClass(s.department)}">${App.escapeHtml(s.department)}</span></td>
        <td>Semester ${s.semester}</td>
        <td>${s.email ? App.escapeHtml(s.email) : '<span style="color:var(--gray-400);">-</span>'}</td>
        <td>
          ${canEdit ? `
            <button class="btn btn-sm btn-secondary" onclick="StudentsView.editStudent('${s.id}')">Edit</button>
            <button class="btn btn-sm btn-outline-danger" onclick="StudentsView.deactivateStudent('${s.id}', '${App.escapeHtml(s.rollNo)}')">Deactivate</button>
          ` : '-'}
        </td>
      </tr>
    `).join('');
  },

  getDeptClass(dept) {
    if (!dept) return 'dept-gen';
    const d = dept.toLowerCase();
    if (d.includes('computer')) return 'dept-cs';
    if (d.includes('electro')) return 'dept-ec';
    if (d.includes('mech')) return 'dept-me';
    if (d.includes('civil')) return 'dept-cv';
    if (d.includes('info')) return 'dept-it';
    return 'dept-gen';
  },

  openAddModal() {
    document.getElementById('student-modal-title').textContent = 'Register New Student';
    document.getElementById('student-id').value = '';
    document.getElementById('student-form').reset();
    App.openModal('student-modal');
  },

  async editStudent(id) {
    try {
      const res = await Auth.fetchWithAuth(`/api/students/${id}`);
      const json = await res.json();
      if (!json.success) throw new Error(json.message);

      const s = json.data;
      document.getElementById('student-modal-title').textContent = 'Edit Student Details';
      document.getElementById('student-id').value = s.id;
      document.getElementById('student-roll').value = s.rollNo;
      document.getElementById('student-name').value = s.name;
      document.getElementById('student-dept').value = s.department;
      document.getElementById('student-sem').value = s.semester;
      document.getElementById('student-email').value = s.email || '';
      App.openModal('student-modal');
    } catch (e) {
      App.showToast(e.message, 'error');
    }
  },

  async saveStudent(e) {
    e.preventDefault();
    const id = document.getElementById('student-id').value;
    const payload = {
      rollNo: document.getElementById('student-roll').value.trim(),
      name: document.getElementById('student-name').value.trim(),
      department: document.getElementById('student-dept').value.trim(),
      semester: parseInt(document.getElementById('student-sem').value),
      email: document.getElementById('student-email').value.trim() || null,
      active: true
    };

    try {
      const url = id ? `/api/students/${id}` : '/api/students';
      const method = id ? 'PUT' : 'POST';

      const res = await Auth.fetchWithAuth(url, {
        method: method,
        body: JSON.stringify(payload)
      });
      const json = await res.json();
      if (!res.ok || !json.success) throw new Error(json.message);

      App.showToast(json.message || 'Student saved successfully', 'success');
      App.closeModal('student-modal');
      this.fetchStudents(this.currentPage);
    } catch (err) {
      App.showToast(err.message, 'error');
    }
  },

  async deactivateStudent(id, rollNo) {
    if (!confirm(`Are you sure you want to deactivate student ${rollNo}?`)) return;

    try {
      const res = await Auth.fetchWithAuth(`/api/students/${id}`, { method: 'DELETE' });
      const json = await res.json();
      if (!res.ok || !json.success) throw new Error(json.message);

      App.showToast('Student deactivated successfully', 'success');
      this.fetchStudents(this.currentPage);
    } catch (err) {
      App.showToast(err.message, 'error');
    }
  },

  /* CSV Import Workflows */
  openCsvModal() {
    this.selectedFile = null;
    document.getElementById('csv-file-input').value = '';
    document.getElementById('csv-preview-container').style.display = 'none';
    document.getElementById('btn-commit-import').style.display = 'none';
    App.openModal('csv-import-modal');
  },

  handleCsvFileChange(e) {
    const file = e.target.files[0];
    if (!file) return;
    this.selectedFile = file;
    this.previewCsv();
  },

  async previewCsv() {
    if (!this.selectedFile) return;

    const formData = new FormData();
    formData.append('file', this.selectedFile);

    try {
      const token = Auth.getToken();
      const res = await fetch('/api/students/import/preview', {
        method: 'POST',
        headers: token ? { 'Authorization': `Bearer ${token}` } : {},
        body: formData
      });

      const json = await res.json();
      if (!res.ok || !json.success) throw new Error(json.message);

      const preview = json.data;
      const previewContainer = document.getElementById('csv-preview-container');
      previewContainer.style.display = 'block';

      if (preview.globalErrors && preview.globalErrors.length > 0) {
        document.getElementById('csv-preview-stats').innerHTML = `
          <div style="color:var(--danger-600); font-weight:600;">
            ${preview.globalErrors.join('<br>')}
          </div>
        `;
        document.getElementById('csv-preview-tbody').innerHTML = '';
        document.getElementById('btn-commit-import').style.display = 'none';
        return;
      }

      document.getElementById('csv-preview-stats').innerHTML = `
        <div style="display:flex; gap:1.5rem; margin-bottom:1rem; font-size:0.85rem;">
          <span>Total Records: <strong>${preview.totalRows}</strong></span>
          <span style="color:var(--success-600);">Valid for Import: <strong>${preview.validRowsCount}</strong></span>
          <span style="color:var(--danger-600);">Invalid / Duplicate: <strong>${preview.invalidRowsCount}</strong></span>
        </div>
      `;

      const tbody = document.getElementById('csv-preview-tbody');
      tbody.innerHTML = (preview.previewRows || []).slice(0, 30).map(r => `
        <tr style="background:${r.valid ? '#f0fdf4' : '#fef2f2'};">
          <td>${r.rowNumber}</td>
          <td><strong>${App.escapeHtml(r.rollNo)}</strong></td>
          <td>${App.escapeHtml(r.name)}</td>
          <td>${App.escapeHtml(r.department)}</td>
          <td>${r.semester}</td>
          <td>
            ${r.valid 
              ? '<span class="badge badge-active">Ready</span>' 
              : `<span class="badge badge-cancelled" title="${App.escapeHtml(r.errorMessage)}">${App.escapeHtml(r.errorMessage)}</span>`}
          </td>
        </tr>
      `).join('');

      document.getElementById('btn-commit-import').style.display = preview.validRowsCount > 0 ? 'inline-flex' : 'none';

    } catch (e) {
      App.showToast('CSV Preview error: ' + e.message, 'error');
    }
  },

  async commitImport() {
    if (!this.selectedFile) return;

    const formData = new FormData();
    formData.append('file', this.selectedFile);

    try {
      const token = Auth.getToken();
      const res = await fetch('/api/students/import', {
        method: 'POST',
        headers: token ? { 'Authorization': `Bearer ${token}` } : {},
        body: formData
      });

      const json = await res.json();
      if (!res.ok || !json.success) throw new Error(json.message);

      const result = json.data;
      App.showToast(`Import completed! Inserted: ${result.insertedCount}, Skipped: ${result.skippedCount}`, 'success');
      App.closeModal('csv-import-modal');
      this.fetchStudents(0);
    } catch (e) {
      App.showToast('Import failed: ' + e.message, 'error');
    }
  }
};
