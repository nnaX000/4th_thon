package com.example.fourth.controller;


import com.example.fourth.service.ReportExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/report/export")
public class ReportExportController {
    private final ReportExportService reportExportService;

    // 마크다운
    @Operation(
            summary = "마크다운 내보내기",
            description = """
        마크다운으로 내보내기를 수행합니다.
        """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "마크다운으로 내보내기 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                                [
                                  # React Hooks
                                            
                                              ### 새로운 개념
                                            
                                              - useEffect의 deps(의존성 배열)
                                              - deps가 바뀔 때만 effect가 다시 실행됨
                                            
                                              ### 바로잡은 개념
                                            
                                              | 잘못된 이해 | 올바른 이해 |
                                              |--------------|--------------|
                                              | • useEffect는 state가 바뀌면 항상 실행된다 | • useEffect는 의존성 배열 값이 바뀔 때만 실행된다 |
                                            
                                              ### 추천 자료
                                            
                                              - [React 공식문서 - useEffect](https://react.dev/reference/react/useEffect)
                                            
                                              ---
                                ]
                                """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "사용자를 찾을 수 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                                {
                                  "error": "존재하지 않는 사용자입니다."
                                }
                                """)
                            )
                    )
            }
    )
    @GetMapping("/{reportId}/markdown")
    public ResponseEntity<ByteArrayResource> exportAsMarkdown(@PathVariable Long reportId) {
        ByteArrayResource resource = reportExportService.exportAsMarkdown(reportId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report_"+reportId+".md")
                .contentType(MediaType.TEXT_MARKDOWN)
                .contentLength(resource.contentLength())
                .body(resource);
    }
}
