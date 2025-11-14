package com.example.fourth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotionOAuthService {

    @Value("${notion.client.id}")
    private String clientId;

    @Value("${notion.client.secret}")
    private String clientSecret;

    @Value("${notion.redirect.uri}")
    private String rawRedirectUri;  // 원본 값

    @Value("${notion.auth.url}")
    private String authUrl;

    @Value("${notion.token.url}")
    private String tokenUrl;

    /**
     * redirectUri를 한 번 정리해서 사용
     * - 앞뒤 공백 제거
     * - CR/LF 제거 (\r, \n)
     */
    private String getCleanRedirectUri() {
        if (rawRedirectUri == null) {
            throw new IllegalStateException("notion.redirect.uri 설정이 없습니다.");
        }

        String cleaned = rawRedirectUri
                .trim()
                .replace("\r", "")
                .replace("\n", "");

        if (!cleaned.equals(rawRedirectUri)) {
            log.warn("Notion redirectUri에 CR/LF 또는 공백이 포함되어 있어 정제했습니다. raw='{}', clean='{}'",
                    rawRedirectUri, cleaned);
        }

        return cleaned;
    }

    /**
     * Notion 로그인 페이지로 보내줄 Authorization URL 생성
     */
    public String getAuthorizationUrl(String email) {
        String redirectUri = getCleanRedirectUri();

        String encodedRedirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        String encodedEmailState = URLEncoder.encode(email, StandardCharsets.UTF_8);

        StringBuilder sb = new StringBuilder();
        sb.append(authUrl)
                .append("?client_id=").append(URLEncoder.encode(clientId, StandardCharsets.UTF_8))
                .append("&response_type=code")
                .append("&owner=user")
                .append("&redirect_uri=").append(encodedRedirectUri)
                .append("&state=").append(encodedEmailState);

        String authRedirectUrl = sb.toString();
        log.info("Generated Notion authorization URL: {}", authRedirectUrl);

        return authRedirectUrl;
    }

    /**
     * Notion에서 받은 code를 access_token으로 교환
     */
    public String codeToToken(String code) {
        String redirectUri = getCleanRedirectUri();

        WebClient webClient = WebClient.builder()
                .baseUrl(tokenUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .build();

        Map<String, Object> body = Map.of(
                "grant_type", "authorization_code",
                "code", code,
                "redirect_uri", redirectUri
        );

        Map<String, Object> response = webClient.post()
                .headers(h -> h.setBasicAuth(clientId, clientSecret))
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        log.info("Notion OAuth token response: {}", response);

        if (response == null || !response.containsKey("access_token")) {
            throw new IllegalStateException("Notion 액세스 토큰을 가져오지 못했습니다. response=" + response);
        }

        return (String) response.get("access_token");
    }
}
