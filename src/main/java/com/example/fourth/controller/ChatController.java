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
    public String crawl(@RequestParam String url) {
        try {
            String result=crawlerService.crawlChat(url);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return "오류: " + e.getMessage();
        }
    }
}
