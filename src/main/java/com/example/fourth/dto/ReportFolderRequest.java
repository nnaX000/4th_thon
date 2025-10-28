package com.example.fourth.dto;

import com.example.fourth.entity.Report;
import lombok.Data;

@Data
public class ReportFolderRequest {
    private Long reportId;
    private Integer userId;
    private String folderName;
}
