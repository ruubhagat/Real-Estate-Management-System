import React from 'react';
import {
  BrowserRouter as Router,
  Route,
  Routes,
  Link,
  Navigate,
  useLocation // Import useLocation for redirect state
} from 'react-router-dom';

// --- Import Page/Layout Components ---
import Register from './components/Register';
import Login from './components/Login';
import Header from './components/Header';
import PropertyList from './components/PropertyList';
import PropertyForm from './components/PropertyForm';
import EditPropertyForm from './components/EditPropertyForm';
import PropertyDetails from './components/PropertyDetails';
import MyBookingsList from './components/MyBookingsList';
import Home from './components/Home';
import AdminPropertyList from './components/AdminPropertyList'; // Import Admin component

// Import global styles FIRST if using App.css for variables
import './App.css'; // Or './index.css' depending on your setup

// --- Reusable Components ---

// Optional: Simple Dashboard component placeholder
function Dashboard() {
    const userEmail = localStorage.getItem('userEmail');
    const userRole = localStorage.getItem('userRole');
    return (
        // Use content-wrapper class for consistent padding/width
        <div className="content-wrapper">
            <h2>Dashboard</h2>
            <p>Welcome, {userEmail || 'User'}!</p>
            <p>Your Role: {userRole || 'N/A'}</p>
            <hr style={{ margin: '1rem 0', borderColor: 'var(--border-color)'}}/>
            <p><Link to="/properties">View Properties</Link></p>
            <p><Link to="/my-bookings">View My Bookings</Link></p>
             {(userRole === 'PROPERTY_OWNER' || userRole === 'ADMIN') && (
                 <p><Link to="/properties/new">Add New Property</Link></p>
             )}
              {userRole === 'ADMIN' && (
                 <p><Link to="/admin/properties">Manage All Properties (Admin)</Link></p>
             )}
             {/* Add more dashboard elements */}
        </div>
    );
}

// Optional: Basic Protected Route Wrapper Component
function ProtectedRoute({ children }) {
  const token = localStorage.getItem('authToken');
  const location = useLocation(); // Get current location

  if (!token) {
    // User not logged in, redirect to login page
    // Pass the current location via state so user can be redirected back after login
    return <Navigate to="/login" replace state={{ from: location }} />;
  }
  // User is logged in, render the child component
  return children;
}

// --- Main Application Component ---
function App() {
  return (
    <Router>
      {/* Header component renders on all pages */}
      <Header />

      {/* Main content area */}
      {/* Removing wrapper div here allows Home component to control full width */}
        <Routes>
          {/* --- Public Routes --- */}
          <Route path="/" element={<Home />} />
          {/* Add wrapper with class for consistent padding/width for non-full-width pages */}
           <Route path="/register" element={ <div className="content-wrapper"><Register /></div> } />
           <Route path="/login" element={ <div className="content-wrapper"><Login /></div> } />

          {/* --- Protected Routes (Wrapped with ProtectedRoute and content-wrapper) --- */}
          <Route
            path="/properties"
            element={<ProtectedRoute><div className="content-wrapper"><PropertyList /></div></ProtectedRoute>}
          />
          <Route
            path="/properties/new"
            element={<ProtectedRoute><div className="content-wrapper"><PropertyForm /></div></ProtectedRoute>}
          />
          <Route
            path="/properties/:id"
            element={<ProtectedRoute><div className="content-wrapper"><PropertyDetails /></div></ProtectedRoute>}
          />
          <Route
            path="/properties/:id/edit"
            element={<ProtectedRoute><div className="content-wrapper"><EditPropertyForm /></div></ProtectedRoute>}
          />
          <Route
                path="/my-bookings"
                element={<ProtectedRoute><div className="content-wrapper"><MyBookingsList /></div></ProtectedRoute>}
            />
          <Route
            path="/dashboard"
            element={<ProtectedRoute><Dashboard /></ProtectedRoute>} // Dashboard already uses wrapper
          />

          {/* --- Admin Route --- */}
          {/* Note: We protect with ProtectedRoute, but actual role check (ADMIN) happens on the backend */}
          <Route
              path="/admin/properties" // Define admin route
              element={<ProtectedRoute><div className="content-wrapper"><AdminPropertyList /></div></ProtectedRoute>}
          />
          {/* Add other admin routes here (e.g., /admin/users) */}


          {/* --- Catch-all 404 Route --- */}
          <Route
            path="*"
            element={
              <div className="content-wrapper" style={{ textAlign: 'center', marginTop: '50px' }}>
                <h2>404 - Page Not Found</h2>
                <p>Sorry, the page you are looking for does not exist.</p>
                <Link to="/">Go back to Home</Link>
              </div>
            }
          />
        </Routes>

        {/* Optional Footer */}
        {/* <footer style={{ textAlign: 'center', padding: '20px', marginTop: '40px', borderTop: '1px solid var(--border-color)', color: 'var(--text-muted)', fontSize: '0.9em' }}>
             © {new Date().getFullYear()} Real Estate. All rights reserved.
        </footer> */}

    </Router> // Router ends here - ensure no extra closing div outside
  );
}

export default App;