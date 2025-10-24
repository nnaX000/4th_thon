package com.example.fourth.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

@Service
public class ChatCrawlerService {

    public String crawlChat(String url) throws IOException {

        String shareId = url.substring(url.lastIndexOf("/") + 1);
        String proxyUrl = "https://r.jina.ai/https://chatgpt.com/share/" + shareId;

        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject(proxyUrl, String.class);

        // 파일 저장
        Path output = Paths.get("chat_output.txt");
        Files.writeString(output, result, StandardCharsets.UTF_8);

        return "대화가 chat_output.txt 로 저장되었습니다.";
    }
}