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
         const response = await apiClient.get('/properties', { params: { status: 'AVAILABLE' } });
         setFeaturedProperties(response.data.slice(0, 3));
       } catch (err) {
         console.error("Failed to fetch featured properties:", err); setError('Could not load featured properties.');
       } finally { setLoading(false); }
    };
    fetchFeatured();
  }, []);

  // Contact Form Submit Handler (Calls Backend)
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
            // --- Make actual API call ---
            console.log("Submitting contact form data:", contactForm);
            // Use basic axios or apiClient (CORS should be configured globally or on PublicController)
            const response = await apiClient.post('/public/contact', contactForm); // <<<--- CALL BACKEND
            console.log("Contact form response:", response.data);
            // --- End API call ---
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

  // Render JSX (Hero, Featured Properties, Contact Section as before)
  return (
    <div>
      {/* --- Hero Section --- */}
      <div className="hero-section">
        {/* ... hero content ... */}
         <div className="hero-content fade-in"> {/* Added fade-in */}
          <h1>Find Your Perfect Haven</h1>
          <p>Discover exceptional properties and unparalleled living experiences.</p>
          <button className="hero-search-button" onClick={() => navigate('/properties')}>
            Explore Properties
          </button>
        </div>
      </div>

      {/* --- Featured Properties Section --- */}
      <div className="featured-properties">
         {/* ... featured properties content ... */}
         <h2>Featured Listings</h2>
        {loading && <p className="loading">Loading featured properties...</p>}
        {error && <p className="error">Error: {error}</p>}
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
      </div>

      {/* --- Contact Us Section --- */}
      <section className="contact-section">
         {/* ... contact section content ... */}
        <h2>Get In Touch</h2>
        <p className="section-subtitle">Have questions or need assistance? Fill out the form below or reach out to us directly.</p> {/* Added class */}

        {/* Contact Form uses shared styles */}
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
        {/* ... contact info blocks ... */}
        <div className="contact-info">
            <div className="contact-info-item"> <h4>Address</h4> <p>PES University<br/>BLR<br/>Prestige City, ST 12345</p> </div>
            <div className="contact-info-item"> <h4>Phone</h4> <p><a href="tel:+1234567890">+1 (234) 567-890</a></p> <p>Mon - Fri, 9am - 6pm</p> </div>
            <div className="contact-info-item"> <h4>Email</h4> <p><a href="mailto:inquiries@luxehaven.com">inquiries@luxehaven.com</a></p> <p>We reply within 24 hours</p> </div>
        </div>
      </section>
    </div>
  );
}
export default Home;