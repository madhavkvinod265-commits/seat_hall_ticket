/**
 * ExamSeat AI - Seating Plan Module & Interactive Visual Grid
 */
const SeatingView = {
  currentExamId: null,
  currentHallId: null,
  currentExam: null,
  currentGrid: null,

  async load() {
    await this.populateExamDropdown();
  },

  async populateExamDropdown() {
    try {
      const res = await Auth.fetchWithAuth('/api/exams');
      const json = await res.json();
      if (!json.success) return;

      const exams = json.data;
      const select = document.getElementById('seating-exam-select');
      if (!select) return;

      select.innerHTML = '<option value="">-- Choose an Examination --</option>' +
        exams.map(e => `
          <option value="${e.id}" ${this.currentExamId === e.id ? 'selected' : ''}>
            ${App.escapeHtml(e.name)} (${e.examDate} [${e.session}]) - [${e.status}]
          </option>
        `).join('');

      if (!this.currentExamId && exams.length > 0) {
        this.selectExam(exams[0].id);
      } else if (this.currentExamId) {
        this.selectExam(this.currentExamId);
      }
    } catch (e) {
      console.error(e);
    }
  },

  async selectExam(examId) {
    if (!examId) {
      document.getElementById('seating-main-panel').style.display = 'none';
      return;
    }

    this.currentExamId = examId;
    const select = document.getElementById('seating-exam-select');
    if (select) select.value = examId;

    try {
      const res = await Auth.fetchWithAuth(`/api/exams/${examId}`);
      const json = await res.json();
      if (!json.success) throw new Error(json.message);

      this.currentExam = json.data;
      this.renderExamHeader();
      this.populateHallDropdown();
    } catch (e) {
      App.showToast('Failed to load exam details: ' + e.message, 'error');
    }
  },

  renderExamHeader() {
    const ex = this.currentExam;
    document.getElementById('seating-main-panel').style.display = 'block';
    document.getElementById('seating-exam-title').textContent = ex.name;
    document.getElementById('seating-exam-meta').innerHTML = `
      <span>Date: <strong>${ex.examDate} (${ex.session})</strong></span> &bull; 
      <span>Timings: <strong>${ex.startTime.substring(0, 5)} - ${ex.endTime.substring(0, 5)}</strong></span> &bull;
      <span>Status: <span class="badge badge-${ex.status.toLowerCase()}">${ex.status.replace(/_/g, ' ')}</span></span>
    `;

    // Button states
    const btnGenerate = document.getElementById('btn-generate-seating');
    const btnVerify = document.getElementById('btn-verify-seating');
    const btnFinalize = document.getElementById('btn-finalize-seating');
    const btnReopen = document.getElementById('btn-reopen-seating');

    const isFinalized = ex.status === 'FINALIZED';
    const isGenerated = ex.status === 'SEATING_GENERATED' || isFinalized;

    if (btnGenerate) btnGenerate.disabled = isFinalized;
    if (btnVerify) btnVerify.style.display = isGenerated ? 'inline-flex' : 'none';
    if (btnFinalize) btnFinalize.style.display = (isGenerated && !isFinalized) ? 'inline-flex' : 'none';
    if (btnReopen) btnReopen.style.display = (isFinalized && Auth.isAdmin()) ? 'inline-flex' : 'none';
  },

  populateHallDropdown() {
    const halls = this.currentExam.halls || [];
    const select = document.getElementById('seating-hall-select');
    if (!select) return;

    if (halls.length === 0) {
      select.innerHTML = '<option value="">No halls assigned to this exam</option>';
      return;
    }

    select.innerHTML = halls.map((h, i) => `
      <option value="${h.hallId}" ${i === 0 ? 'selected' : ''}>
        ${App.escapeHtml(h.hallName)} (${h.capacity} seats)
      </option>
    `).join('');

    this.selectHall(halls[0].hallId);
  },

  async selectHall(hallId) {
    if (!hallId || !this.currentExamId) return;
    this.currentHallId = hallId;

    try {
      const res = await Auth.fetchWithAuth(`/api/exams/${this.currentExamId}/seating/halls/${hallId}`);
      const json = await res.json();
      if (!json.success) throw new Error(json.message);

      this.currentGrid = json.data;
      this.renderVisualGrid();
    } catch (e) {
      // Not yet generated
      this.renderEmptyGridPlaceholder();
    }
  },

  renderEmptyGridPlaceholder() {
    const container = document.getElementById('seating-grid-container');
    container.innerHTML = `
      <div style="text-align:center; padding:3.5rem 1rem; background:var(--white); border-radius:var(--radius-md); border:1px dashed var(--gray-300);">
        <div style="font-size:2.5rem; margin-bottom:1rem;">🪑</div>
        <h3 style="font-size:1.15rem; color:var(--gray-900); font-weight:700;">Seating Plan Not Yet Generated</h3>
        <p style="color:var(--gray-500); max-width:450px; margin:0.5rem auto 1.5rem; font-size:0.85rem;">
          Eligible students: <strong>${this.currentExam.eligibleStudentsCount}</strong> | Selected hall capacity: <strong>${this.currentExam.totalCapacity}</strong> seats.
        </p>
        ${Auth.isStaffOrAdmin() && this.currentExam.status !== 'FINALIZED' ? `
          <button class="btn btn-primary" onclick="SeatingView.generateSeating()">
            ⚡ Run Interleaved Seating Algorithm
          </button>
        ` : ''}
      </div>
    `;
  },

  renderVisualGrid() {
    const grid = this.currentGrid;
    const container = document.getElementById('seating-grid-container');

    document.getElementById('seating-stat-total').textContent = grid.capacity;
    document.getElementById('seating-stat-occupied').textContent = grid.occupiedSeats;
    document.getElementById('seating-stat-empty').textContent = grid.emptySeats;

    container.innerHTML = `
      <div class="seating-hall-container">
        <div class="hall-stage-indicator">BLACKBOARD / PROJECTION SCREEN / PODIUM</div>
        <div class="seating-rows-wrapper" id="seating-rows-wrapper"></div>
      </div>
    `;

    const rowsWrapper = document.getElementById('seating-rows-wrapper');

    // Group benches by row
    const rowMap = new Map();
    for (let b of (grid.benches || [])) {
      if (!rowMap.has(b.rowNumber)) {
        rowMap.set(b.rowNumber, []);
      }
      rowMap.get(b.rowNumber).push(b);
    }

    rowMap.forEach((benches, rowNum) => {
      const rowDiv = document.createElement('div');
      rowDiv.className = 'seating-row';

      const label = document.createElement('div');
      label.className = 'row-label';
      label.textContent = `Row ${rowNum}`;
      rowDiv.appendChild(label);

      const benchesContainer = document.createElement('div');
      benchesContainer.className = 'row-benches';

      benches.forEach(bench => {
        const benchCard = document.createElement('div');
        benchCard.className = 'bench-card';
        benchCard.innerHTML = `<div class="bench-header">Bench ${bench.benchNumber}</div>`;

        const seatsDiv = document.createElement('div');
        seatsDiv.className = 'bench-seats';

        (bench.seats || []).forEach(seat => {
          const seatBox = document.createElement('div');
          if (seat.occupied) {
            const deptClass = this.getDeptBoxClass(seat.department);
            seatBox.className = `seat-box ${deptClass}`;
            seatBox.innerHTML = `
              <div style="display:flex; justify-content:space-between; align-items:center;">
                <span class="seat-pos-tag">S${seat.position}</span>
                <span class="seat-dept">${App.escapeHtml(seat.department.substring(0, 3).toUpperCase())}</span>
              </div>
              <div class="seat-roll">${App.escapeHtml(seat.rollNo)}</div>
              <div class="seat-name" title="${App.escapeHtml(seat.studentName)}">${App.escapeHtml(seat.studentName)}</div>
            `;
            seatBox.onclick = () => this.showSeatDetails(seat);
          } else {
            seatBox.className = 'seat-box empty';
            seatBox.innerHTML = `
              <span class="seat-pos-tag">S${seat.position}</span>
              <span class="seat-roll" style="color:var(--gray-400); font-size:0.65rem;">Empty</span>
            `;
          }
          seatsDiv.appendChild(seatBox);
        });

        benchCard.appendChild(seatsDiv);
        benchesContainer.appendChild(benchCard);
      });

      rowDiv.appendChild(benchesContainer);
      rowsWrapper.appendChild(rowDiv);
    });
  },

  getDeptBoxClass(dept) {
    if (!dept) return 'dept-other';
    const d = dept.toLowerCase();
    if (d.includes('computer')) return 'dept-cs';
    if (d.includes('electro')) return 'dept-ec';
    if (d.includes('mech')) return 'dept-me';
    if (d.includes('civil')) return 'dept-cv';
    if (d.includes('info')) return 'dept-it';
    return 'dept-other';
  },

  showSeatDetails(seat) {
    document.getElementById('seat-modal-roll').textContent = seat.rollNo;
    document.getElementById('seat-modal-name').textContent = seat.studentName;
    document.getElementById('seat-modal-dept').textContent = `${seat.department} (Semester ${seat.semester})`;
    document.getElementById('seat-modal-venue').textContent = `${seat.hallName}`;
    document.getElementById('seat-modal-coords').textContent = `Row ${seat.rowNumber}, Bench ${seat.benchNumber}, Seat Position ${seat.position}`;
    App.openModal('seat-detail-modal');
  },

  async generateSeating() {
    if (!this.currentExamId) return;

    if (!confirm('Run automatic interleaved round-robin seating algorithm for this examination? Any existing draft seating will be replaced.')) {
      return;
    }

    try {
      App.showToast('Allocating seats across halls...', 'info');
      const res = await Auth.fetchWithAuth(`/api/exams/${this.currentExamId}/seating/generate`, {
        method: 'POST'
      });
      const json = await res.json();
      if (!res.ok || !json.success) {
        throw new Error(json.message || 'Seating generation failed');
      }

      App.showToast(json.message || 'Seating plan generated successfully!', 'success');
      await this.selectExam(this.currentExamId);
    } catch (e) {
      App.showToast(e.message, 'error');
    }
  },

  async verifySeating() {
    if (!this.currentExamId) return;

    try {
      const res = await Auth.fetchWithAuth(`/api/exams/${this.currentExamId}/seating/verify`);
      const json = await res.json();
      if (!json.success) throw new Error(json.message);

      const v = json.data;
      const tbody = document.getElementById('verify-checklist-tbody');
      tbody.innerHTML = `
        <tr><td>Total Eligible Students</td><td><strong>${v.totalEligibleStudents}</strong></td><td><span class="badge badge-active">Verified</span></td></tr>
        <tr><td>Total Selected Hall Capacity</td><td><strong>${v.totalHallCapacity} seats</strong></td><td><span class="badge badge-active">Verified</span></td></tr>
        <tr><td>Successfully Allocated Seats</td><td><strong>${v.totalAllocatedStudents}</strong></td><td><span class="badge ${v.totalAllocatedStudents === v.totalEligibleStudents ? 'badge-active' : 'badge-cancelled'}">${v.totalAllocatedStudents === v.totalEligibleStudents ? 'Exact Match' : 'Mismatch'}</span></td></tr>
        <tr><td>Unassigned Students</td><td><strong>${v.unassignedStudents}</strong></td><td><span class="badge ${v.unassignedStudents === 0 ? 'badge-active' : 'badge-cancelled'}">${v.unassignedStudents === 0 ? 'Zero (Passed)' : 'Action Required'}</span></td></tr>
        <tr><td>Duplicate Student Allocations</td><td><strong>${v.duplicateStudentAllocations}</strong></td><td><span class="badge ${v.duplicateStudentAllocations === 0 ? 'badge-active' : 'badge-cancelled'}">${v.duplicateStudentAllocations === 0 ? 'Zero (Passed)' : 'Error'}</span></td></tr>
        <tr><td>Duplicate Seat Allocations</td><td><strong>${v.duplicateSeatAllocations}</strong></td><td><span class="badge ${v.duplicateSeatAllocations === 0 ? 'badge-active' : 'badge-cancelled'}">${v.duplicateSeatAllocations === 0 ? 'Zero (Passed)' : 'Error'}</span></td></tr>
        <tr><td>Physical Coordinates Consistency</td><td><strong>${v.coordinatesConsistent ? 'Consistent' : 'Faulty'}</strong></td><td><span class="badge ${v.coordinatesConsistent ? 'badge-active' : 'badge-cancelled'}">${v.coordinatesConsistent ? 'Passed' : 'Error'}</span></td></tr>
      `;

      // Department distribution breakdown
      const deptList = Object.entries(v.departmentDistribution || {})
        .map(([dept, count]) => `<li>${dept}: <strong>${count} students</strong></li>`)
        .join('');
      document.getElementById('verify-dept-distribution').innerHTML = `<ul>${deptList}</ul>`;

      const alertBox = document.getElementById('verify-status-banner');
      if (v.valid) {
        alertBox.className = 'badge badge-finalized';
        alertBox.style.padding = '8px 12px';
        alertBox.style.display = 'block';
        alertBox.textContent = '✅ All academic seating constraints verified. The plan is deterministic and ready for finalization.';
      } else {
        alertBox.className = 'badge badge-cancelled';
        alertBox.style.padding = '8px 12px';
        alertBox.style.display = 'block';
        alertBox.textContent = '❌ Verification warnings: ' + (v.errors || []).join('; ');
      }

      App.openModal('seating-verify-modal');
    } catch (e) {
      App.showToast(e.message, 'error');
    }
  },

  async finalizeSeating() {
    if (!this.currentExamId) return;

    if (!confirm('Are you sure you want to FINALIZE this seating plan? Once finalized, hall allocations are locked and hall tickets become authoritative.')) {
      return;
    }

    try {
      const res = await Auth.fetchWithAuth(`/api/exams/${this.currentExamId}/seating/finalize`, {
        method: 'POST'
      });
      const json = await res.json();
      if (!res.ok || !json.success) throw new Error(json.message);

      App.showToast('Seating plan finalized and locked successfully!', 'success');
      await this.selectExam(this.currentExamId);
    } catch (e) {
      App.showToast(e.message, 'error');
    }
  },

  async reopenSeating() {
    if (!this.currentExamId) return;

    if (!confirm('ADMINISTRATOR ACTION: Reopen finalized seating plan? This allows examination staff to alter arrangements.')) {
      return;
    }

    try {
      const res = await Auth.fetchWithAuth(`/api/exams/${this.currentExamId}/seating/reopen`, {
        method: 'POST'
      });
      const json = await res.json();
      if (!res.ok || !json.success) throw new Error(json.message);

      App.showToast('Seating plan reopened for modification', 'info');
      await this.selectExam(this.currentExamId);
    } catch (e) {
      App.showToast(e.message, 'error');
    }
  }
};
