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
                String contentJson = objectMapper.writeValueAsString(List.of(request.getContent()));
                result.setExtraUser(contentJson);
            } else {
                result.setExtraUser(existing + ", " + request.getContent());
            }

            resultRepository.save(result);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("extra_user JSON 처리 중 오류 발생", e);
        }
    }

    public Map<String, Object> getResultDetail(Long entranceId, Long userId, String topic) {
        List<Result> results = resultRepository.findByEntranceIdAndUserId(entranceId, userId);

        Result matched = results.stream()
                .filter(r -> r.getTopic().equals(topic))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 조건의 결과를 찾을 수 없습니다."));

        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("id", matched.getId());
        detail.put("entrance_id", matched.getEntrance().getId());
        detail.put("user_id", matched.getUser().getId());
        detail.put("topic", matched.getTopic());
        detail.put("new_concept", matched.getNewConcept());
        detail.put("new_cc_content", cleanString(matched.getNewCcContent()));
        detail.put("redirect_concept", matched.getRedirectConcept());
        detail.put("redirect_cc_content", cleanString(matched.getRedirectCcContent()));
        detail.put("reference", cleanString(matched.getReference()));
        detail.put("officials", cleanString(matched.getOfficials()));
        detail.put("extra_user", cleanString(matched.getExtraUser()));
        detail.put("created_at", matched.getCreatedAt());

        return detail;
    }

    private String cleanString(String value) {
        if (value == null) return "없음";
        String cleaned = value.replaceAll("\\r\\n|\\n|\\r", " ").replace("\\", "").trim();
        if (cleaned.isEmpty() || cleaned.equalsIgnoreCase("null")) {
            return "없음";
        }
        return cleaned;
    }
}
