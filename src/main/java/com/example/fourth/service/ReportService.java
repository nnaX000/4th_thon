package com.example.fourth.service;

import com.example.fourth.entity.Report;
import com.example.fourth.entity.Result;
import com.example.fourth.repository.EntranceRepository;
import com.example.fourth.repository.ReportRepository;
import com.example.fourth.repository.ResultRepository;
import com.example.fourth.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ResultRepository resultRepository;
    private final ReportRepository reportRepository;
    private final EntranceRepository entranceRepository;
    private final UserRepository userRepository;

    public Map<String, Object> generateReport(Long entranceId, Long userId, Report.ReportOption options) {
        List<Result> results = resultRepository.findByEntranceIdAndUserId(entranceId, userId);

        if (results.isEmpty()) {
            throw new IllegalArgumentException("해당 entranceId와 userId에 대한 결과가 없습니다.");
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("entranceId", entranceId);
        response.put("userId", userId);

        // 주제별로 결과 구성
        Map<String, Object> topicResults = new LinkedHashMap<>();
        List<String> topicTitles = new ArrayList<>();

        for (Result result : results) {
            topicTitles.add(result.getTopic());

            Map<String, Object> topicData = new LinkedHashMap<>();
            topicData.put("topic", result.getTopic());
            topicData.put("newConceptCount", result.getNewConcept());
            topicData.put("redirectConceptCount", result.getRedirectConcept());
            topicData.put("newConcept", parseJson(result.getNewCcContent()));
            topicData.put("redirectConcept", parseJson(result.getRedirectCcContent()));
            topicData.put("reference", parseJson(result.getReference()));

            topicResults.put(result.getTopic(), topicData);
        }

        String joinedTitle = String.join(" / ", topicTitles);

        // 통합 리포트 처리 (option = TOTAL)
        if (options == Report.ReportOption.TOTAL) {
            response.put("title", joinedTitle);
        } else if (options == Report.ReportOption.TOPIC && !topicTitles.isEmpty()) {
            response.put("title", topicTitles.get(0));
        }

        response.put("results", topicResults);

        // Fetch Entrance and User
        var entranceOpt = entranceRepository.findById(entranceId);
        var userOpt = userRepository.findById(Math.toIntExact(userId));
        if (entranceOpt.isEmpty() || userOpt.isEmpty()) {
            throw new IllegalArgumentException("Entrance 또는 User를 찾을 수 없습니다.");
        }

        try {
            String contentJson = new ObjectMapper().writeValueAsString(response);
            Report report = Report.builder()
                    .entrance(entranceOpt.get())
                    .user(userOpt.get())
                    .title(options == Report.ReportOption.TOTAL ? joinedTitle : (topicTitles.isEmpty() ? "" : topicTitles.get(0)))
                    .options(options)
                    .content(contentJson)
                    .createdAt(LocalDateTime.now())
                    .build();

            reportRepository.save(report);
        } catch (Exception e) {
            throw new RuntimeException("리포트 저장 중 에러 발생", e);
        }

        return response;
    }

    private Object parseJson(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return new ObjectMapper().readValue(json, Object.class);
        } catch (Exception e) {
            return Map.of("error", "JSON 파싱 실패", "raw", json);
        }
    }
}