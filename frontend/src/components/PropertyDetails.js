import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import apiClient from '../api/axiosConfig';
import RequestBookingForm from './RequestBookingForm';
// import './PropertyDetails.css'; // Consider using a CSS file for styles

function PropertyDetails() {
  const [property, setProperty] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(''); // For fetch errors
  const [actionError, setActionError] = useState(''); // For delete/booking errors
  const { id } = useParams();
  const navigate = useNavigate();
  const [showBookingForm, setShowBookingForm] = useState(false);

  // Get current user info from local storage
  const currentUserId = localStorage.getItem('userId');
  const currentUserRole = localStorage.getItem('userRole');

  // Fetch property details when component mounts or ID changes
  useEffect(() => {
    const fetchPropertyDetails = async () => {
      setLoading(true);
      setError('');
      setActionError('');
      if (!id) {
        setError("No property ID provided.");
        setLoading(false);
        return;
      }
      try {
        console.log(`[PropertyDetails] Fetching details for ID: ${id}`);
        // Use relative path '/properties/{id}'
        const response = await apiClient.get(`/properties/${id}`);
        console.log("[PropertyDetails] API Response Data:", response.data);
        setProperty(response.data);
      } catch (err) {
        console.error("[PropertyDetails] Failed to fetch property details:", err);
        let errMsg = 'Failed to load property details.';
         if (err.response?.status === 404) {
            errMsg = "Property not found.";
         } else if (err.response?.status === 403) {
             errMsg = "Permission denied to view this property.";
         } else if (err.response?.data?.error) {
            errMsg = err.response.data.error;
         } else {
            errMsg = err.message;
         }
        setError(errMsg);
      } finally {
        setLoading(false);
      }
    };
    fetchPropertyDetails();
  }, [id]); // Re-run effect if the id parameter changes

  // Handle property deletion
  const handleDelete = async () => {
    if (!window.confirm(`Are you sure you want to delete property "${property?.address || 'N/A'}"? This action cannot be undone.`)) return;
    setActionError(''); // Clear previous action errors
    try {
        console.log(`Owner: Deleting property ${id} via owner endpoint...`);

        // --- CORRECTED API CALL PATH ---
        // Use relative path '/owner/properties/{id}' - baseURL 'http://localhost:8081/api' will be prepended by Axios
        await apiClient.delete(`/owner/properties/${id}`);
        // --- END CORRECTION ---

        alert('Property deleted successfully!');
        navigate('/properties'); // Navigate back to the list after deletion
    } catch (err) {
         console.error("Owner: Failed to delete property:", err);
         // Log detailed error information
         if (err.response) {
             console.error("[PropertyDetails] Delete Error Response Status:", err.response.status);
             console.error("[PropertyDetails] Delete Error Response Data:", err.response.data);
         } else if (err.request) {
             console.error("[PropertyDetails] Delete Error Request (No response received):", err.request);
         } else {
             console.error("[PropertyDetails] Delete Error Message (Setup or other issue):", err.message);
         }

         // Set user-friendly message
         let deleteErrMsg = 'Failed to delete property.';
         if (err.response?.status === 403) {
            // This message is now more likely accurate as the URL is fixed
            deleteErrMsg = "Permission Denied: You might not be the owner of this property or your session expired.";
         } else if (err.response?.status === 404) {
            deleteErrMsg = "Property not found. It might have already been deleted.";
         } else if (err.response?.data?.error) {
            deleteErrMsg = err.response.data.error;
         } else {
            deleteErrMsg = err.message;
         }
         setActionError(`Delete failed: ${deleteErrMsg}`); // Set action error state
         alert(`Delete failed: ${deleteErrMsg}`); // Also alert the user
    }
  }; // End of handleDelete

  // --- Inline Styles (Consider moving to a CSS file) ---
  const containerStyle = {
      padding: '20px',
      maxWidth: '1000px',
      margin: '20px auto',
      backgroundColor: 'var(--bg-content)',
      borderRadius: '6px',
      boxShadow: 'var(--shadow-color)',
      border: '1px solid var(--border-color)',
  };
  const mainImageStyle = {
      display: 'block', width: '100%', maxHeight: '65vh', objectFit: 'contain',
      marginBottom: '25px', borderRadius: '4px', backgroundColor: '#f0f0f0',
      border: '1px solid var(--border-color)'
  };
   const thumbnailContainerStyle = {
      display: 'flex', gap: '10px', flexWrap: 'wrap', marginBottom: '20px',
      paddingTop: '10px', borderTop: '1px solid var(--border-extra-light-color)'
   };
   const thumbnailStyle = {
      height: '80px', width: 'auto', objectFit: 'cover', borderRadius: '3px',
      border: '1px solid var(--border-color)', cursor: 'pointer'
   };
  const detailsSectionStyle = {
      marginBottom: '20px', paddingTop: '15px',
      borderTop: '1px solid var(--border-extra-light-color)'
  };
  const detailItemStyle = { marginBottom: '8px', fontSize: '1rem', color: 'var(--text-light)' };
  const detailLabelStyle = { fontWeight: '600', color: 'var(--text-dark)', marginRight: '8px' };
  const buttonContainerStyle = {
      marginTop: '25px', paddingTop: '20px', borderTop: '1px solid var(--border-color)',
      display: 'flex', gap: '10px', flexWrap: 'wrap'
   };
  const buttonStyle = {
      padding: '10px 18px', fontSize: '0.95rem', borderRadius: '4px', border: 'none', cursor: 'pointer',
      fontWeight: '500', transition: 'background-color 0.2s ease, transform 0.1s ease',
      textDecoration: 'none', display: 'inline-block', textAlign: 'center',
  };
  const editButtonStyle = { ...buttonStyle, backgroundColor: 'var(--accent-color)', color: '#fff', '&:hover': { backgroundColor: 'var(--accent-hover-color)' } };
  const deleteButtonStyle = { ...buttonStyle, backgroundColor: 'var(--error-bg)', color: 'var(--error-text)', border: '1px solid var(--error-border)', '&:hover': { backgroundColor: 'var(--error-hover-bg)' } };
  const bookButtonStyle = { ...buttonStyle, backgroundColor: 'var(--primary-color)', color: '#fff', '&:hover': { backgroundColor: 'var(--primary-hover-color)' } };
  const errorBoxStyle = {
    color: 'var(--error-text)', backgroundColor: 'var(--error-bg)', border: '1px solid var(--error-border)',
    padding: '15px', marginBottom: '15px', borderRadius: '4px'
  };
  // --- End Styles ---

  // --- Loading and Error States ---
  if (loading) return <p className="content-wrapper" style={{ textAlign: 'center' }}>Loading property details...</p>;
  if (error) return <div className="content-wrapper" style={errorBoxStyle}>Error fetching details: {error} <Link to="/properties">Back to list</Link></div>;
  if (!property) return <p className="content-wrapper" style={{ textAlign: 'center' }}>Property not found. <Link to="/properties">Back to list</Link></p>;
  // --- End Loading/Error States ---

  // --- Calculate Permissions ---
  const ownerId = property.owner?.id;
  const isOwner = ownerId !== null && ownerId !== undefined && String(ownerId) === String(currentUserId);
  const isCustomer = currentUserRole === 'CUSTOMER';
  // const isAdmin = currentUserRole === 'ADMIN';

  // --- Prepare Image URLs ---
  const imageBaseUrl = "/uploads/";
  const placeholderUrl = 'https://via.placeholder.com/800x500/cccccc/969696?text=No+Image+Available';
  const imageUrlsArray = (property.imageUrls && typeof property.imageUrls === 'string')
    ? property.imageUrls.split(',')
        .map(name => name.trim()).filter(name => name)
        .map(name => `${imageBaseUrl}${name}`)
    : [];
  const mainImageUrl = imageUrlsArray.length > 0 ? imageUrlsArray[0] : placeholderUrl;

  // Image error handler
  const handleImageError = (e) => {
      console.warn(`[PropertyDetails] Image failed to load: ${e.target.src}. Replacing with placeholder.`);
      if (e.target.src !== placeholderUrl) {
          e.target.onerror = null;
          e.target.src = placeholderUrl;
      }
  };

  // --- Render Logic ---
  return (
    <div style={containerStyle} className="property-details-container">
      {/* Display action errors (like delete failure) */}
      {actionError && <div style={errorBoxStyle}>Action Error: {actionError}</div>}

      {/* --- Main Image --- */}
      <img src={mainImageUrl} alt={`${property.address || 'Property'} main view`} style={mainImageStyle} onError={handleImageError}/>

      {/* --- Thumbnails --- */}
      {imageUrlsArray.length > 1 && (
        <div style={thumbnailContainerStyle}>
          {imageUrlsArray.map((url, index) => (
            <img key={index} src={url} alt={`Thumbnail ${index + 1}`} style={thumbnailStyle} onError={handleImageError} />
          ))}
        </div>
      )}

      {/* --- Property Info Sections --- */}
      <h2>{property.address || 'Address Not Available'}</h2>
      <p style={detailItemStyle}> {property.city || 'N/A'}, {property.state || 'N/A'} {property.postalCode || ''} </p>
      <div style={detailsSectionStyle}>
        <h4>Key Details</h4>
        <p style={detailItemStyle}> <span style={detailLabelStyle}>Price:</span> ₹{property.price ? Number(property.price).toLocaleString('en-IN') : 'N/A'} {property.type === 'RENT' && <span> / month</span>} </p>
        <p style={detailItemStyle}><span style={detailLabelStyle}>Type:</span> {property.type || 'N/A'}</p>
        <p style={detailItemStyle}><span style={detailLabelStyle}>Status:</span> {property.status || 'N/A'}</p>
        <p style={detailItemStyle}> <span style={detailLabelStyle}>Beds:</span> {property.bedrooms ?? 'N/A'} | <span style={detailLabelStyle}> Baths:</span> {property.bathrooms ?? 'N/A'} | <span style={detailLabelStyle}> Area:</span> {property.areaSqft ? `${property.areaSqft} sqft` : 'N/A'} </p>
      </div>
      {property.description && ( <div style={detailsSectionStyle}> <h4>Description</h4> <p style={{...detailItemStyle, whiteSpace: 'pre-wrap', lineHeight: '1.6'}}>{property.description}</p> </div> )}
      {property.owner && ( <div style={detailsSectionStyle}> <h4>Listed By</h4> {!isOwner && ( <p style={detailItemStyle}><span style={detailLabelStyle}>Contact:</span> {property.owner.name || 'Owner/Agent'}</p> )} {isOwner && ( <p style={detailItemStyle}>This is your property listing.</p> )} </div> )}

      {/* --- Booking Section --- */}
      {isCustomer && !isOwner && (property.status === 'AVAILABLE' || property.status === 'PENDING') && (
        <div style={{ marginTop: '20px', paddingTop: '15px', borderTop: '1px solid var(--border-color)' }}>
          <h4>Request a Visit</h4>
          {!showBookingForm ? (
            <button onClick={() => setShowBookingForm(true)} style={bookButtonStyle}>Request Visit</button>
          ) : (
            <RequestBookingForm propertyId={property.id} onBookingSuccess={() => { alert('Booking request submitted successfully!'); setShowBookingForm(false); }} onCancel={() => setShowBookingForm(false)}/>
          )}
        </div>
      )}

      {/* --- Action Buttons (Owner Only) --- */}
      {isOwner && (
        <div style={buttonContainerStyle}>
          <Link to={`/properties/${id}/edit`} style={{ textDecoration: 'none' }}> <button style={editButtonStyle}>Edit Property</button> </Link>
          <button onClick={handleDelete} style={deleteButtonStyle}>Delete Property</button>
        </div>
      )}

      {/* --- Back Link --- */}
      <div style={{ marginTop: '30px' }}> <Link to="/properties" style={{ color: 'var(--primary-color)', textDecoration: 'none' }}> ← Back to Properties List </Link> </div>
    </div>
  );
}

export default PropertyDetails;