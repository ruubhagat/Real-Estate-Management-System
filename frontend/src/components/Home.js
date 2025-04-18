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
       setLoading(true); setError('');
       try {
         // Fetch AVAILABLE properties using the correct endpoint
         const response = await apiClient.get('/properties', { params: { status: 'AVAILABLE' } });
         console.log("[Home] Featured properties API Response:", response.data);
         // Ensure response.data is an array
         const properties = Array.isArray(response.data) ? response.data : [];
         // Slice the first 3 properties or fewer if less than 3 available
         setFeaturedProperties(properties.slice(0, 3));
       } catch (err) {
         console.error("[Home] Failed to fetch featured properties:", err);
         setError('Could not load featured properties.');
       } finally { setLoading(false); }
    };
    fetchFeatured();
  }, []);

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
            console.log("Submitting contact form data:", contactForm);
            // Use public endpoint
            const response = await apiClient.post('/public/contact', contactForm);
            console.log("Contact form response:", response.data);
            setContactSuccess(response.data.message || 'Message sent successfully!');
            setContactForm({ name: '', email: '', message: '' }); // Clear form
       } catch (err) {
            console.error("Failed to send contact message:", err);
            const errMsg = err.response?.data?.error || err.message || 'Failed to send message. Please try again.';
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
          <h1>Find Your Perfect Estate</h1> {/* Updated heading */}
          <p>Discover exceptional properties and opportunities.</p>
          <button className="hero-search-button" onClick={() => navigate('/properties')}>
            Explore Properties
          </button>
        </div>
      </div>

      {/* --- Featured Properties Section --- */}
      <div className="featured-properties">
         <h2>Featured Listings</h2>
        {loading && <p className="loading">Loading featured properties...</p>}
        {error && <p className="error" style={{color:'red', padding:'10px', border:'1px solid red'}}>Error: {error}</p>}
        {!loading && !error && (
          <div className="property-grid">
            {featuredProperties.length > 0 ? (
              featuredProperties.map(property => (
                <PropertyCard key={property.id} property={property} />
              ))
            ) : (
              <p>No featured properties available at the moment.</p>
            )}
          </div>
        )}
         {/* Link to view all properties */}
         {!loading && featuredProperties.length > 0 && (
              <div style={{marginTop: '40px'}}>
                  <Link to="/properties" className="hero-search-button" style={{padding: '12px 30px', fontSize:'1rem'}}>
                      View All Properties
                  </Link>
              </div>
         )}
      </div>

      {/* --- Contact Us Section --- */}
      <section className="contact-section">
        <h2>Get In Touch</h2>
        <p className="section-subtitle">Have questions or need assistance? Fill out the form below or reach out to us directly.</p>

        {/* Contact Form */}
        <form onSubmit={handleContactSubmit} className="contact-form">
             {contactError && <p className="form-message form-error">{contactError}</p>}
             {contactSuccess && <p className="form-message form-success">{contactSuccess}</p>}
            <div className="form-input-group">
                 <label htmlFor="contactName">Name:</label>
                 <input type="text" id="contactName" name="name" value={contactForm.name} onChange={handleContactChange} required />
            </div>
             <div className="form-input-group">
                <label htmlFor="contactEmail">Email:</label>
                <input type="email" id="contactEmail" name="email" value={contactForm.email} onChange={handleContactChange} required />
            </div>
            <div className="form-input-group">
                <label htmlFor="contactMessage">Message:</label>
                <textarea id="contactMessage" name="message" rows="5" value={contactForm.message} onChange={handleContactChange} required></textarea>
            </div>
             <button type="submit" className="form-button" disabled={contactSubmitting}>
                 {contactSubmitting ? 'Sending...' : 'Send Message'}
            </button>
        </form>

        {/* --- Contact Info Blocks --- */}
        {/* VVV --- MODIFIED CONTACT INFO --- VVV */}
        <div className="contact-info">
            <div className="contact-info-item">
                <h4>Address</h4>
                <p>Estates, Banashankari<br/>Bengaluru<br/>Karnataka, 560050</p>
            </div>
            <div className="contact-info-item">
                <h4>Phone</h4>
                <p><a href="tel:+91123456789">+91-1234567890</a></p> {/* Replace with actual phone */}
                <p>Mon - Fri, 11am - 5pm IST</p> {/* Updated Timings */}
            </div>
            <div className="contact-info-item">
                <h4>Email</h4>
                <p><a href="mailto:inquiries@estates.com">inquiries@estates.com</a></p> {/* Updated Email */}
                <p>We typically reply within 24 hours.</p>
            </div>
        </div>
         {/* ^^^ --- END MODIFIED CONTACT INFO --- ^^^ */}
      </section>
    </div>
  );
}
export default Home;