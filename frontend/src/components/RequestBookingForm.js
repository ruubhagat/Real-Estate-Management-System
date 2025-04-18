import React, { useState } from 'react';
import apiClient from '../api/axiosConfig';
import './Form.css';

// Simple date/time validation helper (basic examples)
const validateDate = (dateStr) => {
    if (!dateStr) return false;
    const today = new Date();
    today.setHours(0, 0, 0, 0); // Set time to start of day for comparison
    const selectedDate = new Date(dateStr + 'T00:00:00'); // Ensure parsing as local date
    return selectedDate >= today;
};

const validateTime = (timeStr) => {
    return /^(0[8-9]|1[0-7]):[0-5]\d$/.test(timeStr); // Example: Allow 08:00 to 17:59
};


function RequestBookingForm({ propertyId, onBookingSuccess, onCancel }) {
    const [visitDate, setVisitDate] = useState('');
    const [visitTime, setVisitTime] = useState(''); // Store time as HH:mm string
    const [customerNotes, setCustomerNotes] = useState('');
    const [error, setError] = useState('');
    const [submitting, setSubmitting] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        // Frontend Validation
        if (!visitDate || !visitTime) {
            setError('Please select both a date and a time for the visit.');
            return;
        }
        if (!validateDate(visitDate)) {
            setError('Visit date must be today or a future date.');
            return;
        }
         if (!validateTime(visitTime)) {
            setError('Please select a valid time between 08:00 and 17:59.');
            return;
        }


        setSubmitting(true);
        try {
            const bookingRequest = {
                propertyId: propertyId,
                visitDate: visitDate,
                visitTime: visitTime, // Send as HH:mm string
                customerNotes: customerNotes,
            };
            console.log("Submitting booking request:", bookingRequest);
            await apiClient.post('/bookings', bookingRequest); // POST /api/bookings

            onBookingSuccess(); // Notify parent component
            // Reset form? Parent component hides it now.
            setVisitDate('');
            setVisitTime('');
            setCustomerNotes('');

        } catch (err) {
            console.error("Failed to submit booking request:", err);
            const errMsg = err.response?.data?.error || err.message || 'Failed to submit request.';
            setError(errMsg);
        } finally {
            setSubmitting(false);
        }
    };

    // --- Styles ---
    const formStyle = { border: '1px dashed #ccc', padding: '15px', marginTop: '10px', borderRadius: '5px' };
    const inputGroupStyle = { marginBottom: '10px' };
    const labelStyle = { display: 'block', marginBottom: '3px', fontSize: '0.9em' };
    const inputStyle = { width: '95%', padding: '8px', boxSizing: 'border-box', marginBottom: '5px', border: '1px solid #ccc', borderRadius: '4px'};
    const buttonStyle = { padding: '8px 15px', cursor: 'pointer', marginRight: '10px', border: 'none', borderRadius: '4px'};
    const errorStyle = { color: 'red', fontSize: '0.9em', marginTop: '5px'};


    return (
        <form onSubmit={handleSubmit} style={formStyle}>
             {error && <p style={errorStyle}>Error: {error}</p>}
            <div style={inputGroupStyle}>
                <label htmlFor="visitDate" style={labelStyle}>Preferred Date:</label>
                <input
                    type="date"
                    id="visitDate"
                    value={visitDate}
                    onChange={(e) => setVisitDate(e.target.value)}
                    style={inputStyle}
                    required
                />
            </div>
             <div style={inputGroupStyle}>
                 <label htmlFor="visitTime" style={labelStyle}>Preferred Time (HH:mm):</label>
                 <input
                    type="time"
                    id="visitTime"
                    value={visitTime}
                    onChange={(e) => setVisitTime(e.target.value)}
                    style={inputStyle}
                    required
                    // Optional: Add min/max if browser supports it well
                    // min="08:00" max="17:00"
                 />
                  <small> (Available 08:00 - 17:59)</small>
            </div>
            <div style={inputGroupStyle}>
                <label htmlFor="customerNotes" style={labelStyle}>Notes (Optional):</label>
                <textarea
                    id="customerNotes"
                    value={customerNotes}
                    onChange={(e) => setCustomerNotes(e.target.value)}
                    rows="3"
                    style={inputStyle}
                    placeholder="Any specific questions or requests?"
                ></textarea>
            </div>
            <div>
                <button type="submit" disabled={submitting} style={{...buttonStyle, backgroundColor: '#007bff', color: 'white'}}>
                    {submitting ? 'Submitting...' : 'Submit Request'}
                </button>
                 <button type="button" onClick={onCancel} style={{...buttonStyle, backgroundColor: '#6c757d', color: 'white'}}>
                    Cancel
                </button>
            </div>
        </form>
    );
}

export default RequestBookingForm;