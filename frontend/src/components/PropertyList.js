import React, { useEffect, useState, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import apiClient from '../api/axiosConfig'; // Use the configured Axios instance
import './Form.css';

function PropertyList() {
  const [properties, setProperties] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  // State for Filter Values
  const [filters, setFilters] = useState({
    city: '',
    type: '', // 'SALE', 'RENT', or '' for all
    minPrice: '',
    maxPrice: '',
    minBedrooms: '',
    minBathrooms: ''
  });

  // Callback function for fetching properties
  const fetchProperties = useCallback(async () => {
    setError('');
    setLoading(true);
    try {
      console.log("Fetching properties with filters:", filters); // Frontend log

      // Prepare params for Axios, removing empty values
      const params = {};
      for (const key in filters) {
        if (filters[key] !== null && filters[key] !== '') {
          params[key] = filters[key];
        }
      }

      // ***** This is the API call that receives the 403 *****
      const response = await apiClient.get('/properties', { params });
      // ***** If it gets past here, the request succeeded *****

      console.log("Filtered Properties fetched:", response.data); // Frontend log
      setProperties(response.data || []);
    } catch (err) {
      // This block executes because the await above failed due to 403
      console.error("Failed to fetch properties:", err); // Frontend log
      const errMsg = err.response?.data?.error
                     || err.response?.data?.message // Use backend message if available
                     || (err.response?.status === 403 ? 'Forbidden - You may not have permission.' : err.message) // Specific 403 message
                     || 'Failed to load properties.';
      setError(errMsg);
    } finally {
      setLoading(false);
    }
  }, [filters]); // Dependency: fetchProperties runs when filters change

  // Initial Fetch on Component Mount
  useEffect(() => {
    fetchProperties();
  }, [fetchProperties]); // Initial call

  // Handlers for Filter Changes
  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters(prevFilters => ({ ...prevFilters, [name]: value }));
  };

  const handleFilterSubmit = (e) => {
    e.preventDefault();
    fetchProperties(); // Re-fetch data with current filters
  };

  // Styles (keep as before)
  const filterContainerStyle = { display: 'flex', flexWrap: 'wrap', gap: '15px', padding: '15px', marginBottom: '20px', border: '1px solid #ddd', borderRadius: '5px', backgroundColor: '#f9f9f9'};
  const filterGroupStyle = { display: 'flex', flexDirection: 'column' };
  const filterInputStyle = { padding: '8px', borderRadius: '4px', border: '1px solid #ccc' };
  const filterLabelStyle = { marginBottom: '4px', fontSize: '0.9em', color: '#555' };
  const filterButtonStyle = { padding: '8px 15px', cursor: 'pointer', backgroundColor: '#007bff', color: 'white', border: 'none', borderRadius: '4px', alignSelf: 'flex-end' };
  const listStyle = { listStyleType: 'none', padding: 0 };
  const listItemStyle = { border: '1px solid #eee', padding: '15px', marginBottom: '15px', borderRadius: '5px', backgroundColor: '#fff', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' };
  const buttonStyle = { padding: '10px 15px', marginBottom: '20px', cursor: 'pointer', backgroundColor: '#28a745', color: 'white', border: 'none', borderRadius: '4px', fontSize: '1em'};
  const errorStyle = { color: '#721c24', backgroundColor: '#f8d7da', border: '1px solid #f5c6cb', padding: '15px', marginBottom: '15px', borderRadius: '4px'};
  const detailLinkStyle = { marginTop: '10px', display: 'inline-block', color: '#007bff', textDecoration: 'none', fontWeight: 'bold'};

  return (
    <div>
      <h2 style={{ marginBottom: '10px' }}>Available Properties</h2>

      {/* Filter Form */}
      <form onSubmit={handleFilterSubmit} style={filterContainerStyle}>
         {/* Filter inputs as before */}
         <div style={filterGroupStyle}>
          <label style={filterLabelStyle} htmlFor="city">City:</label>
          <input type="text" id="city" name="city" value={filters.city} onChange={handleFilterChange} placeholder="e.g., Testville" style={filterInputStyle}/>
        </div>
         <div style={filterGroupStyle}>
          <label style={filterLabelStyle} htmlFor="type">Type:</label>
          <select id="type" name="type" value={filters.type} onChange={handleFilterChange} style={filterInputStyle}>
            <option value="">All Types</option><option value="SALE">For Sale</option><option value="RENT">For Rent</option>
          </select>
        </div>
        {/* Add other filter inputs: minPrice, maxPrice, minBedrooms, minBathrooms */}
        <button type="submit" style={filterButtonStyle}>Apply Filters</button>
      </form>

      {/* Add New Property Button */}
      <button onClick={() => navigate('/properties/new')} style={buttonStyle}>+ Add New Property</button>

      {/* Loading/Error/List Display */}
      {loading && <p>Loading properties...</p>}
      {!loading && error && <div style={errorStyle}>Error: {error}</div>}
      {!loading && !error && properties.length === 0 ? ( <p>No properties found matching your criteria.</p> ) :
       !loading && !error ? (
        <ul style={listStyle}>
          {properties.map(prop => (
            <li key={prop.id} style={listItemStyle}>
               <div style={{ fontWeight: 'bold', fontSize: '1.1em', marginBottom: '5px' }}>{prop.address || 'N/A'}, {prop.city || 'N/A'}</div>
               <div><strong>Price:</strong> ${prop.price ? Number(prop.price).toLocaleString() : 'N/A'}</div>
               <div><strong>Type:</strong> {prop.type || 'N/A'} | <strong>Status:</strong> {prop.status || 'N/A'}</div>
               <div><strong>Beds:</strong> {prop.bedrooms || 'N/A'} | <strong>Baths:</strong> {prop.bathrooms || 'N/A'}</div>
               <Link to={`/properties/${prop.id}`} style={detailLinkStyle}>View Details →</Link>
            </li>
          ))}
        </ul>
      ) : null}
    </div>
  );
}

export default PropertyList;