package com.example.fourth.service;

import com.example.fourth.dto.MyPageResponse;
import com.example.fourth.entity.User;
import com.example.fourth.repository.EntranceRepository;
import com.example.fourth.repository.ReportRepository;
import com.example.fourth.repository.ResultRepository;
import com.example.fourth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final ResultRepository resultRepository;
    private final ReportRepository reportRepository;
    private final EntranceRepository entranceRepository;
    private final UserRepository userRepository;

    public MyPageResponse getMyPage(int userId) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);

        // 닉네임
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 오늘까지 총합
        int newConceptCount = resultRepository.sumNewConceptByUserId(userId);
        int fixedConceptCount = resultRepository.sumFixedConceptByUserId(userId);
        int reportCount = reportRepository.countByUserId(userId);
        int totalSessionCount = entranceRepository.countByUserId(userId);

        // 어제까지 총합
        int newConceptUntilYesterday = resultRepository.sumNewConceptByUserIdAndCreatedAtBefore(userId, todayStart);
        int fixedConceptUntilYesterday = resultRepository.sumFixedConceptByUserIdAndCreatedAtBefore(userId, todayStart);
        int reportUntilYesterday = reportRepository.countByUserIdAndCreatedAtBefore(userId, todayStart);

        // 증감
        int newConceptDiff = newConceptCount - newConceptUntilYesterday;
        int fixedConceptDiff = fixedConceptCount - fixedConceptUntilYesterday;
        int reportDiff = reportCount - reportUntilYesterday;

        // 오늘의 리포트 리스트
        List<MyPageResponse.ReportSummary> todayReports = reportRepository
                .findTodayReports(userId, todayStart, todayEnd)
                .stream()
                .map(r -> new MyPageResponse.ReportSummary(r.getId(), r.getTitle()))
                .toList();

        // 응답
        MyPageResponse response = new MyPageResponse();
        response.setNickname(user.getNickname());
        response.setNewConceptCount(newConceptCount);
        response.setNewConceptDiff(newConceptDiff);
        response.setFixedConceptCount(fixedConceptCount);
        response.setFixedConceptDiff(fixedConceptDiff);
        response.setReportCount(reportCount);
        response.setReportDiff(reportDiff);
        response.setTotalSessionCount(totalSessionCount);
        response.setTodayReports(todayReports);
        return response;

    }

}
