import React, { useState, useEffect } from 'react';
// Removed Link import
import { useNavigate } from 'react-router-dom';
import apiClient from '../api/axiosConfig';
import './Form.css'; // For potential error message styling

function MyBookingsList() {
    const [bookings, setBookings] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [actionError, setActionError] = useState('');
    const [actionLoading, setActionLoading] = useState(null);

    const userRole = localStorage.getItem('userRole');
    const navigate = useNavigate();

    // Fetch bookings effect
    useEffect(() => {
        const fetchBookings = async () => {
            setLoading(true); setError(''); setActionError('');
            let url = '';
            if (userRole === 'CUSTOMER') url = '/bookings/my/customer';
            else if (userRole === 'PROPERTY_OWNER') url = '/bookings/my/owner';
            else if (userRole === 'ADMIN') url = '/bookings/admin/all';
            else { setError('Invalid user role.'); setLoading(false); return; }

            try {
                const response = await apiClient.get(url);
                setBookings(Array.isArray(response.data) ? response.data : []);
            } catch (err) { /* ... error handling ... */ }
             finally { setLoading(false); }
        };
        if (userRole) { fetchBookings(); }
        else { /* ... handle no role ... */ }
    }, [userRole]);

    // Handle MAIN Status Update
    const handleStatusUpdate = async (bookingId, newStatus, notes = '') => {
        // ... (confirmations) ...
        setActionLoading(bookingId); setActionError('');
        try {
             const response = await apiClient.patch(`/bookings/${bookingId}/status`, { newStatus, notes });
             setBookings(prev => prev.map(b => (b.id === bookingId ? response.data : b)));
             alert(`Booking status updated to ${newStatus}`);
        } catch (err) { /* ... error handling ... */ setActionError(/*...*/); alert(/*...*/); }
        finally { setActionLoading(null); }
    };

    // Handle PAYMENT Status Update
    const handlePaymentUpdate = async (bookingId) => {
        if (!window.confirm('Mark payment as received?')) return;
        setActionLoading(bookingId); setActionError('');
        try {
             const response = await apiClient.post(`/payments/booking/${bookingId}/confirm-manual`); // Use POST to payment endpoint
             setBookings(prev => prev.map(b => (b.id === bookingId ? response.data : b)));
             alert(`Payment status marked as RECEIVED.`);
        } catch (err) { /* ... error handling ... */ setActionError(/*...*/); alert(/*...*/); }
        finally { setActionLoading(null); }
    };

    // --- Styles ---
    const listStyle = { listStyleType: 'none', padding: 0 };
    const listItemStyle = { border: '1px solid #eee', padding: '15px', marginBottom: '15px', borderRadius: '5px', backgroundColor: '#fff', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' };
    const errorStyle = { color: 'var(--error-text)', backgroundColor: 'var(--error-bg)', border: '1px solid var(--error-border)', padding: '10px 15px', margin: '10px 0', borderRadius: '4px'};
    const buttonGroupStyle = { marginTop: '12px', paddingTop: '12px', borderTop: '1px solid #eee', display: 'flex', flexWrap: 'wrap', gap: '8px' };
    const buttonStyle = { padding: '6px 12px', cursor: 'pointer', border: '1px solid transparent', borderRadius: '4px', fontSize: '0.9em', fontWeight: '500'};
    const confirmButtonStyle = {...buttonStyle, backgroundColor: 'var(--success-bg)', color: 'var(--success-text)', borderColor: 'var(--success-border)'};
    const rejectButtonStyle = {...buttonStyle, backgroundColor: 'var(--warning-bg)', color: 'var(--warning-text)', borderColor: 'var(--warning-border)'};
    const cancelButtonStyle = {...buttonStyle, backgroundColor: 'var(--error-bg)', color: 'var(--error-text)', borderColor: 'var(--error-border)'};
    const completeButtonStyle = {...buttonStyle, backgroundColor: 'var(--info-bg)', color: 'var(--info-text)', borderColor: 'var(--info-border)'};
    const paymentReceivedButtonStyle = {...buttonStyle, backgroundColor: '#17a2b8', color: 'white', borderColor: '#138496'};
    const disabledButtonStyle = {...buttonStyle, opacity: 0.6, cursor: 'not-allowed', backgroundColor: '#e9ecef', color:'#6c757d', borderColor:'#ced4da'};
    // --- End Styles ---

    if (loading) return <p className="content-wrapper">Loading bookings...</p>;

    return (
        <div className="content-wrapper my-bookings-list">
            <h2>My Bookings{userRole ? ` (${userRole.replace('_', ' ')})` : ''}</h2>
             {error && !actionLoading && <div style={errorStyle}>Error: {error}</div>}

             {bookings.length === 0 && !loading && !error ? ( <p>No bookings found.</p> ) : (
                <ul style={listStyle}>
                    {bookings.map(booking => {
                        const isLoadingAction = actionLoading === booking.id;
                        const canConfirmVisit = booking.status === 'PENDING' && booking.paymentStatus === 'RECEIVED';
                        // Determine if booking is in a final state
                        const isTerminalStatus = ['COMPLETED', 'CANCELLED', 'REJECTED'].includes(booking.status);

                        return (
                        <li key={booking.id} style={listItemStyle}>
                             {/* Booking Info */}
                             <div><strong>Property:</strong><span onClick={() => navigate(`/properties/${booking.propertyId}`)} style={{color: 'var(--primary-color)', cursor:'pointer', textDecoration:'underline', marginLeft:'5px'}}>{booking.propertyAddress || 'N/A'} ({booking.propertyCity || 'N/A'})</span></div>
                             {userRole !== 'CUSTOMER' && <div><strong>Customer:</strong> {booking.customerName || 'N/A'} ({booking.customerId})</div>}
                             {(userRole === 'CUSTOMER' || userRole === 'ADMIN') && <div><strong>Owner:</strong> {booking.ownerName || 'N/A'} ({booking.ownerId})</div>}
                             <div><strong>Visit Date:</strong> {booking.visitDate || 'N/A'} at {booking.visitTime || 'N/A'}</div>
                             <div><strong>Status:</strong> <span style={{ fontWeight: 'bold', textTransform: 'capitalize' }}>{booking.status ? booking.status.toLowerCase() : 'N/A'}</span></div>
                             <div><strong>Payment Status:</strong> <span style={{ fontWeight: 'bold', textTransform: 'capitalize' }}>{booking.paymentStatus ? booking.paymentStatus.toLowerCase() : 'N/A'}</span></div>
                             {booking.customerNotes && <div><strong>Customer Notes:</strong> {booking.customerNotes}</div>}
                             {booking.ownerAgentNotes && <div><strong>Owner/Agent Notes:</strong> {booking.ownerAgentNotes}</div>}
                             <div><strong>Requested On:</strong> {booking.createdAt ? new Date(booking.createdAt).toLocaleString() : 'N/A'}</div>
                             {actionError && actionLoading === booking.id && <div style={errorStyle}>{actionError}</div>}

                             {/* Action Buttons */}
                             <div style={buttonGroupStyle}>
                                 {/* --- VVV MODIFIED Condition for Payment Button VVV --- */}
                                 {userRole === 'PROPERTY_OWNER' &&
                                  booking.paymentStatus === 'PENDING' &&
                                  !isTerminalStatus && ( // Only show if booking isn't finished/cancelled/rejected
                                      <button
                                          onClick={() => handlePaymentUpdate(booking.id)}
                                          style={isLoadingAction ? disabledButtonStyle : paymentReceivedButtonStyle}
                                          disabled={isLoadingAction}
                                          title="Mark payment as received offline"
                                      >
                                          {isLoadingAction ? 'Processing...' : 'Mark Payment Received'}
                                      </button>
                                  )}
                                 {/* --- ^^^ End MODIFIED Condition ^^^ --- */}

                                 {/* Owner/Admin Confirm/Reject */}
                                 {(userRole === 'PROPERTY_OWNER' || userRole === 'ADMIN') && booking.status === 'PENDING' && (
                                     <>
                                         <button onClick={() => handleStatusUpdate(booking.id, 'CONFIRMED', 'Visit confirmed.')} style={isLoadingAction || !canConfirmVisit ? disabledButtonStyle : confirmButtonStyle} disabled={isLoadingAction || !canConfirmVisit} title={canConfirmVisit ? "Confirm" : "Confirm after payment received"}>{isLoadingAction ? '...' : 'Confirm Visit'}</button>
                                         <button onClick={() => handleStatusUpdate(booking.id, 'REJECTED', prompt('Reason:') || '')} style={isLoadingAction ? disabledButtonStyle : rejectButtonStyle} disabled={isLoadingAction} title="Reject request">{isLoadingAction ? '...' : 'Reject Visit'}</button>
                                     </>
                                 )}

                                 {/* Cancel Button */}
                                 { (userRole === 'CUSTOMER' || userRole === 'PROPERTY_OWNER' || userRole === 'ADMIN') && !isTerminalStatus && (
                                    <button onClick={() => handleStatusUpdate(booking.id, 'CANCELLED', prompt('Reason:') || '')} style={isLoadingAction ? disabledButtonStyle : cancelButtonStyle} disabled={isLoadingAction} title="Cancel booking"> {isLoadingAction ? '...' : 'Cancel Booking'} </button>
                                 )}

                                 {/* Complete Button */}
                                 {(userRole === 'PROPERTY_OWNER' || userRole === 'ADMIN') && booking.status === 'CONFIRMED' && (
                                    <button onClick={() => handleStatusUpdate(booking.id, 'COMPLETED', 'Visit completed.')} style={isLoadingAction ? disabledButtonStyle : completeButtonStyle} disabled={isLoadingAction} title="Mark visit completed"> {isLoadingAction ? '...' : 'Mark Completed'} </button>
                                 )}
                             </div>
                        </li>
                    )})}
                </ul>
            )}
        </div>
    );
}

export default MyBookingsList;