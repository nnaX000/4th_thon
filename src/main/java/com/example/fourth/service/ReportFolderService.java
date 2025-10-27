package com.example.fourth.service;


import com.example.fourth.dto.ReportFolderResponse;
import com.example.fourth.entity.Folder;
import com.example.fourth.entity.Report;
import com.example.fourth.entity.User;
import com.example.fourth.repository.FolderRepository;
import com.example.fourth.repository.ReportRepository;
import com.example.fourth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportFolderService {

    private final ReportRepository reportRepository;
    private final FolderRepository folderRepository;
    private final UserRepository userRepository;

    public Report assignReportToFolder(Long reportId, String folderName, Integer userId) {

        // 리포트 찾기
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 리포트를 찾을 수 없습니다."));

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."));

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

    // 유저가 가진 폴더 리스트 띄우기
    public List<ReportFolderResponse> getFolderByUser(int userId) {
        List<Folder> folders = folderRepository.findByUser_Id(userId);
        return folders.stream()
                .map(folder -> new ReportFolderResponse(folder.getId(), folder.getName()))
                .toList();
    }

    // 폴더 안의 리포트들 띄우기
    public List<ReportFolderResponse> getReportsByFolder(Long folderId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 폴더입니다."));

        List<Report> reports = reportRepository.findByFolder_Id(folderId);

        return reports.stream()
                .map(report -> new ReportFolderResponse(report.getId(), report.getTitle()))
                .toList();
    }
}
