package com.example.fourth.dto;

import com.example.fourth.entity.Report;
import lombok.Data;

@Data
public class ReportSaveRequest {
    private Long entranceId;
    private Long userId;
    private String folderName;
    private Report.ReportOption option;

}
