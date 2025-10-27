package com.example.fourth.controller;


import com.example.fourth.dto.ReportFolderRequest;
import com.example.fourth.entity.Report;
import com.example.fourth.service.ReportFolderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportFolderController {

    private final ReportFolderService reportFolderService;

    @Operation(
            summary = "리포트 내부 저장하기",
            description = """
            생성된 리포트를 특정 폴더에 저장합니다.
            기존 폴더를 선택하거나 새 폴더 이름을 입력해야 합니다.
        """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "리포트 저장 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            }
    )
    @PostMapping("/assign-folder")
    public ResponseEntity<?> assignFolder(@RequestBody ReportFolderRequest request) {
        Report updated = reportFolderService.assignReportToFolder(
                request.getReportId(),
                request.getFolderName(),
                request.getUserId()
        );
        return ResponseEntity.ok(updated);
    }

}
