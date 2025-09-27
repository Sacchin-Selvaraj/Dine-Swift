package com.dineswift.restaurant_service.mapper;

import com.dineswift.restaurant_service.model.entites.Employee;
import com.dineswift.restaurant_service.payload.dto.EmployeeDTO;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmployeeMapper {

    private final ModelMapper mapper;

    public EmployeeDTO toDTO(Employee employee){
        return mapper.map(employee, EmployeeDTO.class);
    }
}
