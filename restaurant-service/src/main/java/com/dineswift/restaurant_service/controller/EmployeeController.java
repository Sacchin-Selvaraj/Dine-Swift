package com.dineswift.restaurant_service.controller;


import com.dineswift.restaurant_service.payload.request.employee.*;
import com.dineswift.restaurant_service.payload.dto.EmployeeDto;
import com.dineswift.restaurant_service.payload.response.MessageResponse;
import com.dineswift.restaurant_service.payload.response.employee.EmployeeResponse;
import com.dineswift.restaurant_service.payload.response.employee.RoleDTOResponse;
import com.dineswift.restaurant_service.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    public ResponseEntity<MessageResponse> createEmployee(@Valid @RequestBody EmployeeCreateRequest employeeCreateRequest) {
        employeeService.createEmployee(employeeCreateRequest);
        return ResponseEntity.ok(MessageResponse.builder().message("New Employee Details created Successfully").build());
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
    @PatchMapping("/change-employee-name")
    public ResponseEntity<Void> changeUsername(@Valid @RequestBody EmployeeNameRequest employeeNameRequest) {
        employeeService.changeUsername(employeeNameRequest);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize(("!hasRole('ROLE_USER')"))
    @DeleteMapping("/delete-employee/{employeeId}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable UUID employeeId) {
        employeeService.deleteEmployee(employeeId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize(("!hasRole('ROLE_USER')"))
    @PutMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword( @Valid @RequestBody PasswordChangeRequest passwordChangeRequest) {
        employeeService.changePassword(passwordChangeRequest);
        return ResponseEntity.ok(MessageResponse.builder().message("Password changed successfully").build());
    }

    @PreAuthorize(("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')"))
    @PostMapping("create-employee/{restaurantId}")
    public ResponseEntity<MessageResponse> createEmployeeByRestaurant(@Valid @RequestBody EmployeeCreateRequest employeeCreateRequest, @PathVariable UUID restaurantId) {
        String employeeName = employeeService.createEmployer(employeeCreateRequest, restaurantId);
        return ResponseEntity.ok(MessageResponse.builder().message("Employee created successfully with name: " + employeeName).build());
    }

    @PreAuthorize(("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')"))
    @GetMapping("/get-roles")
    public ResponseEntity<List<RoleDTOResponse>> getAllRoles() {
        List<RoleDTOResponse> roles = employeeService.getAllRoles();
        return ResponseEntity.ok(roles);
    }
    @PreAuthorize(("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')"))
    @DeleteMapping("/remove-roles/{employeeId}")
    public ResponseEntity<MessageResponse> removeAllRolesFromEmployee(@PathVariable UUID employeeId, @Valid @RequestBody RoleRequest roleRemovalRequest) {
        employeeService.removeRolesFromEmployee(employeeId, roleRemovalRequest);
        return ResponseEntity.ok(MessageResponse.builder().message("Removed Roles from the Employee Successfully").build());
    }

    @PreAuthorize(("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')"))
    @PostMapping("/add-roles/{employeeId}")
    public ResponseEntity<MessageResponse> addRolesToEmployee(@PathVariable UUID employeeId, @Valid @RequestBody RoleRequest roleAddRequest) {
        employeeService.addRolesToEmployee(employeeId, roleAddRequest);
        return ResponseEntity.ok(MessageResponse.builder().message("Added roles to the Employee").build());
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @GetMapping("/get-all-employees-list")
    public ResponseEntity<List<EmployeeDto>> getAllEmployees() {
        List<EmployeeDto> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/get-all-employees")
    public ResponseEntity<Page<EmployeeDto>> getEmployeesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<EmployeeDto> employees = employeeService.getEmployeesPaginated(page, size);
        return ResponseEntity.ok(employees);
    }

    @PreAuthorize("!hasRole('ROLE_USER')")
    @DeleteMapping("/delete-account")
    public ResponseEntity<MessageResponse> deleteOwnAccount() {
        employeeService.deleteOwnAccount();
        return ResponseEntity.ok(MessageResponse.builder().message("Your account has been deleted successfully").build());
    }

    @PreAuthorize("!hasRole('ROLE_USER')")
    @GetMapping("/current-employee")
    public ResponseEntity<EmployeeDto> getCurrentEmployee() {
        EmployeeDto employeeDTO = employeeService.getCurrentEmployee();
        return ResponseEntity.ok(employeeDTO);
    }

}
