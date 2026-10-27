/**
 * ExamSeat AI - Hall Tickets Module
 */
const TicketsView = {
  currentExamId: null,
  ticketsList: [],

  async load() {
    await this.populateExamDropdown();
  },

  async populateExamDropdown() {
    try {
      const res = await Auth.fetchWithAuth('/api/exams');
      const json = await res.json();
      if (!json.success) return;

      const select = document.getElementById('tickets-exam-select');
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
    if (!examId) {
      this.ticketsList = [];
      this.renderTable();
      document.getElementById('btn-download-bulk-tickets').style.display = 'none';
      return;
    }

    this.currentExamId = examId;

    try {
      const res = await Auth.fetchWithAuth(`/api/exams/${examId}/tickets`);
      const json = await res.json();
      if (!json.success) throw new Error(json.message);

      this.ticketsList = json.data;
      this.renderTable();

      const bulkBtn = document.getElementById('btn-download-bulk-tickets');
      if (bulkBtn) {
        bulkBtn.style.display = this.ticketsList.length > 0 ? 'inline-flex' : 'none';
      }
    } catch (e) {
      App.showToast('Failed to load tickets: ' + e.message, 'error');
    }
  },

  renderTable() {
    const tbody = document.getElementById('tickets-table-body');
    if (!tbody) return;

    if (this.ticketsList.length === 0) {
      tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding:2rem; color:var(--gray-500);">No hall tickets generated yet for this examination. Please ensure seating plan has been generated.</td></tr>`;
      return;
    }

    tbody.innerHTML = this.ticketsList.map(t => `
      <tr>
        <td><code>${App.escapeHtml(t.ticketNumber)}</code></td>
        <td><strong>${App.escapeHtml(t.rollNo)}</strong></td>
        <td>${App.escapeHtml(t.studentName)}</td>
        <td>${App.escapeHtml(t.department)}</td>
        <td><strong>${App.escapeHtml(t.hallName)}</strong></td>
        <td>Row ${t.rowNumber}, Bench ${t.benchNumber}, Seat ${t.position}</td>
        <td>
          <a href="/api/exams/${t.examId}/tickets/${t.studentId}/pdf" target="_blank" class="btn btn-sm btn-primary">
            Download PDF
          </a>
        </td>
      </tr>
    `).join('');
  },

  downloadBulkZip() {
    if (!this.currentExamId) return;
    App.showToast('Packaging hall tickets into ZIP archive...', 'info');
    window.location.href = `/api/exams/${this.currentExamId}/tickets/bulk`;
  }
};
