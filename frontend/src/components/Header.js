import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import './Header.css'; // Import the CSS file

function Header() {
  const navigate = useNavigate();
  const isLoggedIn = !!localStorage.getItem('authToken');
  const userEmail = localStorage.getItem('userEmail');
  const userRole = localStorage.getItem('userRole');

  const handleLogout = () => {
    // ... logout logic ...
    localStorage.removeItem('authToken');
    localStorage.removeItem('userId');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('userRole');
    navigate('/login');
  };

  return (
    <nav className="app-header">
      <div className="header-logo">
        <Link to="/">Estates</Link> {/* Example: Changed Name */}
      </div>

      {/* Navigation Links */}
      <div className="header-nav-links">
        <Link to="/">Home</Link>
        {isLoggedIn && (
          <>
            <Link to="/properties">Properties</Link>
            {(userRole === 'PROPERTY_OWNER' || userRole === 'ADMIN') && (
                 <Link to="/properties/new">Add Property</Link>
            )}
             {/* Add "My Bookings" Link */}
            <Link to="/my-bookings">My Bookings</Link>
             {/* Add Admin link if user is ADMIN */}
             {userRole === 'ADMIN' && (
                 <Link to="/admin/properties">Admin Panel</Link>
             )}
          </>
        )}
      </div>

      {/* User Actions */}
      <div className="header-user-actions">
        {isLoggedIn ? (
          <>
            <span className="header-user-info">Welcome, {userEmail || 'User'}!</span>
            <button onClick={handleLogout} className="header-logout-button">Logout</button>
          </>
        ) : (
          <>
            <Link to="/login">Login</Link>
            <Link to="/register">Register</Link>
          </>
        )}
      </div>
    </nav>
  );
}

export default Header;