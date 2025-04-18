import React from 'react';
import { Link } from 'react-router-dom';
// Assuming styles are driven by Home.css via class names

function PropertyCard({ property }) {
  // console.log('[PropertyCard] Received property:', property); // Keep for debugging if needed

  if (!property) {
    // console.warn("[PropertyCard] Rendered with null or undefined property prop.");
    return null; // Don't render if no property data
  }

  const imageBaseUrl = "/uploads/"; // Relative path for images
  const placeholderUrl = 'https://via.placeholder.com/400x250/cccccc/969696?text=No+Image+Available';

  // Construct URL for the first image using the /uploads/ path
  let imageUrl = placeholderUrl; // Default to placeholder

  if (property.imageUrls && typeof property.imageUrls === 'string') {
      const imageNames = property.imageUrls.split(',')
                            .map(name => name.trim())
                            .filter(name => name); // Get valid, non-empty names
      if (imageNames.length > 0) {
          imageUrl = `${imageBaseUrl}${imageNames[0]}`; // Construct URL for the first image
      }
  }

  // console.log(`[PropertyCard] Property ID: ${property.id}, Raw imageUrls: ${property.imageUrls}, Constructed imageUrl for card: ${imageUrl}`);


  // Function to handle image loading errors
  const handleImageError = (e) => {
       console.warn(`[PropertyCard] Image failed to load: ${e.target.src}. Replacing with placeholder.`);
       if (e.target.src !== placeholderUrl) {
            e.target.onerror = null; // prevent infinite loop if placeholder fails
            e.target.src = placeholderUrl;
       }
  };


  return (
    // Apply the main class name styled by Home.css or specific card styles
    <div className="property-card">
      <Link to={`/properties/${property.id}`} className="property-card-image-link">
        <img
          src={imageUrl} // Use constructed URL (will be placeholder if no images)
          alt={`${property.address || 'Property'} preview`}
          className="property-card-image" // Styled by CSS (MUST include object-fit)
          onError={handleImageError} // Use the error handler
        />
      </Link>

      <div className="property-card-content"> {/* Styled by CSS */}
         {/* Title Link */}
        <Link to={`/properties/${property.id}`} style={{ textDecoration: 'none', color: 'inherit' }}>
          <h3 title={property.address || 'No Address Provided'}>
             {property.address || 'No Address Provided'}
          </h3>
        </Link>
        {/* Location */}
        <p className="location"> {/* Styled by CSS */}
            {property.city || 'N/A'}, {property.state || 'N/A'}
        </p>
        {/* --- Price - Using ₹ and en-IN --- */}
        <p className="price"> {/* Styled by CSS */}
            ₹{property.price ? Number(property.price).toLocaleString('en-IN') : 'Price N/A'}
            {property.type === 'RENT' && <span style={{fontSize: '0.8em', color: 'var(--text-muted)'}}> / month</span>}
        </p>
        {/* --- End Price --- */}
        {/* Stats */}
        <p className="stats"> {/* Styled by CSS */}
            <span>{property.bedrooms ?? '?'} beds</span> | {/* Use nullish coalescing ?? */}
            <span>{property.bathrooms ?? '?'} baths</span> |
            <span>{property.areaSqft ?? '?'} sqft</span>
        </p>

        {/* Details Link Button */}
        <Link to={`/properties/${property.id}`} className="property-card-link"> {/* Styled by CSS */}
          View Details
        </Link>
      </div>
    </div>
  );
}

export default PropertyCard;