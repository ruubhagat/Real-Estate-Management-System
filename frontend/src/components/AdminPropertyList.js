import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import apiClient from '../api/axiosConfig';

function AdminPropertyList() {
    console.log("[AdminPropertyList] Component Rendering/Re-rendering."); // <-- Log component render
    const [properties, setProperties] = useState([]);
    const [loading, setLoading] = useState(true); // Start in loading state
    const [error, setError] = useState('');
    const navigate = useNavigate();

    // Fetch all properties (Admin endpoint)
    const fetchAllProperties = async () => {
        console.log("[AdminPropertyList] ENTERING fetchAllProperties function."); // <-- Log function entry
        setLoading(true); // Ensure loading is true at start of fetch
        setError('');
        try {
            console.log("[AdminPropertyList] Preparing API call to /admin/properties..."); // <-- Log before API call
            // Ensure baseURL in apiClient is correct (e.g., http://localhost:8081/api)
            const response = await apiClient.get('/admin/properties'); // Path relative to baseURL
            console.log("[AdminPropertyList] API CALL SUCCEEDED. Response data:", response.data); // <-- Log success
            // Ensure the response data is an array before setting
            setProperties(Array.isArray(response.data) ? response.data : []);
            console.log("[AdminPropertyList] Successfully set properties state."); // <-- Log state set
        } catch (err) {
            // --- Log Detailed Error ---
            console.error("[AdminPropertyList] API CALL FAILED:", err);
             if (err.response) {
                 console.error("[AdminPropertyList] Error Response Status:", err.response.status);
                 console.error("[AdminPropertyList] Error Response Data:", err.response.data);
             } else if (err.request) {
                 console.error("[AdminPropertyList] Error Request (No response received):", err.request);
             } else {
                 console.error("[AdminPropertyList] Error Message (Setup or other issue):", err.message);
             }
            // --- End Detailed Error Log ---

            let errMsg = 'Failed to load properties.';
            if (err.response) {
                if (err.response.status === 403) { errMsg = "Access Denied: You do not have permission to view this page."; }
                else if (err.response.status === 401) { errMsg = "Authentication required. Please log in as Admin."; }
                else if (err.response.data?.error) { errMsg = err.response.data.error; }
                else { errMsg = err.message || errMsg; }
            } else { errMsg = err.message || errMsg; }
            setError(errMsg);
            setProperties([]);
            console.log("[AdminPropertyList] Set error state:", errMsg); // <-- Log error set
        } finally {
            console.log("[AdminPropertyList] ENTERING finally block."); // <-- Log finally entry
            setLoading(false);
            console.log("[AdminPropertyList] Set loading state to false."); // <-- Log loading set
        }
    };

    // Fetch on mount
    useEffect(() => {
        console.log("[AdminPropertyList] useEffect hook fired. Calling fetchAllProperties."); // <-- Log useEffect
        fetchAllProperties();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []); // Empty dependency array ensures it runs only once on mount

    // Handle deleting a property (Admin action)
    const handleDelete = async (propertyId, propertyAddress) => {
         // ... (delete logic remains the same) ...
         if (!window.confirm(`ADMIN ACTION: Are you sure you want to delete property "${propertyAddress || 'N/A'}" (ID: ${propertyId})? This action cannot be undone.`)) return;
         setError('');
         try {
             console.log(`Admin: Deleting property ${propertyId} via admin endpoint...`);
             await apiClient.delete(`/admin/properties/${propertyId}`); // Relative path
             alert(`Property ${propertyId} deleted successfully by Admin.`);
             fetchAllProperties(); // Refetch after delete
         } catch (err) {
              console.error(`Admin: Failed to delete property ${propertyId}:`, err);
              let deleteErrMsg = 'Failed to delete property.';
              // ... (error handling for delete) ...
              setError(`Delete Error: ${deleteErrMsg}`);
              alert(`Delete Error: ${deleteErrMsg}`);
         }
    };

    // --- Styles ---
    const tableStyle = { width: '100%', borderCollapse: 'collapse', marginTop: '20px', backgroundColor: '#fff', fontSize: '0.9rem' };
    const thTdStyle = { border: '1px solid var(--border-color)', padding: '8px 10px', textAlign: 'left', verticalAlign: 'middle' };
    const thStyle = { ...thTdStyle, backgroundColor: '#e9ecef', fontWeight: '600' };
    const errorStyle = { color: 'var(--error-text)', backgroundColor: 'var(--error-bg)', border: '1px solid var(--error-border)', padding: '15px', marginBottom: '15px', borderRadius: '4px'};
    const actionButtonStyle = { fontSize: '0.85em', padding: '4px 8px', marginRight: '5px', cursor: 'pointer', borderRadius: '3px', border: '1px solid transparent', whiteSpace: 'nowrap' };
    const deleteButtonStyle = {...actionButtonStyle, backgroundColor: 'var(--error-bg)', color: 'var(--error-text)', borderColor: 'var(--error-border)'};
    const viewButtonStyle = {...actionButtonStyle, backgroundColor: 'var(--secondary-color)', color: 'var(--text-dark)', borderColor: 'var(--secondary-color)'};
    // --- End Styles ---

    // --- Render Logic ---
    console.log(`[AdminPropertyList] Rendering - Loading State: ${loading}, Error State: ${error}`); // <-- Log state before returning JSX

    // Display loading message first
    if (loading) {
        console.log("[AdminPropertyList] Returning Loading JSX."); // <-- Log loading return
        // Ensure it's wrapped if App.js doesn't provide context wrapper
        return <div className="content-wrapper"><p>Loading all properties for Admin...</p></div>;
    }

    // Display error message if loading is false and error exists
    if (error) {
         console.log("[AdminPropertyList] Returning Error JSX."); // <-- Log error return
         return (
             <div className="content-wrapper admin-property-list">
                 <h2>Admin Panel - All Properties</h2>
                 <p style={errorStyle}>Error: {error}</p>
             </div>
         );
    }

    // Display table or "not found" message if loading is false and no error
    console.log(`[AdminPropertyList] Returning Table/Not Found JSX. Property Count: ${properties.length}`); // <-- Log final return
    return (
        // Use content-wrapper if needed for consistent padding/width
        <div className="content-wrapper admin-property-list">
            <h2>Admin Panel - All Properties</h2>
            {/* Message if no properties found */}
            {properties.length === 0 && (
                <p>No properties found in the system.</p>
            )}

            {/* Display table if properties exist */}
            {properties.length > 0 && (
                <div style={{overflowX: 'auto'}}>
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
                                    <td style={thTdStyle}>
                                        ₹{prop.price ? Number(prop.price).toLocaleString('en-IN') : 'N/A'}
                                        {prop.type === 'RENT' && <span style={{fontSize: '0.8em', color: 'var(--text-muted)'}}> /mo</span>}
                                    </td>
                                    <td style={thTdStyle}>{prop.type}</td>
                                    <td style={thTdStyle}>{prop.status}</td>
                                    <td style={thTdStyle}>{prop.owner?.email || 'N/A'}</td>
                                    <td style={thTdStyle}>
                                        {/* Admin Actions: Delete and View */}
                                        <button onClick={() => handleDelete(prop.id, prop.address)} style={deleteButtonStyle} title="Delete Property">Delete</button>
                                        <button onClick={() => navigate(`/properties/${prop.id}`)} style={viewButtonStyle} title="View Property Details">View</button>
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