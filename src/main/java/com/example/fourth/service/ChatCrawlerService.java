package com.example.fourth.service;

import com.example.fourth.entity.Extract_txt;
import com.example.fourth.entity.User;
import com.example.fourth.repository.ExtractRepository;
import com.example.fourth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service
public class ChatCrawlerService {

    @Autowired
    private ExtractRepository extractTxtRepository;

    @Autowired
    private UserRepository userRepository;  // 유저 찾기용

    @Autowired
    private OpenAIService openAIService;

    public Map<String, Object> analyzeChat(int userId, String url) throws IOException {
        String shareId = url.substring(url.lastIndexOf("/") + 1);
        String proxyUrl = "https://r.jina.ai/https://chatgpt.com/share/" + shareId;

        // 2. 대화 내용 요청
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject(proxyUrl, String.class);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

        Extract_txt extract = Extract_txt.builder()
                .user(user)
                .extract(result)
                .build();
        extractTxtRepository.save(extract);

        String prompt = "다음 대화의 가장 큰 핵심 주제들을 간단하게 키워드로만 3개 정도 한국어로 내뱉어줘 예를 들어 async/await 이런식으로:\n\n" + result;
        String apiResponse = openAIService.getTopicFromOpenAI(prompt);

        JsonObject jsonObject = JsonParser.parseString(apiResponse).getAsJsonObject();
        String content = jsonObject
                .getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString();

        Map<String, Object> response = new HashMap<>();
        response.put("extract_id", extract.getId());
        response.put("content", content);

        return response;
    }
}