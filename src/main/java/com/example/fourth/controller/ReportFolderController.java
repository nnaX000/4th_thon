package com.example.fourth.controller;


import com.example.fourth.dto.ReportFolderResponse;
import com.example.fourth.dto.ReportFolderRequest;
import com.example.fourth.entity.Report;
import com.example.fourth.service.ReportFolderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportFolderController {

    private final ReportFolderService reportFolderService;

    @Operation(
            summary = "리포트 내부 저장하기",
            description = """
            생성된 리포트를 특정 폴더에 저장합니다.
            - `folderName`은 기존 폴더 이름 또는 새로 생성할 폴더 이름입니다.
            - 존재하지 않으면 자동으로 새 폴더를 생성합니다.
            - 콤보박스 입력 기반으로 설계되었습니다.
        """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "리포트와 연결할 폴더 정보",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                              "reportId": 1,
                              "userId": 1,
                              "folderName": "test1"
                            }
                            """)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "리포트 저장 성공 — 폴더 정보 반환",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                                    {
                                      "id": 2,
                                      "name": "test1"
                                    }
                                    """))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 — reportId나 userId가 존재하지 않음",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                                    {
                                      "error": "존재하지 않는 사용자입니다."
                                    }
                                    """))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 내부 오류 — 리포트 저장 중 예외 발생",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                                    {
                                      "error": "리포트 저장 중 오류가 발생했습니다."
                                    }
                                    """))
                    )
            }
    )
    @PostMapping("/assign-folder")
    public ResponseEntity<?> assignFolder(@RequestBody ReportFolderRequest request) {
        Report updated = reportFolderService.assignReportToFolder(
                request.getReportId(),
                request.getFolderName(),
                request.getUserId()
        );
        // Dto로 변환되면 다시 updated만 내뱉는 걸로 수정하기
        return ResponseEntity.ok(new ReportFolderResponse(
                updated.getFolder().getId(),
                updated.getFolder().getName()
        ));
    }

    @Operation(
            summary = "사용자의 폴더 목록 조회",
            description = """
        특정 사용자(userId)의 리포트 폴더 목록을 조회합니다.
        - 사용자는 자신이 생성한 폴더 리스트를 확인할 수 있습니다.
        - 각 폴더에는 폴더 이름과 폴더 ID가 포함됩니다.
        """,
            parameters = {
                    @Parameter(
                            name = "userId",
                            description = "조회할 사용자 ID",
                            example = "1",
                            required = true
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "폴더 목록 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                                [
                                  { "id": 1, "name": "test1" },
                                  { "id": 2, "name": "test2" }
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
    @GetMapping("/folders")
    public List<ReportFolderResponse> getFolders(@RequestParam int userId) {
        return reportFolderService.getFolderByUser(userId);
    }

    @Operation(
            summary = "폴더 내 리포트 목록 조회",
            description = """
        특정 폴더(folderId)에 속한 리포트 목록을 조회합니다.
        - 사용자가 클릭한 폴더 안의 리포트들을 반환합니다.
        - 각 리포트에는 제목, 생성일자 등의 기본 정보가 포함됩니다.
        """,
            parameters = {
                    @Parameter(
                            name = "folderId",
                            description = "조회할 폴더 ID",
                            required = true,
                            example = "2"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "리포트 목록 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                                [
                                  {
                                    "reportId": 1,
                                    "title": "제목",
                                    "createdAt": "2025-10-26T15:30:00"
                                  },
                                  {
                                    "reportId": 2,
                                    "title": "제목2",
                                    "createdAt": "2025-10-27T09:15:00"
                                  }
                                ]
                                """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "폴더를 찾을 수 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                                {
                                  "error": "존재하지 않는 폴더입니다."
                                }
                                """)
                            )
                    )
            }
    )
    @GetMapping("/{folderId}/reports")
    public ResponseEntity<List<ReportFolderResponse>> getReportsByFolder(@PathVariable long folderId) {
        List<ReportFolderResponse> reports = reportFolderService.getReportsByFolder(folderId);
        return ResponseEntity.ok(reports);
    }
}
