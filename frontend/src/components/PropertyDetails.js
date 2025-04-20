import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import apiClient from '../api/axiosConfig';
import RequestBookingForm from './RequestBookingForm';
// Import icons
import {
    FaParking, FaSwimmingPool, FaShieldAlt, FaDumbbell, FaPlug, FaWind,
    FaCouch, FaTree, FaPaw, FaPhoneVolume, FaWater, FaCheckCircle,
    FaSubway, FaBuilding, FaChild, FaCar
} from 'react-icons/fa';
import { MdElevator, MdOutlineBalcony } from "react-icons/md";
import { FaKitchenSet } from "react-icons/fa6";
// import './PropertyDetails.css';

// Icon Mapping
const AMENITY_ICONS = {
    'Parking': FaParking, 'Swimming Pool': FaSwimmingPool, 'Gymnasium': FaDumbbell,
    'Clubhouse': FaBuilding, 'Power Backup': FaPlug, 'Security': FaShieldAlt,
    'Elevator': MdElevator, 'Air Conditioning': FaWind, 'Furnished': FaCouch,
    'Semi-Furnished': FaCouch, 'Modular Kitchen': FaKitchenSet,
    'Balcony': MdOutlineBalcony, 'Garden': FaTree, 'Pet Friendly': FaPaw,
    'Intercom': FaPhoneVolume, 'Rainwater Harvesting': FaWater, 'Vaastu Compliant': FaCheckCircle,
    'Near Metro': FaSubway, 'Gated Community': FaShieldAlt, 'Children Play Area': FaChild,
    'Visitor Parking': FaCar, 'Default': FaCheckCircle
};

