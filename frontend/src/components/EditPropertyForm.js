import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import apiClient from '../api/axiosConfig';
import './Form.css'; // Import shared form styles

function EditPropertyForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    address: '', city: '', state: '', postalCode: '', price: '',
    bedrooms: '', bathrooms: '', areaSqft: '', description: '',
    type: 'SALE', status: 'AVAILABLE', imageUrls: ''
  });
  const [selectedFiles, setSelectedFiles] = useState(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    const fetchProperty = async () => {
      setLoading(true); setError('');
      try {
        // Fetch using the general GET endpoint
        const response = await apiClient.get(`/properties/${id}`);
        const data = response.data;
        setFormData({
            address: data.address || '', city: data.city || '', state: data.state || '',
            postalCode: data.postalCode || '', price: data.price !== null ? data.price : '',
            bedrooms: data.bedrooms !== null ? data.bedrooms : '', bathrooms: data.bathrooms !== null ? data.bathrooms : '',
            areaSqft: data.areaSqft !== null ? data.areaSqft : '', description: data.description || '',
            type: data.type || 'SALE', status: data.status || 'AVAILABLE',
            imageUrls: data.imageUrls || ''
        });
      } catch (err) {
        console.error("Failed to fetch property for edit:", err);
        if (err.response?.status === 404) {
             setError('Property not found.');
        } else if (err.response?.status === 403) {
             setError('Permission denied to view this property for editing.'); // Should not happen if owner link works
        } else {
             setError('Failed to load property data.');
        }
      } finally { setLoading(false); }
    };
    if (id) fetchProperty(); else { setError("No property ID found."); setLoading(false); }
  }, [id]);

  const handleInputChange = (e) => {
     const { name, value } = e.target;
     setFormData(prev => ({ ...prev, [name]: value }));
     setError(''); setSuccess('');
  };
  const handleFileChange = (e) => {
     setSelectedFiles(e.target.files);
     setError(''); setSuccess('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault(); setError(''); setSuccess(''); setSubmitting(true);

    // Frontend validation (keep or enhance)
    if (!formData.address || !formData.city || !formData.state || !formData.postalCode || !formData.price || !formData.bedrooms || !formData.bathrooms) {
        setError('Please ensure all required fields are filled (Address, City, State, Postal Code, Price, Bedrooms, Bathrooms).');
        setSubmitting(false); return;
    }

    // 1. Prepare Property Data for PUT (without image file info or old URL string)
    const propertyPayload = { ...formData };
    delete propertyPayload.imageUrls; // Don't send the old URL string back

    try {
        console.log(`Owner: Updating property ${id} data via owner endpoint:`, propertyPayload);
        // --- MODIFIED URL for PUT ---
        const response = await apiClient.put(`/api/owner/properties/${id}`, propertyPayload);
        // --- END MODIFICATION ---
        const updatedProperty = response.data;
        console.log("Owner: Property updated:", updatedProperty);

        // 2. If update successful AND NEW files were selected, Upload ONLY NEW images
        if (updatedProperty?.id && selectedFiles?.length > 0) {
            console.log(`Owner: Uploading ${selectedFiles.length} NEW files for property ${updatedProperty.id} via owner endpoint...`);
            const imageFormData = new FormData();
            for (let i = 0; i < selectedFiles.length; i++) { imageFormData.append('files', selectedFiles[i]); }
            try {
                 // --- MODIFIED URL for Image POST ---
                 await apiClient.post(`/api/owner/properties/${updatedProperty.id}/images`, imageFormData, { headers: { 'Content-Type': 'multipart/form-data' } });
                 // --- END MODIFICATION ---
                 console.log("Owner: New images uploaded successfully.");
                 setSuccess(`Property updated and new images added! Redirecting...`);
            } catch (uploadError) {
                 console.error("Owner: New image upload failed after property update:", uploadError);
                 let uploadErrMsg = "Failed to upload new images.";
                 if (uploadError.response?.status === 403) {
                    uploadErrMsg = "Permission denied to upload images for this property.";
                 } else if (uploadError.response?.data?.error) {
                    uploadErrMsg = uploadError.response.data.error;
                 } else {
                    uploadErrMsg = uploadError.message;
                 }
                 setError(`Property updated, but adding new images failed: ${uploadErrMsg}.`);
                 setSubmitting(false); return; // Stop
            }
        } else {
             setSuccess(`Property updated successfully! Redirecting...`);
        }
        // Redirect on success
        setTimeout(() => navigate(`/properties/${id}`), 1500);

    } catch (err) { // Catch errors from the property data PUT request
      console.error(`Owner: Failed to update property:`, err);
      // Check for 403 Forbidden specifically
      if (err.response?.status === 403) {
         setError("Permission Denied: You might not be the owner of this property or your session expired.");
      } else if (err.response?.status === 404) {
         setError("Property not found. It might have been deleted.");
      } else {
         const errMsg = err.response?.data?.error || err.message || `Failed to update property.`; setError(errMsg);
      }
      setSubmitting(false); // Ensure state is reset on error
    }
    // Removed finally block to avoid resetting submitting state before redirect
  };

  if (loading) return <p className="content-wrapper">Loading property data for editing...</p>;
  if (error && !formData.address) return <div className="content-wrapper form-message form-error">Error: {error} <Link to="/properties">Back to list</Link></div>;

  return (
    <div className="form-container">
      <h2>Edit Property (ID: {id})</h2>
      {/* Display error/success messages */}
      {error && <p className="form-message form-error">{error}</p>}
      {success && <p className="form-message form-success">{success}</p>}

      <form onSubmit={handleSubmit}>
         {/* Form Inputs using classes */}
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
         <div className="form-input-group"><label htmlFor="description">Description:</label><textarea id="description" name="description" value={formData.description} onChange={handleInputChange} rows="4"></textarea></div>

        {/* Display Existing Image URLs (Read-only) */}
        {formData.imageUrls && (
             <div className="form-input-group">
                <label>Current Image Filenames:</label>
                 <ul style={{fontSize: '0.8em', paddingLeft: '20px', margin: '5px 0', listStyle: 'disc'}}>
                    {formData.imageUrls.split(',')
                      .map(name => name.trim())
                      .filter(name => name)
                      .map((name, index) => <li key={index}>{name}</li>)}
                 </ul>
                 {/* TODO: Add remove image functionality (would require another backend endpoint) */}
             </div>
         )}

        {/* File Input for ADDING NEW Images */}
        <div className="form-input-group">
            <label htmlFor="propertyImages">Add New Images (Optional):</label>
            <input type="file" id="propertyImages" name="files" multiple onChange={handleFileChange} accept="image/png, image/jpeg, image/gif" />
            <small style={{display: 'block', marginTop: '5px', color: 'var(--text-muted)'}}>Select new images to append to this property. Existing images will remain unless removed.</small>
            {selectedFiles && <p style={{fontSize: '0.9em', marginTop: '5px'}}>{selectedFiles.length} new file(s) selected.</p>}
        </div>

        {/* Action Buttons */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '20px' }}>
            <button type="submit" className="form-button" style={{ width: 'auto' }} disabled={submitting || loading}>
                {submitting ? 'Saving...' : 'Save Changes'}
            </button>
            {/* Ensure cancel navigates correctly */}
            <button type="button" onClick={() => navigate(`/properties/${id}`)} className="form-button form-button-cancel" style={{ width: 'auto' }} disabled={submitting}>
                Cancel
            </button>
        </div>
      </form>
    </div>
  );
}
export default EditPropertyForm;