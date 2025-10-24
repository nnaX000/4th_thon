package com.example.fourth.service;

import com.example.fourth.entity.Report;
import com.example.fourth.entity.Result;
import com.example.fourth.repository.ResultRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ResultRepository resultRepository;

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

        // 통합 리포트 처리 (option = TOTAL)
        if (options == Report.ReportOption.TOTAL) {
            String joinedTitle = String.join(" / ", topicTitles);
            response.put("title", joinedTitle);
        }

        response.put("results", topicResults);

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