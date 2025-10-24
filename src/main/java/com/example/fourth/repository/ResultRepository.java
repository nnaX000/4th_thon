package com.example.fourth.repository;

import com.example.fourth.entity.Result;
import com.example.fourth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResultRepository extends JpaRepository<Result, Long> {

}
