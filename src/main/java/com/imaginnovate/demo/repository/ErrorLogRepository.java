package com.imaginnovate.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.imaginnovate.demo.entity.ErrorLog;

public interface ErrorLogRepository extends JpaRepository<ErrorLog, Integer> { }