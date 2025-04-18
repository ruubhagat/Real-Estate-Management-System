import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import './Header.css'; // Import the CSS file

function Header() {
  const navigate = useNavigate();
  // Read authentication status and user info from localStorage
  const isLoggedIn = !!localStorage.getItem('authToken');
  const userEmail = localStorage.getItem('userEmail');
  const userRole = localStorage.getItem('userRole'); // Get the user's role

  const handleLogout = () => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userId');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('userRole');
    navigate('/login');
    window.location.reload(); // Force reload to clear state simply
  };

  return (
    <nav className="app-header">
      <div className="header-logo">
        <Link to="/">Estates</Link>
      </div>

      {/* Navigation Links */}
      <div className="header-nav-links">
        <Link to="/">Home</Link>
        {isLoggedIn && (
          <>
            <Link to="/properties">Properties</Link>

            {/* --- MODIFICATION: Only PROPERTY_OWNER sees "Add Property" --- */}
            {userRole === 'PROPERTY_OWNER' && (
                 <Link to="/properties/new">Add Property</Link>
            )}
            {/* --- END MODIFICATION --- */}

            <Link to="/my-bookings">My Bookings</Link>

             {/* Admin link only visible if user is ADMIN */}
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
            <span className="header-user-info">Welcome, {userEmail || 'User'}! ({userRole})</span>
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