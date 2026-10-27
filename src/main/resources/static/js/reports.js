/**
 * ExamSeat AI - Invigilator Reports Module
 */
const ReportsView = {
  currentExamId: null,
  currentHallId: null,
  reportData: null,

  async load() {
    await this.populateExamDropdown();
  },

  async populateExamDropdown() {
    try {
      const res = await Auth.fetchWithAuth('/api/exams');
      const json = await res.json();
      if (!json.success) return;

      const select = document.getElementById('report-exam-select');
      if (!select) return;

      select.innerHTML = '<option value="">-- Select an Examination --</option>' +
        json.data.map(e => `
          <option value="${e.id}">${App.escapeHtml(e.name)} (${e.examDate})</option>
        `).join('');

      if (json.data.length > 0 && !this.currentExamId) {
        select.value = json.data[0].id;
        this.selectExam(json.data[0].id);
      }
    } catch (e) {
      console.error(e);
    }
  },

  async selectExam(examId) {
    if (!examId) return;
    this.currentExamId = examId;

    try {
      const res = await Auth.fetchWithAuth(`/api/exams/${examId}`);
      const json = await res.json();
      if (!json.success) return;

      const halls = json.data.halls || [];
      const select = document.getElementById('report-hall-select');
      if (!select) return;

      select.innerHTML = halls.map((h, idx) => `
        <option value="${h.hallId}" ${idx === 0 ? 'selected' : ''}>
          ${App.escapeHtml(h.hallName)} (${h.capacity} capacity)
        </option>
      `).join('');

      if (halls.length > 0) {
        this.selectHall(halls[0].hallId);
      } else {
        document.getElementById('report-content-panel').style.display = 'none';
      }
    } catch (e) {
      console.error(e);
    }
  },

  async selectHall(hallId) {
    if (!hallId || !this.currentExamId) return;
    this.currentHallId = hallId;

    try {
      const res = await Auth.fetchWithAuth(`/api/exams/${this.currentExamId}/reports/halls/${hallId}`);
      const json = await res.json();
      if (!json.success) throw new Error(json.message);

      this.reportData = json.data;
      this.renderReport();
    } catch (e) {
      App.showToast('Failed to load invigilator report: ' + e.message, 'error');
    }
  },

  renderReport() {
    const r = this.reportData;
    document.getElementById('report-content-panel').style.display = 'block';

    document.getElementById('rep-college-name').textContent = r.collegeName;
    document.getElementById('rep-college-addr').textContent = r.collegeAddress;
    document.getElementById('rep-exam-name').textContent = r.examName;
    document.getElementById('rep-hall-name').textContent = r.hallName;
    document.getElementById('rep-date-session').textContent = `${r.examDate} (${r.session})`;
    document.getElementById('rep-timings').textContent = `${r.startTime.substring(0, 5)} - ${r.endTime.substring(0, 5)}`;
    document.getElementById('rep-capacity').textContent = `${r.totalCapacity} total seats`;
    document.getElementById('rep-occupied').textContent = `${r.occupiedSeats} students allocated`;
    document.getElementById('rep-vacant').textContent = `${r.emptySeats} vacant seats`;

    // Department summary badges
    const deptDiv = document.getElementById('rep-dept-summary');
    deptDiv.innerHTML = (r.departmentSummary || []).map(d => `
      <span class="badge badge-scheduled" style="padding:4px 10px; font-size:0.8rem;">
        ${App.escapeHtml(d.department)}: <strong>${d.studentCount}</strong>
      </span>
    `).join('');

    // Attendance Table
    const tbody = document.getElementById('rep-attendance-tbody');
    if (r.attendanceList.length === 0) {
      tbody.innerHTML = `<tr><td colspan="8" style="text-align:center; padding:2rem; color:var(--gray-500);">No allocated students in this hall.</td></tr>`;
    } else {
      tbody.innerHTML = r.attendanceList.map(a => `
        <tr>
          <td>${a.slNo}</td>
          <td><strong>R${a.rowNumber} - B${a.benchNumber} - S${a.position}</strong></td>
          <td><strong>${App.escapeHtml(a.rollNo)}</strong></td>
          <td>${App.escapeHtml(a.studentName)}</td>
          <td>${App.escapeHtml(a.department)}</td>
          <td>Sem ${a.semester}</td>
          <td style="border-bottom: 1px dotted #cbd5e1; min-width:140px;"></td>
        </tr>
      `).join('');
    }

    // Vacant seats
    const vacantList = document.getElementById('rep-vacant-list');
    if (r.vacantSeatsList.length === 0) {
      vacantList.textContent = 'None (100% Occupied)';
    } else {
      vacantList.textContent = r.vacantSeatsList.join('; ');
    }
  },

  downloadPdf() {
    if (!this.currentExamId || !this.currentHallId) return;
    window.open(`/api/exams/${this.currentExamId}/reports/halls/${this.currentHallId}/pdf`, '_blank');
  },

  downloadCsv() {
    if (!this.currentExamId || !this.currentHallId) return;
    window.location.href = `/api/exams/${this.currentExamId}/reports/halls/${this.currentHallId}/csv`;
  },

  printReport() {
    window.print();
  }
};