function PropertyDetails() {
  const [property, setProperty] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [actionError, setActionError] = useState('');
  const [showBookingForm, setShowBookingForm] = useState(false);
  const { id } = useParams();
  const navigate = useNavigate();

  const currentUserId = localStorage.getItem('userId');
  const currentUserRole = localStorage.getItem('userRole');

  useEffect(() => {
    const fetchPropertyDetails = async () => {
      setLoading(true); setError(''); setActionError('');
      if (!id) { setError("No property ID provided."); setLoading(false); return; }
      try {
        const response = await apiClient.get(`/properties/${id}`);
        setProperty(response.data);
      } catch (err) {
        let errMsg = 'Failed to load property details.';
         if (err.response?.status === 404) { errMsg = "Property not found."; }
         else if (err.response?.status === 403) { errMsg = "Permission denied."; }
         else if (err.response?.status === 401) { errMsg = "Authentication error."; }
         else if (err.response?.data?.error) { errMsg = err.response.data.error; }
         else { errMsg = err.message; }
        setError(errMsg);
      } finally { setLoading(false); }
    };
    fetchPropertyDetails();
  }, [id]);

  const handleDelete = async () => {
      if (!window.confirm(`Are you sure you want to delete property "${property?.address || 'N/A'}"?`)) return;
      setActionError('');
       try {
           await apiClient.delete(`/owner/properties/${id}`);
           alert('Property deleted successfully!');
           navigate('/properties');
       } catch (err) {
            let deleteErrMsg = 'Failed to delete property.';
             if (err.response?.status === 403) { deleteErrMsg = "Permission Denied: Not owner or session expired.";}
             else if (err.response?.status === 404) { deleteErrMsg = "Property not found.";}
             else if (err.response?.data?.error) { deleteErrMsg = err.response.data.error; }
             else { deleteErrMsg = err.message; }
            setActionError(`Delete failed: ${deleteErrMsg}`);
            alert(`Delete failed: ${deleteErrMsg}`);
       }
  };
  const placeholderUrl = 'https://via.placeholder.com/800x500/cccccc/969696?text=No+Image+Available';
  const handleImageError = (e) => { if (e.target.src !== placeholderUrl) { e.target.onerror = null; e.target.src = placeholderUrl; } };

  // --- Styles ---
  const containerStyle = { padding: '20px', maxWidth: '1000px', margin: '20px auto', backgroundColor: 'var(--bg-content)', borderRadius: '6px', boxShadow: 'var(--shadow-color)', border: '1px solid var(--border-color)'};
  const mainImageStyle = { display: 'block', width: '100%', maxWidth: '900px', aspectRatio: '16 / 9', objectFit: 'cover', marginBottom: '25px', borderRadius: '4px', backgroundColor: '#f0f0f0', border: '1px solid var(--border-color)', margin: '0 auto 25px auto'};
  const thumbnailContainerStyle = { display: 'flex', gap: '10px', flexWrap: 'wrap', marginBottom: '20px', paddingTop: '10px', borderTop: '1px solid var(--border-extra-light-color)'};
  const thumbnailStyle = { height: '80px', width: '120px', objectFit: 'cover', borderRadius: '3px', border: '1px solid var(--border-color)', cursor: 'pointer', backgroundColor: '#f0f0f0'};
  const detailsSectionStyle = { marginBottom: '20px', paddingTop: '15px', borderTop: '1px solid var(--border-extra-light-color)'};
  const detailItemStyle = { marginBottom: '8px', fontSize: '1rem', color: 'var(--text-light)'};
  const detailLabelStyle = { fontWeight: '600', color: 'var(--text-dark)', marginRight: '8px'};
  const amenitiesListStyle = { listStyle: 'none', padding: 0, margin: '10px 0 0 0', display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '10px 15px', fontSize: '0.95rem'};
  const amenityItemStyle = { display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--text-light)'};
  const amenityIconStyle = { color: 'var(--primary-color)', fontSize: '1.1em', flexShrink: 0 };
  const buttonContainerStyle = { marginTop: '25px', paddingTop: '20px', borderTop: '1px solid var(--border-color)', display: 'flex', gap: '10px', flexWrap: 'wrap'};
  const buttonStyle = { padding: '10px 18px', fontSize: '0.95rem', borderRadius: '4px', border: 'none', cursor: 'pointer', fontWeight: '500', transition: 'background-color 0.2s ease, transform 0.1s ease', textDecoration: 'none', display: 'inline-block', textAlign: 'center'};
  const editButtonStyle = { ...buttonStyle, backgroundColor: 'var(--accent-color)', color: '#fff'};
  const deleteButtonStyle = { ...buttonStyle, backgroundColor: 'var(--error-bg)', color: 'var(--error-text)', border: '1px solid var(--error-border)'};
  const bookButtonStyle = { ...buttonStyle, backgroundColor: 'var(--primary-color)', color: '#fff'};
  const errorBoxStyle = { color: 'var(--error-text)', backgroundColor: 'var(--error-bg)', border: '1px solid var(--error-border)', padding: '15px', marginBottom: '15px', borderRadius: '4px'};
  // --- End Styles ---

  // --- Render Logic Guard Clauses ---
  if (loading) return <p className="content-wrapper" style={{ textAlign: 'center' }}>Loading property details...</p>;
  if (error) return <div className="content-wrapper" style={errorBoxStyle}>Error fetching details: {error} <Link to="/properties">Back to list</Link></div>;
  if (!property) return <p className="content-wrapper" style={{ textAlign: 'center' }}>Property not found. <Link to="/properties">Back to list</Link></p>;
  // --- End Guard Clauses ---

  // --- Calculations ---
  const ownerId = property.ownerId; // Use ownerId from DTO
  const isOwner = ownerId !== null && ownerId !== undefined && String(ownerId) === String(currentUserId);
  const isCustomer = currentUserRole === 'CUSTOMER';
  const imageBaseUrl = "/uploads/";
  const imageUrlsArray = (property.imageUrls && typeof property.imageUrls === 'string') ? property.imageUrls.split(',').map(n=>n.trim()).filter(n=>n).map(n=>`${imageBaseUrl}${n}`) : [];
  const mainImageUrl = imageUrlsArray.length > 0 ? imageUrlsArray[0] : placeholderUrl;

  // --- FINAL RENDER ---
  return (
    <div style={containerStyle} className="property-details-container">
         {actionError && <div style={errorBoxStyle}>Action Error: {actionError}</div>}

         <img src={mainImageUrl} alt={`${property.address || 'Property'} view`} style={mainImageStyle} onError={handleImageError}/>
         {imageUrlsArray.length > 1 && ( <div style={thumbnailContainerStyle}> {/* ... thumbnails ... */} </div> )}

         <h2>{property.address ?? 'Address Not Available'}</h2>
         <p style={detailItemStyle}> {property.city ?? 'N/A'}, {property.state ?? 'N/A'} {property.postalCode ?? ''} </p>

         {/* Key Details */}
         <div style={detailsSectionStyle}>
            <h4>Key Details</h4>
            <p style={detailItemStyle}><span style={detailLabelStyle}>Price:</span> ₹{property.price ? Number(property.price).toLocaleString('en-IN') : 'N/A'} {property.type === 'RENT' && <span> / month</span>}</p>
            <p style={detailItemStyle}><span style={detailLabelStyle}>Type:</span> {property.type ?? 'N/A'}</p>
            <p style={detailItemStyle}><span style={detailLabelStyle}>Status:</span> {property.status ?? 'N/A'}</p>
            <p style={detailItemStyle}><span style={detailLabelStyle}>Beds:</span> {property.bedrooms ?? 'N/A'} | <span style={detailLabelStyle}> Baths:</span> {property.bathrooms ?? 'N/A'} | <span style={detailLabelStyle}> Area:</span> {property.areaSqft ? `${property.areaSqft} sqft` : 'N/A'} </p>
         </div>

         {/* Description */}
         {property.description && (
            <div style={detailsSectionStyle}>
                <h4>Description</h4>
                <p style={{...detailItemStyle, whiteSpace: 'pre-wrap', lineHeight: '1.6'}}>
                    {property.description}
                </p>
            </div>
         )}

         {/* Amenities */}
         <div style={detailsSectionStyle}>
              <h4>Amenities</h4>
              {Array.isArray(property.amenities) && property.amenities.length > 0 ? (
                  <ul style={amenitiesListStyle}>
                      {[...property.amenities].sort().map(amenity => {
                          const IconComponent = AMENITY_ICONS[amenity] || AMENITY_ICONS['Default'];
                          return IconComponent ? ( <li key={amenity} style={amenityItemStyle}><span style={amenityIconStyle}><IconComponent /></span> {amenity}</li> ) : null;
                      })}
                  </ul>
              ) : (
                  <p style={detailItemStyle}>No specific amenities listed for this property.</p>
              )}
          </div>

         {/* --- VVV CORRECTED Listed By Section (Single Instance) VVV --- */}
         {/* Render section only if some owner info exists */}
         {(property.ownerId || property.ownerName || property.ownerEmail) && (
            <div style={detailsSectionStyle}>
                 <h4>Listed By</h4>
                 {isOwner
                   ? (<p style={detailItemStyle}>This is your property listing.</p>)
                   // If not the owner, display contact info (prioritizing email)
                   : (
                       <p style={detailItemStyle}>
                           <span style={detailLabelStyle}>Contact:</span>
                           {property.ownerEmail || property.ownerName || 'Owner/Agent'}
                           {property.ownerEmail && (
                              <> (<a href={`mailto:${property.ownerEmail}`} style={{color: 'var(--primary-color)'}}>Send Email</a>)</>
                           )}
                       </p>
                     )
                 }
            </div>
         )}
         {/* --- ^^^ END CORRECTED Listed By Section ^^^ --- */}

         {/* Booking Section */}
         {isCustomer && !isOwner && (property.status === 'AVAILABLE' || property.status === 'PENDING') && ( <div style={{ marginTop: '20px', paddingTop: '15px', borderTop: '1px solid var(--border-color)' }}> <h4>Request a Visit</h4> {!showBookingForm ? ( <button onClick={() => setShowBookingForm(true)} style={bookButtonStyle}>Request Visit</button> ) : ( <RequestBookingForm propertyId={property.id} onBookingSuccess={() => { alert('Booking request submitted!'); setShowBookingForm(false); }} onCancel={() => setShowBookingForm(false)}/> )} </div> )}

         {/* Action Buttons (Owner Only) */}
         {isOwner && (
             <div style={buttonContainerStyle}>
                <Link to={`/properties/${id}/edit`} style={{ textDecoration: 'none' }}>
                    <button style={editButtonStyle}>Edit Property</button>
                </Link>
                <button onClick={handleDelete} style={deleteButtonStyle}>Delete Property</button>
             </div>
         )}

         {/* Back Link */}
         <div style={{ marginTop: '30px' }}> <Link to="/properties" style={{ color: 'var(--primary-color)', textDecoration: 'none' }}> ← Back to Properties List </Link> </div>
    </div>
  );
}

export default PropertyDetails;