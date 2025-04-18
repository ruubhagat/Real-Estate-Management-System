import React, { useState } from 'react';
import apiClient from '../api/axiosConfig';
import './Form.css'; // Ensure form styles are available

// Simple date validation helper (basic example)
const validateDate = (dateStr) => {
    if (!dateStr) return false;
    const today = new Date();
    today.setHours(0, 0, 0, 0); // Set time to start of day for comparison
    try {
      // Ensure date string is parsed correctly, considering potential timezone issues
      // Parsing as YYYY-MM-DD assumes local timezone
      const selectedDate = new Date(dateStr + 'T00:00:00');
      if (isNaN(selectedDate.getTime())) return false; // Invalid date format
      return selectedDate >= today;
    } catch (e) {
      return false; // Error during date parsing
    }
};

// VVV --- MODIFIED TIME VALIDATION --- VVV
const validateTime = (timeStr) => {
    // Check format HH:mm and range 11:00 to 18:59 (for up to 7 PM)
    if (!/^(0[0-9]|1[0-9]|2[0-3]):[0-5]\d$/.test(timeStr)) return false; // Basic HH:mm format check
    const [hours, minutes] = timeStr.split(':').map(Number);
    // Check if time is between 11:00 AM and 6:59 PM (inclusive of 11:00, exclusive of 19:00)
    return hours >= 11 && hours < 19; // 11 AM to just before 7 PM
};
// ^^^ --- END MODIFIED TIME VALIDATION --- ^^^


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
         // Use updated validation function
         if (!validateTime(visitTime)) {
             // Update error message for new time range
            setError('Please select a valid visit time between 11:00 AM and 6:59 PM.');
            return;
        }

        setSubmitting(true);
        try {
            const bookingRequest = {
                propertyId: propertyId, // Passed as prop
                visitDate: visitDate,
                visitTime: visitTime, // Send as HH:mm string
                customerNotes: customerNotes,
            };
            console.log("Submitting booking request:", bookingRequest);
            // Use the correct endpoint (relative to apiClient baseURL)
            await apiClient.post('/bookings', bookingRequest);

            onBookingSuccess(); // Notify parent component
            // Clear form after successful submission (optional, as parent hides it)
            setVisitDate('');
            setVisitTime('');
            setCustomerNotes('');

        } catch (err) {
            console.error("Failed to submit booking request:", err);
            let errMsg = 'Failed to submit request.';
            if(err.response?.data?.error) {
                errMsg = err.response.data.error;
            } else if (err.response?.status === 400) { // Catch potential backend validation errors
                 errMsg = err.response.data?.message || "Invalid booking data provided.";
            } else if (err.response?.status === 401 || err.response?.status === 403) {
                 errMsg = "Authentication error. Please log in again.";
            } else {
                 errMsg = err.message || errMsg;
            }
            setError(errMsg);
        } finally {
            setSubmitting(false);
        }
    };

    // --- Styles --- (Keep or move to CSS)
    const formStyle = { border: '1px dashed #ccc', padding: '15px', marginTop: '10px', borderRadius: '5px', backgroundColor: '#fdfdfd' };
    const inputGroupStyle = { marginBottom: '15px' }; // Increased spacing
    const labelStyle = { display: 'block', marginBottom: '5px', fontSize: '0.9em', fontWeight: '500' };
    const inputStyle = { width: '100%', padding: '10px', boxSizing: 'border-box', border: '1px solid #ccc', borderRadius: '4px', fontSize:'1rem'}; // Consistent input size
    const textareaStyle = { ...inputStyle, resize: 'vertical', minHeight: '70px' };
    const buttonContainerStyle = { display: 'flex', gap: '10px', marginTop: '10px'};
    const buttonStyle = { padding: '10px 15px', cursor: 'pointer', border: 'none', borderRadius: '4px', fontWeight: '500'};
    const submitButtonStyle = {...buttonStyle, backgroundColor: 'var(--primary-color)', color: 'white'};
    const cancelButtonStyle = {...buttonStyle, backgroundColor: 'var(--secondary-color)', color: 'var(--text-dark)'};
    const disabledButtonStyle = {...submitButtonStyle, opacity: 0.6, cursor: 'not-allowed'};
    const errorStyle = { color: 'var(--error-text)', fontSize: '0.9em', marginTop: '10px', marginBottom:'10px', fontWeight:'500'};
    const helperTextStyle = { fontSize: '0.85em', color: 'var(--text-muted)', display: 'block', marginTop: '4px'};

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
                    // Consider adding a min attribute for better browser UX
                    // min={new Date().toISOString().split('T')[0]}
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
                    // Optional: HTML5 min/max might provide basic browser-level constraints
                    // min="11:00" max="18:59" // Note: max is often exclusive in time inputs
                 />
                  {/* VVV --- MODIFIED HELPER TEXT --- VVV */}
                  <small style={helperTextStyle}> (Visit hours: 11:00 AM - 7:00 PM)</small>
                  {/* ^^^ --- END MODIFIED HELPER TEXT --- ^^^ */}
            </div>
            <div style={inputGroupStyle}>
                <label htmlFor="customerNotes" style={labelStyle}>Notes (Optional):</label>
                <textarea
                    id="customerNotes"
                    value={customerNotes}
                    onChange={(e) => setCustomerNotes(e.target.value)}
                    rows="3"
                    style={textareaStyle} // Use specific style for textarea
                    placeholder="Any specific questions or requests?"
                ></textarea>
            </div>
            <div style={buttonContainerStyle}>
                <button type="submit" disabled={submitting} style={submitting ? disabledButtonStyle : submitButtonStyle}>
                    {submitting ? 'Submitting...' : 'Submit Request'}
                </button>
                 <button type="button" onClick={onCancel} style={cancelButtonStyle} disabled={submitting}>
                    Cancel
                </button>
            </div>
        </form>
    );
}

export default RequestBookingForm;