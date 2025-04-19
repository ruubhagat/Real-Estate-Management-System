# Real Estate Management System

A comprehensive web platform designed to streamline the process of searching, listing, managing, and booking real estate properties. This system caters to different user roles: Customers looking for properties, Property Owners managing their listings, and Administrators overseeing the platform.

## Problem Statement

The traditional process of searching for, listing, and managing real estate properties often suffers from inefficiency and fragmentation. Prospective buyers and renters struggle with scattered listings, difficulty in scheduling viewings, and accessing comprehensive property details, while property owners face challenges in reaching a wide audience, managing inquiries, updating listings effectively, and coordinating visits. Furthermore, the lack of a centralized, secure platform with distinct user roles makes oversight and streamlined communication difficult, leading to potential delays and missed opportunities for all parties involved in a real estate transaction. This necessitates a digital solution to bridge the gap between property seekers, owners, and administrators, simplifying the entire property management lifecycle. This project aims to develop a Real Estate Management System using Java and MySQL that provides a secure, efficient, and user-friendly platform where buyers can browse available properties and book visits, while admins can manage listings. 

## Features

**General:**

*   **Secure User Authentication:** Robust registration and JWT-based login system.
*   **Role-Based Access Control:** Functionality and data visibility are tailored based on user roles (Customer, Property Owner, Admin).
*   **RESTful API:** Backend exposes a well-defined API for frontend interaction.

**Customer:**

*   **Property Search & Filtering:** Search properties based on location (city), type (Sale/Rent), price range, bedrooms, bathrooms.
*   **View Property Listings:** Browse available properties with details, images, and amenities.
*   **View Property Details:** See comprehensive information for a specific property.
*   **Request Property Visit:** Submit booking requests for desired properties with preferred date and time.
*   **View My Bookings:** Track the status (Pending, Confirmed, Rejected, Cancelled, Completed) of submitted visit requests.

**Property Owner:**

*   **Property CRUD:** Create, Read, Update, and Delete their own property listings.
*   **Multiple Image Upload:** Upload multiple images for each property.
*   **Amenity Management:** Select and manage a list of amenities associated with each property.
*   **Booking Management:** View booking requests for their properties and Confirm or Reject pending requests. Mark confirmed visits as Completed.
*   **View My Properties:** See a list of properties they own.

**Admin:**

*   **Admin Panel:** Centralized dashboard for platform oversight.
*   **View All Properties:** Access a list of all properties registered on the platform, regardless of owner or status.
*   **Delete Any Property:** Ability to remove property listings for moderation or platform management.
    *   *(Note: Admin Edit functionality is currently disabled in the UI but could be added)*.
*   **View All Bookings (Optional):** Can view all booking requests across the system (if implemented).

## Technology Stack

*   **Backend:**
    *   Java 17+
    *   Spring Boot 3.x
    *   Spring Web (MVC, REST Controllers)
    *   Spring Security (JWT Authentication, Role-Based Authorization, Method Security)
    *   Spring Data JPA / Hibernate (ORM)
    *   MySQL (Database)
    *   Maven (Build Tool)
    *   Lombok (Reduced boilerplate - *Note: Ensure it's consistently used or removed*)
*   **Frontend:**
    *   React 18+
    *   React Router v6
    *   Axios (HTTP Client)
    *   CSS (Styling, potentially CSS Variables)
*   **Database:**
    *   MySQL 8.0+

## Setup and Installation

**Prerequisites:**

*   Java Development Kit (JDK) 17 or later
*   Apache Maven 3.6+
*   Node.js (includes npm) v18+ or v20+
*   MySQL Server 8.0+
*   Git

**1. Backend Setup:**

   ```bash
   # 1. Clone the repository
   git clone <your-repository-url>
   cd <repository-folder>/backend # Or your backend project root folder

   # 2. Configure Database Connection
   #    - Open src/main/resources/application.properties
   #    - Update the following properties with your MySQL details:
   #      spring.datasource.url=jdbc:mysql://localhost:3306/realestate # Ensure 'realestate' DB exists
   #      spring.datasource.username=your_mysql_username
   #      spring.datasource.password=your_mysql_password

   # 3. Database Schema:
   #    - Manually create the 'realestate' database in your MySQL server.
   #    - Ensure 'spring.jpa.hibernate.ddl-auto' is set to 'update' (for development)
   #      in application.properties. Spring Boot/Hibernate will attempt to create/update tables.
   #    - **OR** If you created the property_amenities table manually, set ddl-auto to 'validate' or 'none'.

   # 4. Build (Optional - mvnw run compiles automatically)
   # ./mvnw clean package -DskipTests

   # 5. Run the Spring Boot application
   ./mvnw spring-boot:run


The backend server should start on http://localhost:8081 (or the port specified in application.properties).

2. Frontend Setup:

# 1. Navigate to the frontend directory
cd ../frontend # Or your frontend project root folder

# 2. Install dependencies
npm install

# 3. Start the React development server
npm start
IGNORE_WHEN_COPYING_START
content_copy
download
Use code with caution.
Bash
IGNORE_WHEN_COPYING_END

The frontend development server should start on http://localhost:3001 (or the next available port). The proxy setting in package.json should automatically forward API requests to the backend on port 8081.

Running the Application

Start the MySQL Database Server.

Start the Spring Boot Backend application (./mvnw spring-boot:run). Wait for it to initialize successfully.

Start the React Frontend application (npm start).

Open your web browser and navigate to http://localhost:3001.

API Endpoints Overview

/api/users/ (Register, Login, Test Auth)

/api/public/ (Public actions like Contact Form)

/api/properties/ (General GET for list/details, POST for create)

/api/owner/properties/ (PUT, DELETE, POST images for owner-specific actions)

/api/bookings/ (POST create booking, PATCH status, GET user-specific bookings)

/api/admin/ (Admin-specific actions like GET all properties, DELETE any property)

/uploads/ (Serves uploaded property images - requires backend configuration)

Architecture & Design

The application follows standard web architecture patterns and design principles:

Architecture: Client-Server, Layered Architecture, MVC (Backend), RESTful API.

Design Principles: SOLID (SRP, OCP, LSP, ISP, DIP) and GRASP (Information Expert, Creator, Controller, Low Coupling, High Cohesion) principles were considered to promote maintainability, testability, and separation of concerns. Key examples include separating controller/service/repository layers, using dependency injection, and assigning responsibilities appropriately.

Design Patterns: Repository Pattern (Spring Data JPA), Service Layer, Data Transfer Object (DTO), Dependency Injection (Spring), Component Pattern (React), Hook Pattern (React).

Submitted by:

Rutuja Bhagat : PES1UG23CS808

Bhoomika R P : PES1UG23CS809

Harshita Gujjar : PES1UG23CS810

Hemavathi V M : PES1UG23CS811

License

[Specify Your License Here - e.g., MIT, Apache 2.0, None]

**To Use:**

1.  Copy the entire content above.
2.  Create a file named `README.md` in the **root directory** of your GitHub project (the main folder containing both your `frontend` and `backend` subdirectories).
3.  Paste the copied content into the `README.md` file.
4.  Save the file.
5.  Commit and push the `README.md` file to your GitHub repository. GitHub will automatically render it on your repository's main page.
6.  **Remember to replace `<your-repository-url>`** in the setup instructions if you include the clone command.
7.  **Choose and specify a license** in the License section, or remove that section if you don't want to specify one yet.
IGNORE_WHEN_COPYING_START
content_copy
download
Use code with caution.
IGNORE_WHEN_COPYING_END