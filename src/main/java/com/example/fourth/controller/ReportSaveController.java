package com.example.fourth.controller;


import com.example.fourth.dto.ReportSaveRequest;
import com.example.fourth.entity.Report;
import com.example.fourth.service.ReportSaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportSaveController {

    private final ReportSaveService reportSaveService;

    @Operation(
            summary = "리포트 내부 저장하기",
            description = """
            생성된 리포트를 내부 DB에 저장합니다.
            기존 폴더를 선택하거나 새 폴더 이름을 입력해야 합니다.
        """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "리포트 저장 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자 또는 폴더"),
            }
    )
    @PostMapping("/save")
    public Report saveReport(@RequestBody ReportSaveRequest request) {
        return reportSaveService.saveReport(
                request.getEntranceId(),
                request.getUserId(),
                request.getOption(),
                request.getFolderName()
        );
    }


}
