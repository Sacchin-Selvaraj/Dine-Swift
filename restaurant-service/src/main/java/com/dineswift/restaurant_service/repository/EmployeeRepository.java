package com.dineswift.restaurant_service.repository;

import com.dineswift.restaurant_service.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    boolean existsByEmployeeName(String employeeName);

    boolean existsByEmail(String email);

    @Query("SELECT emp FROM Employee emp where employeeId=:employeeId AND employeeIsActive=true")
    Optional<Employee> findByIdAndIsActive(@Param("employeeId") UUID employeeId);

    boolean existsByPhoneNumber(String phoneNumber);

    @Query("SELECT emp FROM Employee emp where emp.email=:email AND emp.employeeIsActive=true")
    Optional<Employee> findByEmailAndIsActive(String email);
}
