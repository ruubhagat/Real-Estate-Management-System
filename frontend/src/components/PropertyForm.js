import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import apiClient from '../api/axiosConfig';
import { AVAILABLE_AMENITIES } from '../constants'; // Assuming you created constants.js
import './Form.css';

function PropertyForm() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    address: '', city: '', state: '', postalCode: '', price: '',
    bedrooms: '', bathrooms: '', areaSqft: '', description: '',
    type: 'SALE',
    amenities: new Set() // <-- Initialize amenities as a Set
  });
  const [selectedFiles, setSelectedFiles] = useState(null); // Keep track of FileList object
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    setError(''); setSuccess('');
  };

  // --- Handler for Amenity Checkboxes ---
  const handleAmenityChange = (e) => {
      const { value, checked } = e.target;
      setFormData(prev => {
          const newAmenities = new Set(prev.amenities); // Clone the set
          if (checked) {
              newAmenities.add(value); // Add amenity if checked
          } else {
              newAmenities.delete(value); // Remove amenity if unchecked
          }
          return { ...prev, amenities: newAmenities }; // Update state
      });
      setError(''); setSuccess('');
  };

  const handleFileChange = (e) => {
    setSelectedFiles(e.target.files); // Store the FileList object
    setError(''); setSuccess('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault(); setError(''); setSuccess(''); setSubmitting(true);

    if (!formData.address || !formData.city || !formData.state /* ... add other required fields ... */) {
        setError('Please fill in all required fields.');
        setSubmitting(false); return;
    }

    // --- Convert Amenities Set to Array for JSON ---
    const propertyPayload = {
        ...formData,
        amenities: Array.from(formData.amenities) // Convert Set to Array before sending
    };
    // --- End Conversion ---

    try {
        console.log("Creating property payload:", propertyPayload);
        const response = await apiClient.post('/properties', propertyPayload); // Use relative path
        const savedProperty = response.data;
        console.log("Property created successfully:", savedProperty);

        // Image Upload Logic (using owner endpoint)
        if (savedProperty?.id && selectedFiles?.length > 0) {
            console.log(`Uploading ${selectedFiles.length} files for property ${savedProperty.id} via owner endpoint...`);
            const imageFormData = new FormData();
            // Loop through FileList and append each file
            for (let i = 0; i < selectedFiles.length; i++) {
                imageFormData.append('files', selectedFiles[i]);
            }
            try {
                 await apiClient.post(`/owner/properties/${savedProperty.id}/images`, imageFormData, {
                     headers: { 'Content-Type': 'multipart/form-data' },
                 });
                 console.log("Images uploaded successfully.");
                 setSuccess(`Property created and images uploaded! Redirecting...`);
                 setTimeout(() => navigate('/properties'), 2000);
            } catch (uploadError) {
                 console.error("Image upload failed after property creation:", uploadError);
                 // ... (improved error handling) ...
                 let uploadErrMsg = "Image upload failed."; /* ... */
                 setError(`Property created (ID: ${savedProperty.id}), but image upload failed: ${uploadErrMsg}. You can add images later by editing.`);
                 setSubmitting(false);
                 return;
            }
        } else {
             setSuccess(`Property created successfully! Redirecting...`);
             setTimeout(() => navigate('/properties'), 1500);
        }

    } catch (err) { // Catch errors from property creation
      console.error(`Failed to create property:`, err);
      // ... (improved error handling) ...
      let createErrMsg = 'Failed to create property.'; /* ... */
      setError(createErrMsg);
      setSubmitting(false);
    }
  };

  // --- Styles for Amenities and File List ---
  const amenitiesContainerStyle = {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', // Responsive columns
    gap: '10px',
    marginBottom: '20px',
    padding: '15px',
    border: '1px solid var(--border-color)',
    borderRadius: '4px'
  };
  const amenityLabelStyle = {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
    fontSize: '0.95rem',
    cursor: 'pointer'
  };
  const fileListStyle = {
      listStyle: 'decimal inside',
      margin: '10px 0 0 0',
      paddingLeft: '5px',
      fontSize: '0.9em',
      color: 'var(--text-light)'
  };
  // --- End Styles ---


  return (
    <div className="form-container">
      <h2>Add New Property</h2>
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

         {/* --- Amenities Section --- */}
         <div className="form-input-group">
            <label>Amenities:</label>
            <div style={amenitiesContainerStyle}>
                {AVAILABLE_AMENITIES.map(amenity => (
                    <label key={amenity} style={amenityLabelStyle}>
                        <input
                            type="checkbox"
                            value={amenity}
                            checked={formData.amenities.has(amenity)} // Check if amenity is in the Set
                            onChange={handleAmenityChange}
                        />
                        {amenity}
                    </label>
                ))}
            </div>
         </div>
         {/* --- End Amenities Section --- */}

         {/* File Input & Preview */}
         <div className="form-input-group">
             <label htmlFor="propertyImages">Property Images (Select Multiple):</label>
             <input
                type="file"
                id="propertyImages"
                name="files"
                multiple // Allow multiple file selection
                onChange={handleFileChange}
                accept="image/png, image/jpeg, image/gif"
                style={{ display: 'block', width: '100%', padding: '10px', boxSizing: 'border-box'}}
             />
             <small style={{display: 'block', marginTop: '5px', color: 'var(--text-muted)'}}>Select one or more images (JPG, PNG, GIF).</small>
             {/* --- Display Selected File Names --- */}
             {selectedFiles && selectedFiles.length > 0 && (
                 <div style={{marginTop: '10px'}}>
                    <strong>Selected Files:</strong>
                    <ul style={fileListStyle}>
                        {/* Convert FileList to Array to map */}
                        {Array.from(selectedFiles).map((file, index) => (
                            <li key={index}>{file.name} ({(file.size / 1024).toFixed(1)} KB)</li>
                        ))}
                    </ul>
                 </div>
             )}
             {/* --- End Display Selected File Names --- */}
         </div>

         <div className="form-input-group"><label htmlFor="description">Description:</label><textarea id="description" name="description" value={formData.description} onChange={handleInputChange} rows="4"></textarea></div>

         <button type="submit" className="form-button" disabled={submitting}>
           {submitting ? 'Creating...' : 'Create Property'}
         </button>
      </form>
       <div style={{ marginTop: '20px', textAlign: 'center' }}>
            <Link to="/properties" style={{color: 'var(--primary-color)'}}>Cancel</Link>
       </div>
    </div>
  );
}
export default PropertyForm;