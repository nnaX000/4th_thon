package com.example.fourth.controller;

import com.example.fourth.service.NotionOAuthService;
import com.example.fourth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notion")
public class NotionOAuthController {

    private final NotionOAuthService notionOAuthService;
    private final UserService userService;

    // 노션 로그인 연결 여부 확인
    @Operation(
            summary = "노션 연동 상태 확인 - 김도윤",
            description = """
                사용자가 Notion 계정과 이미 연동되어 있는지 확인합니다.  
                `connected: true` → 토큰이 이미 저장된 상태입니다.
                """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "연동 상태 반환",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                                        { "connected": true }
                                        """)
                            )
                    )
            }
    )
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> checkConnection(
            @Parameter(description = "가입한 이메일", example = "user@example.com")
            @RequestParam String email
    ) {
        boolean connected = userService.hasNotionToken(email);
        return ResponseEntity.ok(Map.of("connected", connected));
    }

    // 노션 로그인 URL 발급
    @Operation(
            summary = "노션 로그인 URL 발급 - 김도윤",
            description = """
                Notion OAuth 로그인 페이지로 리다이렉트합니다.  
                사용자가 로그인하면 `/api/notion/callback` 으로 코드가 전달됩니다.
                """,
            responses = {
                    @ApiResponse(
                            responseCode = "302",
                            description = "Notion OAuth 페이지로 리다이렉트"
                    )
            }
    )
    @GetMapping("/authorize")
    public ResponseEntity<String> authorize(
            @Parameter(description = "사용자 이메일", example = "user@example.com")
            @RequestParam("email") String email
    ) {
        String url = notionOAuthService.getAuthorizationUrl(email);
        return ResponseEntity.status(302)
                .header("Location", url)
                .build();
    }

    // 로그인 완료 후 콜백
    @Operation(
            summary = "노션 로그인 완료 콜백 - 김도윤",
            description = """
                Notion 로그인 완료 후 authorization code를 받아  
                access token을 발급받고 사용자 계정에 저장합니다.  
                이후 프론트엔드의 성공 페이지(`integrations/notion/success`)로 리다이렉트됩니다.
                """,
            responses = {
                    @ApiResponse(
                            responseCode = "302",
                            description = "성공적으로 토큰 저장 후 리다이렉트",
                            content = @Content(
                                    mediaType = "text/plain",
                                    examples = @ExampleObject(value = "Redirecting to frontend success page...")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 (code 또는 state 누락)"
                    )
            }
    )
    @GetMapping("/callback")
    public ResponseEntity<String> callback(
            @Parameter(description = "Notion에서 전달된 Authorization Code", example = "7b61c8a6-xxxx-xxxx")
            @RequestParam("code") String code,
            @Parameter(description = "state 파라미터로 전달된 사용자 이메일", example = "test@example.com")
            @RequestParam("state") String email
    ) {
        String accessToken = notionOAuthService.codeToToken(code);
        userService.saveNotionToken(email, accessToken);

        URI uri = URI.create("http://localhost:3000/integrations/notion/success");
        return ResponseEntity.status(302).location(uri).build();
    }
}
