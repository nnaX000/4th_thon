package com.example.fourth.repository;

import com.example.fourth.entity.Result;
import com.example.fourth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ResultRepository extends JpaRepository<Result, Long> {
    List<Result> findByEntranceIdAndUserId(Long entranceId, Long userId);

    // 유저별 총 newConcept 합계
    @Query("SELECT COALESCE(SUM(r.newConcept), 0) FROM Result r WHERE r.user.id = :userId")
    int sumNewConceptByUserId(@Param("userId") int userId);

    // 유저별 총 fixedConcept 합계
    @Query("SELECT COALESCE(SUM(r.redirectConcept), 0) FROM Result r WHERE r.user.id = :userId")
    int sumFixedConceptByUserId(@Param("userId") int userId);

    // 날짜 기준
    @Query("SELECT COALESCE(SUM(r.newConcept), 0) FROM Result r WHERE r.user.id = :userId AND r.createdAt < :before")
    int sumNewConceptByUserIdAndCreatedAtBefore(@Param("userId") int userId,
                                     @Param("before") LocalDateTime before);

    @Query("SELECT COALESCE(SUM(r.redirectConcept), 0) FROM Result r WHERE r.user.id = :userId AND r.createdAt < :before")
    int sumFixedConceptByUserIdAndCreatedAtBefore(@Param("userId") int userId,
                                       @Param("before") LocalDateTime before);


}
