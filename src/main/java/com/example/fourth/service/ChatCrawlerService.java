package com.example.fourth.service;

import com.example.fourth.entity.Extract_txt;
import com.example.fourth.entity.User;
import com.example.fourth.repository.ExtractRepository;
import com.example.fourth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

@Service
public class ChatCrawlerService {

    @Autowired
    private ExtractRepository extractTxtRepository;

    @Autowired
    private UserRepository userRepository;  // 유저 찾기용

    public String crawlChat(Long userId, String url) throws IOException {
        // 1. URL 파싱
        String shareId = url.substring(url.lastIndexOf("/") + 1);
        String proxyUrl = "https://r.jina.ai/https://chatgpt.com/share/" + shareId;

        // 2. 요청 보내기
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject(proxyUrl, String.class);

        // 3. DB 저장
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));
        Extract_txt extract = Extract_txt.builder()
                .user(user)
                .extract(result)
                .build();
        extractTxtRepository.save(extract);

        // 4. 파일 저장 (유저별 폴더 생성)
        Path userDir = Paths.get("outputs", "user_" + userId);
        Files.createDirectories(userDir);

        String filename = "chat_output_" + System.currentTimeMillis() + ".txt";
        Path output = userDir.resolve(filename);
        Files.writeString(output, result, StandardCharsets.UTF_8);

        return "대화가 DB 및 " + output + " 에 저장되었습니다.";
    }
}