package com.example.fourth.service;


import com.example.fourth.entity.Report;
import com.example.fourth.repository.ReportRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportExportService {
    private final ReportRepository reportRepository;

    private Report getReport(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리포트입니다."));
    }

    // 마크다운
    public ByteArrayResource exportAsMarkdown(Long reportId) {
        Report report = getReport(reportId);

        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(report.getTitle()).append("\n\n");

        try {
            ObjectMapper mapper = new ObjectMapper();

            String content = report.getContent().trim();

            List<Map<String, Object>> normalizedList;

            // 1) TOTAL 모드인지 확인
            if (content.startsWith("{")) {
                Map<String, Object> root = mapper.readValue(content, Map.class);

                // TOTAL 모드인데 normalized가 없으면 빈 리스트 처리
                Object normalizedObj = root.get("normalized");
                if (normalizedObj instanceof List) {
                    normalizedList = (List<Map<String, Object>>) normalizedObj;
                } else {
                    normalizedList = List.of();
                }

            } else if (content.startsWith("[")) {
                // 2) TOPIC 모드 (최상위가 배열)
                normalizedList = mapper.readValue(content, List.class);
            } else {
                throw new RuntimeException("지원하지 않는 JSON 구조입니다.");
            }

            // 공통 마크다운 생성
            for (Map<String, Object> item : normalizedList) {

                sb.append("### 새로운 개념 \n\n");
                for (int i = 1; item.containsKey("new_concept_" + i); i++) {
                    sb.append("- ").append(item.get("new_concept_" + i)).append("\n");
                }
                sb.append("\n");

                sb.append("### 바로잡은 개념\n\n");
                sb.append("| 잘못된 이해 | 올바른 이해 |\n");
                sb.append("|--------------|--------------|\n");

                for (int i = 1; item.containsKey("redirect_" + i + "_wrong"); i++) {
                    String wrong = String.valueOf(item.get("redirect_" + i + "_wrong")).replace("\n", "<br>• ");
                    String correct = String.valueOf(item.get("redirect_" + i + "_correct")).replace("\n", "<br>• ");

                    sb.append("| • ").append(wrong)
                            .append(" | • ").append(correct)
                            .append(" |\n");
                }
                sb.append("\n");

                sb.append("### 추천 자료\n\n");
                for (int i = 1; item.containsKey("reference_" + i + "_title"); i++) {
                    sb.append("- [")
                            .append(item.get("reference_" + i + "_title"))
                            .append("](")
                            .append(item.get("reference_" + i + "_link"))
                            .append(")\n");
                }

                sb.append("\n---\n\n");
            }

        } catch (Exception e) {
            throw new RuntimeException("Markdown 변환 중 오류 발생: " + e.getMessage(), e);
        }

        return new ByteArrayResource(sb.toString().getBytes(StandardCharsets.UTF_8));
    }


    // pdf 추후에 구현
}
