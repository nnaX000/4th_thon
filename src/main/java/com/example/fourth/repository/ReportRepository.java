package com.example.fourth.repository;

import com.example.fourth.entity.Report;
import com.example.fourth.entity.Result;
import com.example.fourth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report>findByFolder_Id(long folderId);

    int countByUserId(int userId);

    @Query("SELECT COUNT(r) FROM Report r WHERE r.user.id = :userId AND r.createdAt < :before")
    int countByUserIdAndCreatedAtBefore(@Param("userId") int userId, @Param("before") LocalDateTime before);


    @Query("SELECT r FROM Report r " +
            "WHERE r.user.id = :userId " +
            "AND r.createdAt BETWEEN :start AND :end " +
            "ORDER BY r.createdAt DESC")
    List<Report> findTodayReports(@Param("userId") int userId,
                                  @Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end);
}