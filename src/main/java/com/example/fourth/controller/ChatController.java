package com.example.fourth.controller;

import com.example.fourth.service.ChatCrawlerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatCrawlerService crawlerService;

    public ChatController(ChatCrawlerService crawlerService) {
        this.crawlerService = crawlerService;
    }

    @GetMapping("/crawl")
    public String crawl(@RequestParam Long userId, @RequestParam String url) {
        try {
            return crawlerService.crawlChat(userId, url);
        } catch (Exception e) {
            e.printStackTrace();
            return "오류 발생: " + e.getMessage();
        }
    }
}
