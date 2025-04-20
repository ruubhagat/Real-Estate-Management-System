import React, { useState } from 'react';
import axios from 'axios'; // Using axios directly for public endpoint
import { Link, useNavigate } from 'react-router-dom'; // Import Link and useNavigate
import './Form.css'; // Import the shared form styles

const Register = () => {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    role: 'CUSTOMER' // Default role
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate(); // Hook for navigation

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    // Clear messages when user types
    setError('');
    setSuccess('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(''); // Clear previous errors
    setSuccess(''); // Clear previous success message

    // Basic Frontend Validation
    if (!formData.name || !formData.email || !formData.password) {
        setError("Please fill in Name, Email, and Password.");
        return;
    }
    // Add more validation if needed (e.g., password complexity)

    setLoading(true); // Set loading state

    try {
      // Use the correct backend endpoint URL
      const response = await axios.post('http://localhost:8081/api/users/register', formData);

      // Check response structure based on your backend UserController
      if (response && response.data) {
        setSuccess(response.data.message || 'Registration successful! Redirecting to login...'); // Show success
        setFormData({ name: '', email: '', password: '', role: 'CUSTOMER' }); // Clear form
        // Redirect to login after a short delay
        setTimeout(() => {
            navigate('/login');
        }, 2000); // 2-second delay
      } else {
        // This case might not happen if backend always returns data or error
        setError('Registration failed. Unexpected response from server.');
      }
    } catch (err) {
      console.error('Registration failed:', err); // Log the full error
      // Extract user-friendly error message from backend response
      const backendError = err.response?.data?.message || err.response?.data?.error;
      if (backendError) {
          setError(backendError); // Show backend error (e.g., "Email already registered")
      } else {
          setError('Registration failed. Please try again.'); // Generic error
      }
    } finally {
      setLoading(false); // Reset loading state regardless of success/failure
    }
  };

  return (
    // Use the form-container class for consistent styling
    <div className="form-container">
      <h2>Register</h2>

      <form onSubmit={handleSubmit}>
        {/* Display Success/Error Messages */}
        {error && <p className="form-message form-error">{error}</p>}
        {success && <p className="form-message form-success">{success}</p>}

        {/* Apply form-input-group structure */}
        <div className="form-input-group">
          <label htmlFor="reg-name">Name:</label> {/* Use htmlFor */}
          <input
            type="text"
            id="reg-name" // Add id
            name="name"
            value={formData.name}
            onChange={handleChange}
            required
            disabled={loading} // Disable during loading
          />
        </div>

        <div className="form-input-group">
          <label htmlFor="reg-email">Email:</label>
          <input
            type="email"
            id="reg-email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            required
            disabled={loading}
          />
        </div>

        <div className="form-input-group">
          <label htmlFor="reg-password">Password:</label>
          <input
            type="password"
            id="reg-password"
            name="password"
            value={formData.password}
            onChange={handleChange}
            required
            minLength={6} // Example: enforce minimum length
            disabled={loading}
          />
           {/* Optional: Add password requirements hint */}
           <small style={{fontSize: '0.8em', color: 'var(--text-muted)', display:'block', marginTop:'3px'}}>Minimum 6 characters</small>
        </div>

        <div className="form-input-group">
          <label htmlFor="reg-role">Register As:</label>
          <select
            id="reg-role"
            name="role"
            value={formData.role}
            onChange={handleChange}
            disabled={loading}
          >
            <option value="CUSTOMER">Customer</option>
            <option value="PROPERTY_OWNER">Property Owner</option>
          </select>
        </div>

        {/* Apply form-button class */}
        <button type="submit" className="form-button" disabled={loading}>
          {loading ? 'Registering...' : 'Register'}
        </button>
      </form>

      {/* Optional: Link to Login page */}
      <div style={{ textAlign: 'center', marginTop: '20px', fontSize: '0.9em' }}>
        Already have an account? <Link to="/login" style={{ color: 'var(--primary-color)', fontWeight: '500' }}>Login here</Link>
      </div>

    </div> // End of form-container
  );
};

export default Register;