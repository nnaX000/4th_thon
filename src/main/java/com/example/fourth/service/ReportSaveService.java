package com.example.fourth.service;

import com.example.fourth.dto.ReportSaveRequest;
import com.example.fourth.entity.Folder;
import com.example.fourth.entity.Report;
import com.example.fourth.entity.User;
import com.example.fourth.repository.FolderRepository;
import com.example.fourth.repository.ReportRepository;
import com.example.fourth.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportSaveService {
    private final ReportRepository reportRepository;
    private final FolderRepository folderRepository;
    private final UserRepository userRepository;
    private final ReportService reportService;

    @Transactional
    public Report saveReport(Long entranceId, Long userId, Report.ReportOption option, String folderName) {
        // 1. 유저 조회하기
        User user = userRepository.findById(userId.intValue())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 2. folder Id가 들어온 경우 (기존 폴더로 저장하는 경우)
        Folder folder = folderRepository.findByNameAndUser_Id(folderName, user.getId())
                .orElseGet(() -> folderRepository.save(
                        Folder.builder()
                                .user(user)
                                .name(folderName)
                                .build()
                ));

        // 3. 리포트 저장
        Map<String, Object> generatedReport = reportService.generateReport(entranceId, userId, option);

        // 4. JSON 변환
        String content;
        try {
            content = new ObjectMapper().writeValueAsString(generatedReport);
        } catch (Exception e) {
            throw new RuntimeException("리포트 JSON 변환 실패: " + e.getMessage());
        }

        // 5. 리포트 엔티티 저장
        Report report = Report.builder()
                .user(user)
                .folder(folder)
                .title((String) generatedReport.get("title"))
                .content(content)
                .options(option)
                .build();

        return reportRepository.save(report);
    }
}
