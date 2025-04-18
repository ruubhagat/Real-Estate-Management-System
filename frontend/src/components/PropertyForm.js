import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import apiClient from '../api/axiosConfig';
import './Form.css'; // Import shared form styles

function PropertyForm() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    address: '', city: '', state: '', postalCode: '', price: '',
    bedrooms: '', bathrooms: '', areaSqft: '', description: '',
    type: 'SALE', // Default type
    // Status is set by backend now
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

    // Frontend validation (ensure it matches backend required fields)
    if (!formData.address || !formData.city || !formData.state || !formData.postalCode || !formData.price || !formData.bedrooms || !formData.bathrooms || !formData.type) {
        setError('Please fill in all required fields (Address, City, State, Postal Code, Price, Type, Bedrooms, Bathrooms).');
        setSubmitting(false); return;
    }

    // --- Step 1: Create Property with Text Data ---
    const propertyPayload = { ...formData }; // Copy text data
    try {
        console.log("Creating property payload:", propertyPayload); // Log before call

        // --- CORRECTED API CALL PATH ---
        // Use relative path '/properties' - baseURL 'http://localhost:8081/api' will be prepended by Axios
        console.log("Sending POST request to: /properties"); // Log the relative path
        const response = await apiClient.post('/properties', propertyPayload);
        // --- END CORRECTION ---

        const savedProperty = response.data;
        console.log("Property created successfully:", savedProperty);

        // --- Step 2: If successful AND files exist, Upload Images via Owner Endpoint ---
        if (savedProperty?.id && selectedFiles?.length > 0) {
            console.log(`Uploading ${selectedFiles.length} files for property ${savedProperty.id} via owner endpoint...`);
            const imageFormData = new FormData();
            for (let i = 0; i < selectedFiles.length; i++) {
                imageFormData.append('files', selectedFiles[i]);
            }
            try {
                 // Use the owner endpoint (relative path)
                 console.log(`Sending POST request for images to: /owner/properties/${savedProperty.id}/images`);
                 await apiClient.post(`/owner/properties/${savedProperty.id}/images`, imageFormData, {
                     headers: { 'Content-Type': 'multipart/form-data' },
                 });
                 console.log("Images uploaded successfully.");
                 setSuccess(`Property created and images uploaded! Redirecting...`);
                 setTimeout(() => navigate('/properties'), 2000); // Redirect after success

            } catch (uploadError) {
                 console.error("Image upload failed after property creation:", uploadError);
                 let uploadErrMsg = "Image upload failed.";
                 if (uploadError.response?.status === 403) {
                     uploadErrMsg = "Permission denied for image upload. Please check login status.";
                 } else if (uploadError.response?.data?.error) {
                     uploadErrMsg = uploadError.response.data.error;
                 } else {
                     uploadErrMsg = uploadError.message;
                 }
                 // Crucially, inform the user the property was created but images failed
                 setError(`Property created (ID: ${savedProperty.id}), but image upload failed: ${uploadErrMsg}. You can add images later by editing.`);
                 setSubmitting(false); // Allow user to retry or navigate away
                 return; // Stop execution here
            }
        } else {
             // Property created, no images were selected to upload
             setSuccess(`Property created successfully! Redirecting...`);
             setTimeout(() => navigate('/properties'), 1500);
        }
        // Don't set submitting false here if redirecting

    } catch (err) { // Catch errors from the initial property creation POST
      console.error(`Failed to create property:`, err);
      // --- Log Detailed Error ---
      if (err.response) {
          console.error("[PropertyForm] Create Error Response Status:", err.response.status);
          console.error("[PropertyForm] Create Error Response Data:", err.response.data);
      } else if (err.request) {
          console.error("[PropertyForm] Create Error Request (No response received):", err.request);
      } else {
          console.error("[PropertyForm] Create Error Message (Setup or other issue):", err.message);
      }
      // --- End Detailed Error Log ---
      let createErrMsg = 'Failed to create property.';
       if (err.response?.status === 401 || err.response?.status === 403) {
            createErrMsg = "Authentication failed, session expired, or insufficient permissions to create property.";
       } else if (err.response?.data?.error) {
            createErrMsg = err.response.data.error;
       } else {
            createErrMsg = err.message; // Use the generic Axios error message if no specific details
       }
      setError(createErrMsg);
      setSubmitting(false); // Set submitting false on creation error
    }
  }; // End of handleSubmit


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
         {/* File Input */}
         <div className="form-input-group">
             <label htmlFor="propertyImages">Property Images (Optional):</label>
             <input type="file" id="propertyImages" name="files" multiple onChange={handleFileChange} accept="image/png, image/jpeg, image/gif" />
             <small style={{display: 'block', marginTop: '5px', color: 'var(--text-muted)'}}>Select one or more images (JPG, PNG, GIF).</small>
             {selectedFiles && <p style={{fontSize: '0.9em', marginTop: '5px'}}>{selectedFiles.length} file(s) selected.</p>}
         </div>
         <div className="form-input-group"><label htmlFor="description">Description:</label><textarea id="description" name="description" value={formData.description} onChange={handleInputChange} rows="4"></textarea></div>

         <button type="submit" className="form-button" disabled={submitting}>
           {submitting ? 'Creating...' : 'Create Property'}
         </button>
      </form>
       <div style={{ marginTop: '20px', textAlign: 'center' }}>
            {/* Link back to properties list */}
            <Link to="/properties" style={{color: 'var(--primary-color)'}}>Cancel</Link>
       </div>
    </div>
  );
}
export default PropertyForm;