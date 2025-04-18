import axios from 'axios';

// Create an Axios instance
const apiClient = axios.create({
  baseURL: 'http://localhost:8081/api', // Your backend API base URL
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add a request interceptor to include the token in requests
apiClient.interceptors.request.use(
  (config) => {
    // Get the token from localStorage
    const token = localStorage.getItem('authToken');
    if (token) {
      // If the token exists, add it to the Authorization header
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config; // Return the modified config
  },
  (error) => {
    // Handle request errors
    return Promise.reject(error);
  }
);

// Optional: Add a response interceptor for handling global errors (like 401 Unauthorized)
apiClient.interceptors.response.use(
  (response) => {
    // If request was successful, just return the response
    return response;
  },
  (error) => {
    // Handle errors globally
    if (error.response && error.response.status === 401) {
      // Example: Token expired or invalid - redirect to login
      console.error("Unauthorized! Redirecting to login.");
      // Clear potentially invalid token
      localStorage.removeItem('authToken');
      localStorage.removeItem('userId');
      localStorage.removeItem('userEmail');
      localStorage.removeItem('userRole');
      // Redirect (make sure this doesn't cause infinite loops)
      if (window.location.pathname !== '/login') {
         window.location.href = '/login'; // Or use navigate if possible
      }
    }
    // Return the error so components can handle specific errors if needed
    return Promise.reject(error);
  }
);


export default apiClient;