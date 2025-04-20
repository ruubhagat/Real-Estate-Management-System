import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate, Link, useLocation } from 'react-router-dom'; // Added Link, useLocation
import './Form.css'; // Import shared form styles

function Login() {
  const [form, setForm] = useState({
    email: '',
    password: ''
  });
  const [error, setError] = useState(''); // State to hold login errors
  const [loading, setLoading] = useState(false); // State for loading indicator
  const navigate = useNavigate(); // Hook for navigation
  const location = useLocation(); // Hook to get previous location (if redirected)

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    setError(''); // Clear error when user types
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(''); // Clear previous errors
    setLoading(true); // Start loading

    if (!form.email || !form.password) {
        setError("Please enter both email and password.");
        setLoading(false);
        return;
    }

    try {
      // Use the correct backend URL
      const response = await axios.post("http://localhost:8081/api/users/login", {
        email: form.email.trim(), // Trim whitespace
        password: form.password, // Don't trim password
      });

      console.log("Login Response:", response.data);

      // Check if login was successful and token exists
      if (response.data && response.data.token) {
        // alert("Login successful!"); // Replaced with navigation

        // <<<--- STORE TOKEN and User Info --->>>
        localStorage.setItem('authToken', response.data.token);
        localStorage.setItem('userId', response.data.userId);
        localStorage.setItem('userEmail', response.data.userEmail);
        localStorage.setItem('userRole', response.data.userRole);

        // Redirect user: Check if redirected from a protected route
        const from = location.state?.from?.pathname || "/"; // Default to home page
        console.log("Login successful, navigating to:", from);
        // Use replace: true so login page isn't in history
        navigate(from, { replace: true });

        // Force a reload to update Header state etc. (Simpler than context for now)
         // Delay slightly to ensure navigation completes before reload potentially interrupts
         setTimeout(() => {
            window.location.reload();
         }, 100);


      } else {
        // Handle cases where the backend might return 200 OK but no token
        setError("Login failed: Invalid response from server.");
      }
    } catch (err) {
      console.error("Login failed:", err);
      // Extract error message from backend response if available
      const errorMessage = err.response?.data?.error || "Login failed. Please check your credentials.";
      setError(errorMessage);
    } finally {
        setLoading(false); // Stop loading
    }
  };

  return (
    // Apply form-container class
    <div className="form-container">
      <h2>Login</h2>

      <form onSubmit={handleSubmit}>
        {/* Display error messages using Form.css classes */}
        {error && <p className="form-message form-error">{error}</p>}

        {/* Apply form-input-group structure */}
        <div className="form-input-group">
          <label htmlFor="login-email">Email:</label>
          <input
            id="login-email"
            name="email"
            type="email"
            placeholder="your.email@example.com" // Added placeholder
            value={form.email}
            onChange={handleChange}
            required
            disabled={loading} // Disable when loading
          />
        </div>

        <div className="form-input-group">
          <label htmlFor="login-password">Password:</label>
          <input
            id="login-password"
            name="password"
            type="password"
            placeholder="Password"
            value={form.password}
            onChange={handleChange}
            required
            disabled={loading} // Disable when loading
          />
        </div>

        {/* Apply form-button class */}
        <button type="submit" className="form-button" disabled={loading}>
          {loading ? 'Logging In...' : 'Login'}
        </button>
      </form>

       {/* Optional: Link to Registration page */}
       <div style={{ textAlign: 'center', marginTop: '20px', fontSize: '0.9em' }}>
            Don't have an account? <Link to="/register" style={{ color: 'var(--primary-color)', fontWeight: '500' }}>Register here</Link>
       </div>

    </div> // End form-container
  );
}

export default Login;