package com.example.fourth.service;

import com.example.fourth.entity.Extract_txt;
import com.example.fourth.entity.User;
import com.example.fourth.repository.ExtractRepository;
import com.example.fourth.repository.UserRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.example.fourth.entity.Extract_txt;
import com.example.fourth.entity.User;
import com.example.fourth.repository.ExtractRepository;
import com.example.fourth.repository.UserRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class ChatCrawlerService {

    @Autowired
    private ExtractRepository extractTxtRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OpenAIService openAIService;

    public Map<String, Object> analyzeChat(int userId, String url) throws IOException {
        // 1. 공유 링크 ID 추출
        String shareId = url.substring(url.lastIndexOf("/") + 1);
        String proxyUrl = "https://r.jina.ai/https://chatgpt.com/share/" + shareId;

        // 2. RestTemplate 타임아웃 설정
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        RestTemplate restTemplate = new RestTemplate(factory);

        // 3. 대화 내용 가져오기
        String result = restTemplate.getForObject(proxyUrl, String.class);
        if (result == null || result.isBlank()) {
            throw new IllegalArgumentException("ChatGPT 공유 링크에서 대화를 가져오지 못했습니다.");
        }

        // 4. 유저 조회 및 대화 저장
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

        Extract_txt extract = Extract_txt.builder()
                .user(user)
                .extract(result)
                .build();
        extractTxtRepository.save(extract);

        // 5. 프롬프트 구성
        String prompt = """
다음 대화 내용을 분석해서 주요 주제 3가지를 추출해줘.
각 주제별로 대화 전체에서 차지하는 비율(%)을 정규화해서 알려줘.
반드시 JSON 형태로만 응답해. 예시는 아래와 같아:

{
  "topics": [
    {"name": "JPA", "percentage": 30},
    {"name": "JUnit", "percentage": 25},
    {"name": "Spring", "percentage": 45}
  ]
}

대화 내용:
""" + result.substring(0, Math.min(result.length(), 3000));

        // 6. OpenAI API 호출
        String apiResponse = openAIService.getTopicFromOpenAI(prompt);
        if (apiResponse == null || apiResponse.isBlank()) {
            throw new IllegalArgumentException("OpenAI API 응답이 비어 있습니다.");
        }

        // 7. 1차 파싱 (OpenAI 전체 응답 구조)
        JsonObject root = JsonParser.parseString(apiResponse).getAsJsonObject();
        JsonArray choices = root.getAsJsonArray("choices");
        if (choices == null || choices.size() == 0) {
            throw new IllegalArgumentException("응답에 choices 필드가 없습니다: " + apiResponse);
        }

        JsonObject firstChoice = choices.get(0).getAsJsonObject();
        JsonObject message = firstChoice.getAsJsonObject("message");
        if (message == null || !message.has("content")) {
            throw new IllegalArgumentException("응답에 message.content가 없습니다: " + apiResponse);
        }

        // 8. content 문자열 정제
        String content = message.get("content").getAsString()
                .replace("\\n", "")
                .replace("\\", "")
                .replace("\"{", "{")
                .replace("}\"", "}");

        // 9. 2차 파싱 (topics 배열 추출)
        JsonObject contentJson = JsonParser.parseString(content).getAsJsonObject();
        JsonArray topics = contentJson.getAsJsonArray("topics");
        if (topics == null || topics.size() == 0) {
            throw new IllegalArgumentException("응답에 topics 필드가 없습니다: " + content);
        }

        // 10. 사람이 보기 좋은 문자열 포맷으로 변환
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < topics.size(); i++) {
            JsonObject topic = topics.get(i).getAsJsonObject();
            String name = topic.has("name") ? topic.get("name").getAsString() : "Unknown";
            int percentage = topic.has("percentage") ? topic.get("percentage").getAsInt() : 0;

            formatted.append(i + 1)
                    .append(". ")
                    .append(name)
                    .append(" - ")
                    .append(percentage)
                    .append("%");

            if (i < topics.size() - 1) formatted.append("\n");
        }

        // 11. 결과 반환
        Map<String, Object> response = new HashMap<>();
        response.put("extract_id", extract.getId());
        response.put("content", formatted.toString());

        return response;
    }
}