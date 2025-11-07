package com.example.fourth.service;

import com.example.fourth.entity.Report;
import com.example.fourth.repository.ReportRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportDetailService {

    private final ReportRepository reportRepository;

    public Map<String, Object> getReportById(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리포트입니다."));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("reportId", report.getId());
        response.put("entranceId", report.getEntrance().getId());
        response.put("userId", report.getUser().getId());
        response.put("title", report.getTitle());
        response.put("content", report.getContent());
        response.put("options", report.getOptions());
        response.put("createdAt", report.getCreatedAt());

        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> contentMap = mapper.readValue(report.getContent(), Map.class);
            response.put("content", contentMap);
        } catch (Exception e) {
            response.put("content", Map.of("error", "JSON 파싱 실패"));
        }

        return response;
    }

}
