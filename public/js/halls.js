/**
 * ExamSeat AI - Rooms & Benches Management
 */
const HallsView = {
  hallsList: [],

  async load() {
    try {
      const res = await Auth.fetchWithAuth('/api/halls');
      const json = await res.json();
      if (json.success) {
        this.hallsList = json.data;
        this.renderTable();
      }
    } catch (e) {
      App.showToast('Failed to load halls: ' + e.message, 'error');
    }
  },

  renderTable() {
    const tbody = document.getElementById('halls-table-body');
    if (!tbody) return;

    if (this.hallsList.length === 0) {
      tbody.innerHTML = `<tr><td colspan="8" style="text-align:center; padding:2rem; color:var(--gray-500);">No examination halls configured yet. Click "Add Hall" to create one.</td></tr>`;
      return;
    }

    const canEdit = Auth.isStaffOrAdmin();

    tbody.innerHTML = this.hallsList.map(h => `
      <tr>
        <td><strong>${App.escapeHtml(h.name)}</strong></td>
        <td>${App.escapeHtml(h.building)}</td>
        <td>${App.escapeHtml(h.floor)}</td>
        <td>${h.rows} rows × ${h.benchesPerRow} benches</td>
        <td>${h.seatsPerBench} seats/bench</td>
        <td><strong style="color:var(--primary-700);">${h.capacity} seats</strong></td>
        <td><span class="badge ${h.active ? 'badge-active' : 'badge-inactive'}">${h.active ? 'Active' : 'Inactive'}</span></td>
        <td>
          <button class="btn btn-sm btn-secondary" onclick="HallsView.viewLayout('${h.id}')" title="View visual seating grid">Layout</button>
          ${canEdit ? `
            <button class="btn btn-sm btn-secondary" onclick="HallsView.editHall('${h.id}')">Edit</button>
            <button class="btn btn-sm btn-outline-danger" onclick="HallsView.deleteHall('${h.id}', '${App.escapeHtml(h.name)}')">Delete</button>
          ` : ''}
        </td>
      </tr>
    `).join('');
  },

  updateCapacityPreview() {
    const rows = parseInt(document.getElementById('hall-rows').value) || 0;
    const benches = parseInt(document.getElementById('hall-benches').value) || 0;
    const seats = parseInt(document.getElementById('hall-seats').value) || 0;
    const cap = rows * benches * seats;
    document.getElementById('hall-capacity-preview').textContent = cap;
  },

  openAddModal() {
    document.getElementById('hall-modal-title').textContent = 'Add Examination Hall';
    document.getElementById('hall-id').value = '';
    document.getElementById('hall-form').reset();
    document.getElementById('hall-capacity-preview').textContent = '0';
    App.openModal('hall-modal');
  },

  editHall(id) {
    const hall = this.hallsList.find(h => h.id === id);
    if (!hall) return;

    document.getElementById('hall-modal-title').textContent = 'Edit Examination Hall';
    document.getElementById('hall-id').value = hall.id;
    document.getElementById('hall-name').value = hall.name;
    document.getElementById('hall-building').value = hall.building;
    document.getElementById('hall-floor').value = hall.floor;
    document.getElementById('hall-rows').value = hall.rows;
    document.getElementById('hall-benches').value = hall.benchesPerRow;
    document.getElementById('hall-seats').value = hall.seatsPerBench;
    document.getElementById('hall-active').checked = hall.active;
    this.updateCapacityPreview();
    App.openModal('hall-modal');
  },

  async saveHall(e) {
    e.preventDefault();
    const id = document.getElementById('hall-id').value;
    const payload = {
      name: document.getElementById('hall-name').value.trim(),
      building: document.getElementById('hall-building').value.trim(),
      floor: document.getElementById('hall-floor').value.trim(),
      rows: parseInt(document.getElementById('hall-rows').value),
      benchesPerRow: parseInt(document.getElementById('hall-benches').value),
      seatsPerBench: parseInt(document.getElementById('hall-seats').value),
      active: document.getElementById('hall-active').checked
    };

    try {
      const url = id ? `/api/halls/${id}` : '/api/halls';
      const method = id ? 'PUT' : 'POST';

      const res = await Auth.fetchWithAuth(url, {
        method: method,
        body: JSON.stringify(payload)
      });
      const json = await res.json();
      if (!res.ok || !json.success) {
        throw new Error(json.message || 'Operation failed');
      }

      App.showToast(json.message || 'Hall saved successfully', 'success');
      App.closeModal('hall-modal');
      this.load();
    } catch (err) {
      App.showToast(err.message, 'error');
    }
  },

  async deleteHall(id, name) {
    if (!confirm(`Are you sure you want to delete hall "${name}"? This action cannot be undone.`)) {
      return;
    }

    try {
      const res = await Auth.fetchWithAuth(`/api/halls/${id}`, { method: 'DELETE' });
      const json = await res.json();
      if (!res.ok || !json.success) {
        throw new Error(json.message || 'Delete failed');
      }
      App.showToast('Hall deleted successfully', 'success');
      this.load();
    } catch (err) {
      App.showToast(err.message, 'error');
    }
  },

  async viewLayout(id) {
    try {
      const res = await Auth.fetchWithAuth(`/api/halls/${id}/layout`);
      const json = await res.json();
      if (!json.success) throw new Error(json.message);

      const layout = json.data;
      document.getElementById('layout-modal-title').textContent = `${layout.hallName} - Physical Layout (${layout.capacity} Seats)`;

      const container = document.getElementById('hall-layout-visual');
      container.innerHTML = `
        <div class="hall-stage-indicator">BLACKBOARD / PROJECTION SCREEN / PODIUM</div>
        <div class="seating-rows-wrapper" id="hall-layout-rows"></div>
      `;

      const rowsWrapper = document.getElementById('hall-layout-rows');

      // Group seats by row, then by bench
      for (let r = 1; r <= layout.rows; r++) {
        const rowDiv = document.createElement('div');
        rowDiv.className = 'seating-row';
        rowDiv.innerHTML = `<div class="row-label">Row ${r}</div><div class="row-benches" id="layout-row-${r}"></div>`;
        rowsWrapper.appendChild(rowDiv);

        const benchesContainer = document.getElementById(`layout-row-${r}`);
        for (let b = 1; b <= layout.benchesPerRow; b++) {
          const benchCard = document.createElement('div');
          benchCard.className = 'bench-card';
          benchCard.innerHTML = `
            <div class="bench-header">Bench ${b}</div>
            <div class="bench-seats" id="bench-seats-${r}-${b}"></div>
          `;
          benchesContainer.appendChild(benchCard);

          const seatsContainer = document.getElementById(`bench-seats-${r}-${b}`);
          for (let p = 1; p <= layout.seatsPerBench; p++) {
            const seatBox = document.createElement('div');
            seatBox.className = 'seat-box empty';
            seatBox.innerHTML = `
              <span class="seat-pos-tag">Seat ${p}</span>
              <span class="seat-roll" style="color:var(--gray-500); font-size:0.65rem;">Available</span>
            `;
            seatsContainer.appendChild(seatBox);
          }
        }
      }

      App.openModal('hall-layout-modal');
    } catch (e) {
      App.showToast('Failed to load hall layout: ' + e.message, 'error');
    }
  }
};
