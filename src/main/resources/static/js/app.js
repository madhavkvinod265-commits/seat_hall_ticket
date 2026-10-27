/**
 * ExamSeat AI - Application Controller & Router
 */
const App = {
  currentView: 'dashboard',

  init() {
    this.setupNavigation();
    this.setupUserUI();

    // Default route
    const hash = window.location.hash.replace('#', '') || 'dashboard';
    this.navigateTo(hash);

    window.addEventListener('hashchange', () => {
      const route = window.location.hash.replace('#', '') || 'dashboard';
      this.navigateTo(route);
    });
  },

  setupUserUI() {
    const user = Auth.getUser();
    if (!user) {
      if (!window.location.pathname.endsWith('login.html') && !window.location.pathname.endsWith('lookup.html')) {
        window.location.href = '/login.html';
      }
      return;
    }

    const userNameEl = document.getElementById('sidebar-user-name');
    const userRoleEl = document.getElementById('sidebar-user-role');
    const userAvatarEl = document.getElementById('sidebar-user-avatar');

    if (userNameEl) userNameEl.textContent = user.displayName || user.username;
    if (userRoleEl) userRoleEl.textContent = user.role.replace(/_/g, ' ');
    if (userAvatarEl) userAvatarEl.textContent = (user.displayName || user.username).charAt(0).toUpperCase();

    // Hide or show links based on role
    document.querySelectorAll('[data-role-min]').forEach(el => {
      const allowedRoles = el.getAttribute('data-role-min').split(',');
      if (!allowedRoles.includes(user.role)) {
        el.style.display = 'none';
      }
    });
  },

  setupNavigation() {
    document.querySelectorAll('.nav-link[data-view]').forEach(link => {
      link.addEventListener('click', (e) => {
        e.preventDefault();
        const viewName = link.getAttribute('data-view');
        window.location.hash = viewName;
      });
    });

    const logoutBtn = document.getElementById('btn-logout');
    if (logoutBtn) {
      logoutBtn.addEventListener('click', () => {
        if (confirm('Are you sure you want to log out?')) {
          Auth.logout();
        }
      });
    }
  },

  navigateTo(viewName) {
    this.currentView = viewName;

    // Update active nav link
    document.querySelectorAll('.nav-link').forEach(link => {
      link.classList.remove('active');
      if (link.getAttribute('data-view') === viewName) {
        link.classList.add('active');
      }
    });

    // Update view visibility
    document.querySelectorAll('.view-section').forEach(view => {
      view.classList.remove('active');
    });

    const targetView = document.getElementById(`view-${viewName}`);
    if (targetView) {
      targetView.classList.add('active');
      const pageTitle = document.getElementById('current-page-title');
      if (pageTitle) {
        const titleText = targetView.getAttribute('data-title') || 'Dashboard';
        pageTitle.textContent = titleText;
      }

      // Trigger view-specific loaders
      this.loadViewData(viewName);
    }
  },

  loadViewData(viewName) {
    switch (viewName) {
      case 'dashboard':
        DashboardView.load();
        break;
      case 'halls':
        HallsView.load();
        break;
      case 'students':
        StudentsView.load();
        break;
      case 'exams':
        ExamsView.load();
        break;
      case 'seating':
        SeatingView.load();
        break;
      case 'lookup':
        LookupView.load();
        break;
      case 'tickets':
        TicketsView.load();
        break;
      case 'reports':
        ReportsView.load();
        break;
      case 'users':
        if (Auth.isAdmin() && window.UsersView) UsersView.load();
        break;
      case 'settings':
        break;
    }
  },

  showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `
      <span>${this.escapeHtml(message)}</span>
    `;

    container.appendChild(toast);
    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateX(100%)';
      toast.style.transition = 'all 0.3s ease';
      setTimeout(() => toast.remove(), 300);
    }, 4000);
  },

  openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) modal.classList.add('active');
  },

  closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) modal.classList.remove('active');
  },

  escapeHtml(str) {
    if (!str) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }
};
