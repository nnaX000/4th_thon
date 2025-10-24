package com.example.fourth.controller;

import com.example.fourth.service.ChatCrawlerService;
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

    //extract_id랑 추출한 토픽 단어 반환
    @GetMapping("/analyze")
    public ResponseEntity<Map<String, Object>> crawl(@RequestParam Long userId, @RequestParam String url) {
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
