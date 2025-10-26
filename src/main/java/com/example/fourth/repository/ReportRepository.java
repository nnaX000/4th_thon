package com.example.fourth.repository;

import com.example.fourth.entity.Report;
import com.example.fourth.entity.Result;
import com.example.fourth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {
}