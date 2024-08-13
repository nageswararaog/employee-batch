package com.imaginnovate.demo.controller;

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.imaginnovate.demo.dto.EmployeeDTO;
import com.imaginnovate.demo.entity.Employee;
import com.imaginnovate.demo.entity.PhoneNumber;
import com.imaginnovate.demo.service.EmployeeService;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<EmployeeDTO> getEmployee(@RequestParam(required = false) Integer id,
                                                        @RequestParam(required = false) String email) {
        Optional<Employee> employee = employeeService.getEmployeeByIdOrEmail(id, email);
        if (employee.isPresent()) {
        	EmployeeDTO response = convertToDTO(employee.get());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    private EmployeeDTO convertToDTO(Employee employee) {
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setEmployeeId(employee.getEmployeeId());
        employeeDTO.setFirstName(employee.getFirstName());
        employeeDTO.setLastName(employee.getLastName());
        employeeDTO.setEmail(employee.getEmail());
        employeeDTO.setDoj(employee.getDoj());
        employeeDTO.setSalary(employee.getSalary());
        employeeDTO.setPhoneNumber(employee.getPhoneNumbers().stream()
                .map(PhoneNumber::getPhoneNumber)
                .collect(Collectors.toList()));
        return employeeDTO;
    }
}
