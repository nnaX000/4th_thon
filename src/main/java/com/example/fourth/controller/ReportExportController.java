package com.example.fourth.controller;


import com.example.fourth.service.NotionExportService;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/report/export")
public class ReportExportController {
    private final ReportExportService reportExportService;
    private final NotionExportService notionExportService;

    // 마크다운
    @Operation(
            summary = "마크다운 내보내기 - 김도윤",
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

    @Operation(
            summary = "노션으로 내보내기 - 김도윤",
            description = """
                지정된 리포트를 사용자의 Notion 계정에 새 페이지로 생성합니다.  
                이 기능을 사용하기 전, 반드시 `/api/notion/authorize` → `/api/notion/callback` 과정을 통해  
                사용자가 Notion OAuth 연동을 완료해야 합니다.
                """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "노션 페이지 생성 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                                {
                                  "message": "노션으로 내보내기 완료"
                                }
                                """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Notion 토큰이 없음 또는 잘못된 요청",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                                {
                                  "error": "사용자의 Notion 토큰이 없습니다. 먼저 OAuth 연동을 완료해주세요."
                                }
                                """)
                            )
                    )
            }
    )
    @PostMapping("/{reportId}/notion")
    public ResponseEntity<String> exportToNotion(
            @PathVariable Long reportId,
            @Parameter(description = "사용자 이메일", example = "user@example.com")
            @RequestParam String email,
            @Parameter(description = "노션 부모 페이지 ID", example = "244247ac69f68033a9d8dbd2c0ad34e5")
            @RequestParam String parentPageId
    ) {
        notionExportService.exportToNotion(reportId, parentPageId, email);
        return ResponseEntity.ok("노션으로 내보내기 완료");
    }


}
