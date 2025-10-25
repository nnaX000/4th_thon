package com.example.fourth.controller;

import com.example.fourth.service.ResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(
            summary = "분석 결과 요약 조회",
            description = """
    주어진 entranceId와 userId를 기반으로, 사용자가 진행한 학습 분석 결과를 반환합니다.

    반환 구조 예시:

    ```json
    {
      "entranceId": 1,
      "userId": 1,
      "results": {
        "SSL TLS": {
          "topic": "SSL TLS",
          "newConceptCount": 7,
          "redirectConceptCount": 8,
          "newConcept": {
            "새로알게된": {
              "1": "TLS 1.3에서 세션키는 대칭 암호화 방식으로 데이터를 전송하는 데 사용되며, 초기 키 교환 단계에서는 공개키(비대칭 암호화)를 사용한다.",
              "2": "현재 'SSL 인증서'라는 용어가 일반적으로 사용되지만, 실제로 대부분의 시스템에서는 TLS 프로토콜이 사용되고 있다.",
              "3": "TLS가 SSL의 후속 버전으로 보안 수준이 강화되었으며, SSL보다 개선된 프로토콜로 현재는 TLS 1.2와 TLS 1.3이 주로 사용된다."
            }
          },
          "redirectConcept": {
            "바로잡은": {
              "1": {
                "잘못된이해": "SSL은 이전 프로토콜이기 때문에 현재도 널리 사용되고 있다.",
                "올바른이해": "SSL은 이제 거의 사용되지 않으며, TLS가 그 자리를 대체하고 표준으로 사용된다."
              }
            }
          },
          "reference": {
            "추천자료": {
              "1": {
                "제목": "The Transport Layer Security (TLS) Protocol Version 1.3",
                "링크": "https://datatracker.ietf.org/doc/html/rfc8446"
              },
              "2": {
                "제목": "SSL/TLS Deployment Best Practices",
                "링크": "https://www.ssllabs.com/downloads/SSL_TLS_Deployment_Best_Practices_1.0.pdf"
              }
            }
          }
        },
        "보안 프로토콜": {
          "topic": "보안 프로토콜",
          "newConceptCount": 7,
          "redirectConceptCount": 12
        }
      }
    }
    ```
    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "리포트 요약 조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 entranceId 또는 userId에 대한 결과 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/summary")
    public ResponseEntity<?> getResultSummary(
            @RequestParam Long entranceId,
            @RequestParam Long userId) {
        return ResponseEntity.ok(resultService.getResultSummary(entranceId, userId));
    }
}
