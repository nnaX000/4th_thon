package com.example.fourth.service;

import com.example.fourth.entity.Report;
import com.example.fourth.entity.Result;
import com.example.fourth.repository.EntranceRepository;
import com.example.fourth.repository.ReportRepository;
import com.example.fourth.repository.ResultRepository;
import com.example.fourth.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
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

    public Map<String, Object> generateReport(Long entranceId, Long userId, Report.ReportOption options, Map<String, String> tags) {
        List<Result> results = resultRepository.findByEntranceIdAndUserId(entranceId, userId);
        if (results.isEmpty()) {
            throw new IllegalArgumentException("해당 entranceId와 userId에 대한 결과가 없습니다.");
        }

        var entranceOpt = entranceRepository.findById(entranceId);
        var userOpt = userRepository.findById(Math.toIntExact(userId));
        if (entranceOpt.isEmpty() || userOpt.isEmpty()) {
            throw new IllegalArgumentException("Entrance 또는 User를 찾을 수 없습니다.");
        }

        List<Map<String, Object>> reportSummaries = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        if (options == Report.ReportOption.TOPIC) {
            // 주제별 리포트 생성
            Map<String, List<Result>> resultsByTopic = results.stream()
                    .collect(Collectors.groupingBy(Result::getTopic));

            for (Map.Entry<String, List<Result>> entry : resultsByTopic.entrySet()) {
                String topic = entry.getKey();
                List<Result> topicResults = entry.getValue();

                String tagStr = tags != null ? tags.getOrDefault(topic, null) : null;
                Report.TagOption tagOption = Report.TagOption.REVIEW;
                if (tagStr != null) {
                    try {
                        tagOption = Report.TagOption.valueOf(tagStr);
                    } catch (IllegalArgumentException e) {
                        tagOption = Report.TagOption.REVIEW;
                    }
                }

                String title = topic;

                // Flatten Result entities to avoid Hibernate proxy issues
                List<Map<String, Object>> resultList = topicResults.stream().map(result -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", result.getId());
                    map.put("entrance_id", result.getEntrance().getId());
                    map.put("user_id", result.getUser().getId());
                    map.put("topic", result.getTopic());
                    map.put("new_concept", result.getNewConcept());
                    map.put("new_cc_content", parseJsonString(result.getNewCcContent()));
                    map.put("redirect_concept", result.getRedirectConcept());
                    map.put("redirect_cc_content", parseJsonString(result.getRedirectCcContent()));
                    map.put("reference", parseJsonString(result.getReference()));
                    map.put("officials", parseJsonString(result.getOfficials()));
                    map.put("extra_user", result.getExtraUser());
                    map.put("created_at", result.getCreatedAt());
                    return map;
                }).toList();

                String contentJson;
                try {
                    contentJson = mapper.writeValueAsString(resultList);
                } catch (Exception e) {
                    throw new RuntimeException("리포트 내용 직렬화 중 에러 발생", e);
                }

                Report report = Report.builder()
                        .entrance(entranceOpt.get())
                        .user(userOpt.get())
                        .title(title)
                        .options(options)
                        .tag(tagOption)
                        .content(contentJson) // Save JSON in DB, not in response
                        .createdAt(LocalDateTime.now())
                        .build();

                reportRepository.save(report);

                Map<String, Object> reportMap = new LinkedHashMap<>();
                reportMap.put("reportId", report.getId());
                reportMap.put("title", title);
                reportMap.put("tag", tagOption.name());
                reportMap.put("results", resultList);

                reportSummaries.add(reportMap);
            }
        } else if (options == Report.ReportOption.TOTAL) {
            // 통합 리포트 생성
            List<Map<String, Object>> normalizedList = normalizeResults(results, entranceId, userId);
            Set<String> topicsAggregated = results.stream()
                    .map(Result::getTopic)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            Map<String, Object> dbContent = new LinkedHashMap<>();
            dbContent.put("normalized", normalizedList);
            dbContent.put("topicsAggregated", topicsAggregated);

            String contentJson;
            try {
                contentJson = mapper.writeValueAsString(dbContent);
            } catch (Exception e) {
                throw new RuntimeException("통합 리포트 저장 중 JSON 변환 오류", e);
            }

            // total tag (사용자 입력 기반)
            String tagValue = tags != null && tags.containsKey("total")
                    ? tags.get("total").toUpperCase()
                    : "REVIEW";
            Report.TagOption tagOption = Report.TagOption.valueOf(tagValue);

            Report report = Report.builder()
                    .entrance(entranceOpt.get())
                    .user(userOpt.get())
                    .title(resultTitle(results)) // 제목 하나
                    .options(options)
                    .tag(tagOption)
                    .content(contentJson)
                    .createdAt(LocalDateTime.now())
                    .build();

            reportRepository.save(report);

            Map<String, Object> reportMap = new LinkedHashMap<>();
            reportMap.put("reportId", report.getId());
            reportMap.put("title", report.getTitle());
            reportMap.put("tag", tagOption.name());
            reportMap.put("results", normalizedList);
            reportSummaries.add(reportMap);
        } else {
            throw new IllegalArgumentException("지원하지 않는 ReportOption 입니다.");
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "success");
        response.put("reportCount", reportSummaries.size());
        response.put("reports", reportSummaries);
        return response;
    }

    // 통합 리포트에서 사용할 결과 정규화 (예시)
    private List<Map<String, Object>> normalizeResults(List<Result> results, Long entranceId, Long userId) {
        // 기존의 flatten과 동일하게 처리
        return results.stream().map(result -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", result.getId());
            map.put("entrance_id", result.getEntrance().getId());
            map.put("user_id", result.getUser().getId());
            map.put("topic", result.getTopic());
            map.put("new_concept", result.getNewConcept());
            map.put("new_cc_content", parseJsonString(result.getNewCcContent()));
            map.put("redirect_concept", result.getRedirectConcept());
            map.put("redirect_cc_content", parseJsonString(result.getRedirectCcContent()));
            map.put("reference", parseJsonString(result.getReference()));
            map.put("officials", parseJsonString(result.getOfficials()));
            map.put("extra_user", result.getExtraUser());
            map.put("created_at", result.getCreatedAt());
            return map;
        }).toList();
    }

    // JSON에서 특정 key를 꺼내어 Map으로 변환
    private Map<String, Object> parseToMap(String json, String key) {
        if (json == null || json.isBlank()) return new LinkedHashMap<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> outer = mapper.readValue(json, Map.class);
            if (outer.containsKey(key) && outer.get(key) instanceof Map<?, ?> inner) {
                return (Map<String, Object>) inner;
            }
            return outer;
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    // 여러 토픽명을 슬래시(/)로 연결한 제목 생성
    private String resultTitle(List<Result> results) {
        return results.stream()
                .map(Result::getTopic)
                .distinct()
                .collect(Collectors.joining(" / "));
    }

    private Object parseJsonString(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, Map.class);
        } catch (Exception e) {
            return json;
        }
    }

}