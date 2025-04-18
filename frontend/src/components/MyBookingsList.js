import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import apiClient from '../api/axiosConfig';
import './Form.css';

function MyBookingsList() {
    const [bookings, setBookings] = useState([]); // State will now hold BookingResponseDTO[]
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const userRole = localStorage.getItem('userRole');

    useEffect(() => {
        const fetchBookings = async () => {
            setLoading(true);
            setError('');
            let url = '';
            if (userRole === 'CUSTOMER') url = '/bookings/my/customer';
            else if (userRole === 'PROPERTY_OWNER') url = '/bookings/my/owner';
            else if (userRole === 'ADMIN') url = '/bookings/admin/all';
            else {
                setError('Invalid user role to view bookings.');
                setLoading(false);
                return;
            }

            try {
                console.log(`Fetching bookings from URL: ${url}`);
                const response = await apiClient.get(url); // API now returns List<BookingResponseDTO>
                console.log("Bookings data received:", response.data);
                setBookings(response.data || []);
            } catch (err) {
                 console.error(`Failed to fetch bookings from ${url}:`, err);
                 const errMsg = err.response?.data?.error || err.message || 'Failed to load bookings.';
                 setError(errMsg);
            } finally {
                setLoading(false);
            }
        };
        fetchBookings();
    }, [userRole]);

    const handleStatusUpdate = async (bookingId, newStatus, notes = '') => {
         // ... (confirmation dialogs as before) ...
         if (newStatus === 'CANCELLED' && !window.confirm('Are you sure you want to cancel?')) return;
         if (newStatus === 'REJECTED' && !window.confirm('Are you sure you want to reject?')) return;

        try {
             console.log(`Updating booking ${bookingId} to status ${newStatus}`);
             const response = await apiClient.patch(`/bookings/${bookingId}/status`, { newStatus, notes });
             console.log("Booking status update response:", response.data); // API now returns updated BookingResponseDTO

             // Update local state with the DTO returned from the backend
             setBookings(prevBookings =>
                 prevBookings.map(b => b.id === bookingId ? response.data : b)
             );
             alert(`Booking status updated to ${newStatus}`);

        } catch (err) {
            console.error(`Failed to update booking ${bookingId} status:`, err);
            const errMsg = err.response?.data?.error || err.message || 'Failed to update status.';
            alert(`Error: ${errMsg}`);
        }
    };

    // Styles (keep as before)
    const listStyle = { listStyleType: 'none', padding: 0 };
    const listItemStyle = { border: '1px solid #eee', padding: '15px', marginBottom: '15px', borderRadius: '5px', backgroundColor: '#fff', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' };
    const errorStyle = { color: '#721c24', backgroundColor: '#f8d7da', border: '1px solid #f5c6cb', padding: '15px', marginBottom: '15px', borderRadius: '4px'};
    const buttonStyle = { padding: '5px 10px', cursor: 'pointer', marginRight: '5px', border: 'none', borderRadius: '4px', fontSize: '0.9em' };


    if (loading) return <p>Loading bookings...</p>;
    if (error) return <div style={errorStyle}>Error: {error}</div>;

    return (
        <div>
            <h2>My Bookings ({userRole})</h2>
             {bookings.length === 0 ? (
                <p>You have no booking requests matching the criteria.</p> // Updated message
            ) : (
                <ul style={listStyle}>
                    {bookings.map(booking => ( // booking is now a BookingResponseDTO
                        <li key={booking.id} style={listItemStyle}>
                             {/* VVV--- Access data directly from DTO ---VVV */}
                             <div><strong>Property:</strong> <Link to={`/properties/${booking.propertyId}`}>{booking.propertyAddress || 'N/A'} ({booking.propertyCity || 'N/A'})</Link></div>
                             {userRole !== 'CUSTOMER' && <div><strong>Customer:</strong> {booking.customerName || 'N/A'}</div>}
                             {userRole === 'CUSTOMER' || userRole === 'ADMIN' && <div><strong>Property Owner:</strong> {booking.ownerName || 'N/A'}</div>}
                             <div><strong>Visit Date:</strong> {booking.visitDate} at {booking.visitTime}</div>
                             <div><strong>Status:</strong> <span style={{ fontWeight: 'bold' }}>{booking.status}</span></div>
                             {booking.customerNotes && <div><strong>Your Notes:</strong> {booking.customerNotes}</div>}
                             {booking.ownerAgentNotes && <div><strong>Owner/Agent Notes:</strong> {booking.ownerAgentNotes}</div>}
                             <div><strong>Requested On:</strong> {booking.createdAt ? new Date(booking.createdAt).toLocaleString() : 'N/A'}</div>
                             {/* ^^^--- Access data directly from DTO ---^^^ */}

                             {/* Action Buttons based on Role and Status (logic remains the same) */}
                             <div style={{ marginTop: '10px', paddingTop: '10px', borderTop: '1px solid #eee' }}>
                                 {(userRole === 'PROPERTY_OWNER' || userRole === 'ADMIN') && booking.status === 'PENDING' && (
                                     <>
                                         <button onClick={() => handleStatusUpdate(booking.id, 'CONFIRMED', 'Visit confirmed.')} style={{...buttonStyle, backgroundColor: 'green', color: 'white'}}>Confirm</button>
                                         <button onClick={() => handleStatusUpdate(booking.id, 'REJECTED', prompt('Reason for rejection (optional):') || '')} style={{...buttonStyle, backgroundColor: 'orange'}}>Reject</button>
                                     </>
                                 )}
                                 {booking.status !== 'COMPLETED' && booking.status !== 'CANCELLED' && booking.status !== 'REJECTED' && (
                                     <button onClick={() => handleStatusUpdate(booking.id, 'CANCELLED', prompt('Reason for cancellation (optional):') || '')} style={{...buttonStyle, backgroundColor: 'red', color: 'white'}}>Cancel Booking</button>
                                 )}
                                  {(userRole === 'PROPERTY_OWNER' || userRole === 'ADMIN') && booking.status === 'CONFIRMED' && (
                                     <button onClick={() => handleStatusUpdate(booking.id, 'COMPLETED', 'Visit completed.')} style={{...buttonStyle, backgroundColor: 'blue', color: 'white'}}>Mark as Completed</button>
                                 )}
                             </div>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
}

export default MyBookingsList;