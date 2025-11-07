package com.example.fourth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class MyPageResponse {
    private String nickname;
    private int newConceptCount;
    private int newConceptDiff;
    private int fixedConceptCount;
    private int fixedConceptDiff;
    private int reportCount;
    private int reportDiff;
    private int totalSessionCount;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReportSummary {
        private long id;
        private String title;
    }

    private List<ReportSummary> todayReports;
}
