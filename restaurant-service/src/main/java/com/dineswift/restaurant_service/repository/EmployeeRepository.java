package com.dineswift.restaurant_service.repository;

import com.dineswift.restaurant_service.model.Employee;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
}
