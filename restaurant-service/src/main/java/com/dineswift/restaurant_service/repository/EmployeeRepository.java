package com.dineswift.restaurant_service.repository;

import com.dineswift.restaurant_service.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    boolean existsByEmployeeName(String employeeName);

    boolean existsByEmail(String email);
}
