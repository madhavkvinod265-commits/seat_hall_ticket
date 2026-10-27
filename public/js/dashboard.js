/**
 * ExamSeat AI - Dashboard View Logic
 */
const DashboardView = {
  deptChart: null,
  hallChart: null,

  async load() {
    try {
      const res = await Auth.fetchWithAuth('/api/dashboard/stats');
      const json = await res.json();
      if (json.success) {
        this.renderStats(json.data);
      }
    } catch (e) {
      App.showToast('Failed to load dashboard statistics: ' + e.message, 'error');
    }
  },

  renderStats(stats) {
    // Metric cards
    document.getElementById('kpi-total-halls').textContent = stats.totalHalls || 0;
    document.getElementById('kpi-available-seats').textContent = stats.totalAvailableSeats || 0;
    document.getElementById('kpi-total-students').textContent = stats.totalStudents || 0;
    document.getElementById('kpi-scheduled-exams').textContent = stats.totalScheduledExams || 0;
    document.getElementById('kpi-finalized-plans').textContent = stats.completedSeatingPlans || 0;
    document.getElementById('kpi-unassigned-drafts').textContent = stats.unassignedStudentsInDrafts || 0;
    document.getElementById('kpi-occupied-seats').textContent = stats.totalOccupiedSeatsInUpcoming || 0;
    document.getElementById('kpi-vacant-seats').textContent = stats.totalVacantSeatsInUpcoming || 0;

    // Upcoming exams table
    const tableBody = document.getElementById('upcoming-exams-table-body');
    if (tableBody) {
      if (!stats.upcomingExams || stats.upcomingExams.length === 0) {
        tableBody.innerHTML = `<tr><td colspan="7" class="text-center" style="padding:2rem; color:var(--gray-500);">No upcoming examinations scheduled.</td></tr>`;
      } else {
        tableBody.innerHTML = stats.upcomingExams.map(ex => `
          <tr>
            <td><strong>${App.escapeHtml(ex.name)}</strong></td>
            <td>${ex.examDate}</td>
            <td><span class="badge badge-draft">${ex.session}</span></td>
            <td>${ex.startTime.substring(0, 5)} - ${ex.endTime.substring(0, 5)}</td>
            <td><strong>${ex.eligibleStudentsCount}</strong> students</td>
            <td><strong>${ex.allocatedStudentsCount} / ${ex.totalCapacity}</strong> seats</td>
            <td><span class="badge badge-${ex.status.toLowerCase()}">${ex.status.replace(/_/g, ' ')}</span></td>
          </tr>
        `).join('');
      }
    }

    // Render Charts if Chart.js is loaded
    if (typeof Chart !== 'undefined') {
      this.renderDepartmentChart(stats.departmentDistribution);
      this.renderHallChart(stats.hallUtilization);
    }
  },

  renderDepartmentChart(distribution) {
    const canvas = document.getElementById('deptDistributionChart');
    if (!canvas) return;

    if (this.deptChart) {
      this.deptChart.destroy();
    }

    const labels = Object.keys(distribution || {});
    const data = Object.values(distribution || {});

    this.deptChart = new Chart(canvas, {
      type: 'doughnut',
      data: {
        labels: labels,
        datasets: [{
          data: data,
          backgroundColor: [
            '#3b82f6', '#8b5cf6', '#f59e0b', '#10b981', '#06b6d4', '#64748b'
          ],
          borderWidth: 2,
          borderColor: '#ffffff'
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { position: 'bottom' }
        }
      }
    });
  },

  renderHallChart(hallUtilization) {
    const canvas = document.getElementById('hallUtilizationChart');
    if (!canvas) return;

    if (this.hallChart) {
      this.hallChart.destroy();
    }

    const labels = Object.keys(hallUtilization || {});
    const data = Object.values(hallUtilization || {});

    this.hallChart = new Chart(canvas, {
      type: 'bar',
      data: {
        labels: labels,
        datasets: [{
          label: 'Total Capacity',
          data: data,
          backgroundColor: '#0d9488',
          borderRadius: 6
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          y: { beginAtZero: true }
        },
        plugins: {
          legend: { display: false }
        }
      }
    });
  }
};
