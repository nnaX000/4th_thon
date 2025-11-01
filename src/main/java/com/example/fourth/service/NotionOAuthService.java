package com.example.fourth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotionOAuthService {

    @Value("${notion.client.id}")
    private String clientId;

    @Value("${notion.client.secret}")
    private String clientSecret;

    @Value("${notion.redirect.uri}")
    private String redirectUri;

    @Value("${notion.auth.url}")
    private String authUrl;

    @Value("${notion.token.url}")
    private String tokenUrl;

    // 로그인 페이지로 이동시킬 url
    public String getAuthorizationUrl(String email) {
        return authUrl
                +"?client_id=" + clientId
                +"&response_type=code"
                +"&owner=user"
                +"&redirect_uri=" + redirectUri
                + "&state=" + URLEncoder.encode(email, StandardCharsets.UTF_8);
    }

    // 로그인 페이지로 이동
    public String codeToToken(String code) {
        WebClient webClient = WebClient.builder()
                .baseUrl(tokenUrl)
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

        System.out.println("Notion OAuth Response: " + response);

        return (String) response.get("access_token");
    }

}
