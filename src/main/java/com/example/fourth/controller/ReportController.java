package com.example.fourth.controller;


import com.example.fourth.entity.Report;
import com.example.fourth.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportGenerateService;

    @GetMapping
    public Map<String, Object> generateReport(
            @RequestParam Long entranceId,
            @RequestParam Long userId,
            @RequestParam Report.ReportOption options
    ) {
        return reportGenerateService.generateReport(entranceId, userId, options);
    }
}
