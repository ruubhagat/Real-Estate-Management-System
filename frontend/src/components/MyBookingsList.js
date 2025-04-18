import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import apiClient from '../api/axiosConfig';
import './Form.css'; // For potential error message styling

function MyBookingsList() {
    const [bookings, setBookings] = useState([]); // State will now hold BookingResponseDTO[]
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [actionLoading, setActionLoading] = useState(null); // Track which booking action is loading

    // Get user role from localStorage
    const userRole = localStorage.getItem('userRole');

    // Fetch bookings based on user role
    useEffect(() => {
        const fetchBookings = async () => {
            setLoading(true);
            setError('');
            let url = '';
            // Determine API endpoint based on role
            if (userRole === 'CUSTOMER') url = '/bookings/my/customer';
            else if (userRole === 'PROPERTY_OWNER') url = '/bookings/my/owner';
            else if (userRole === 'ADMIN') url = '/bookings/admin/all'; // Admins see all
            else {
                setError('Invalid user role or not logged in.');
                setLoading(false);
                return;
            }

            try {
                console.log(`Fetching bookings from URL: ${url}`);
                // Ensure apiClient baseURL is set correctly (e.g., http://localhost:8081/api)
                const response = await apiClient.get(url);
                console.log("Bookings data received:", response.data);
                // Ensure response data is an array
                setBookings(Array.isArray(response.data) ? response.data : []);
            } catch (err) {
                 console.error(`Failed to fetch bookings from ${url}:`, err);
                 let fetchErrMsg = 'Failed to load bookings.';
                 if (err.response) {
                     if (err.response.status === 401 || err.response.status === 403) { fetchErrMsg = "Authentication error or insufficient permissions."; }
                     else if (err.response.data?.error) { fetchErrMsg = err.response.data.error; }
                     else { fetchErrMsg = err.message || fetchErrMsg; }
                 } else { fetchErrMsg = err.message || fetchErrMsg; }
                 setError(fetchErrMsg);
                 setBookings([]); // Clear bookings on error
            } finally {
                setLoading(false);
            }
        };
        // Only fetch if userRole is defined
        if (userRole) {
            fetchBookings();
        } else {
            setError("Cannot determine user role. Please log in.");
            setLoading(false);
        }
    }, [userRole]); // Re-fetch if userRole changes (e.g., after login/logout refresh)

    // Handle updating booking status
    const handleStatusUpdate = async (bookingId, newStatus, notes = '') => {
        // Confirmation dialogs
        if (newStatus === 'CANCELLED' && !window.confirm('Are you sure you want to cancel this booking?')) return;
        if (newStatus === 'REJECTED' && !window.confirm('Are you sure you want to reject this booking request?')) return;
        if (newStatus === 'CONFIRMED' && !window.confirm('Are you sure you want to confirm this booking request?')) return;
        if (newStatus === 'COMPLETED' && !window.confirm('Are you sure you want to mark this booking as completed?')) return;

        setActionLoading(bookingId); // Indicate loading state for this specific booking
        setError(''); // Clear previous errors

        try {
             console.log(`Updating booking ${bookingId} to status ${newStatus}`);
             // Use the PATCH endpoint relative to apiClient baseURL
             const response = await apiClient.patch(`/bookings/${bookingId}/status`, { newStatus, notes });
             console.log("Booking status update response:", response.data); // API returns updated BookingResponseDTO

             // Update local state with the updated booking DTO returned from the backend
             setBookings(prevBookings =>
                 prevBookings.map(b => (b.id === bookingId ? response.data : b))
             );
             alert(`Booking status updated to ${newStatus}`);

        } catch (err) {
            console.error(`Failed to update booking ${bookingId} status:`, err);
            let updateErrMsg = 'Failed to update status.';
            if (err.response) {
                if (err.response.status === 403) { updateErrMsg = "Permission denied."; }
                else if (err.response.status === 404) { updateErrMsg = "Booking not found."; }
                else if (err.response.data?.error) { updateErrMsg = err.response.data.error; }
                else { updateErrMsg = err.message || updateErrMsg; }
            } else { updateErrMsg = err.message || updateErrMsg; }
            setError(`Error updating booking ${bookingId}: ${updateErrMsg}`); // Set error specific to action
            alert(`Error: ${updateErrMsg}`);
        } finally {
             setActionLoading(null); // Clear loading state
        }
    };

    // --- Styles ---
    const listStyle = { listStyleType: 'none', padding: 0 };
    const listItemStyle = { border: '1px solid #eee', padding: '15px', marginBottom: '15px', borderRadius: '5px', backgroundColor: '#fff', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' };
    const errorStyle = { color: 'var(--error-text)', backgroundColor: 'var(--error-bg)', border: '1px solid var(--error-border)', padding: '10px 15px', margin: '10px 0', borderRadius: '4px'};
    const buttonGroupStyle = { marginTop: '10px', paddingTop: '10px', borderTop: '1px solid #eee', display: 'flex', flexWrap: 'wrap', gap: '8px' };
    const buttonStyle = { padding: '5px 10px', cursor: 'pointer', border: 'none', borderRadius: '4px', fontSize: '0.9em', fontWeight: '500'};
    const confirmButtonStyle = {...buttonStyle, backgroundColor: 'var(--success-bg)', color: 'var(--success-text)', border: '1px solid var(--success-border)'};
    const rejectButtonStyle = {...buttonStyle, backgroundColor: 'var(--warning-bg)', color: 'var(--warning-text)', border: '1px solid var(--warning-border)'}; // Example warning style
    const cancelButtonStyle = {...buttonStyle, backgroundColor: 'var(--error-bg)', color: 'var(--error-text)', border: '1px solid var(--error-border)'};
    const completeButtonStyle = {...buttonStyle, backgroundColor: 'var(--info-bg)', color: 'var(--info-text)', border: '1px solid var(--info-border)'}; // Example info style
    const disabledButtonStyle = {...buttonStyle, opacity: 0.6, cursor: 'not-allowed'};
    // --- End Styles ---


    if (loading) return <p className="content-wrapper">Loading bookings...</p>;

    return (
        // Use content-wrapper for consistent padding/width
        <div className="content-wrapper my-bookings-list">
            <h2>My Bookings{userRole ? ` (${userRole.replace('_', ' ')})` : ''}</h2>
             {error && <div style={errorStyle}>Error: {error}</div>}

             {bookings.length === 0 && !loading && !error ? (
                <p>You have no booking requests {userRole === 'PROPERTY_OWNER' ? 'for your properties' : 'at the moment'}.</p>
            ) : (
                <ul style={listStyle}>
                    {bookings.map(booking => {
                        const isLoadingAction = actionLoading === booking.id; // Check if action is loading for this booking
                        return (
                        <li key={booking.id} style={listItemStyle}>
                             {/* Display Booking Info using DTO fields */}
                             <div><strong>Property:</strong> <Link to={`/properties/${booking.propertyId}`}>{booking.propertyAddress || 'N/A'} ({booking.propertyCity || 'N/A'})</Link></div>
                             {/* Conditionally show customer/owner names based on role */}
                             {userRole !== 'CUSTOMER' && <div><strong>Customer:</strong> {booking.customerName || 'N/A'} ({booking.customerId})</div>}
                             {(userRole === 'CUSTOMER' || userRole === 'ADMIN') && <div><strong>Property Owner:</strong> {booking.ownerName || 'N/A'} ({booking.ownerId})</div>}

                             <div><strong>Visit Date:</strong> {booking.visitDate} at {booking.visitTime}</div>
                             <div><strong>Status:</strong> <span style={{ fontWeight: 'bold', textTransform: 'capitalize' }}>{booking.status ? booking.status.toLowerCase() : 'N/A'}</span></div>
                             {booking.customerNotes && <div><strong>Customer Notes:</strong> {booking.customerNotes}</div>}
                             {booking.ownerAgentNotes && <div><strong>Owner/Agent Notes:</strong> {booking.ownerAgentNotes}</div>}
                             <div><strong>Requested On:</strong> {booking.createdAt ? new Date(booking.createdAt).toLocaleString() : 'N/A'}</div>

                             {/* --- Action Buttons Based on Role and Status --- */}
                             <div style={buttonGroupStyle}>
                                 {/* VVV--- MODIFICATION START: Owner Accept/Reject ---VVV */}
                                 {userRole === 'PROPERTY_OWNER' && booking.status === 'PENDING' && (
                                     <>
                                         <button
                                             onClick={() => handleStatusUpdate(booking.id, 'CONFIRMED', 'Visit confirmed by owner.')}
                                             style={isLoadingAction ? disabledButtonStyle : confirmButtonStyle}
                                             disabled={isLoadingAction}
                                             title="Confirm this visit request"
                                         >
                                             {isLoadingAction ? 'Processing...' : 'Confirm Visit'}
                                         </button>
                                         <button
                                             onClick={() => handleStatusUpdate(booking.id, 'REJECTED', prompt('Reason for rejection (optional):') || '')}
                                             style={isLoadingAction ? disabledButtonStyle : rejectButtonStyle}
                                             disabled={isLoadingAction}
                                             title="Reject this visit request"
                                         >
                                             {isLoadingAction ? 'Processing...' : 'Reject Visit'}
                                         </button>
                                     </>
                                 )}
                                 {/* VVV--- END MODIFICATION ---VVV */}

                                 {/* Cancel Button (Visible to relevant roles for non-terminal statuses) */}
                                 { (userRole === 'CUSTOMER' || userRole === 'PROPERTY_OWNER' || userRole === 'ADMIN') &&
                                   !['COMPLETED', 'CANCELLED', 'REJECTED'].includes(booking.status) && ( // Check status is appropriate for cancelling
                                     <button
                                         onClick={() => handleStatusUpdate(booking.id, 'CANCELLED', prompt('Reason for cancellation (optional):') || '')}
                                         style={isLoadingAction ? disabledButtonStyle : cancelButtonStyle}
                                         disabled={isLoadingAction}
                                         title="Cancel this booking"
                                     >
                                          {isLoadingAction ? 'Processing...' : 'Cancel Booking'}
                                     </button>
                                 )}

                                  {/* Mark as Completed Button (Visible to Owner/Admin for CONFIRMED bookings) */}
                                  {(userRole === 'PROPERTY_OWNER' || userRole === 'ADMIN') && booking.status === 'CONFIRMED' && (
                                     <button
                                         onClick={() => handleStatusUpdate(booking.id, 'COMPLETED', 'Visit completed.')}
                                         style={isLoadingAction ? disabledButtonStyle : completeButtonStyle}
                                         disabled={isLoadingAction}
                                         title="Mark visit as completed"
                                     >
                                         {isLoadingAction ? 'Processing...' : 'Mark Completed'}
                                     </button>
                                 )}
                             </div>
                             {/* --- End Action Buttons --- */}
                        </li>
                    )})}
                </ul>
            )}
        </div>
    );
}

export default MyBookingsList;