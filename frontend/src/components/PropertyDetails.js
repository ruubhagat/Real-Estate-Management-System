import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import apiClient from '../api/axiosConfig';
import RequestBookingForm from './RequestBookingForm';
// import './PropertyDetails.css';

function PropertyDetails() {
  const [property, setProperty] = useState(null);
  // ... other state variables (loading, error, actionError, showBookingForm) ...
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [actionError, setActionError] = useState('');
  const { id } = useParams();
  const navigate = useNavigate();
  const [showBookingForm, setShowBookingForm] = useState(false);

  const currentUserId = localStorage.getItem('userId');
  const currentUserRole = localStorage.getItem('userRole');

  useEffect(() => {
    // ... fetchPropertyDetails logic ...
     const fetchPropertyDetails = async () => { /* ... */ };
     fetchPropertyDetails();
  }, [id]);

  const handleDelete = async () => { /* ... delete logic ... */ };

  // --- Styles ---
  const containerStyle = { /* ... */ };
  const mainImageStyle = { /* ... */ };
  const thumbnailContainerStyle = { /* ... */ };
  const thumbnailStyle = { /* ... */ };
  const detailsSectionStyle = { /* ... */ };
  const detailItemStyle = { /* ... */ };
  const detailLabelStyle = { /* ... */ };
  // ** NEW Amenities Style **
  const amenitiesListStyle = {
      listStyle: 'none',
      padding: 0,
      margin: 0,
      display: 'grid', // Use grid for layout
      gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', // Responsive columns
      gap: '8px 15px', // Row and column gap
      fontSize: '0.95rem'
  };
  const amenityItemStyle = {
      display: 'flex',
      alignItems: 'center',
      gap: '6px', // Space between icon and text
      color: 'var(--text-light)'
  };
  // Example Check Icon (replace with actual icon component/SVG if available)
  const checkIconStyle = { color: 'var(--success-text)', fontWeight: 'bold' };
  // ** End Amenities Style **
  const buttonContainerStyle = { /* ... */ };
  const buttonStyle = { /* ... */ };
  const editButtonStyle = { /* ... */ };
  const deleteButtonStyle = { /* ... */ };
  const bookButtonStyle = { /* ... */ };
  const errorBoxStyle = { /* ... */ };
  // --- End Styles ---


  if (loading) return <p className="content-wrapper">Loading property details...</p>;
  if (error) return <div className="content-wrapper">{/* ... error display ... */}</div>;
  if (!property) return <p className="content-wrapper">{/* ... not found display ... */}</p>;

  const ownerId = property.owner?.id;
  const isOwner = ownerId !== null && ownerId !== undefined && String(ownerId) === String(currentUserId);
  const isCustomer = currentUserRole === 'CUSTOMER';

  // --- Image URL Preparation ---
  const imageBaseUrl = "/uploads/";
  const placeholderUrl = 'https://via.placeholder.com/800x500/cccccc/969696?text=No+Image+Available';
  const imageUrlsArray = (property.imageUrls && typeof property.imageUrls === 'string')
    ? property.imageUrls.split(',').map(name => name.trim()).filter(name => name).map(name => `${imageBaseUrl}${name}`)
    : [];
  const mainImageUrl = imageUrlsArray.length > 0 ? imageUrlsArray[0] : placeholderUrl;
  const handleImageError = (e) => { /* ... */ };

  return (
    <div style={containerStyle} className="property-details-container">
      {actionError && <div style={errorBoxStyle}>Action Error: {actionError}</div>}

      {/* --- Main Image --- */}
      <img src={mainImageUrl} alt={`${property.address || 'Property'} main view`} style={mainImageStyle} onError={handleImageError}/>

      {/* --- Thumbnails --- */}
      {imageUrlsArray.length > 1 && ( <div style={thumbnailContainerStyle}> {/* ... */} </div> )}

      {/* --- Property Info Sections --- */}
      <h2>{property.address || 'Address Not Available'}</h2>
      <p style={detailItemStyle}> {/* ... city, state ... */} </p>
      <div style={detailsSectionStyle}> <h4>Key Details</h4> {/* ... price, type, beds etc ... */} </div>
      {property.description && ( <div style={detailsSectionStyle}> <h4>Description</h4> {/* ... */} </div> )}

      {/* --- Display Amenities --- */}
      {property.amenities && property.amenities.length > 0 && (
          <div style={detailsSectionStyle}>
              <h4>Amenities</h4>
              <ul style={amenitiesListStyle}>
                  {/* Sort amenities alphabetically for consistency */}
                  {[...property.amenities].sort().map(amenity => (
                      <li key={amenity} style={amenityItemStyle}>
                          {/* Replace check mark with a proper icon if you have an icon library */}
                          <span style={checkIconStyle}>✓</span>
                          {amenity}
                      </li>
                  ))}
              </ul>
          </div>
      )}
      {/* --- End Display Amenities --- */}

      {property.owner && ( <div style={detailsSectionStyle}> <h4>Listed By</h4> {/* ... */} </div> )}

      {/* --- Booking Section --- */}
      {isCustomer && !isOwner && (property.status === 'AVAILABLE' || property.status === 'PENDING') && ( <div style={{ marginTop: '20px', paddingTop: '15px', borderTop: '1px solid var(--border-color)' }}> {/* ... booking form ... */} </div> )}

      {/* --- Action Buttons (Owner Only) --- */}
      {isOwner && ( <div style={buttonContainerStyle}> {/* ... edit/delete buttons ... */} </div> )}

      {/* --- Back Link --- */}
      <div style={{ marginTop: '30px' }}> <Link to="/properties" style={{ color: 'var(--primary-color)', textDecoration: 'none' }}> ← Back to Properties List </Link> </div>
    </div>
  );
}

export default PropertyDetails;