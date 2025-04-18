import React from 'react';
import { Link } from 'react-router-dom';
// Assuming styles are driven by Home.css via class names

function PropertyCard({ property }) {
  if (!property) {
    return null; // Don't render if no property data
  }

  // Construct URL for the first image using the /uploads/ path
  const firstImageUrl = property.imageUrls
                   ? `/uploads/${property.imageUrls.split(',')[0].trim()}` // Use /uploads/ path
                   : 'https://via.placeholder.com/400x250/cccccc/969696?text=No+Image+Available';
  const imageUrl = firstImageUrl;


  // Function to handle image loading errors
  const handleImageError = (e) => {
       if (e.target.src !== 'https://via.placeholder.com/400x250/cccccc/969696?text=No+Image+Available') {
            e.target.onerror = null; // prevent infinite loop if placeholder fails
            e.target.src = 'https://via.placeholder.com/400x250/cccccc/969696?text=No+Image+Available';
       } else {
           e.target.style.display='none'; // Hide broken placeholder too
       }
  };


  return (
    // Apply the main class name styled by Home.css
    <div className="property-card">
      <Link to={`/properties/${property.id}`} className="property-card-image-link">
        <img
          src={imageUrl} // Use constructed URL
          alt={`${property.address || 'Property'} preview`}
          className="property-card-image" // Styled by Home.css
          onError={handleImageError}
        />
      </Link>

      <div className="property-card-content"> {/* Styled by Home.css */}
         {/* Title Link */}
        <Link to={`/properties/${property.id}`} style={{ textDecoration: 'none', color: 'inherit' }}>
          <h3 title={property.address || 'No Address Provided'}>
             {property.address || 'No Address Provided'}
          </h3>
        </Link>
        {/* Location */}
        <p className="location"> {/* Styled by Home.css */}
            {property.city || 'N/A'}, {property.state || 'N/A'}
        </p>
        {/* Price - Updated Symbol and Formatting */}
        <p className="price"> {/* Styled by Home.css */}
            {/* VVV--- UPDATED LINE ---VVV */}
            ₹{property.price ? Number(property.price).toLocaleString('en-IN') : 'Price N/A'}
            {/* ^^^--- UPDATED LINE ---^^^ */}
            {property.type === 'RENT' && <span> / month</span>}
        </p>
        {/* Stats */}
        <p className="stats"> {/* Styled by Home.css */}
            <span>{property.bedrooms || '?'} beds</span> |
            <span>{property.bathrooms || '?'} baths</span> |
            <span>{property.areaSqft || '?'} sqft</span>
        </p>

        {/* Details Link Button */}
        <Link to={`/properties/${property.id}`} className="property-card-link"> {/* Styled by Home.css */}
          View Details
        </Link>
      </div>
    </div>
  );
}

export default PropertyCard;