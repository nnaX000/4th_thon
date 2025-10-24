package com.example.fourth.service;

import com.example.fourth.entity.Result;
import com.example.fourth.repository.ResultRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ResultService {

    private final ResultRepository resultRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> getResultSummary(Long entranceId, Long userId) {
        List<Result> results = resultRepository.findByEntranceIdAndUserId(entranceId, userId);

        Map<String, Object> summary = new LinkedHashMap<>();

        for (Result result : results) {
            Map<String, Object> topicData = new LinkedHashMap<>();

            topicData.put("topic", result.getTopic());
            topicData.put("newConceptCount", result.getNewConcept());
            topicData.put("redirectConceptCount", result.getRedirectConcept());

            topicData.put("newConcept", parseJsonSafe(result.getNewCcContent()));
            topicData.put("redirectConcept", parseJsonSafe(result.getRedirectCcContent()));
            topicData.put("reference", parseJsonSafe(result.getReference()));

            summary.put(result.getTopic(), topicData);
        }

        return Map.of(
                "entranceId", entranceId,
                "userId", userId,
                "results", summary
        );
    }

    private Object parseJsonSafe(String json) {
        if (json == null || json.isBlank()) return "없음";
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (Exception e) {
            return json; // JSON 파싱 실패 시 원문 그대로 반환
        }
    }
}
