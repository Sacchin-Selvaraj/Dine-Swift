package com.dineswift.restaurant_service.controller;


import com.dineswift.restaurant_service.payload.dto.RoleDTO;
import com.dineswift.restaurant_service.payload.request.employee.EmployeeCreateRequest;
import com.dineswift.restaurant_service.payload.dto.EmployeeDTO;
import com.dineswift.restaurant_service.payload.request.employee.EmployeeNameRequest;
import com.dineswift.restaurant_service.payload.request.employee.PasswordChangeRequest;
import com.dineswift.restaurant_service.payload.request.employee.RoleRequest;
import com.dineswift.restaurant_service.payload.response.employee.RoleDTOResponse;
import com.dineswift.restaurant_service.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @GetMapping("/{employeeId}")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable UUID employeeId) {
        EmployeeDTO employeeDTO = employeeService.getEmployee(employeeId);
        return ResponseEntity.ok(employeeDTO);
    }

    @PatchMapping("/change-employee-name/{employeeId}")
    public ResponseEntity<String> changeUsername(@Valid @RequestBody EmployeeNameRequest employeeNameRequest, @PathVariable UUID employeeId) {
        employeeService.changeUsername(employeeNameRequest, employeeId);
        return ResponseEntity.ok(employeeNameRequest.getEmployeeName());
    }

    @DeleteMapping("/delete/{employeeId}")
    public ResponseEntity<String> deleteEmployee(@PathVariable UUID employeeId) {
        employeeService.deleteEmployee(employeeId);
        return ResponseEntity.ok("Employee deleted successfully");
    }

    @PutMapping("/change-password/{employeeId}")
    public ResponseEntity<String> changePassword(@RequestBody PasswordChangeRequest passwordChangeRequest, @PathVariable UUID employeeId) {
        employeeService.changePassword(passwordChangeRequest, employeeId);
        return ResponseEntity.ok("Password changed successfully");
    }

    @PostMapping("create-employee/{restaurantId}")
    public ResponseEntity<String> createEmployeeByRestaurant(@Valid @RequestBody EmployeeCreateRequest employeeCreateRequest, @PathVariable UUID restaurantId) {
        String employeeName = employeeService.createEmployer(employeeCreateRequest, restaurantId);
        return ResponseEntity.ok("Employee created successfully with name: " + employeeName);
    }

    @GetMapping("/get-roles")
    public ResponseEntity<List<RoleDTOResponse>> getAllRoles() {
        List<RoleDTOResponse> roles = employeeService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    @DeleteMapping("/remove-roles/{employeeId}")
    public ResponseEntity<EmployeeDTO> removeAllRolesFromEmployee(@PathVariable UUID employeeId, @Valid @RequestBody RoleRequest roleRemovalRequest) {
        EmployeeDTO employeeDTO = employeeService.removeRolesFromEmployee(employeeId, roleRemovalRequest);
        return ResponseEntity.ok(employeeDTO);
    }

    @PostMapping("/add-roles/{employeeId}")
    public ResponseEntity<EmployeeDTO> addRolesToEmployee(@PathVariable UUID employeeId, @Valid @RequestBody RoleRequest roleAddRequest) {
        EmployeeDTO employeeDTO = employeeService.addRolesToEmployee(employeeId, roleAddRequest);
        return ResponseEntity.ok(employeeDTO);
    }

}
