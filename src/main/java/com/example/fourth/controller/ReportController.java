package com.example.fourth.controller;


import com.example.fourth.entity.Report;
import com.example.fourth.service.MyPageService;
import com.example.fourth.service.ReportDetailService;
import com.example.fourth.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportGenerateService;
    private final ReportDetailService reportDetailService;
    private final MyPageService myPageService;

    @Operation(
            summary = "리포트 생성 - 김나영",
            description = """
    entrance_Id와 user_Id, options 를 기반으로 리포트를 생성합니다.
    옵션(`options`)에 따라 결과가 다르게 반환됩니다.
    
    🔹 통합 리포트 : 여러 주제를 하나로 통합한 리포트 (제목이 /를 기준으로 합쳐져 있음.)  
    🔹 주제별 리포트 : 각 주제별 리포트를 개별 JSON으로 반환  
    
    ⚙️ 요청 파라미터  
    - `entranceId`  
    - `userId`: 사용자 ID  
    - `options`: 리포트 생성 방식 (통합 리포트는 `TOTAL`, 주제별 리포트는 `TOPIC`으로 요청해야 함.)
    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "리포트 생성 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "통합 리포트 예시",
                                                    value = """
                        {
                          "entranceId": 1,
                          "userId": 3,
                          "title": "SSL TLS / 보안 프로토콜",
                          "results": {
                            "SSL TLS": {
                              "topic": "SSL TLS",
                              "newConceptCount": 3,
                              "redirectConceptCount": 2,
                              "newConcept": {
                                "새로알게된": {
                                  "1": "SSL/TLS는 https를 통해 암호화된 통신을 제공한다.",
                                  "2": "TLS는 SSL의 개선판으로 보안성이 향상되었다."
                                }
                              },
                              "redirectConcept": {
                                "바로잡은": {
                                  "1": {
                                    "잘못된이해": "SSL은 현재도 널리 사용된다.",
                                    "올바른이해": "TLS가 SSL을 대체하였다."
                                  }
                                }
                              },
                              "reference": {
                                "추천자료": {
                                  "1": {
                                    "제목": "TLS 1.3 RFC 8446",
                                    "링크": "https://datatracker.ietf.org/doc/html/rfc8446"
                                  }
                                }
                              }
                            },
                            "보안 프로토콜": {
                              "topic": "보안 프로토콜",
                              "newConceptCount": 2,
                              "redirectConceptCount": 1,
                              "newConcept": {...},
                              "redirectConcept": {...},
                              "reference": {...}
                            }
                          }
                        }
                        """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
                    @ApiResponse(responseCode = "404", description = "해당 세션 또는 사용자 없음"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            }
    )
    @GetMapping
    public Map<String, Object> generateReport(
            @RequestParam Long entranceId,
            @RequestParam Long userId,
            @RequestParam Report.ReportOption options
    ) {
        return reportGenerateService.generateReport(entranceId, userId, options);
    }

    @Operation(
            summary = "리포트 상세 조회 - 김도윤",
            description = "reportId를 기반으로 저장된 리포트를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "리포트 조회 성공"),
                    @ApiResponse(responseCode = "404", description = "리포트를 찾을 수 없음")
            }
    )
    @GetMapping("/{reportId}")
    public Map<String, Object> getReportDetail(@PathVariable Long reportId) {
        return reportDetailService.getReportById(reportId);
    }

    @GetMapping("/by-date")
    public List<Map<String, Object>> getReportByDate(
            @RequestParam int userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
                return myPageService.getReportsByDate(userId, date);
    }

}
