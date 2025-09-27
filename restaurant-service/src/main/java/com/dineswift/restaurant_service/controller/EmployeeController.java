package com.dineswift.restaurant_service.controller;


import com.dineswift.restaurant_service.payload.request.EmployeeCreateRequest;
import com.dineswift.restaurant_service.payload.dto.EmployeeDTO;
import com.dineswift.restaurant_service.payload.request.EmployeeNameRequest;
import com.dineswift.restaurant_service.payload.request.PasswordChangeRequest;
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

    @PostMapping("/change-username/{employeeId}")
    public ResponseEntity<String> changeUsername(@Valid @RequestBody EmployeeNameRequest employeeNameRequest, @PathVariable UUID employeeId) {
        employeeService.changeUsername(employeeNameRequest,employeeId);
        return ResponseEntity.ok(employeeNameRequest.getEmployeeName());
    }

    @DeleteMapping("/delete/{employeeId}")
    public ResponseEntity<String> deleteEmployee(@PathVariable UUID employeeId) {
        employeeService.deleteEmployee(employeeId);
        return ResponseEntity.ok("Employee deleted successfully");
    }

    @PostMapping("/change-password/{employeeId}")
    public ResponseEntity<String> changePassword(@RequestBody PasswordChangeRequest passwordChangeRequest, @PathVariable UUID employeeId) {
        employeeService.changePassword(passwordChangeRequest,employeeId);
        return ResponseEntity.ok("Password changed successfully");
    }
}
