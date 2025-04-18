import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import apiClient from '../api/axiosConfig';

function AdminPropertyList() {
    const [properties, setProperties] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const fetchAllProperties = async () => {
        setLoading(true);
        setError('');
        try {
            console.log("Admin: Fetching all properties...");
            const response = await apiClient.get('/admin/properties'); // Call admin endpoint
            console.log("Admin: Properties fetched:", response.data);
            setProperties(response.data || []);
        } catch (err) {
            console.error("Admin: Failed to fetch properties:", err);
            const errMsg = err.response?.data?.error || err.message || 'Failed to load properties.';
            setError(errMsg);
             if (err.response?.status === 403) {
                 setError("Access Denied: You do not have permission to view this page.");
             }
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchAllProperties();
    }, []);

    const handleDelete = async (propertyId, propertyAddress) => {
         if (!window.confirm(`ADMIN ACTION: Are you sure you want to delete property "${propertyAddress}" (ID: ${propertyId})? This cannot be undone.`)) {
            return;
         }
         setError(''); // Clear previous errors
         try {
             console.log(`Admin: Deleting property ${propertyId}`);
             // Use admin endpoint or regular delete endpoint (as service allows admin)
             await apiClient.delete(`/admin/properties/${propertyId}`); // or /api/properties/${propertyId}
             alert(`Property ${propertyId} deleted successfully.`);
             // Refetch the list after deletion
             fetchAllProperties();
         } catch (err) {
              console.error(`Admin: Failed to delete property ${propertyId}:`, err);
              const errMsg = err.response?.data?.error || err.message || 'Failed to delete property.';
              setError(`Delete Error: ${errMsg}`);
              alert(`Delete Error: ${errMsg}`);
         }
    };

    // Basic Styles (Consider a dedicated Admin CSS)
    const tableStyle = { width: '100%', borderCollapse: 'collapse', marginTop: '20px', backgroundColor: '#fff' };
    const thTdStyle = { border: '1px solid var(--border-color)', padding: '10px 12px', textAlign: 'left' };
    const thStyle = { ...thTdStyle, backgroundColor: '#e9ecef', fontWeight: '600' };
    const errorStyle = { color: 'var(--error-text)', backgroundColor: 'var(--error-bg)', border: '1px solid var(--error-border)', padding: '15px', marginBottom: '15px', borderRadius: '4px'};
    const actionButtonStyle = { fontSize: '0.85em', padding: '4px 8px', marginRight: '5px', cursor: 'pointer', borderRadius: '3px', border: '1px solid transparent'};
    const editButtonStyle = {...actionButtonStyle, backgroundColor: 'var(--accent-color)', color: '#fff', borderColor: 'var(--accent-color)'};
    const deleteButtonStyle = {...actionButtonStyle, backgroundColor: 'var(--error-text)', color: '#fff', borderColor: 'var(--error-text)'};

    if (loading) return <p>Loading all properties...</p>;

    return (
        <div>
            <h2>Admin Panel - All Properties</h2>
            {error && <p style={errorStyle}>Error: {error}</p>}
            {!error && properties.length === 0 && <p>No properties found in the system.</p>}
            {!error && properties.length > 0 && (
                <table style={tableStyle}>
                    <thead>
                        <tr>
                            <th style={thStyle}>ID</th>
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
                        {properties.map(prop => (
                            <tr key={prop.id}>
                                <td style={thTdStyle}>{prop.id}</td>
                                <td style={thTdStyle}>{prop.address}</td>
                                <td style={thTdStyle}>{prop.city}</td>
                                <td style={thTdStyle}>${prop.price ? Number(prop.price).toLocaleString() : 'N/A'}</td>
                                <td style={thTdStyle}>{prop.type}</td>
                                <td style={thTdStyle}>{prop.status}</td>
                                <td style={thTdStyle}>{prop.owner?.email || 'N/A'}</td>
                                <td style={thTdStyle}>
                                    <button onClick={() => navigate(`/properties/${prop.id}/edit`)} style={editButtonStyle}>Edit</button>
                                    <button onClick={() => handleDelete(prop.id, prop.address)} style={deleteButtonStyle}>Delete</button>
                                    <button onClick={() => navigate(`/properties/${prop.id}`)} style={{...actionButtonStyle, backgroundColor: 'var(--secondary-color)', color: 'var(--text-dark)'}}>View</button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </div>
    );
}

export default AdminPropertyList;