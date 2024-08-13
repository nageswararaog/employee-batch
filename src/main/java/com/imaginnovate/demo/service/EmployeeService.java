package com.imaginnovate.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.imaginnovate.demo.entity.Employee;
import com.imaginnovate.demo.entity.PhoneNumber;
import com.imaginnovate.demo.repository.EmployeeRepository;
import com.imaginnovate.demo.repository.PhoneNumberRepository;

@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PhoneNumberRepository phoneNumberRepository;

    public Optional<Employee> getEmployeeByIdOrEmail(Integer id, String email) {
        if (id != null) {
            return employeeRepository.findById(id);
        } else if (email != null) {
            return employeeRepository.findByEmail(email);
        }
        return Optional.empty();
    }

    public List<PhoneNumber> getPhoneNumbersByEmployeeId(Integer employeeId) {
        return phoneNumberRepository.findByEmployee_EmployeeId(employeeId);
    }
    
    
}
