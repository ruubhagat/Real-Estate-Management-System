import React, { useState } from 'react';
import axios from 'axios';
import './Form.css';

const Register = () => {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    role: 'CUSTOMER' // Default role
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.post('http://localhost:8081/api/users/register', formData);
      if (response && response.data) {
        alert('Registration successful!');
      } else {
        alert('Registration failed. No response from server.');
      }
    } catch (error) {
      console.error('Registration failed:', error.response?.data || error.message);
      alert('Something went wrong during registration.');
    }
  };

  return (
    <div style={{ padding: '2rem', fontFamily: 'Arial, sans-serif' }}>
      <h2>Register</h2>
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', width: '300px' }}>
        <label>Name:</label>
        <input type="text" name="name" value={formData.name} onChange={handleChange} required />

        <label>Email:</label>
        <input type="email" name="email" value={formData.email} onChange={handleChange} required />

        <label>Password:</label>
        <input type="password" name="password" value={formData.password} onChange={handleChange} required />

        <label>Role:</label>
        <select name="role" value={formData.role} onChange={handleChange}>
          <option value="CUSTOMER">Customer</option>
          <option value="PROPERTY_OWNER">Property Owner</option>
          <option value="AGENT">Agent</option>
          <option value="ADMIN">Admin</option>
        </select>

        <button type="submit" style={{ marginTop: '1rem' }}>Register</button>
      </form>
    </div>
  );
};

export default Register;
