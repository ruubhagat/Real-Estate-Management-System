import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import apiClient from '../api/axiosConfig';
import PropertyCard from './PropertyCard';
import './Home.css';
import './Form.css'; // Import shared form styles

function Home() {
  const navigate = useNavigate();
  const [featuredProperties, setFeaturedProperties] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [contactForm, setContactForm] = useState({ name: '', email: '', message: '' });
  const [contactError, setContactError] = useState('');
  const [contactSuccess, setContactSuccess] = useState('');
  const [contactSubmitting, setContactSubmitting] = useState(false);

  // Fetch featured properties
  useEffect(() => {
    const fetchFeatured = async () => {
       console.log("[Home] useEffect for featured properties running.");
       setLoading(true);
       setError('');

       // --- VVV TOKEN CHECK REMOVED VVV ---
       // The GET /api/properties endpoint is now public
       // --- ^^^ END REMOVAL ^^^ ---

       try {
         console.log("[Home] Making API Call: GET /properties?status=AVAILABLE");
         // Fetch AVAILABLE properties - Endpoint is public
         const response = await apiClient.get('/properties', { params: { status: 'AVAILABLE' } });
         console.log("[Home] Featured properties API Response Status:", response.status);
         console.log("[Home] Featured properties API Response Data:", response.data);
         const properties = Array.isArray(response.data) ? response.data : [];
         setFeaturedProperties(properties.slice(0, 3));
         console.log("[Home] Featured properties state set.");
       } catch (err) {
         console.error("[Home] Failed to fetch featured properties:", err);
         if (err.response) { console.error("[Home] Error Status:", err.response.status); console.error("[Home] Error Data:", err.response.data); }
         else if (err.request) { console.error("[Home] Error Request:", err.request); }
         else { console.error("[Home] Error Msg:", err.message); }

         let fetchErrorMsg = 'Could not load featured properties.';
         // Authentication errors (401/403) are no longer expected for this specific call
         if (err.response?.data?.error) { fetchErrorMsg = err.response.data.error; }
         else { fetchErrorMsg = err.message || fetchErrorMsg; }
         setError(fetchErrorMsg);
         setFeaturedProperties([]);
       } finally {
           console.log("[Home] Fetch attempt finished. Setting loading to false.");
           setLoading(false);
       }
    };
    fetchFeatured();
  }, []); // Empty dependency array means run once on mount

  // Contact Form Handlers
  const handleContactChange = (e) => {
        const { name, value } = e.target;
        setContactForm(prev => ({ ...prev, [name]: value }));
        setContactError(''); setContactSuccess('');
   };
   const handleContactSubmit = async (e) => {
       e.preventDefault();
       setContactError(''); setContactSuccess('');
       if (!contactForm.name || !contactForm.email || !contactForm.message) {
           setContactError('Please fill in all fields.');
           return;
       }
       setContactSubmitting(true);
       try {
            const response = await apiClient.post('/public/contact', contactForm); // Public endpoint
            setContactSuccess(response.data.message || 'Message sent successfully!');
            setContactForm({ name: '', email: '', message: '' }); // Clear form
       } catch (err) {
            const errMsg = err.response?.data?.error || err.message || 'Failed to send message.';
            setContactError(errMsg);
       } finally {
           setContactSubmitting(false);
       }
   };

  // Render JSX
  return (
    <div>
      {/* --- Hero Section --- */}
      <div className="hero-section">
         <div className="hero-content fade-in">
          <h1>Find Your Perfect Estate</h1>
          <p>Discover exceptional properties and opportunities.</p>
          <button className="hero-search-button" onClick={() => navigate('/properties')}>
            Explore Properties
          </button>
        </div>
      </div>

      {/* --- Featured Properties Section --- */}
      <div className="featured-properties">
         <h2>Featured Listings</h2>
        {loading && <p className="loading" style={{textAlign:'center', padding:'20px'}}>Loading featured properties...</p>}
        {error && !loading && <p className="error" style={{color:'var(--error-text)', backgroundColor:'var(--error-bg)', border:'1px solid var(--error-border)', padding:'15px', borderRadius:'4px', textAlign:'center'}}>Error: {error}</p>}

        {!loading && !error && (
          <>
            {featuredProperties.length > 0 ? (
              <div className="property-grid">
                {featuredProperties.map(property => (
                  <PropertyCard key={property.id} property={property} />
                ))}
              </div>
            ) : (
              <p style={{textAlign:'center', padding:'20px'}}>No featured properties available at the moment.</p>
            )}
            {/* Link to view all properties */}
            {featuredProperties.length > 0 && (
                 <div style={{marginTop: '40px', textAlign: 'center'}}>
                     <Link to="/properties" className="hero-search-button" style={{padding: '12px 30px', fontSize:'1rem'}}>
                         View All Properties
                     </Link>
                 </div>
            )}
          </>
        )}
      </div>

      {/* --- Contact Us Section --- */}
      <section className="contact-section">
        <h2>Get In Touch</h2>
        <p className="section-subtitle">Have questions or need assistance? Fill out the form below or reach out to us directly.</p>
        <form onSubmit={handleContactSubmit} className="contact-form">
             {contactError && <p className="form-message form-error">{contactError}</p>}
             {contactSuccess && <p className="form-message form-success">{contactSuccess}</p>}
             {/* ... form inputs ... */}
             <div className="form-input-group"><label htmlFor="contactName">Name:</label><input type="text" id="contactName" name="name" value={contactForm.name} onChange={handleContactChange} required /></div>
             <div className="form-input-group"><label htmlFor="contactEmail">Email:</label><input type="email" id="contactEmail" name="email" value={contactForm.email} onChange={handleContactChange} required /></div>
             <div className="form-input-group"><label htmlFor="contactMessage">Message:</label><textarea id="contactMessage" name="message" rows="5" value={contactForm.message} onChange={handleContactChange} required></textarea></div>
             <button type="submit" className="form-button" disabled={contactSubmitting}>{contactSubmitting ? 'Sending...' : 'Send Message'}</button>
        </form>
        <div className="contact-info">
            {/* ... contact details ... */}
             <div className="contact-info-item"><h4>Address</h4><p>Estates, Banashankari<br/>Bengaluru<br/>Karnataka, 560050</p></div>
             <div className="contact-info-item"><h4>Phone</h4><p><a href="tel:+91123456789">+91-1234567890</a></p><p>Mon - Fri, 11 am - 5 pm IST</p></div>
             <div className="contact-info-item"><h4>Email</h4><p><a href="mailto:inquiries@estates.com">inquiries@estates.com</a></p><p>Typically replies within 24 hours.</p></div>
        </div>
      </section>
    </div>
  );
}
export default Home;