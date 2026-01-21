package com.example.fourth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DiscordErrorPayload {

    private String timestamp;
    private String method;
    private String uri;
    private String clientIp;
    private String errorType;
    private String message;

    public DiscordWebhookRequest toWebhookRequest() {

        String content = """
        🚨 **API 요청 처리 중 에러 발생**
        - 발생 시각: %s
        - Method: %s
        - URL: %s
        - Client IP: %s
        - Exception: %s
        - Message: %s
        """.formatted(
                timestamp,
                method,
                uri,
                clientIp,
                errorType,
                message
        );

        return new DiscordWebhookRequest(content);
    }
}
