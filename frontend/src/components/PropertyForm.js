import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import apiClient from '../api/axiosConfig';
import './Form.css'; // Import shared form styles

function PropertyForm() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    address: '', city: '', state: '', postalCode: '', price: '',
    bedrooms: '', bathrooms: '', areaSqft: '', description: '',
    type: 'SALE',
  });
  const [selectedFiles, setSelectedFiles] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

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

    if (!formData.address || !formData.city || !formData.price || !formData.bedrooms || !formData.bathrooms) {
        setError('Please fill in Address, City, Price, Bedrooms, Bathrooms.'); setSubmitting(false); return;
    }

    // --- Step 1: Create Property with Text Data ---
    const propertyPayload = { ...formData }; // Copy text data
    try {
        const response = await apiClient.post('/properties', propertyPayload); // POST JSON data
        const savedProperty = response.data; // Should be the saved Property entity
        console.log("Property created:", savedProperty);

        // --- Step 2: If successful AND files exist, Upload Images ---
        if (savedProperty?.id && selectedFiles?.length > 0) { // Use optional chaining
            console.log(`Uploading ${selectedFiles.length} files for property ${savedProperty.id}...`);
            const imageFormData = new FormData(); // Create FormData for files
            for (let i = 0; i < selectedFiles.length; i++) {
                imageFormData.append('files', selectedFiles[i]); // Key MUST match @RequestParam("files")
            }
            try {
                 // Make separate POST request for images as multipart/form-data
                 await apiClient.post(`/properties/${savedProperty.id}/images`, imageFormData, {
                     headers: { 'Content-Type': 'multipart/form-data' },
                 });
                 console.log("Images uploaded successfully.");
                 setSuccess(`Property created and images uploaded! Redirecting...`);
                 setTimeout(() => navigate('/properties'), 2000); // Redirect after success

            } catch (uploadError) {
                 console.error("Image upload failed after property creation:", uploadError);
                 setError(`Property created (ID: ${savedProperty.id}), but image upload failed: ${uploadError.response?.data?.error || uploadError.message}. You can add images later by editing.`);
                 // Don't redirect if image upload fails, keep submitting=false
                 setSubmitting(false);
                 return;
            }
        } else {
             // Property created, no images to upload
             setSuccess(`Property created successfully! Redirecting...`);
             setTimeout(() => navigate('/properties'), 1500);
        }
        // Don't set submitting false here if redirecting

    } catch (err) { // Catch errors from the initial property creation POST
      console.error(`Failed to create property:`, err);
      const errMsg = err.response?.data?.error || err.message || `Failed to create property.`;
      setError(errMsg);
      setSubmitting(false); // Set submitting false on error
    }
  };


  return (
    <div className="form-container">
      <h2>Add New Property</h2>
      {error && <p className="form-message form-error">{error}</p>}
      {success && <p className="form-message form-success">{success}</p>}

      <form onSubmit={handleSubmit}>
         {/* Inputs using shared form styles */}
         <div className="form-input-group"><label htmlFor="address">Address:</label><input type="text" id="address" name="address" value={formData.address} onChange={handleInputChange} required /></div>
         <div className="form-row">
            <div className="form-input-group form-col-2"><label htmlFor="city">City:</label><input type="text" id="city" name="city" value={formData.city} onChange={handleInputChange} required /></div>
            <div className="form-input-group form-col-1"><label htmlFor="state">State:</label><input type="text" id="state" name="state" value={formData.state} onChange={handleInputChange} /></div>
            <div className="form-input-group form-col-1"><label htmlFor="postalCode">Postal Code:</label><input type="text" id="postalCode" name="postalCode" value={formData.postalCode} onChange={handleInputChange} /></div>
         </div>
         <div className="form-input-group"><label htmlFor="price">Price (₹):</label><input type="number" id="price" name="price" value={formData.price} onChange={handleInputChange} required step="0.01" min="0"/></div>
         <div className="form-row">
             <div className="form-input-group form-col"><label htmlFor="bedrooms">Bedrooms:</label><input type="number" id="bedrooms" name="bedrooms" value={formData.bedrooms} onChange={handleInputChange} required min="0"/></div>
             <div className="form-input-group form-col"><label htmlFor="bathrooms">Bathrooms:</label><input type="number" id="bathrooms" name="bathrooms" value={formData.bathrooms} onChange={handleInputChange} required min="0"/></div>
             <div className="form-input-group form-col"><label htmlFor="areaSqft">Area (sqft):</label><input type="number" id="areaSqft" name="areaSqft" value={formData.areaSqft} onChange={handleInputChange} step="0.01" min="0"/></div>
         </div>
         <div className="form-input-group"><label htmlFor="type">Property Type:</label><select id="type" name="type" value={formData.type} onChange={handleInputChange} required><option value="SALE">For Sale</option><option value="RENT">For Rent</option></select></div>
         {/* File Input */}
         <div className="form-input-group">
             <label htmlFor="propertyImages">Property Images:</label>
             <input type="file" id="propertyImages" name="files" multiple onChange={handleFileChange} accept="image/png, image/jpeg, image/gif" />
             <small className="text-muted">Select one or more images (JPG, PNG, GIF).</small>
             {selectedFiles && <p className="text-small">{selectedFiles.length} file(s) selected.</p>}
         </div>
         <div className="form-input-group"><label htmlFor="description">Description:</label><textarea id="description" name="description" value={formData.description} onChange={handleInputChange} rows="4"></textarea></div>

         <button type="submit" className="form-button" disabled={submitting}>
           {submitting ? 'Creating...' : 'Create Property'}
         </button>
      </form>
       <div style={{ marginTop: '20px', textAlign: 'center' }}>
            <Link to="/properties">Cancel</Link>
       </div>
    </div>
  );
}
export default PropertyForm;