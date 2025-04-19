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

## 🛠️ Project Setup

### 1. Clone the Repository

```bash
git clone <your-repository-url>
cd <repository-folder>/backend
```

> Replace `<your-repository-url>` with your actual GitHub repo URL.

---

## ⚙️ Backend Setup (Spring Boot)

### 2. Configure Database Connection

Open the file:

```text
src/main/resources/application.properties
```

Update with your MySQL configuration:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/realestate
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password
```

> Ensure the `realestate` database exists in your MySQL server.

---

### 3. Database Schema

Manually create the `realestate` database in your MySQL server.

For development:

```properties
spring.jpa.hibernate.ddl-auto=update
```

If you’ve created tables like `property_amenities` manually, use:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

or

```properties
spring.jpa.hibernate.ddl-auto=none
```

---

### 4. Build the Project (Optional)

```bash
./mvnw clean package -DskipTests
```

---

### 5. Run the Spring Boot Application

```bash
./mvnw spring-boot:run
```

> The backend server should start on: `http://localhost:8081`

---

## 💻 Frontend Setup (React.js)

### 1. Navigate to the Frontend Directory

```bash
cd ../frontend
```

---

### 2. Install Dependencies

```bash
npm install
```

---

### 3. Start the React Development Server

```bash
npm start
```

> The frontend will run on: `http://localhost:3001`  
> Proxy settings in `package.json` will route API calls to the backend server on port `8081`.

---

## 🚀 Running the Application

1. Start your **MySQL** database server.
2. Run the **Spring Boot** backend:

```bash
./mvnw spring-boot:run
```

3. Start the **React** frontend:

```bash
npm start
```

4. Open your browser and go to: `http://localhost:3001`

---

## 🔗 API Endpoints Overview

| Endpoint Group             | Description                                         |
|----------------------------|-----------------------------------------------------|
| `/api/users/`              | Register, Login, Test Auth                          |
| `/api/public/`             | Public actions like Contact Form                    |
| `/api/properties/`         | GET list/details, POST create property              |
| `/api/owner/properties/`   | PUT, DELETE, POST images (owner-specific actions)   |
| `/api/bookings/`           | POST booking, PATCH status, GET user bookings       |
| `/api/admin/`              | Admin actions (GET all properties, DELETE any)      |
| `/uploads/`                | Serves uploaded property images                     |

---

## 🧱 Architecture & Design

### Architecture
- Client-Server
- Layered Architecture
- MVC (Spring Boot)
- RESTful API

### Design Principles
- **SOLID**: SRP, OCP, LSP, ISP, DIP
- **GRASP**: Information Expert, Creator, Controller, Low Coupling, High Cohesion

### Design Patterns
- **Backend**: Repository Pattern, Service Layer, DTO, Dependency Injection
- **Frontend**: Component Pattern, Hook Pattern (React)

---

## 👥 Submitted By

- **Rutuja Bhagat** – PES1UG23CS808  
- **Bhoomika R P** – PES1UG23CS809  
- **Harshita Gujjar** – PES1UG23CS810  
- **Hemavathi V M** – PES1UG23CS811

---
