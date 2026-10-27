/**
 * ExamSeat AI - Authentication & Session Management
 */
const Auth = {
  TOKEN_KEY: 'examseat_jwt_token',
  USER_KEY: 'examseat_user_profile',

  getToken() {
    return localStorage.getItem(this.TOKEN_KEY);
  },

  getUser() {
    const userStr = localStorage.getItem(this.USER_KEY);
    try {
      return userStr ? JSON.parse(userStr) : null;
    } catch (e) {
      return null;
    }
  },

  isAuthenticated() {
    return !!this.getToken();
  },

  hasRole(...roles) {
    const user = this.getUser();
    if (!user || !user.role) return false;
    return roles.includes(user.role);
  },

  isAdmin() {
    return this.hasRole('ADMINISTRATOR');
  },

  isStaffOrAdmin() {
    return this.hasRole('ADMINISTRATOR', 'EXAM_CELL_STAFF');
  },

  isInvigilator() {
    return this.hasRole('INVIGILATOR');
  },

  isStudent() {
    return this.hasRole('STUDENT');
  },

  async login(username, password) {
    const response = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });

    const result = await response.json();
    if (!response.ok || !result.success) {
      throw new Error(result.message || 'Login failed');
    }

    const data = result.data;
    localStorage.setItem(this.TOKEN_KEY, data.token);
    localStorage.setItem(this.USER_KEY, JSON.stringify({
      id: data.id,
      username: data.username,
      displayName: data.displayName,
      email: data.email,
      role: data.role
    }));

    return data;
  },

  logout() {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    window.location.href = '/login.html';
  },

  getAuthHeaders() {
    const headers = { 'Content-Type': 'application/json' };
    const token = this.getToken();
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    return headers;
  },

  async fetchWithAuth(url, options = {}) {
    const token = this.getToken();
    const headers = Object.assign({}, options.headers || {});
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const method = (options.method || 'GET').toUpperCase();
    const isFormData = typeof FormData !== 'undefined' && options.body instanceof FormData;

    // Automatically ensure Content-Type is application/json for JSON payloads or mutating calls
    if (!isFormData && !headers['Content-Type']) {
      if (options.body || ['POST', 'PUT', 'PATCH'].includes(method)) {
        headers['Content-Type'] = 'application/json';
      }
    }

    options.headers = headers;
    const response = await fetch(url, options);

    if (response.status === 401) {
      this.logout();
      throw new Error('Session expired. Please log in again.');
    }

    return response;
  }
};
