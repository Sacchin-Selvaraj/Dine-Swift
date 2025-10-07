package com.dineswift.restaurant_service.mapper;

import com.dineswift.restaurant_service.exception.RoleException;
import com.dineswift.restaurant_service.model.Employee;
import com.dineswift.restaurant_service.model.Role;
import com.dineswift.restaurant_service.model.RoleName;
import com.dineswift.restaurant_service.payload.dto.EmployeeDTO;
import com.dineswift.restaurant_service.payload.request.employee.EmployeeCreateRequest;
import com.dineswift.restaurant_service.payload.response.employee.RoleDTOResponse;
import com.dineswift.restaurant_service.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EmployeeMapper {

    private final ModelMapper mapper;
    private final RoleRepository roleRepository;

    public EmployeeDTO toDTO(Employee employee){
        EmployeeDTO employeeDTO=mapper.map(employee, EmployeeDTO.class);
        employeeDTO.setRoles(employee.getRoles().stream()
                .map(this::toRoleDTO).collect(Collectors.toSet()));

        return employeeDTO;
    }

    public Employee convertToEntity(EmployeeCreateRequest employeeCreateRequest) {
        Employee employee = new Employee();
        if (employeeCreateRequest.getEmployeeName() != null)
            employee.setEmployeeName(employeeCreateRequest.getEmployeeName());
        if (employeeCreateRequest.getPassword() != null)
            // need to encode the password before setting it
            employee.setPassword(employeeCreateRequest.getPassword());
        if (employeeCreateRequest.getEmail() != null)
            employee.setEmail(employeeCreateRequest.getEmail());
        if (employeeCreateRequest.getPhoneNumber() != null)
            employee.setPhoneNumber(employeeCreateRequest.getPhoneNumber());
        employee.setEmployeeIsActive(true);
        if (employeeCreateRequest.getRoles()!=null){
            employee.setRoles(getRoles(employeeCreateRequest.getRoles()));
        }
        return employee;
    }

    public Set<Role> getRoles(Set<RoleRequest> roles) {
        Set<Role> roleSet = new HashSet<>();
        for (RoleRequest tempRole : roles) {
            try {
                RoleName roleName = RoleName.fromDisplayName(tempRole.getRoleName());
                Role role = roleRepository.findByRoleName(roleName)
                        .orElseThrow(() -> new RoleException("Role not found: " + tempRole.getRoleName()));
                roleSet.add(role);
            } catch (IllegalArgumentException e) {
                throw new RoleException ("Invalid role: " + tempRole.getRoleName());
            }
        }
        return roleSet;
    }

    public RoleDTOResponse toRoleDTO(Role role) {
        RoleDTOResponse roleDTOResponse=new RoleDTOResponse();
        roleDTOResponse.setRoleId(role.getRoleId());
        roleDTOResponse.setRoleName(role.getRoleName());
        return roleDTOResponse;
    }
}
