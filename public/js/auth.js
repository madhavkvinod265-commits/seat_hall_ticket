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

  getApiUrl(path) {
    const base = localStorage.getItem('EXAMSEAT_API_BASE') || '';
    if (base && path.startsWith('/api')) {
      return base.replace(/\/$/, '') + path;
    }
    return path;
  },

  async handleResponse(response, url) {
    const contentType = response.headers.get('content-type') || '';
    if (!contentType.includes('application/json')) {
      const text = await response.text();
      if (response.status === 404 || text.includes('The page could not be found')) {
        throw new Error(
          `Java Backend not reachable at "${url}". On Vercel, the Java Spring Boot backend needs to be connected or run locally on http://localhost:8080.`
        );
      }
      throw new Error(`Server returned non-JSON response (${response.status}): ${text.substring(0, 100)}`);
    }
    return response.json();
  },

  async login(username, password) {
    const targetUrl = this.getApiUrl('/api/auth/login');
    const response = await fetch(targetUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });

    const result = await this.handleResponse(response, targetUrl);
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
    const targetUrl = this.getApiUrl(url);
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
    const response = await fetch(targetUrl, options);

    if (response.status === 401) {
      this.logout();
      throw new Error('Session expired. Please log in again.');
    }

    const contentType = response.headers.get('content-type') || '';
    if (!response.ok && !contentType.includes('application/json') && !contentType.includes('application/pdf') && !contentType.includes('text/csv')) {
      const text = await response.text();
      if (response.status === 404 || text.includes('The page could not be found')) {
        throw new Error(`Backend API not reachable at "${targetUrl}". The Java Spring Boot server is not running on this host.`);
      }
      throw new Error(`Request failed (${response.status}): ${text.substring(0, 100)}`);
    }

    return response;
  }
};
