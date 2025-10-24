package com.example.fourth.controller;

import com.example.fourth.service.ResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/result")
@RequiredArgsConstructor
public class ResultController {

    private final ResultService resultService;

    //결과 반환
    @GetMapping("/summary")
    public ResponseEntity<?> getResultSummary(
            @RequestParam Long entranceId,
            @RequestParam Long userId) {
        return ResponseEntity.ok(resultService.getResultSummary(entranceId, userId));
    }
}
