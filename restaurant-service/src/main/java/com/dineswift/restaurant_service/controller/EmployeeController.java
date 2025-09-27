package com.dineswift.restaurant_service.controller;


import com.dineswift.restaurant_service.payload.request.EmployeeCreateRequest;
import com.dineswift.restaurant_service.payload.dto.EmployeeDTO;
import com.dineswift.restaurant_service.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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




}
