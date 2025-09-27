package com.dineswift.restaurant_service.controller;


import com.dineswift.restaurant_service.payload.request.employee.EmployeeCreateRequest;
import com.dineswift.restaurant_service.payload.dto.EmployeeDTO;
import com.dineswift.restaurant_service.payload.request.employee.EmployeeNameRequest;
import com.dineswift.restaurant_service.payload.request.employee.PasswordChangeRequest;
import com.dineswift.restaurant_service.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/restaurant/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping("/sign-up")
    public ResponseEntity<EmployeeDTO> createEmployee(@Valid @RequestBody EmployeeCreateRequest employeeCreateRequest) {
        EmployeeDTO employeeDTO = employeeService.createEmployee(employeeCreateRequest);
        return ResponseEntity.ok(employeeDTO);
    }

    @PatchMapping("/change-username/{employeeId}")
    public ResponseEntity<String> changeUsername(@Valid @RequestBody EmployeeNameRequest employeeNameRequest, @PathVariable UUID employeeId) {
        employeeService.changeUsername(employeeNameRequest,employeeId);
        return ResponseEntity.ok(employeeNameRequest.getEmployeeName());
    }

    @DeleteMapping("/delete/{employeeId}")
    public ResponseEntity<String> deleteEmployee(@PathVariable UUID employeeId) {
        employeeService.deleteEmployee(employeeId);
        return ResponseEntity.ok("Employee deleted successfully");
    }

    @PutMapping("/change-password/{employeeId}")
    public ResponseEntity<String> changePassword(@RequestBody PasswordChangeRequest passwordChangeRequest, @PathVariable UUID employeeId) {
        employeeService.changePassword(passwordChangeRequest,employeeId);
        return ResponseEntity.ok("Password changed successfully");
    }

    @PostMapping("create-employee/{restaurantId}" )
    public ResponseEntity<String> createEmployeeByRestaurant(@Valid @RequestBody EmployeeCreateRequest employeeCreateRequest, @PathVariable UUID restaurantId) {
        String employeeName = employeeService.createEmployer(employeeCreateRequest, restaurantId);
        return ResponseEntity.ok("Employee created successfully with name: " + employeeName);
    }
}
