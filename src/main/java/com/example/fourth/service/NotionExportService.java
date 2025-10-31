package com.example.fourth.service;

import com.example.fourth.entity.Report;
import com.example.fourth.entity.User;
import com.example.fourth.repository.ReportRepository;
import com.example.fourth.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
@RequiredArgsConstructor
public class NotionExportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ObjectMapper om = new ObjectMapper();

    /**
     * 🔹 reportId: 내보낼 리포트 ID
     * 🔹 parentPageId: 노션 상 부모 페이지 ID
     * 🔹 userEmail: OAuth 인증 완료된 사용자 이메일
     */
    public void exportToNotion(Long reportId, String parentPageId, String userEmail) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리포트입니다."));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        String token = user.getNotionToken();
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("사용자의 Notion 토큰이 없습니다. 먼저 OAuth 연동을 완료해주세요.");
        }

        WebClient webClient = WebClient.builder()
                .baseUrl("https://api.notion.com/v1")
                .defaultHeader("Authorization", "Bearer " + token)
                .defaultHeader("Notion-Version", "2022-06-28")
                .build();

        // content JSON 파싱
        Map<String, Object> content = readJson(report.getContent());
        List<Map<String, Object>> normalized = (List<Map<String, Object>>) content.getOrDefault("normalized", List.of());

        // 루트 및 제목 세팅
        ObjectNode root = buildPageRoot(parentPageId);
        putTitle(root, report.getTitle());
        ArrayNode children = root.putArray("children");

        // 페이지 상단 제목
        children.add(heading1(report.getTitle()));

        // 각 주제별 섹션 생성
        for (Map<String, Object> item : normalized) {
            String topic = String.valueOf(item.getOrDefault("topic", "주제 미상"));

            children.add(divider());
            children.add(heading2(topic));

            // 새로운 개념
            List<String> newConcepts = collectSequential(item, "new_concept_");
            if (!newConcepts.isEmpty()) {
                children.add(heading3("새로운 개념"));
                newConcepts.forEach(s -> children.add(bulleted(s)));
            }

            // 바로잡은 개념
            List<Map<String, String>> redirections = collectPairs(item, "redirect_", "_wrong", "_correct");
            if (!redirections.isEmpty()) {
                children.add(heading3("바로잡은 개념"));
                redirections.forEach(pair -> {
                    String wrong = nvl(pair.get("wrong"));
                    String correct = nvl(pair.get("correct"));
                    children.add(bulleted("잘못된 이해: " + wrong));
                    children.add(bulleted("올바른 이해: " + correct));
                });
            }

            // 추천 자료
            List<Map<String, String>> refs = collectReference(item, "reference_");
            if (!refs.isEmpty()) {
                children.add(heading3("추천 자료"));
                refs.forEach(r -> children.add(linkItem(nvl(r.get("title")), nvl(r.get("link")))));
            }
        }


        // 노션 API 호출
        String response = webClient.post()
                .uri("/pages")
                .bodyValue(root)
                .retrieve()
                .bodyToMono(String.class)
                .block();

    }

    // ======= JSON Helpers =======
    private Map<String, Object> readJson(String json) {
        try {
            return om.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("리포트 content JSON 파싱 실패", e);
        }
    }

    private List<String> collectSequential(Map<String, Object> item, String prefix) {
        List<String> out = new ArrayList<>();
        for (int i = 1; ; i++) {
            String key = prefix + i;
            if (!item.containsKey(key)) break;
            Object v = item.get(key);
            if (v != null) out.add(String.valueOf(v));
        }
        return out;
    }

    private List<Map<String, String>> collectPairs(Map<String, Object> item,
                                                   String base, String wSuffix, String cSuffix) {
        List<Map<String, String>> out = new ArrayList<>();
        for (int i = 1; ; i++) {
            String wKey = base + i + wSuffix;
            String cKey = base + i + cSuffix;
            if (!item.containsKey(wKey) && !item.containsKey(cKey)) break;
            Map<String, String> pair = new LinkedHashMap<>();
            pair.put("wrong", item.containsKey(wKey) ? String.valueOf(item.get(wKey)) : "");
            pair.put("correct", item.containsKey(cKey) ? String.valueOf(item.get(cKey)) : "");
            out.add(pair);
        }
        return out;
    }

    private List<Map<String, String>> collectReference(Map<String, Object> item, String prefix) {
        List<Map<String, String>> out = new ArrayList<>();
        for (int i = 1; ; i++) {
            String tKey = prefix + i + "_title";
            String lKey = prefix + i + "_link";
            if (!item.containsKey(tKey) && !item.containsKey(lKey)) break;
            Map<String, String> m = new LinkedHashMap<>();
            m.put("title", item.containsKey(tKey) ? String.valueOf(item.get(tKey)) : "");
            m.put("link", item.containsKey(lKey) ? String.valueOf(item.get(lKey)) : "");
            out.add(m);
        }
        return out;
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }

    // ======= Notion JSON Builders =======

    private ObjectNode buildPageRoot(String parentPageId) {
        ObjectNode root = om.createObjectNode();
        ObjectNode parent = root.putObject("parent");
        parent.put("type", "page_id");
        parent.put("page_id", parentPageId);
        root.putObject("properties");
        root.putArray("children");
        return root;
    }

    private void putTitle(ObjectNode root, String title) {
        ObjectNode props = (ObjectNode) root.get("properties");
        ObjectNode titleNode = props.putObject("title");
        ArrayNode titleArr = titleNode.putArray("title");
        titleArr.add(textNode(title));
    }

    private ObjectNode textNode(String content) {
        ObjectNode n = om.createObjectNode();
        ObjectNode text = n.putObject("text");
        text.put("content", content);
        return n;
    }

    private ObjectNode heading1(String text) {
        return headingBlock("heading_1", text);
    }

    private ObjectNode heading2(String text) {
        return headingBlock("heading_2", text);
    }

    private ObjectNode heading3(String text) {
        return headingBlock("heading_3", text);
    }

    private ObjectNode headingBlock(String type, String text) {
        ObjectNode b = om.createObjectNode();
        b.put("object", "block");
        b.put("type", type);
        ObjectNode h = b.putObject(type);
        h.putArray("rich_text").add(textNode(text));
        return b;
    }

    private ObjectNode bulleted(String text) {
        ObjectNode b = om.createObjectNode();
        b.put("object", "block");
        b.put("type", "bulleted_list_item");
        ObjectNode it = b.putObject("bulleted_list_item");
        it.putArray("rich_text").add(textNode(text));
        return b;
    }

    private ObjectNode linkItem(String title, String url) {
        ObjectNode b = om.createObjectNode();
        b.put("object", "block");
        b.put("type", "bulleted_list_item");
        ObjectNode it = b.putObject("bulleted_list_item");
        ArrayNode arr = it.putArray("rich_text");

        ObjectNode t = om.createObjectNode();
        ObjectNode text = t.putObject("text");
        text.put("content", title);
        if (url != null && !url.isBlank()) {
            ObjectNode link = text.putObject("link");
            link.put("url", url);
        }
        arr.add(t);
        return b;
    }

    private ObjectNode divider() {
        ObjectNode b = om.createObjectNode();
        b.put("object", "block");
        b.put("type", "divider");
        b.putObject("divider");
        return b;
    }
}
