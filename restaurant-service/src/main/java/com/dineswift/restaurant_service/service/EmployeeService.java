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
import com.dineswift.restaurant_service.specification.EmployeeSpecification;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final EmployeeMapper employeeMapper;
    private final RestaurantRepository restaurantRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final EmployeeSpecification employeeSpecification;


    @Transactional
    public void createEmployee(EmployeeCreateRequest employeeCreateRequest) {
        log.info("Creating new employee with name: {}", employeeCreateRequest.getEmployeeName());
        verifyUser(employeeCreateRequest);

        Employee employee = employeeMapper.convertToEntity(employeeCreateRequest);

        Role role = roleRepository.findByRoleName(RoleName.ROLE_ADMIN)
                .orElseThrow(() -> new EmployeeException("Role not found"));
        employee.setRoles(Set.of(role));

        Employee savedEmployee = employeeRepository.save(employee);

        log.info("Employee created successfully with id: {}", savedEmployee.getEmployeeId());
    }

    private void verifyUser(EmployeeCreateRequest employeeCreateRequest) {
        if (employeeCreateRequest == null) {
            throw new EmployeeException("Employee Details not found");
        }
        log.info("Verifying employee details for name: {}", employeeCreateRequest.getEmployeeName());
        if (employeeRepository.existsByEmployeeName(employeeCreateRequest.getEmployeeName())) {
            log.error("Employee name already taken");
            throw new EmployeeException("Employee name already taken!");
        }
        if (employeeRepository.existsByEmail(employeeCreateRequest.getEmail())) {
            log.error("Email already registered");
            throw new EmployeeException("Email already registered!");
        }
    }
    @Cacheable(
            value = "restaurant:employeeById",
            key = "#employeeId",
            unless = "#result == null"
    )
    @Transactional(readOnly = true)
    public EmployeeDto getEmployee(UUID employeeId) {
        if (employeeId == null) {
            throw new EmployeeException("Invalid request with employee id");
        }
        log.info("Fetching employee details for id: {}", employeeId);

        Employee employee=employeeRepository.findByIdWithRoles(employeeId)
                .orElseThrow(() -> new EmployeeException("Employee not found"));

        return employeeMapper.toDTO(employee);
    }

    @Caching(
            evict = {
                    @CacheEvict(
                            value = {"restaurant:employeeById"},
                            key = "@authService.getAuthenticatedId()"
                    ),
                    @CacheEvict(
                            value = {"restaurant:employeesPaginated"},
                            allEntries = true
                    )
            }
    )
    @Transactional
    public void changeUsername(EmployeeNameRequest employeeNameRequest) {
        log.info("Getting Employee Id from security context for username change");
        UUID employeeId = authService.getAuthenticatedId();

        if (employeeNameRequest == null || employeeId == null) {
            throw new EmployeeException("Invalid request or Employee Id is null");
        }

        if (employeeRepository.existsByEmployeeName(employeeNameRequest.getEmployeeName())) {
            log.error("Employee name already exists: {}", employeeNameRequest.getEmployeeName());
            throw new EmployeeException("Employee name already taken!");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeException("Employee not found"));


        employee.setEmployeeName(employeeNameRequest.getEmployeeName());
        employeeRepository.save(employee);
    }

    @Caching(
            evict = {
                    @CacheEvict(
                            value = {"restaurant:employeeById"},
                            key = "#employeeId"
                    ),
                    @CacheEvict(
                            value = {"restaurant:employeesPaginated"},
                            allEntries = true
                    )
            }
    )
    @Transactional
    public void deleteEmployee(UUID employeeId) {
        if (employeeId == null) {
            throw new EmployeeException("Invalid request with employee id");
        }

        Employee employee = employeeRepository.findByIdAndGetRestaurant(employeeId)
                .orElseThrow(() -> new EmployeeException("Employee not found or already inactive"));

        isAdmin(employee);
        log.info("Deleting employee with id: {}", employeeId);

        employee.deactivate();

        employeeRepository.save(employee);

    }

    private void isAdmin(Employee employee) {

        Set<Role> roles=employee.getRoles();
        boolean isAdmin = roles.stream()
                .anyMatch(role -> role.getRoleName().equals(RoleName.ROLE_ADMIN));

        if (isAdmin) {
            if (employee.getRestaurant() != null && employee.getRestaurant().getIsActive()) {
                log.error("Cannot delete admin of an active restaurant: {}", employee.getEmployeeId());
                throw new EmployeeException("Cannot delete admin of an active restaurant");
            }
        }

    }

    @Transactional
    public void changePassword(PasswordChangeRequest passwordChangeRequest) {

        UUID employeeId = authService.getAuthenticatedId();
        if (employeeId == null || passwordChangeRequest == null) {
            throw new EmployeeException("Invalid request");
        }

        if (!passwordChangeRequest.getNewPassword().equals(passwordChangeRequest.getConfirmNewPassword())){
            log.error("New password and confirm password do not match for employee id: {}", employeeId);
            throw new EmployeeException("New password and confirm password do not match");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeException("Employee not found with provided Id"));

        if (!passwordEncoder.matches(passwordChangeRequest.getOldPassword(), employee.getPassword())) {
            log.error("Old password is incorrect for employee id: {}", employeeId);
            throw new EmployeeException("Old password is incorrect");
        }

        log.info("Encoding new password for employee id: {}", employeeId);

        employee.setPassword(passwordEncoder.encode(passwordChangeRequest.getNewPassword()));

        employeeRepository.save(employee);
    }

    @CacheEvict(
            value = {"restaurant:employeesPaginated"},
            allEntries = true
    )
    @Transactional
    public void createEmployer(EmployeeCreateRequest employeeCreateRequest, UUID restaurantId) {
        if (restaurantId == null || !restaurantRepository.existsById(restaurantId)) {
            throw new RestaurantException("Invalid Restaurant id or Restaurant Id not found");
        }
        verifyUser(employeeCreateRequest);
        log.info("Creating employee for restaurant id: {}", restaurantId);
        Employee employee = employeeMapper.convertToEntity(employeeCreateRequest);

        Restaurant restaurant= restaurantRepository.getReferenceById(restaurantId);

        log.info("Setting restaurant admin for restaurant id: {}", restaurantId);
        employee.setRestaurant(restaurant);
        employeeRepository.save(employee);

    }

    @Caching(
            evict = {
                    @CacheEvict(
                            value = {"restaurant:employeeById"},
                            key = "#employeeId"
                    ),
                    @CacheEvict(
                            value = {"restaurant:employeesPaginated"},
                            allEntries = true
                    )
            }
    )
    @Transactional
    public void removeRolesFromEmployee(UUID employeeId, RoleRequest roleRemovalRequest) {

        if (employeeId == null || roleRemovalRequest == null || roleRemovalRequest.getRoleIds().isEmpty()) {
            log.info("Invalid request to remove roles from employee");
            throw new EmployeeException("Invalid request to remove roles from employee");
        }

        Employee employee = employeeRepository.findByIdAndIsActive(employeeId)
                .orElseThrow(() -> new EmployeeException("Employee not found"));

        Set<Role> updatedRoles=employee.getRoles().stream().filter(
                role -> !roleRemovalRequest.getRoleIds().contains(role.getRoleId())).collect(Collectors.toSet());

        employee.setRoles(updatedRoles);
        employeeRepository.save(employee);
        log.info("Roles removed successfully from employee id: {}", employeeId);
    }

    @Cacheable(
            value = "restaurant:employee:allRoles",
            key = "'allRoles'",
            unless = "#result == null || #result.isEmpty()"
    )
    @Transactional(readOnly = true)
    public List<RoleDTOResponse> getAllRoles() {

        List<Role> roles=roleRepository.findAll();

        return roles.stream().map(employeeMapper::toRoleDTO)
                .collect(Collectors.toList());
    }

    @Caching(
            evict = {
                    @CacheEvict(
                            value = {"restaurant:employeeById"},
                            key = "#employeeId"
                    ),
                    @CacheEvict(
                            value = {"restaurant:employeesPaginated"},
                            allEntries = true
                    )
            }
    )
    @Transactional
    public void addRolesToEmployee(UUID employeeId, RoleRequest roleAddRequest) {
        if (employeeId==null || roleAddRequest==null || roleAddRequest.getRoleIds().isEmpty()){
            log.error("Invalid request to add roles to employee");
            throw new EmployeeException("Invalid request to add roles to employee");
        }
        Employee employee=employeeRepository.findByIdAndIsActive(employeeId)
                .orElseThrow(()-> new EmployeeException("Employee not found"));

        Set<Role> rolesToAdd= new HashSet<>(roleRepository.findAllById(roleAddRequest.getRoleIds()));

        if (rolesToAdd.isEmpty()) {
            log.error("No valid roles found to add to employee id: {}", employeeId);
            throw new RoleException("No valid roles found to add");
        }
        employee.getRoles().addAll(rolesToAdd);
        employeeRepository.save(employee);

        log.info("Roles added successfully to employee id: {}", employeeId);
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

    @Transactional(readOnly = true)
    public List<EmployeeDto> getAllEmployees() {
        log.info("Fetching all active employees");
        UUID employeeId = authService.getAuthenticatedId();

        UUID restaurantId = employeeRepository.findRestaurantIdByEmployeeId(employeeId);

        List<Employee> employees = employeeRepository.findAllByRestaurant_RestaurantId(restaurantId);

        log.info("Employees fetched successfully for restaurant id: {}", restaurantId);
        return employees.stream().map(employeeMapper::toDTO).collect(Collectors.toList());
    }


    @Cacheable(
            value = "restaurant:employeesPaginated",
            key = "'page:' + #page + ':size:' + #size + ':employeeId:' + @authService.getAuthenticatedId()",
            unless = "#result == null || #result.isEmpty()"
    )
    @Transactional(readOnly = true)
    public CustomPageDto<EmployeeDto> getEmployeesPaginated(int page, int size) {

        log.info("Fetching paginated employees: page {}, size {}", page, size);
        UUID employeeId = authService.getAuthenticatedId();

        UUID restaurantId = employeeRepository.findRestaurantIdByEmployeeId(employeeId);

        log.info("Logged in employee and check the restaurant ");
        if (restaurantId==null){
            log.error("Restaurant not found for employee id: {}", employeeId);
            throw new RestaurantException("Restaurant not found for the employee");
        }

        Pageable pageable = PageRequest.of(page, size);

        Specification<Employee> spec = employeeSpecification.hasRestaurantId(restaurantId);

        Page<Employee> employees = employeeRepository.findAll(spec,pageable);

        log.info("Paginated employees fetched successfully for restaurant id: {}", restaurantId);
        return new CustomPageDto<>(employees.map(employeeMapper::toDTO));
    }

    @Caching(
            evict = {
                    @CacheEvict(
                            value = {"restaurant:employeeById"},
                            key = "@authService.getAuthenticatedId()"
                    ),
                    @CacheEvict(
                            value = {"restaurant:employeesPaginated"},
                            allEntries = true
                    )
            }
    )
    @Transactional
    public void deleteOwnAccount() {
        log.info("Getting Employee Id from security context for account deletion");
        UUID employeeId = authService.getAuthenticatedId();

        if (employeeId == null) {
            throw new EmployeeException("Employee Id is null");
        }

        Employee employee = employeeRepository.findByIdAndIsActive(employeeId)
                .orElseThrow(() -> new EmployeeException("Employee not found"));

        isAdmin(employee);
        log.info("Deleting own account for employee id: {}", employeeId);

        employee.deactivate();

        employeeRepository.save(employee);
    }

    @Cacheable(
            value = "restaurant:employeeById",
            key = "@authService.getAuthenticatedId()",
            unless = "#result == null"
    )
    @Transactional(readOnly = true)
    public EmployeeDto getCurrentEmployee() {
        log.info("Getting current authenticated employee details");
        UUID employeeId = authService.getAuthenticatedId();
        if (employeeId == null) {
            throw new EmployeeException("Employee Id is null");
        }
        Employee employee = employeeRepository.findByIdAndIsActive(employeeId)
                .orElseThrow(() -> new EmployeeException("Employee not found"));

        log.info("Current employee details fetched successfully for employee id: {}", employeeId);
        return employeeMapper.toDTO(employee);
    }
}
