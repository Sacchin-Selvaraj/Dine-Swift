package com.dineswift.restaurant_service.controller;


import com.dineswift.restaurant_service.payload.request.employee.*;
import com.dineswift.restaurant_service.payload.dto.EmployeeDto;
import com.dineswift.restaurant_service.payload.response.employee.EmployeeResponse;
import com.dineswift.restaurant_service.payload.response.employee.RoleDTOResponse;
import com.dineswift.restaurant_service.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/restaurant/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping("/sign-up")
    public ResponseEntity<EmployeeDto> createEmployee(@Valid @RequestBody EmployeeCreateRequest employeeCreateRequest) {
        EmployeeDto employeeDTO = employeeService.createEmployee(employeeCreateRequest);
        return ResponseEntity.ok(employeeDTO);
    }

    @PostMapping("/login")
    public ResponseEntity<EmployeeResponse> loginRequest(@RequestBody LoginRequest loginRequest){
        EmployeeResponse employeeResponse = employeeService.authenticateEmployee(loginRequest);
        return ResponseEntity.ok(employeeResponse);
    }
    @PreAuthorize(("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')"))
    @GetMapping("/{employeeId}")
    public ResponseEntity<EmployeeDto> getEmployeeById(@PathVariable UUID employeeId) {
        EmployeeDto employeeDTO = employeeService.getEmployee(employeeId);
        return ResponseEntity.ok(employeeDTO);
    }
    @PreAuthorize(("!hasRole('ROLE_USER')"))
    @PatchMapping("/change-employee-name/{employeeId}")
    public ResponseEntity<String> changeUsername(@Valid @RequestBody EmployeeNameRequest employeeNameRequest, @PathVariable UUID employeeId) {
        employeeService.changeUsername(employeeNameRequest, employeeId);
        return ResponseEntity.ok(employeeNameRequest.getEmployeeName());
    }

    @PreAuthorize(("!hasRole('ROLE_USER')"))
    @DeleteMapping("/delete/{employeeId}")
    public ResponseEntity<String> deleteEmployee(@PathVariable UUID employeeId) {
        employeeService.deleteEmployee(employeeId);
        return ResponseEntity.ok("Employee deleted successfully");
    }

    @PreAuthorize(("!hasRole('ROLE_USER')"))
    @PutMapping("/change-password/{employeeId}")
    public ResponseEntity<String> changePassword( @Valid @RequestBody PasswordChangeRequest passwordChangeRequest, @PathVariable UUID employeeId) {
        employeeService.changePassword(passwordChangeRequest, employeeId);
        return ResponseEntity.ok("Password changed successfully");
    }

    @PreAuthorize(("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')"))
    @PostMapping("create-employee/{restaurantId}")
    public ResponseEntity<String> createEmployeeByRestaurant(@Valid @RequestBody EmployeeCreateRequest employeeCreateRequest, @PathVariable UUID restaurantId) {
        String employeeName = employeeService.createEmployer(employeeCreateRequest, restaurantId);
        return ResponseEntity.ok("Employee created successfully with name: " + employeeName);
    }

    @PreAuthorize(("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')"))
    @GetMapping("/get-roles")
    public ResponseEntity<List<RoleDTOResponse>> getAllRoles() {
        List<RoleDTOResponse> roles = employeeService.getAllRoles();
        return ResponseEntity.ok(roles);
    }
    @PreAuthorize(("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')"))
    @DeleteMapping("/remove-roles/{employeeId}")
    public ResponseEntity<EmployeeDto> removeAllRolesFromEmployee(@PathVariable UUID employeeId, @Valid @RequestBody RoleRequest roleRemovalRequest) {
        EmployeeDto employeeDTO = employeeService.removeRolesFromEmployee(employeeId, roleRemovalRequest);
        return ResponseEntity.ok(employeeDTO);
    }

    @PreAuthorize(("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')"))
    @PostMapping("/add-roles/{employeeId}")
    public ResponseEntity<EmployeeDto> addRolesToEmployee(@PathVariable UUID employeeId, @Valid @RequestBody RoleRequest roleAddRequest) {
        EmployeeDto employeeDTO = employeeService.addRolesToEmployee(employeeId, roleAddRequest);
        return ResponseEntity.ok(employeeDTO);
    }

}
