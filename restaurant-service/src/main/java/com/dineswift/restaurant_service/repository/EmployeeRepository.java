package com.dineswift.restaurant_service.repository;

import com.dineswift.restaurant_service.model.Employee;
import com.dineswift.restaurant_service.model.Restaurant;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID>,
        JpaSpecificationExecutor<Employee> {

    boolean existsByEmployeeName(String employeeName);

    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = {"roles"})
    @Query("SELECT emp FROM Employee emp where employeeId=:employeeId AND employeeIsActive=true")
    Optional<Employee> findByIdAndIsActive(@Param("employeeId") UUID employeeId);

    boolean existsByPhoneNumber(String phoneNumber);

    @EntityGraph(attributePaths = {"roles"})
    @Query("SELECT emp FROM Employee emp where emp.email=:email AND emp.employeeIsActive=true")
    Optional<Employee> findByEmailAndIsActive(String email);

    @Query("SELECT emp FROM Employee emp where emp.phoneNumber=:phoneNumber AND emp.employeeIsActive=true")
    Optional<Employee> findByPhoneNumberAndIsActive(String phoneNumber);

    @Query("SELECT emp FROM Employee emp where emp.restaurant=:restaurant")
    List<Employee> findAllByRestaurant(Restaurant restaurant);


    @EntityGraph(attributePaths = {"roles", "restaurant"})
    @Query("SELECT emp FROM Employee emp where employeeId=:employeeId AND employeeIsActive=true")
    Optional<Employee> findByIdAndGetRestaurant(UUID employeeId);


    @Query("SELECT emp.restaurant.restaurantId FROM Employee emp WHERE emp.employeeId = :employeeId AND emp.employeeIsActive = true")
    UUID findRestaurantIdByEmployeeId(UUID employeeId);


    @EntityGraph(attributePaths = {"roles"})
    @Query("SELECT DISTINCT emp FROM Employee emp WHERE emp.restaurant.restaurantId = :restaurantId")
    List<Employee> findAllByRestaurant_RestaurantId(UUID restaurantId);

    @EntityGraph(attributePaths = {"restaurant"})
    @Query("SELECT emp FROM Employee emp WHERE emp.employeeId = :employeeId AND emp.employeeIsActive = true")
    Optional<Employee> findByIdAndIsActiveWithRestaurant(UUID employeeId);

    @Query("SELECT emp FROM Employee emp where employeeId=:employeeId AND employeeIsActive=true")
    Optional<Employee> findByIdAndIsActiveWoEntity(UUID employeeId);

    @EntityGraph(attributePaths = "roles")
    @Query("SELECT emp FROM Employee emp where employeeId=:employeeId ")
    Optional<Employee> findByIdWithRoles(UUID employeeId);

}
