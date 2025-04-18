import React, { useEffect, useState, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import apiClient from '../api/axiosConfig';
import './Form.css'; // For filter styles if needed
// No need to import PropertyCard if we are building the layout here
// import './PropertyList.css'; // Optional: Define styles in a separate CSS file

function PropertyList() {
  const [properties, setProperties] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const userRole = localStorage.getItem('userRole');
  const [filters, setFilters] = useState({
    city: '', type: '', minPrice: '', maxPrice: '', minBedrooms: '', minBathrooms: ''
  });

  // --- Image Handling Logic (similar to PropertyCard) ---
  const imageBaseUrl = "/uploads/"; // Make sure this matches proxy/backend config
  const placeholderUrl = 'https://via.placeholder.com/150x100/cccccc/969696?text=No+Image'; // Smaller placeholder

  // Function to handle image loading errors
  const handleImageError = (e) => {
       console.warn(`[PropertyList] Image failed to load: ${e.target.src}. Replacing with placeholder.`);
       if (e.target.src !== placeholderUrl) {
            e.target.onerror = null; // prevent infinite loop
            e.target.src = placeholderUrl;
       }
  };
  // --- End Image Handling ---


  // Callback function for fetching properties
  const fetchProperties = useCallback(async () => {
    setError('');
    setLoading(true);
    try {
      console.log("[PropertyList] Fetching properties with filters:", filters);
      const params = {};
      for (const key in filters) {
          if (filters[key] !== null && filters[key] !== '') {
              params[key] = filters[key];
          }
      }
      const response = await apiClient.get('/properties', { params }); // Use relative path
      console.log("[PropertyList] Properties fetched successfully:", response.data);
      setProperties(Array.isArray(response.data) ? response.data : []);
    } catch (err) {
      console.error("[PropertyList] Failed to fetch properties:", err);
      // ... (error handling logic) ...
      let errMsg = 'Failed to load properties.'; /* ... */ setError(errMsg);
      setProperties([]);
    } finally {
      setLoading(false);
    }
  }, [filters]);

  // Initial Fetch
  useEffect(() => {
    fetchProperties();
  }, [fetchProperties]);

  // Filter Handlers
  const handleFilterChange = (e) => { /* ... */ setFilters(prev => ({...prev, [e.target.name]: e.target.value})); };
  const handleFilterSubmit = (e) => { e.preventDefault(); fetchProperties(); };


  // --- Styles ---
  const filterContainerStyle = { display: 'flex', flexWrap: 'wrap', gap: '15px', padding: '15px', marginBottom: '20px', border: '1px solid #ddd', borderRadius: '5px', backgroundColor: '#f9f9f9'};
  const filterGroupStyle = { display: 'flex', flexDirection: 'column' };
  const filterInputStyle = { padding: '8px', borderRadius: '4px', border: '1px solid #ccc', minWidth: '100px' };
  const filterLabelStyle = { marginBottom: '4px', fontSize: '0.9em', color: '#555' };
  const filterButtonStyle = { padding: '8px 15px', cursor: 'pointer', backgroundColor: 'var(--primary-color)', color: 'white', border: 'none', borderRadius: '4px', alignSelf: 'flex-end', marginLeft: '10px' };
  const listStyle = { listStyleType: 'none', padding: 0 };
  // ** MODIFIED listItemStyle for Flexbox **
  const listItemStyle = {
      display: 'flex', // Use flexbox for layout
      gap: '20px', // Space between image and text
      alignItems: 'flex-start', // Align items to the top
      border: '1px solid var(--border-color)',
      padding: '15px',
      marginBottom: '15px',
      borderRadius: '5px',
      backgroundColor: 'var(--bg-content)',
      boxShadow: 'var(--shadow-light-color)'
   };
   // ** NEW Style for Image **
   const imageStyle = {
       width: '150px', // Fixed width for the image
       height: '100px', // Fixed height
       objectFit: 'cover', // Cover the area, crop if needed
       borderRadius: '4px',
       flexShrink: 0 // Prevent image from shrinking if text is long
   };
   // ** NEW Style for Text Container **
   const textContainerStyle = {
       flexGrow: 1 // Allow text container to take remaining space
   };
  const addButtonStyle = { padding: '10px 15px', marginBottom: '20px', cursor: 'pointer', backgroundColor: 'var(--success-bg)', color: 'var(--success-text)', border: '1px solid var(--success-border)', borderRadius: '4px', fontSize: '1em', fontWeight: '500'};
  const errorBoxStyle = { color: 'var(--error-text)', backgroundColor: 'var(--error-bg)', border: '1px solid var(--error-border)', padding: '15px', margin: '15px 0', borderRadius: '4px'};
  const detailLinkStyle = { marginTop: '10px', display: 'inline-block', color: 'var(--primary-color)', textDecoration: 'none', fontWeight: 'bold'};
  // --- End Styles ---


  return (
    <div className="content-wrapper property-list-page">
      <h2 style={{ marginBottom: '10px' }}>Available Properties</h2>

      {/* Filter Form */}
      <form onSubmit={handleFilterSubmit} style={filterContainerStyle}>
         {/* ... Filter inputs ... */}
         <div style={filterGroupStyle}> <label style={filterLabelStyle} htmlFor="city">City:</label> <input type="text" id="city" name="city" value={filters.city} onChange={handleFilterChange} placeholder="e.g., Bengaluru" style={filterInputStyle}/> </div>
         <div style={filterGroupStyle}> <label style={filterLabelStyle} htmlFor="type">Type:</label> <select id="type" name="type" value={filters.type} onChange={handleFilterChange} style={filterInputStyle}> <option value="">All Types</option><option value="SALE">For Sale</option><option value="RENT">For Rent</option></select> </div>
         <div style={filterGroupStyle}> <label style={filterLabelStyle} htmlFor="minPrice">Min Price (₹):</label> <input type="number" id="minPrice" name="minPrice" value={filters.minPrice} onChange={handleFilterChange} placeholder="Any" style={filterInputStyle} min="0"/> </div>
         <div style={filterGroupStyle}> <label style={filterLabelStyle} htmlFor="maxPrice">Max Price (₹):</label> <input type="number" id="maxPrice" name="maxPrice" value={filters.maxPrice} onChange={handleFilterChange} placeholder="Any" style={filterInputStyle} min="0"/> </div>
         <div style={filterGroupStyle}> <label style={filterLabelStyle} htmlFor="minBedrooms">Min Beds:</label> <input type="number" id="minBedrooms" name="minBedrooms" value={filters.minBedrooms} onChange={handleFilterChange} placeholder="Any" style={filterInputStyle} min="0"/> </div>
         <div style={filterGroupStyle}> <label style={filterLabelStyle} htmlFor="minBathrooms">Min Baths:</label> <input type="number" id="minBathrooms" name="minBathrooms" value={filters.minBathrooms} onChange={handleFilterChange} placeholder="Any" style={filterInputStyle} min="0"/> </div>
         <button type="submit" style={filterButtonStyle} disabled={loading}>Apply Filters</button>
      </form>

      {/* Add Button (Conditional) */}
      {(userRole === 'PROPERTY_OWNER') && (
         <button onClick={() => navigate('/properties/new')} style={addButtonStyle} disabled={loading}>
             + Add New Property
         </button>
      )}

      {/* Display Area */}
      {loading && <p>Loading properties...</p>}
      {!loading && error && <div style={errorBoxStyle}>Error: {error}</div>}
      {!loading && !error && properties.length === 0 && <p>No properties found matching your criteria.</p>}
      {!loading && !error && properties.length > 0 && (
        <ul style={listStyle}>
          {properties.map(prop => {
            // --- Calculate imageUrl for this specific property ---
            let imageUrl = placeholderUrl; // Default to placeholder
            if (prop.imageUrls && typeof prop.imageUrls === 'string') {
                const imageNames = prop.imageUrls.split(',')
                                    .map(name => name.trim())
                                    .filter(name => name);
                if (imageNames.length > 0) {
                    imageUrl = `${imageBaseUrl}${imageNames[0]}`;
                }
            }
            // --- End imageUrl calculation ---

            return (
              // Apply flexbox style to the list item
              <li key={prop.id} style={listItemStyle}>
                {/* Image Column */}
                <img
                    src={imageUrl}
                    alt={`${prop.address || 'Property'} preview`}
                    style={imageStyle}
                    onError={handleImageError}
                />
                {/* Text Details Column */}
                <div style={textContainerStyle}>
                    <div style={{ fontWeight: 'bold', fontSize: '1.1em', marginBottom: '5px' }}>{prop.address || 'N/A'}, {prop.city || 'N/A'}</div>
                    <div>
                        <strong>Price:</strong> ₹{prop.price ? Number(prop.price).toLocaleString('en-IN') : 'N/A'}
                        {prop.type === 'RENT' && <span style={{fontSize: '0.8em', color: 'var(--text-muted)'}}> / month</span>}
                    </div>
                    <div><strong>Type:</strong> {prop.type || 'N/A'} | <strong>Status:</strong> {prop.status || 'N/A'}</div>
                    <div><strong>Beds:</strong> {prop.bedrooms ?? 'N/A'} | <strong>Baths:</strong> {prop.bathrooms ?? 'N/A'}</div>
                    <Link to={`/properties/${prop.id}`} style={detailLinkStyle}>View Details →</Link>
                </div>
              </li>
            );
          })}
        </ul>
      )}
    </div>
  );
}

export default PropertyList;