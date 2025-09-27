package com.dineswift.restaurant_service.service;

import com.dineswift.restaurant_service.exception.EmployeeException;
import com.dineswift.restaurant_service.mapper.EmployeeMapper;
import com.dineswift.restaurant_service.model.entites.Employee;
import com.dineswift.restaurant_service.model.entites.Role;
import com.dineswift.restaurant_service.model.entites.RoleName;
import com.dineswift.restaurant_service.payload.request.EmployeeCreateRequest;
import com.dineswift.restaurant_service.payload.dto.EmployeeDTO;
import com.dineswift.restaurant_service.payload.request.EmployeeNameRequest;
import com.dineswift.restaurant_service.payload.request.PasswordChangeRequest;
import com.dineswift.restaurant_service.repository.EmployeeRepository;
import com.dineswift.restaurant_service.repository.RoleRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final EmployeeMapper employeeMapper;


    public EmployeeDTO createEmployee(EmployeeCreateRequest employeeCreateRequest) {

        verifyUser(employeeCreateRequest);

        Employee employee = convertToEntity(employeeCreateRequest);

        Role role = roleRepository.findByRoleName(RoleName.ROLE_ADMIN).orElseThrow(() -> new EmployeeException("Role not found"));
        employee.setRoles(Set.of(role));

        employee = employeeRepository.save(employee);

        return employeeMapper.toDTO(employee);

    }

    private Employee convertToEntity(EmployeeCreateRequest employeeCreateRequest) {
        Employee employee = new Employee();
        if (employeeCreateRequest.getEmployeeName() != null)
            employee.setEmployeeName(employeeCreateRequest.getEmployeeName());
        if (employeeCreateRequest.getPassword() != null)
            employee.setPassword(employeeCreateRequest.getPassword());
        if (employeeCreateRequest.getEmail() != null)
            employee.setEmail(employeeCreateRequest.getEmail());
        if (employeeCreateRequest.getPhoneNumber() != null)
            employee.setPhoneNumber(employeeCreateRequest.getPhoneNumber());
        employee.setEmployeeIsActive(true);
        return employee;
    }


    private void verifyUser(EmployeeCreateRequest employeeCreateRequest) {
        if (employeeCreateRequest == null) {
            throw new EmployeeException("Employee Details not found");
        }
        if (employeeRepository.existsByEmployeeName(employeeCreateRequest.getEmployeeName())) {
            throw new EmployeeException("Employee name already taken!");
        }
        if (employeeRepository.existsByEmail(employeeCreateRequest.getEmail())) {
            throw new EmployeeException("Email already registered!");
        }
    }

    public void changeUsername(EmployeeNameRequest employeeNameRequest, UUID employeeId) {
        if (employeeNameRequest == null || employeeId == null) {
            throw new EmployeeException("Invalid request");
        }
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> new EmployeeException("Employee not found"));
        if (employeeRepository.existsByEmployeeName(employeeNameRequest.getEmployeeName())) {
            throw new EmployeeException("Employee name already taken!");
        }
        employee.setEmployeeName(employeeNameRequest.getEmployeeName());
        employeeRepository.save(employee);
    }

    public void deleteEmployee(UUID employeeId) {
        if (employeeId == null) {
            throw new EmployeeException("Invalid request");
        }
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> new EmployeeException("Employee not found"));
        employee.setEmployeeIsActive(false);
        // need to sent it to auth service to disable the user
        employeeRepository.save(employee);

    }

    public void changePassword(PasswordChangeRequest passwordChangeRequest, UUID employeeId) {
        if (employeeId == null || passwordChangeRequest == null) {
            throw new EmployeeException("Invalid request");
        }
        if (!passwordChangeRequest.getNewPassword().equals(passwordChangeRequest.getConfirmNewPassword())){
            throw new EmployeeException("New password and confirm password do not match");
        }
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> new EmployeeException("Employee not found"));
        if (!employee.getPassword().equals(passwordChangeRequest.getOldPassword())) {
            throw new EmployeeException("Old password is incorrect");
        }
        // need to encode the password and sent it to auth service
        employee.setPassword(passwordChangeRequest.getNewPassword());
        employeeRepository.save(employee);
    }
}
