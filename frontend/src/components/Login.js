import React, { useState } from 'react';
import axios from 'axios';
// Optional: Import useNavigate for redirection after login
import { useNavigate } from 'react-router-dom';
import './Form.css';

function Login() {
  const [form, setForm] = useState({
    email: '',
    password: ''
  });
  const [error, setError] = useState(''); // State to hold login errors
  const navigate = useNavigate(); // Hook for navigation

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    setError(''); // Clear error when user types
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(''); // Clear previous errors

    try {
      // Use the correct backend URL (port 8081)
      const response = await axios.post("http://localhost:8081/api/users/login", {
        email: form.email,
        password: form.password,
      });

      console.log("Login Response:", response.data); // Log the response data

      // Check if login was successful and token exists
      if (response.data && response.data.token) {
        alert("Login successful!");

        // <<<--- STORE TOKEN and User Info --->>>
        localStorage.setItem('authToken', response.data.token);
        localStorage.setItem('userId', response.data.userId);
        localStorage.setItem('userEmail', response.data.userEmail);
        localStorage.setItem('userRole', response.data.userRole);

        // Optional: Redirect user to a dashboard or home page
        // navigate('/dashboard'); // Example redirect

        // You might want to reload the window or update app state
        // to reflect the logged-in status immediately in the UI
        window.location.reload(); // Simple way to refresh UI state, but consider Context API or Redux later

      } else {
        // Handle cases where the backend might return 200 OK but no token (shouldn't happen with current backend code)
        setError("Login failed: No token received.");
        alert("Login failed: No token received.");
      }
    } catch (err) {
      console.error("Login failed:", err);
      // Extract error message from backend response if available
      const errorMessage = err.response?.data?.error || "Login failed. Please check your credentials.";
      setError(errorMessage);
      alert(errorMessage);
    }
  };

  return (
    <div style={{ maxWidth: "400px", margin: "0 auto", padding: "1rem" }}>
      <h2>Login</h2>
      <form onSubmit={handleSubmit}>
        <div style={{ marginBottom: '1rem' }}>
          <label htmlFor="email">Email:</label><br />
          <input
            id="email"
            name="email"
            type="email"
            placeholder="Email"
            value={form.email}
            onChange={handleChange}
            required
            style={{ width: '100%', padding: '8px', boxSizing: 'border-box' }}
          />
        </div>
        <div style={{ marginBottom: '1rem' }}>
          <label htmlFor="password">Password:</label><br />
          <input
            id="password"
            name="password"
            type="password"
            placeholder="Password"
            value={form.password}
            onChange={handleChange}
            required
            style={{ width: '100%', padding: '8px', boxSizing: 'border-box' }}
          />
        </div>
        {error && <p style={{ color: 'red' }}>{error}</p>} {/* Display error messages */}
        <button type="submit" style={{ padding: '10px 15px' }}>Login</button>
      </form>
    </div>
  );
}

export default Login;