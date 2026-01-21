package com.example.fourth.handler;

import com.example.fourth.dto.DiscordErrorPayload;
import com.example.fourth.feign.DiscordFeignClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final DiscordFeignClient discordFeignClient;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(
            Exception e,
            HttpServletRequest request
    ) {
        DiscordErrorPayload = DiscordErrorPayload.builder()
                .timestamp(LocalDataTime.now().toString())
                .method((String) request.getAttribute("httpMethod"))
                .uri((String) request.getAttribute("requestUri"))
                .clientIp((String) request.getAttribute(("clientIp")))
                .message(e.getMessage())
                .build();

        discordFeignClient.sendError(payload);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
    }
}
