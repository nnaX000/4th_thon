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

        // 정규화된 데이터 리스트
        List<Map<String, Object>> normalizedList = new ArrayList<>();

        for (Result result : results) {
            Map<String, Object> flat = new LinkedHashMap<>();
            flat.put("entrance_id", entranceId);
            flat.put("user_id", userId);
            flat.put("topic", result.getTopic());
            flat.put("new_concept_count", result.getNewConcept());
            flat.put("redirect_concept_count", result.getRedirectConcept());

            // newConcept
            Map<String, Object> newConcept = parseToMap(result.getNewCcContent(), "새로알게된");
            int i = 1;
            for (Object value : newConcept.values()) {
                flat.put("new_concept_" + i++, value);
            }

            // redirectConcept
            Map<String, Object> redirectConcept = parseToMap(result.getRedirectCcContent(), "바로잡은");
            i = 1;
            for (Object value : redirectConcept.values()) {
                if (value instanceof Map<?, ?> map) {
                    flat.put("redirect_" + i + "_wrong", map.get("잘못된이해"));
                    flat.put("redirect_" + i + "_correct", map.get("올바른이해"));
                }
                i++;
            }

            // reference
            Map<String, Object> reference = parseToMap(result.getReference(), "추천자료");
            i = 1;
            for (Object value : reference.values()) {
                if (value instanceof Map<?, ?> ref) {
                    flat.put("reference_" + i + "_title", ref.get("제목"));
                    flat.put("reference_" + i + "_link", ref.get("링크"));
                }
                i++;
            }

            normalizedList.add(flat);
        }

        // 최종 응답 JSON — normalized만 포함
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("normalized", normalizedList);

        // DB 저장
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
                    .title(resultTitle(results))
                    .options(options)
                    .content(contentJson)
                    .createdAt(LocalDateTime.now())
                    .build();

            reportRepository.save(report);
        } catch (Exception e) {
            throw new RuntimeException("리포트 저장 중 에러 발생", e);
        }

        return response; // normalized만 반환
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
}