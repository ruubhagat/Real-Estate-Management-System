package com.example.demo.repository;

import com.example.demo.model.Property;
import com.example.demo.model.User;
// Correct imports for enums from the 'enums' package
import com.example.demo.model.enums.PropertyStatus;
import com.example.demo.model.enums.PropertyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Spring Data JPA repository for Property entities.
 * Provides CRUD operations (save, findById, findAll, deleteById, etc.)
 * and custom finder methods derived from method names or defined via @Query.
 */
@Repository // Indicates that this is a Spring Data repository interface
public interface PropertyRepository extends JpaRepository<Property, Long> { // Extends JpaRepository for standard methods

    /**
     * Finds all properties owned by a specific user.
     * Spring Data JPA automatically generates the query based on the method name
     * and the relationship defined in the Property entity (@ManyToOne User owner).
     * @param owner The User entity representing the owner.
     * @return A list of properties owned by the specified user.
     */
    List<Property> findByOwner(User owner);

    /**
     * Finds all properties with a specific status.
     * Uses the PropertyStatus enum imported from com.example.demo.model.enums.
     * Spring Data JPA automatically generates the query based on the method name.
     * @param status The PropertyStatus enum value to filter by (e.g., PropertyStatus.AVAILABLE).
     * @return A list of properties matching the specified status.
     */
    List<Property> findByStatus(PropertyStatus status);

    /**
     * Finds all properties located in a specific city and having a specific status.
     * Uses the PropertyStatus enum imported from com.example.demo.model.enums.
     * Spring Data JPA automatically generates the query based on the method name.
     * @param city The city name to filter by (case-sensitive by default, unless DB collation is case-insensitive).
     * @param status The PropertyStatus enum value to filter by.
     * @return A list of properties matching both the city and status.
     */
    List<Property> findByCityAndStatus(String city, PropertyStatus status);


    /**
     * Finds properties based on multiple optional criteria using a custom JPQL query.
     * This allows for more complex filtering logic than derived query methods alone.
     * Parameters annotated with @Param correspond to the named parameters (:paramName) in the @Query.
     * The query handles NULL values for optional parameters, effectively ignoring that filter condition.
     *
     * @param status Filter by property status (uses imported PropertyStatus enum).
     * @param type Filter by property type (uses imported PropertyType enum).
     * @param city Filter by city name (case-insensitive comparison using LOWER and CONCAT for partial match).
     * @param minPrice Filter by minimum price (inclusive).
     * @param maxPrice Filter by maximum price (inclusive).
     * @param minBedrooms Filter by minimum number of bedrooms (inclusive).
     * @param minBathrooms Filter by minimum number of bathrooms (inclusive).
     * @return A list of properties matching all specified (non-null) criteria.
     */
    @Query("SELECT p FROM Property p WHERE " +
            "(:status IS NULL OR p.status = :status) AND " +           // Filter by status if provided
            "(:type IS NULL OR p.type = :type) AND " +                 // Filter by type if provided
            "(:city IS NULL OR LOWER(p.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND " + // Filter by city if provided
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +       // Filter by min price if provided
            "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +       // Filter by max price if provided
            "(:minBedrooms IS NULL OR p.bedrooms >= :minBedrooms) AND " + // Filter by min bedrooms if provided
            "(:minBathrooms IS NULL OR p.bathrooms >= :minBathrooms)"  // Filter by min bathrooms if provided
            // Consider adding database indexes on frequently filtered columns (status, type, city, price) for performance.
    )
    List<Property> findPropertiesByCriteria(
            @Param("status") PropertyStatus status, // Uses imported enum from com.example.demo.model.enums
            @Param("type") PropertyType type,       // Uses imported enum from com.example.demo.model.enums
            @Param("city") String city,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minBedrooms") Integer minBedrooms,
            @Param("minBathrooms") Integer minBathrooms
    );

}