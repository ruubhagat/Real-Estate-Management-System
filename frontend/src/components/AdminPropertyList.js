import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import apiClient from '../api/axiosConfig';
import './AdminPropertyList.css'; // <-- Import a new CSS file for styling

function AdminPropertyList() {
    const [properties, setProperties] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const navigate = useNavigate();

    // Fetch all properties (Admin endpoint)
    const fetchAllProperties = async () => {
        setLoading(true);
        setError('');
        try {
            console.log("[AdminPropertyList] Fetching all properties...");
            const response = await apiClient.get('/admin/properties');
            console.log("[AdminPropertyList] Properties fetched:", response.data);
            setProperties(Array.isArray(response.data) ? response.data : []);
        } catch (err) {
            console.error("[AdminPropertyList] Failed to fetch properties:", err);
            let errMsg = 'Failed to load properties.';
            // ... (keep existing error handling) ...
             if (err.response?.status === 403) { errMsg = "Access Denied."; }
             else if (err.response?.status === 401) { errMsg = "Authentication required."; }
             else if (err.response?.data?.error) { errMsg = err.response.data.error; }
             else { errMsg = err.message || errMsg; }
            setError(errMsg);
            setProperties([]);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchAllProperties();
    }, []);

    // Handle deleting a property (Admin action)
    const handleDelete = async (propertyId, propertyAddress) => {
         if (!window.confirm(`ADMIN ACTION: Are you sure you want to delete property "${propertyAddress || 'N/A'}" (ID: ${propertyId})? This action cannot be undone.`)) {
            return;
         }
         setError('');
         try {
             console.log(`Admin: Deleting property ${propertyId} via admin endpoint...`);
             await apiClient.delete(`/admin/properties/${propertyId}`); // Relative path
             alert(`Property ${propertyId} deleted successfully by Admin.`);
             fetchAllProperties(); // Refetch after delete
         } catch (err) {
              console.error(`Admin: Failed to delete property ${propertyId}:`, err);
              let deleteErrMsg = 'Failed to delete property.';
              // ... (keep existing error handling) ...
               if (err.response?.status === 403) { deleteErrMsg = "Permission Denied."; }
               else if (err.response?.status === 404) { deleteErrMsg = "Property not found."; }
               else if (err.response?.data?.error) { deleteErrMsg = err.response.data.error; }
               else { deleteErrMsg = err.message || deleteErrMsg; }
              setError(`Delete Error: ${deleteErrMsg}`);
              alert(`Delete Error: ${deleteErrMsg}`);
         }
    };

    // --- Styles --- (Removed inline styles for buttons, kept others)
    const tableStyle = { width: '100%', borderCollapse: 'collapse', marginTop: '20px', backgroundColor: '#fff', fontSize: '0.9rem' };
    const thTdStyle = { border: '1px solid var(--border-color)', padding: '8px 10px', textAlign: 'left', verticalAlign: 'middle' };
    const thStyle = { ...thTdStyle, backgroundColor: '#e9ecef', fontWeight: '600' };
    const errorStyle = { color: 'var(--error-text)', backgroundColor: 'var(--error-bg)', border: '1px solid var(--error-border)', padding: '15px', marginBottom: '15px', borderRadius: '4px'};
    // --- End Styles ---

    if (loading) return <p className="content-wrapper">Loading all properties for Admin...</p>;

    return (
        <div className="content-wrapper admin-property-list">
            <h2>Admin Panel - All Properties</h2>
            {error && <p style={errorStyle}>Error: {error}</p>}

            {!loading && !error && properties.length === 0 && (
                <p>No properties found in the system.</p>
            )}

            {!loading && !error && properties.length > 0 && (
                <div style={{overflowX: 'auto'}}>
                    <table style={tableStyle}>
                        <thead>
                            <tr>
                                {/* --- MODIFICATION: Changed ID to S.No. --- */}
                                <th style={thStyle}>S.No.</th>
                                {/* --- END MODIFICATION --- */}
                                <th style={thStyle}>Address</th>
                                <th style={thStyle}>City</th>
                                <th style={thStyle}>Price</th>
                                <th style={thStyle}>Type</th>
                                <th style={thStyle}>Status</th>
                                <th style={thStyle}>Owner Email</th>
                                <th style={thStyle}>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {/* --- MODIFICATION: Added index for S.No. --- */}
                            {properties.map((prop, index) => (
                                <tr key={prop.id}> {/* Keep prop.id as the unique key */}
                                    {/* --- MODIFICATION: Display index + 1 --- */}
                                    <td style={thTdStyle}>{index + 1}</td>
                                    {/* --- END MODIFICATION --- */}
                                    <td style={thTdStyle}>{prop.address}</td>
                                    <td style={thTdStyle}>{prop.city}</td>
                                    <td style={thTdStyle}>
                                        ₹{prop.price ? Number(prop.price).toLocaleString('en-IN') : 'N/A'}
                                        {prop.type === 'RENT' && <span style={{fontSize: '0.8em', color: 'var(--text-muted)'}}> /mo</span>}
                                    </td>
                                    <td style={thTdStyle}>{prop.type}</td>
                                    <td style={thTdStyle}>{prop.status}</td>
                                    <td style={thTdStyle}>{prop.owner?.email || 'N/A'}</td>
                                    <td style={thTdStyle}>
                                        {/* --- MODIFICATION: Use CSS Classes for Buttons --- */}
                                        <button
                                            onClick={() => handleDelete(prop.id, prop.address)}
                                            className="admin-action-btn admin-btn-delete" // Apply classes
                                            title="Delete Property"
                                        >
                                            Delete
                                        </button>
                                        <button
                                            onClick={() => navigate(`/properties/${prop.id}`)}
                                            className="admin-action-btn admin-btn-view" // Apply classes
                                            title="View Property Details"
                                        >
                                            View
                                        </button>
                                        {/* --- END MODIFICATION --- */}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
}

export default AdminPropertyList;