package com.example.fourth.controller;

import com.example.fourth.service.ChatCrawlerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatCrawlerService crawlerService;

    public ChatController(ChatCrawlerService crawlerService) {
        this.crawlerService = crawlerService;
    }

    @Operation(
            summary = "지피티 채팅 URL을 입력하고 분석하기 버튼 눌렀을 때 extract_id와 추출된 토픽 단어들을 반환 - 김나영",
            description = "지피티 채팅 URL을 입력하고 분석하기 버튼 눌렀을 때 extract_id와 추출된 토픽 단어들을 반환합니다. extract_id는 이후 세션 생성할 때 필요하니 user_id와 같이 보관해두셔야 합니다.",
            parameters = {
                    @Parameter(
                            name = "userId",
                            description = "분석 요청을 보낸 사용자 ID",
                            example = "1",
                            required = true
                    ),
                    @Parameter(
                            name = "url",
                            description = "지피티 채팅 URL",
                            example = "https://chatgpt.com/share/68fa6d87-b2c8-8011-b6c5-bd3272ea992c",
                            required = true
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "분석 성공",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                    {
                            "extract_id": 9,
                            "content": "1. SSL/TLS 암호화\\n2. HTTPS 보안 프로토콜\\n3. 데이터 전송 보안"
                    }
                """))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 내부 오류 (예: 분석 실패, 크롤링 실패 등)",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                {
                  "error": "크롤링 중 오류 발생: 연결 시간 초과"
                }
                """))
                    )
            }
    )
    //extract_id랑 추출한 토픽 단어 반환
    @GetMapping("/analyze")
    public ResponseEntity<Map<String, Object>> crawl(@RequestParam int userId, @RequestParam String url) {
        try {
            Map<String, Object> result = crawlerService.analyzeChat(userId, url);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
