package com.example.fourth.controller;


import com.example.fourth.entity.Report;
import com.example.fourth.service.MyPageService;
import com.example.fourth.service.ReportDetailService;
import com.example.fourth.service.ReportService;
import com.example.fourth.service.ResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportGenerateService;
    private final ReportDetailService reportDetailService;
    private final MyPageService myPageService;
    private final ResultService resultService;

    @Operation(
            summary = "리포트 생성 (리포트 type[주제별/통합별] 선택하고 리포트별로 [리뷰/게시] 선택하여 백엔드로 넘김- 김나영",
            description = """
entranceId, userId, options, tag(s)를 기반으로 리포트를 생성합니다.

🔹 TOTAL (통합 리포트)
- 모든 주제를 통합하여 하나의 리포트로 생성
- 단일 tag(post/review)를 받음

🔹 TOPIC (주제별 리포트)
- 주제별로 각각 리포트를 생성
- tags 객체 형태로 각 주제별 tag(post/review)를 받음

⚙️ 요청 형식
- TOTAL 요청 예시:
{
  "entranceId": 7,
  "userId": 2,
  "options": "TOTAL",
  "tag": "POST"
}

- TOPIC 요청 예시:
{
  "entranceId": 7,
  "userId": 2,
  "options": "TOPIC",
  "tags": {
    "로그인 방법": "POST",
    "소셜 계정 연동": "REVIEW"
  }
}
""",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "리포트 생성 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "통합 리포트 응답 예시 (TOTAL)",
                                                    value = """
{
  "status": "success",
  "reportCount": 1,
  "reports": [
    {
      "reportId": 42,
      "title": "로그인 방법 / 소셜 계정 연동",
      "tag": "POST",
      "results": [
        {
          "id": 64,
          "entrance_id": 7,
          "user_id": 2,
          "topic": "로그인 방법",
          "new_concept": 3,
          "new_cc_content": {
            "새로알게된": {
              "1": "구글, 애플, 마이크로소프트 계정을 통한 소셜 로그인 지원",
              "2": "전화번호를 이용한 로그인 옵션"
            }
          },
          "redirect_concept": 2,
          "redirect_cc_content": {
            "바로잡은": {
              "1": {
                "잘못된이해": "로그인은 항상 복잡해야 한다.",
                "올바른이해": "간단한 로그인도 보안적일 수 있다."
              }
            }
          },
          "reference": {
            "추천자료": {
              "1": {
                "제목": "Modern Authentication Guide",
                "링크": "https://docs.microsoft.com/"
              }
            }
          },
          "created_at": "2025-11-11T01:08:31"
        }
      ]
    }
  ]
}
"""
                                            ),
                                            @ExampleObject(
                                                    name = "주제별 리포트 응답 예시 (TOPIC)",
                                                    value = """
{
  "status": "success",
  "reportCount": 2,
  "reports": [
    {
      "reportId": 101,
      "title": "로그인 방법",
      "tag": "POST",
      "results": [
        {
          "id": 64,
          "entrance_id": 7,
          "user_id": 2,
          "topic": "로그인 방법",
          "new_concept": 3,
          "new_cc_content": {
            "새로알게된": {
              "1": "구글 로그인 기능 추가됨"
            }
          },
          "redirect_concept": 2,
          "redirect_cc_content": {
            "바로잡은": {
              "1": {
                "잘못된이해": "SSL은 여전히 사용됨",
                "올바른이해": "TLS가 SSL을 대체함"
              }
            }
          },
          "reference": {
            "추천자료": {
              "1": {
                "제목": "OAuth2 개요",
                "링크": "https://oauth.net/2/"
              }
            }
          },
          "created_at": "2025-11-11T01:08:31"
        }
      ]
    },
    {
      "reportId": 102,
      "title": "소셜 계정 연동",
      "tag": "REVIEW",
      "results": [
        {
          "id": 65,
          "entrance_id": 7,
          "user_id": 2,
          "topic": "소셜 계정 연동",
          "new_concept": 2,
          "new_cc_content": {
            "새로알게된": {
              "1": "구글, 애플 로그인 연동이 가능함"
            }
          },
          "redirect_concept": 1,
          "redirect_cc_content": {
            "바로잡은": {
              "1": {
                "잘못된이해": "소셜 로그인은 위험함",
                "올바른이해": "OAuth 기반으로 안전하게 처리됨"
              }
            }
          },
          "reference": {
            "추천자료": {
              "1": {
                "제목": "Spring Social Login",
                "링크": "https://www.baeldung.com/spring-security-social-signin"
              }
            }
          },
          "created_at": "2025-11-11T01:09:00"
        }
      ]
    }
  ]
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
    @PostMapping
    public Map<String, Object> generateReport(@RequestBody Map<String, Object> requestBody) {
        Long entranceId = ((Number) requestBody.get("entranceId")).longValue();
        Long userId = ((Number) requestBody.get("userId")).longValue();

        // options 파싱
        Object optionsObj = requestBody.get("options");
        if (optionsObj == null) {
            throw new IllegalArgumentException("options 파라미터가 필요합니다.");
        }

        String typeStr = null;
        if (optionsObj instanceof Map<?, ?> optionMap) {
            Object typeValue = optionMap.get("type");
            if (typeValue instanceof String) {
                typeStr = (String) typeValue;
            } else {
                throw new IllegalArgumentException("options.type 값이 문자열이어야 합니다.");
            }
        } else if (optionsObj instanceof String) {
            typeStr = (String) optionsObj;
        } else {
            throw new IllegalArgumentException("options 값이 유효하지 않습니다.");
        }

        Report.ReportOption options;
        try {
            options = Report.ReportOption.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("options 값이 'TOTAL' 또는 'TOPIC'이어야 합니다.");
        }

        // tag 혹은 tags 받기
        Map<String, String> tags = null;

        // case 1: TOPIC (주제별 tag)
        if (requestBody.containsKey("tags")) {
            tags = (Map<String, String>) requestBody.get("tags");
        }
        // case 2: TOTAL (단일 tag)
        else if (requestBody.containsKey("tag")) {
            tags = new HashMap<>();
            tags.put("TOTAL", (String) requestBody.get("tag"));
        }

        return reportGenerateService.generateReport(entranceId, userId, options, tags);
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

    @Operation(
            summary = "리포트 결과 상세 조회 - 김나영",
            description = """
            entranceId, userId, topic을 기반으로 리포트 데이터를 반환합니다.
            다음과 같은 쿼리 파라미터를 전달해야 합니다:
            - entranceId (Long)
            - userId (Long)
            - topic (String)
            
            ✅ 응답 예시:
            {
              "id": 64,
              "entrance_id": 7,
              "user_id": 2,
              "topic": "로그인 방법",
              "new_concept": 3,
              "new_cc_content": { "새로알게된": { "1": "..." } },
              "redirect_concept": 2,
              "redirect_cc_content": { "바로잡은": { "1": { ... } } },
              "reference": { "추천자료": { "1": { "제목": "...", "링크": "..." } } },
              "officials": "없음",
              "extra_user": ["이 드럼 소리 너무 좋아요!"],
              "created_at": "2025-11-11T01:08:31"
            }
            """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "결과 상세 조회 성공"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "해당 결과를 찾을 수 없음"
                    )
            }
    )
    @GetMapping("/detail")
    public Map<String, Object> getResultDetail(
            @RequestParam Long entranceId,
            @RequestParam Long userId,
            @RequestParam String topic
    ) {
        return resultService.getResultDetail(entranceId, userId, topic);
    }

}
