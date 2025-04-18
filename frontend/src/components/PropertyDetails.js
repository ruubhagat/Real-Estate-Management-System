import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import apiClient from '../api/axiosConfig';
import RequestBookingForm from './RequestBookingForm';
// import './PropertyDetails.css'; // Optional specific styles

function PropertyDetails() {
  const [property, setProperty] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const { id } = useParams();
  const navigate = useNavigate();
  const [showBookingForm, setShowBookingForm] = useState(false);

  // --- Get current user info ---
  // Ensure these are retrieved correctly after login
  const currentUserId = localStorage.getItem('userId');
  const currentUserRole = localStorage.getItem('userRole');


  useEffect(() => {
    const fetchPropertyDetails = async () => {
       setLoading(true); setError('');
       if (!id) { setError("No property ID provided."); setLoading(false); return; }
       try {
         // API call uses apiClient, should include token
         const response = await apiClient.get(`/properties/${id}`);
         setProperty(response.data); // Assuming backend returns the Property entity
       } catch (err) {
         console.error("Failed to fetch property details:", err);
         const errMsg = err.response?.status === 404
                         ? "Property not found."
                         : (err.response?.data?.error || err.message || 'Failed to load property details.');
         setError(errMsg);
       } finally { setLoading(false); }
    };
    fetchPropertyDetails();
  }, [id]);

  // Delete handler
  const handleDelete = async () => {
       if (!window.confirm(`Are you sure you want to delete property "${property?.address}"? This cannot be undone.`)) { // Added optional chaining
          return;
       }
       setError('');
       try {
           await apiClient.delete(`/properties/${id}`); // API call uses apiClient
           alert('Property deleted successfully!');
           navigate('/properties');
       } catch (err) {
            console.error("Failed to delete property:", err);
            const errMsg = err.response?.data?.error || err.message || 'Failed to delete property.';
            setError(`Delete failed: ${errMsg}`);
            alert(`Delete failed: ${errMsg}`);
       }
  };

  // Styles
  const containerStyle = { maxWidth: '900px', margin: '2rem auto', padding: '30px', backgroundColor: '#fff', borderRadius: '8px', boxShadow: 'var(--shadow-color)' };
  const mainImageStyle = { width: '100%', maxHeight: '500px', objectFit: 'cover', marginBottom: '20px', borderRadius: '5px', border: '1px solid var(--border-color)' };
  const thumbnailContainerStyle = { display: 'flex', gap: '10px', overflowX: 'auto', paddingBottom: '10px', marginBottom: '20px' };
  const thumbnailStyle = { height: '90px', width: 'auto', borderRadius: '4px', cursor: 'pointer', border: '1px solid var(--border-color)' };
  const detailSectionStyle = { marginBottom: '20px', paddingBottom: '15px', borderBottom: '1px solid var(--border-color)' };
  const detailItemStyle = { marginBottom: '8px', fontSize: '1.05rem' };
  const labelStyle = { fontWeight: '600', color: 'var(--text-dark)', minWidth: '120px', display: 'inline-block' };
  const descriptionStyle = { whiteSpace: 'pre-wrap', lineHeight: '1.7', color: 'var(--text-light)' };
  const buttonContainerStyle = { marginTop: '25px', paddingTop: '20px', borderTop: '1px solid var(--border-color)', display: 'flex', gap: '10px', flexWrap: 'wrap' }; // Added flexWrap
  const buttonStyle = { padding: '10px 18px', cursor: 'pointer', border: 'none', borderRadius: '4px', fontWeight: '500', transition: 'background-color 0.2s ease' };
  const editButtonStyle = { ...buttonStyle, backgroundColor: 'var(--accent-color, #ffc107)', color: '#fff'}; // Default yellow accent
  const deleteButtonStyle = { ...buttonStyle, backgroundColor: 'var(--error-text, #dc3545)', color: '#fff'};
  const bookButtonStyle = { ...buttonStyle, backgroundColor: 'var(--primary-color, #007bff)', color: '#fff'};
  const errorStyle = { color: 'var(--error-text)', border: '1px solid var(--error-border)', padding: '15px', borderRadius: '5px', backgroundColor: 'var(--error-bg)'};


  if (loading) return <p className="content-wrapper" style={{textAlign: 'center'}}>Loading property details...</p>;
  // Display error prominently if loading failed
  if (error && !property) return <div className="content-wrapper" style={errorStyle}>Error: {error} <Link to="/properties">Back to list</Link></div>;
  // Handle case where property is null after loading (e.g., 404)
  if (!property) return <p className="content-wrapper" style={{textAlign: 'center'}}>Property not found. <Link to="/properties">Back to list</Link></p>;

  // --- Calculate permissions ---
  // Ensure property and property.owner exist before accessing nested properties
  // Convert currentUserId from localStorage (string) to number for comparison if property.owner.id is number
  const ownerId = property.owner ? property.owner.id : null;
  const isOwner = ownerId !== null && String(ownerId) === currentUserId; // Compare as strings for safety
  const isAdmin = currentUserRole === 'ADMIN';
  const isCustomer = currentUserRole === 'CUSTOMER';

  // --- Prepare Image URLs ---
  const imageBaseUrl = "/uploads/"; // Path set in MvcConfig
  const imageUrlsArray = (property.imageUrls && typeof property.imageUrls === 'string')
                         ? property.imageUrls.split(',').map(name => `${imageBaseUrl}${name.trim()}`).filter(url => url !== imageBaseUrl) // Filter out empty results
                         : [];
  const mainImageUrl = imageUrlsArray.length > 0 ? imageUrlsArray[0] : 'https://via.placeholder.com/800x500/cccccc/969696?text=No+Image+Available';

  const handleImageError = (e) => { /* ... as before ... */
       if (e.target.src !== 'https://via.placeholder.com/800x500/cccccc/969696?text=No+Image+Available') {
           e.target.onerror = null;
           e.target.src = 'https://via.placeholder.com/800x500/cccccc/969696?text=Image+Load+Error';
       } else { e.target.style.display = 'none'; }
  };

  return (
    <div style={containerStyle}>
      {/* Error display for actions on this page */}
      {error && <div style={{...errorStyle, marginBottom: '20px'}}>Error: {error}</div>}

      {/* Main Image */}
       <img src={mainImageUrl} alt={`${property.address || 'Property'} main view`} style={mainImageStyle} onError={handleImageError}/>

      {/* Thumbnail Gallery */}
      {imageUrlsArray.length > 1 && (
        <div style={thumbnailContainerStyle}>
            {imageUrlsArray.map((url, index) => (
                 <img key={index} src={url} alt={`Property view ${index + 1}`} style={thumbnailStyle} onError={(e) => { e.target.style.display='none'; }} />
            ))}
        </div>
      )}

      {/* Property Info */}
      <div style={detailSectionStyle}>
          <h2 style={{ marginBottom: '15px' }}>{property.address}</h2>
          <div style={detailItemStyle}><span style={labelStyle}>Location:</span> {property.city}, {property.state} {property.postalCode}</div>
          <div style={detailItemStyle}>
              <span style={labelStyle}>Price:</span>
              <span style={{ fontSize: '1.4em', fontWeight: 'bold', color: 'var(--primary-hover-color)' }}>
                 ₹{property.price ? Number(property.price).toLocaleString('en-IN') : 'N/A'}
                 {property.type === 'RENT' ? ' / month' : ''}
              </span>
          </div>
          <div style={detailItemStyle}><span style={labelStyle}>Type:</span> {property.type}</div>
          <div style={detailItemStyle}><span style={labelStyle}>Status:</span> {property.status}</div>
      </div>
      {/* ... other detail sections ... */}
       <div style={detailSectionStyle}>
           <h3 style={{ marginBottom: '15px' }}>Key Features</h3>
           <div style={detailItemStyle}><span style={labelStyle}>Bedrooms:</span> {property.bedrooms}</div>
           <div style={detailItemStyle}><span style={labelStyle}>Bathrooms:</span> {property.bathrooms}</div>
           <div style={detailItemStyle}><span style={labelStyle}>Area:</span> {property.areaSqft ? `${property.areaSqft} sqft` : 'N/A'}</div>
      </div>
       <div style={detailSectionStyle}>
          <h3 style={{ marginBottom: '15px' }}>Description</h3>
          <p style={descriptionStyle}>{property.description || 'No description available.'}</p>
      </div>
       <div style={{ fontSize: '0.9em', color: 'var(--text-muted)'}}>
          <p>Listed By: {property.owner?.name || 'N/A'} ({property.owner?.email || 'N/A'})</p> {/* Use optional chaining */}
          <p>Listed On: {property.createdAt ? new Date(property.createdAt).toLocaleDateString() : 'N/A'}</p>
          {property.updatedAt && <p>Last Updated: {new Date(property.updatedAt).toLocaleDateString()}</p>}
       </div>


      {/* Booking Section */}
      {isCustomer && property.status === 'AVAILABLE' && (
        <div style={{ marginTop: '20px', paddingTop: '15px', borderTop: '1px solid var(--border-color)' }}>
          <h4>Request a Visit</h4>
          {!showBookingForm ? (
            <button onClick={() => setShowBookingForm(true)} style={bookButtonStyle}>Request Visit</button>
          ) : (
            <RequestBookingForm
                propertyId={property.id}
                onBookingSuccess={() => { alert('Booking request submitted!'); setShowBookingForm(false); }}
                onCancel={() => setShowBookingForm(false)}
             />
          )}
        </div>
      )}

      {/* Action Buttons - Check logic carefully */}
      {(isOwner || isAdmin) && ( // Show buttons ONLY if owner or admin
            <div style={buttonContainerStyle}>
                 <Link to={`/properties/${id}/edit`} style={{ textDecoration: 'none' }}>
                     <button style={editButtonStyle}>Edit Property</button>
                 </Link>
                 <button onClick={handleDelete} style={deleteButtonStyle}>Delete Property</button>
            </div>
      )}

      {/* Back Link */}
      <div style={{ marginTop: '30px' }}>
        <Link to="/properties">← Back to Properties List</Link>
      </div>
    </div>
  );
}

export default PropertyDetails;