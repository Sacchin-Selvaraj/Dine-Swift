package com.dineswift.restaurant_service.controller;


import com.dineswift.restaurant_service.payload.request.employee.*;
import com.dineswift.restaurant_service.payload.dto.EmployeeDto;
import com.dineswift.restaurant_service.payload.response.MessageResponse;
import com.dineswift.restaurant_service.payload.response.employee.EmployeeResponse;
import com.dineswift.restaurant_service.payload.response.employee.RoleDTOResponse;
import com.dineswift.restaurant_service.service.CustomPageDto;
import com.dineswift.restaurant_service.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/restaurant/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("/startup")
    public ResponseEntity<String> initialStartUp(){
        log.info("Request to start the restaurant Service");
        return ResponseEntity.ok("Restaurant Service Started");
    }

    @PostMapping("/sign-up")
    public ResponseEntity<Void> createEmployee(
            @Valid @RequestBody EmployeeCreateRequest employeeCreateRequest) {

        employeeService.createEmployee(employeeCreateRequest);

        return ResponseEntity.ok().build();
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
    public ResponseEntity<Void> changePassword( @Valid @RequestBody PasswordChangeRequest passwordChangeRequest) {
        employeeService.changePassword(passwordChangeRequest);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize(("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')"))
    @PostMapping("create-employee/{restaurantId}")
    public ResponseEntity<Void> createEmployeeByRestaurant(@Valid @RequestBody EmployeeCreateRequest employeeCreateRequest,
                                                           @PathVariable UUID restaurantId) {
        employeeService.createEmployer(employeeCreateRequest, restaurantId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/get-roles")
    public ResponseEntity<List<RoleDTOResponse>> getAllRoles() {
        List<RoleDTOResponse> roles = employeeService.getAllRoles();
        return ResponseEntity.ok(roles);
    }
    @PreAuthorize(("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')"))
    @DeleteMapping("/remove-roles/{employeeId}")
    public ResponseEntity<Void> removeAllRolesFromEmployee(@PathVariable UUID employeeId,
                                                           @Valid @RequestBody RoleRequest roleRemovalRequest) {
        employeeService.removeRolesFromEmployee(employeeId, roleRemovalRequest);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize(("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')"))
    @PostMapping("/add-roles/{employeeId}")
    public ResponseEntity<Void> addRolesToEmployee(@PathVariable UUID employeeId,
                                                   @Valid @RequestBody RoleRequest roleAddRequest) {
        employeeService.addRolesToEmployee(employeeId, roleAddRequest);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @GetMapping("/get-all-employees-list")
    public ResponseEntity<List<EmployeeDto>> getAllEmployees() {
        List<EmployeeDto> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @GetMapping("/get-all-employees")
    public ResponseEntity<CustomPageDto<EmployeeDto>> getEmployeesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        CustomPageDto<EmployeeDto> employees = employeeService.getEmployeesPaginated(page, size);
        return ResponseEntity.ok(employees);
    }

    @PreAuthorize("!hasRole('ROLE_USER')")
    @DeleteMapping("/delete-account")
    public ResponseEntity<MessageResponse> deleteOwnAccount() {
        employeeService.deleteOwnAccount();
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("!hasRole('ROLE_USER')")
    @GetMapping("/current-employee")
    public ResponseEntity<EmployeeDto> getCurrentEmployee() {
        EmployeeDto employeeDTO = employeeService.getCurrentEmployee();
        return ResponseEntity.ok(employeeDTO);
    }

}
