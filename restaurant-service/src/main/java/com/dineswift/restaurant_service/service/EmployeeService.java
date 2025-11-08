package com.dineswift.restaurant_service.service;

import com.dineswift.restaurant_service.exception.EmployeeException;
import com.dineswift.restaurant_service.exception.RestaurantException;
import com.dineswift.restaurant_service.exception.RoleException;
import com.dineswift.restaurant_service.mapper.EmployeeMapper;
import com.dineswift.restaurant_service.model.Employee;
import com.dineswift.restaurant_service.model.Restaurant;
import com.dineswift.restaurant_service.model.Role;
import com.dineswift.restaurant_service.model.RoleName;
import com.dineswift.restaurant_service.payload.request.employee.*;
import com.dineswift.restaurant_service.payload.dto.EmployeeDto;
import com.dineswift.restaurant_service.payload.response.employee.EmployeeResponse;
import com.dineswift.restaurant_service.payload.response.employee.RoleDTOResponse;
import com.dineswift.restaurant_service.repository.EmployeeRepository;
import com.dineswift.restaurant_service.repository.RestaurantRepository;
import com.dineswift.restaurant_service.repository.RoleRepository;
import com.dineswift.restaurant_service.security.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final EmployeeMapper employeeMapper;
    private final RestaurantRepository restaurantRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;


    public EmployeeDto createEmployee(EmployeeCreateRequest employeeCreateRequest) {
        log.info("Creating new employee with name: {}", employeeCreateRequest.getEmployeeName());
        verifyUser(employeeCreateRequest);

        Employee employee = employeeMapper.convertToEntity(employeeCreateRequest);

        Role role = roleRepository.findByRoleName(RoleName.ROLE_ADMIN).orElseThrow(() -> new EmployeeException("Role not found"));
        employee.setRoles(Set.of(role));
        Employee savedEmployee = employeeRepository.save(employee);
        log.info("Employee created successfully with id: {}", savedEmployee.getEmployeeId());
        return employeeMapper.toDTO(savedEmployee);

    }

    private void verifyUser(EmployeeCreateRequest employeeCreateRequest) {
        if (employeeCreateRequest == null) {
            throw new EmployeeException("Employee Details not found");
        }
        log.info("Verifying employee details for name: {}", employeeCreateRequest.getEmployeeName());
        if (employeeRepository.existsByEmployeeName(employeeCreateRequest.getEmployeeName())) {
            log.error("Employee name already taken: {}", employeeCreateRequest.getEmployeeName());
            throw new EmployeeException("Employee name already taken!");
        }
        if (employeeRepository.existsByEmail(employeeCreateRequest.getEmail())) {
            log.error("Email already registered: {}", employeeCreateRequest.getEmail());
            throw new EmployeeException("Email already registered!");
        }
    }

    public EmployeeDto getEmployee(UUID employeeId) {
        if (employeeId == null) {
            throw new EmployeeException("Invalid request with employee id");
        }
        log.info("Fetching employee details for id: {}", employeeId);
        Employee employee=employeeRepository.findByIdAndIsActive(employeeId).orElseThrow(() -> new EmployeeException("Employee not found"));
        return employeeMapper.toDTO(employee);
    }

    public void changeUsername(EmployeeNameRequest employeeNameRequest) {
        log.info("Getting Employee Id from security context for username change");
        UUID employeeId = authService.getAuthenticatedId();
        if (employeeNameRequest == null || employeeId == null) {
            throw new EmployeeException("Invalid request or Employee Id is null");
        }
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> new EmployeeException("Employee not found"));
        if (employeeRepository.existsByEmployeeName(employeeNameRequest.getEmployeeName())) {
            log.error("Employee name already exists: {}", employeeNameRequest.getEmployeeName());
            throw new EmployeeException("Employee name already taken!");
        }
        employee.setEmployeeName(employeeNameRequest.getEmployeeName());
        employeeRepository.save(employee);
    }

    public void deleteEmployee(UUID employeeId) {
        if (employeeId == null) {
            throw new EmployeeException("Invalid request with employee id");
        }
        Employee employee = employeeRepository.findByIdAndIsActive(employeeId).orElseThrow(() -> new EmployeeException("Employee not found or already inactive"));
        isAdmin(employee);
        log.info("Deleting employee with id: {}", employeeId);
        employee.setEmployeeIsActive(false);
        // need to sent it to auth service to disable the user
        employeeRepository.save(employee);

    }

    private void isAdmin(Employee employee) {
        Set<Role> roles=employee.getRoles();
        for (Role role:roles){
            if (role.getRoleName().equals(RoleName.ROLE_ADMIN)){
                if (employee.getRestaurant()!=null && employee.getRestaurant().getIsActive()){
                    log.error("Cannot delete admin of an active restaurant: {}", employee.getEmployeeId());
                    throw new EmployeeException("Cannot delete admin of an active restaurant");
                }else {
                    return;
                }
            }
        }
    }

    public void changePassword(PasswordChangeRequest passwordChangeRequest) {
        UUID employeeId = authService.getAuthenticatedId();
        if (employeeId == null || passwordChangeRequest == null) {
            throw new EmployeeException("Invalid request");
        }
        if (!passwordChangeRequest.getNewPassword().equals(passwordChangeRequest.getConfirmNewPassword())){
            log.error("New password and confirm password do not match for employee id: {}", employeeId);
            throw new EmployeeException("New password and confirm password do not match");
        }
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> new EmployeeException("Employee not found with provided Id"));
        if (!employee.getPassword().equals(passwordChangeRequest.getOldPassword())) {
            log.error("Old password is incorrect for employee id: {}", employeeId);
            throw new EmployeeException("Old password is incorrect");
        }
        log.info("Encoding new password for employee id: {}", employeeId);
        employee.setPassword(passwordEncoder.encode(passwordChangeRequest.getNewPassword()));
        employeeRepository.save(employee);
    }

    public String createEmployer(EmployeeCreateRequest employeeCreateRequest, UUID restaurantId) {
        if (restaurantId == null) {
            throw new EmployeeException("Invalid Restaurant id or Restaurant Id not found");
        }
        verifyUser(employeeCreateRequest);
        log.info("Creating employee for restaurant id: {}", restaurantId);
        Employee employee = employeeMapper.convertToEntity(employeeCreateRequest);

        Restaurant restaurant=restaurantRepository.findById(restaurantId).orElseThrow(()-> new RestaurantException("Restaurant not found with id: " + restaurantId));
        log.info("Setting restaurant admin for restaurant id: {}", restaurantId);
        employee.setRestaurant(restaurant);
        employeeRepository.save(employee);

        return employee.getEmployeeName();
    }

    public EmployeeDto removeRolesFromEmployee(UUID employeeId, RoleRequest roleRemovalRequest) {
        if (employeeId == null || roleRemovalRequest == null || roleRemovalRequest.getRoleIds().isEmpty()) {
            log.info("Invalid request to remove roles from employee");
            throw new EmployeeException("Invalid request to remove roles from employee");
        }
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> new EmployeeException("Employee not found"));

        Set<Role> updatedRoles=employee.getRoles().stream().filter(
                role -> !roleRemovalRequest.getRoleIds().contains(role.getRoleId())).collect(Collectors.toSet());

        employee.setRoles(updatedRoles);
        employee = employeeRepository.save(employee);
        log.info("Roles removed successfully from employee id: {}", employeeId);
        return employeeMapper.toDTO(employee);
    }

    public List<RoleDTOResponse> getAllRoles() {
        List<Role> roles=roleRepository.findAll();
        if (roles.isEmpty()){
            log.error("No roles found in the system");
            throw new RoleException("No roles found");
        }
        return roles.stream().map(employeeMapper::toRoleDTO).collect(Collectors.toList());
    }


    public EmployeeDto addRolesToEmployee(UUID employeeId, RoleRequest roleAddRequest) {
        if (employeeId==null || roleAddRequest==null || roleAddRequest.getRoleIds().isEmpty()){
            log.error("Invalid request to add roles to employee");
            throw new EmployeeException("Invalid request to add roles to employee");
        }
        Employee employee=employeeRepository.findById(employeeId).orElseThrow(()-> new EmployeeException("Employee not found"));
        Set<Role> rolesToAdd= new HashSet<>(roleRepository.findAllById(roleAddRequest.getRoleIds()));
        if (rolesToAdd.isEmpty()) {
            log.error("No valid roles found to add to employee id: {}", employeeId);
            throw new RoleException("No valid roles found to add");
        }
        employee.getRoles().addAll(rolesToAdd);
        employee=employeeRepository.save(employee);
        return employeeMapper.toDTO(employee);
    }

    public EmployeeResponse authenticateEmployee(LoginRequest loginRequest) {
        log.info("Authenticating employee with Email: {}", loginRequest.getEmail());
        Employee registeredEmployee = employeeRepository.findByEmailAndIsActive(loginRequest.getEmail())
                .orElseThrow(() -> new EmployeeException("Invalid credentials provided"));

        if (!passwordEncoder.matches(loginRequest.getPassword(),registeredEmployee.getPassword())){
            log.error("Password mismatch for employee with Email: {}", loginRequest.getEmail());
            throw new EmployeeException("Invalid credentials provided");
        }

        log.info("Employee authenticated successfully with Email: {}", loginRequest.getEmail());
        return employeeMapper.toEmployeeResponse(registeredEmployee);
    }
}
