package com.imaginnovate.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.imaginnovate.demo.entity.PhoneNumber;

public interface PhoneNumberRepository extends JpaRepository<PhoneNumber, Integer> {
    List<PhoneNumber> findByEmployee_EmployeeId(Integer employeeId);
}