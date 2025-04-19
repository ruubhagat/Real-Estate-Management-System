import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import apiClient from '../api/axiosConfig';
// In PropertyForm.js and EditPropertyForm.js
import { AVAILABLE_AMENITIES } from '../constants';
import './Form.css';

function EditPropertyForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    address: '', city: '', state: '', postalCode: '', price: '',
    bedrooms: '', bathrooms: '', areaSqft: '', description: '',
    type: 'SALE', status: 'AVAILABLE', imageUrls: '',
    amenities: new Set() // <-- Initialize amenities Set
  });
  const [selectedFiles, setSelectedFiles] = useState(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Fetch existing property data including amenities
  useEffect(() => {
    const fetchProperty = async () => {
      setLoading(true); setError('');
      try {
        const response = await apiClient.get(`/properties/${id}`);
        const data = response.data;
        setFormData({
            address: data.address || '', city: data.city || '', state: data.state || '',
            postalCode: data.postalCode || '', price: data.price !== null ? data.price : '',
            bedrooms: data.bedrooms !== null ? data.bedrooms : '', bathrooms: data.bathrooms !== null ? data.bathrooms : '',
            areaSqft: data.areaSqft !== null ? data.areaSqft : '', description: data.description || '',
            type: data.type || 'SALE', status: data.status || 'AVAILABLE',
            imageUrls: data.imageUrls || '',
            // --- Populate amenities from fetched data ---
            amenities: new Set(data.amenities || []) // Convert fetched array to Set
            // --- End amenities population ---
        });
      } catch (err) {
        console.error("Failed to fetch property for edit:", err);
        // ... (error handling) ...
         setError('Failed to load property data.');
      } finally { setLoading(false); }
    };
    if (id) fetchProperty(); else { setError("No property ID found."); setLoading(false); }
  }, [id]);

  const handleInputChange = (e) => { /* ... same as before ... */ setFormData(prev => ({...prev, [e.target.name]: e.target.value})); setError(''); setSuccess(''); };
  const handleFileChange = (e) => { /* ... same as before ... */ setSelectedFiles(e.target.files); setError(''); setSuccess(''); };

   // --- Handler for Amenity Checkboxes ---
   const handleAmenityChange = (e) => {
      const { value, checked } = e.target;
      setFormData(prev => {
          const newAmenities = new Set(prev.amenities);
          if (checked) { newAmenities.add(value); }
          else { newAmenities.delete(value); }
          return { ...prev, amenities: newAmenities };
      });
      setError(''); setSuccess('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault(); setError(''); setSuccess(''); setSubmitting(true);

    if (!formData.address || !formData.city || !formData.state /* ... etc ... */) {
        setError('Please ensure all required fields are filled.');
        setSubmitting(false); return;
    }

    // Prepare payload, converting amenities Set back to Array
    const propertyPayload = {
        ...formData,
        amenities: Array.from(formData.amenities) // Convert Set to Array
    };
    delete propertyPayload.imageUrls; // Don't send this string

    try {
        console.log(`Owner: Updating property ${id} data via owner endpoint:`, propertyPayload);
        // Use owner endpoint for update
        const response = await apiClient.put(`/owner/properties/${id}`, propertyPayload);
        const updatedProperty = response.data;
        console.log("Owner: Property updated:", updatedProperty);

        // Image Upload Logic (if new files selected)
        if (updatedProperty?.id && selectedFiles?.length > 0) {
            console.log(`Owner: Uploading ${selectedFiles.length} NEW files...`);
            const imageFormData = new FormData();
            for (let i = 0; i < selectedFiles.length; i++) { imageFormData.append('files', selectedFiles[i]); }
            try {
                 // Use owner endpoint for image upload
                 await apiClient.post(`/owner/properties/${updatedProperty.id}/images`, imageFormData, { headers: { 'Content-Type': 'multipart/form-data' } });
                 console.log("Owner: New images uploaded successfully.");
                 setSuccess(`Property updated and new images added! Redirecting...`);
            } catch (uploadError) {
                 console.error("Owner: New image upload failed:", uploadError);
                 // ... (error handling) ...
                  let uploadErrMsg = "Failed to upload new images."; /* ... */
                 setError(`Property updated, but adding new images failed: ${uploadErrMsg}.`);
                 setSubmitting(false); return;
            }
        } else {
             setSuccess(`Property updated successfully! Redirecting...`);
        }
        setTimeout(() => navigate(`/properties/${id}`), 1500); // Redirect on success

    } catch (err) { // Catch errors from property update PUT
      console.error(`Owner: Failed to update property:`, err);
      // ... (error handling, check for 403/404) ...
       let updateErrMsg = 'Failed to update property.'; /* ... */
      setError(updateErrMsg);
      setSubmitting(false);
    }
  };

   // --- Styles for Amenities and File List (can reuse from PropertyForm or define here) ---
   const amenitiesContainerStyle = { display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '10px', marginBottom: '20px', padding: '15px', border: '1px solid var(--border-color)', borderRadius: '4px' };
   const amenityLabelStyle = { display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.95rem', cursor: 'pointer' };
   const fileListStyle = { listStyle: 'decimal inside', margin: '10px 0 0 0', paddingLeft: '5px', fontSize: '0.9em', color: 'var(--text-light)' };
   // --- End Styles ---

  if (loading) return <p className="content-wrapper">Loading property data for editing...</p>;
  if (error && !formData.address) return <div className="content-wrapper form-message form-error">Error: {error} <Link to="/properties">Back to list</Link></div>;

  return (
    <div className="form-container">
      <h2>Edit Property (ID: {id})</h2>
      {error && <p className="form-message form-error">{error}</p>}
      {success && <p className="form-message form-success">{success}</p>}

      <form onSubmit={handleSubmit}>
         {/* ... Input fields for address, city, state, etc. ... */}
          <div className="form-input-group"><label htmlFor="address">Address:</label><input type="text" id="address" name="address" value={formData.address} onChange={handleInputChange} required /></div>
         <div className="form-row">
            <div className="form-input-group form-col-2"><label htmlFor="city">City:</label><input type="text" id="city" name="city" value={formData.city} onChange={handleInputChange} required /></div>
            <div className="form-input-group form-col-1"><label htmlFor="state">State:</label><input type="text" id="state" name="state" value={formData.state} onChange={handleInputChange} required /></div>
            <div className="form-input-group form-col-1"><label htmlFor="postalCode">Postal Code:</label><input type="text" id="postalCode" name="postalCode" value={formData.postalCode} onChange={handleInputChange} required /></div>
         </div>
         <div className="form-input-group"><label htmlFor="price">Price (₹):</label><input type="number" id="price" name="price" value={formData.price} onChange={handleInputChange} required step="0.01" min="0"/></div>
         <div className="form-row">
             <div className="form-input-group form-col"><label htmlFor="bedrooms">Bedrooms:</label><input type="number" id="bedrooms" name="bedrooms" value={formData.bedrooms} onChange={handleInputChange} required min="0"/></div>
             <div className="form-input-group form-col"><label htmlFor="bathrooms">Bathrooms:</label><input type="number" id="bathrooms" name="bathrooms" value={formData.bathrooms} onChange={handleInputChange} required min="0"/></div>
             <div className="form-input-group form-col"><label htmlFor="areaSqft">Area (sqft):</label><input type="number" id="areaSqft" name="areaSqft" value={formData.areaSqft} onChange={handleInputChange} step="0.01" min="0"/></div>
         </div>
         <div className="form-input-group"><label htmlFor="type">Property Type:</label><select id="type" name="type" value={formData.type} onChange={handleInputChange} required><option value="SALE">For Sale</option><option value="RENT">For Rent</option></select></div>
         <div className="form-input-group"><label htmlFor="status">Status:</label><select id="status" name="status" value={formData.status} onChange={handleInputChange} required><option value="AVAILABLE">Available</option><option value="PENDING">Pending</option><option value="SOLD">Sold</option><option value="RENTED">Rented</option><option value="UNAVAILABLE">Unavailable</option></select></div>

         {/* --- Amenities Section --- */}
         <div className="form-input-group">
            <label>Amenities:</label>
            <div style={amenitiesContainerStyle}>
                {AVAILABLE_AMENITIES.map(amenity => (
                    <label key={amenity} style={amenityLabelStyle}>
                        <input
                            type="checkbox"
                            value={amenity}
                            checked={formData.amenities.has(amenity)} // Reflect current state
                            onChange={handleAmenityChange}
                        />
                        {amenity}
                    </label>
                ))}
            </div>
         </div>
         {/* --- End Amenities Section --- */}

         <div className="form-input-group"><label htmlFor="description">Description:</label><textarea id="description" name="description" value={formData.description} onChange={handleInputChange} rows="4"></textarea></div>

        {/* Display Existing Image URLs */}
        {formData.imageUrls && ( <div className="form-input-group"> <label>Current Image Filenames:</label> <ul style={{fontSize: '0.8em', paddingLeft: '20px', margin: '5px 0', listStyle: 'disc'}}> {formData.imageUrls.split(',').map(name => name.trim()).filter(name => name).map((name, index) => <li key={index}>{name}</li>)} </ul> </div> )}

        {/* File Input for ADDING NEW Images & Preview */}
        <div className="form-input-group">
            <label htmlFor="propertyImages">Add New Images (Optional):</label>
            <input type="file" id="propertyImages" name="files" multiple onChange={handleFileChange} accept="image/png, image/jpeg, image/gif" style={{ display: 'block', width: '100%', padding: '10px', boxSizing: 'border-box'}}/>
            <small style={{display: 'block', marginTop: '5px', color: 'var(--text-muted)'}}>Select new images to append. Existing images remain unless removed.</small>
             {/* Display Selected File Names */}
             {selectedFiles && selectedFiles.length > 0 && (
                 <div style={{marginTop: '10px'}}>
                    <strong>Selected Files for Upload:</strong>
                    <ul style={fileListStyle}>
                        {Array.from(selectedFiles).map((file, index) => (
                            <li key={index}>{file.name} ({(file.size / 1024).toFixed(1)} KB)</li>
                        ))}
                    </ul>
                 </div>
             )}
        </div>

        {/* Action Buttons */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '20px' }}>
            <button type="submit" className="form-button" style={{ width: 'auto' }} disabled={submitting || loading}>
                {submitting ? 'Saving...' : 'Save Changes'}
            </button>
            <button type="button" onClick={() => navigate(`/properties/${id}`)} className="form-button form-button-cancel" style={{ width: 'auto' }} disabled={submitting}>
                Cancel
            </button>
        </div>
      </form>
    </div>
  );
}
export default EditPropertyForm;