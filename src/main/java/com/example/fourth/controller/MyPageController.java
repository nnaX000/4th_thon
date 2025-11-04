package com.example.fourth.controller;

import com.example.fourth.dto.MyPageResponse;
import com.example.fourth.service.MyPageService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/mypage")
@RequiredArgsConstructor
public class MyPageController {
    private final MyPageService myPageService;

    @Operation(
            summary = "마이페이지 보기",
            description = """
                사용자의 닉네임을 반환합니다.  
                또한 새로운 개념 수, 증감, 바로 잡은 개념 수, 증감, 게시 리포트 수, 증감, 총 세션을 반환합니다.
                오늘의 리포트의 id와 title을 반환합니다.
                """
    )
    @GetMapping
    public ResponseEntity<MyPageResponse> getMyPage(@RequestParam int userId) {
        MyPageResponse response = myPageService.getMyPage(userId);
        return ResponseEntity.ok(response);
    }
}
