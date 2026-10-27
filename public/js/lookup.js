/**
 * ExamSeat AI - Student Seat Lookup & Hall Ticket Access
 */
const LookupView = {
  allExams: [],

  async load() {
    await this.populateExams();
  },

  async populateExams() {
    try {
      const url = (typeof Auth !== 'undefined' && Auth.getApiUrl) ? Auth.getApiUrl('/api/exams') : '/api/exams';
      const res = await fetch(url);
      if (!res.ok || !res.headers.get('content-type')?.includes('application/json')) {
        return;
      }
      const json = await res.json();
      if (json.success) {
        this.allExams = json.data;
        const select = document.getElementById('lookup-exam-select');
        if (select) {
          select.innerHTML = '<option value="">-- All Scheduled Examinations --</option>' +
            this.allExams.map(e => `
              <option value="${e.id}">${App.escapeHtml(e.name)} (${e.examDate})</option>
            `).join('');
        }
      }
    } catch (e) {
      console.error(e);
    }
  },

  async search() {
    const rollNo = document.getElementById('lookup-roll-input').value.trim();
    const examId = document.getElementById('lookup-exam-select').value;
    const resultContainer = document.getElementById('lookup-results-container');

    if (!rollNo) {
      App.showToast('Please enter your Student Roll Number.', 'warning');
      return;
    }

    try {
      resultContainer.innerHTML = '<div style="text-align:center; padding:2rem;">Searching allocation records...</div>';

      let apiPath = `/api/lookup/seat?rollNo=${encodeURIComponent(rollNo)}`;
      if (examId) apiPath += `&examId=${examId}`;

      const targetUrl = (typeof Auth !== 'undefined' && Auth.getApiUrl) ? Auth.getApiUrl(apiPath) : apiPath;
      const res = await fetch(targetUrl);

      if (!res.ok || !res.headers.get('content-type')?.includes('application/json')) {
        const text = await res.text();
        if (res.status === 404 || text.includes('The page could not be found')) {
          throw new Error('Backend API not reachable. The Java Spring Boot server is not running on this host.');
        }
        throw new Error(text || 'Lookup failed');
      }

      const json = await res.json();

      if (!res.ok || !json.success) {
        throw new Error(json.message || 'No records found');
      }

      const results = json.data;
      if (!results || results.length === 0) {
        resultContainer.innerHTML = `
          <div class="card" style="text-align:center; padding:2.5rem;">
            <div style="font-size:2rem; margin-bottom:0.5rem;">🔍</div>
            <h4 style="font-weight:700;">No Examination Seat Found</h4>
            <p style="color:var(--gray-500); font-size:0.875rem;">
              No seating arrangement found for Roll Number <strong>${App.escapeHtml(rollNo)}</strong>.
              If the examination seating plan is not finalized yet, please check back shortly.
            </p>
          </div>
        `;
        return;
      }

      resultContainer.innerHTML = results.map(r => `
        <div class="card" style="border-top: 4px solid var(--primary-700); margin-bottom:1.5rem;">
          <div style="display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:1rem; border-bottom:1px solid var(--gray-100); padding-bottom:0.75rem;">
            <div>
              <h3 style="font-size:1.15rem; font-weight:700; color:var(--primary-900);">${App.escapeHtml(r.examName)}</h3>
              <p style="color:var(--gray-500); font-size:0.825rem; margin-top:2px;">
                Date: <strong>${r.examDate} (${r.session})</strong> &bull; Timings: <strong>${r.startTime.substring(0, 5)} - ${r.endTime.substring(0, 5)}</strong>
              </p>
            </div>
            <div>
              <span class="badge ${r.allocated ? 'badge-finalized' : 'badge-draft'}">
                ${r.allocated ? 'Seat Allocated' : 'Pending Seating'}
              </span>
            </div>
          </div>

          <div style="display:grid; grid-template-columns:repeat(auto-fit, minmax(220px, 1fr)); gap:1.25rem;">
            <div>
              <h5 style="font-size:0.75rem; text-transform:uppercase; color:var(--gray-500); font-weight:700;">Student Details</h5>
              <div style="margin-top:0.35rem; font-size:0.9rem;">
                <div>Roll No: <strong>${App.escapeHtml(r.rollNo)}</strong></div>
                <div>Name: <strong>${App.escapeHtml(r.studentName)}</strong></div>
                <div>Department: <strong>${App.escapeHtml(r.department)}</strong> (Sem ${r.semester})</div>
              </div>
            </div>

            <div>
              <h5 style="font-size:0.75rem; text-transform:uppercase; color:var(--gray-500); font-weight:700;">Reporting Schedule</h5>
              <div style="margin-top:0.35rem; font-size:0.9rem;">
                <div>Reporting Time: <strong style="color:var(--danger-600);">${r.reportingTime.substring(0, 5)} AM/PM</strong></div>
                <div>Entry Gate Closes: <strong>15 mins after start</strong></div>
                <div>Status: <span style="color:var(--success-600); font-weight:600;">Eligible & Confirmed</span></div>
              </div>
            </div>
          </div>

          ${r.allocated ? `
            <div class="seat-highlight-box">
              <div style="font-size:0.85rem; font-weight:600; color:var(--accent-700);">ALLOCATED SEAT COORDINATES</div>
              <div class="seat-big-coord">
                ${App.escapeHtml(r.hallName)}
              </div>
              <div style="font-size:1.15rem; font-weight:700; color:var(--primary-700); margin-top:4px;">
                Row ${r.rowNumber} &bull; Bench ${r.benchNumber} &bull; Seat Position ${r.position}
              </div>
              <div style="font-size:0.8rem; color:var(--gray-500); margin-top:4px;">
                Venue: ${App.escapeHtml(r.building)} (${App.escapeHtml(r.floor)})
              </div>
            </div>

            <div style="display:flex; justify-content:flex-end; gap:0.75rem; margin-top:1rem;">
              <a href="/api/exams/${r.examId}/tickets/${r.studentId}/pdf" target="_blank" class="btn btn-primary">
                📄 Download Official Hall Ticket (PDF)
              </a>
            </div>
          ` : `
            <div style="background:var(--gray-50); border:1px dashed var(--gray-300); border-radius:var(--radius-sm); padding:1.25rem; text-align:center; margin-top:1rem; color:var(--gray-500);">
              Seating plan generation is currently in progress for this examination. Please check back before your reporting time.
            </div>
          `}
        </div>
      `).join('');

    } catch (e) {
      resultContainer.innerHTML = `
        <div class="card" style="text-align:center; padding:2rem; color:var(--danger-600);">
          ❌ ${App.escapeHtml(e.message)}
        </div>
      `;
    }
  }
};
