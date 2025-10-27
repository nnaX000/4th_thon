package com.example.fourth.service;


import com.example.fourth.entity.Folder;
import com.example.fourth.entity.Report;
import com.example.fourth.entity.User;
import com.example.fourth.repository.FolderRepository;
import com.example.fourth.repository.ReportRepository;
import com.example.fourth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportFolderService {

    private final ReportRepository reportRepository;
    private final FolderRepository folderRepository;
    private final UserRepository userRepository;

    public Report assignReportToFolder(Long reportId, String folderName, Integer userId) {

        // 리포트 찾기
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("해당 리포트를 찾을 수 없습니다."));

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 입니다."));

        // 폴더 존재 여부 확인
        Folder folder = folderRepository.findByNameAndUser_Id(folderName, userId)
                .orElseGet(() -> folderRepository.save(
                        Folder.builder()
                                .user(user)
                                .name(folderName)
                                .build()
                ));

        // 리포트에 폴더 저장

        report.setFolder(folder);
        return reportRepository.save(report);
    }
}
