package com.example.fourth.service;

import com.example.fourth.dto.ResultRequest;
import com.example.fourth.entity.Result;
import com.example.fourth.repository.ResultRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

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
            topicData.put("officals", parseJsonSafe(result.getOfficials()));

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

    public void addExtraUserContent(ResultRequest request) {
        List<Result> results = resultRepository.findByEntranceIdAndTopic(request.getEntranceId(), request.getTopic().toString());
        if (results.isEmpty()) {
            throw new IllegalArgumentException("해당 결과를 찾을 수 없습니다.");
        }
        Result result = results.get(0);

        try {
            // 기존 extra_user 불러오기
            List<Map<String, String>> extraUsers = new ArrayList<>();

            if (result.getExtraUser() != null) {
                extraUsers = objectMapper.readValue(
                        result.getExtraUser(),
                        new TypeReference<List<Map<String, String>>>() {}
                );
            }

            // 기존 extra_user에 content 추가
            String existing = result.getExtraUser();
            if (existing == null || existing.isBlank()) {
                result.setExtraUser(request.getContent());
            } else {
                result.setExtraUser(existing + ", " + request.getContent());
            }

            resultRepository.save(result);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("extra_user JSON 처리 중 오류 발생", e);
        }
    }
}
