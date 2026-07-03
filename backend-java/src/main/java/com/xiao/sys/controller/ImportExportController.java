package com.xiao.sys.controller;

import com.xiao.sys.common.Result;
import com.xiao.sys.dto.ImportResultDTO;
import com.xiao.sys.entity.AppRecord;
import com.xiao.sys.mapper.AppRecordMapper;
import com.xiao.sys.service.ImportExportService;
import com.xiao.sys.security.LoginUser;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * 导入导出控制器
 * 路径：/api/app/*
 */
@RestController
@RequestMapping("/api/app")
public class ImportExportController {

    private final ImportExportService importExportService;
    private final AppRecordMapper recordMapper;

    public ImportExportController(ImportExportService importExportService, AppRecordMapper recordMapper) {
        this.importExportService = importExportService;
        this.recordMapper = recordMapper;
    }

    /**
     * 下载导入模板
     * GET /api/app/download-template
     * @return Excel 模板文件
     */
    @GetMapping("/download-template")
    public ResponseEntity<byte[]> downloadTemplate(@AuthenticationPrincipal LoginUser loginUser) {
        byte[] data = importExportService.downloadTemplate();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "AccountingTemplate.xlsx");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }

    /**
     * 导入 Excel/CSV 数据
     * POST /api/app/accounting/import
     * @param file 上传的文件
     * @param loginUser 当前登录用户
     * @return 导入结果
     */
    @PostMapping("/accounting/import")
    public Result<ImportResultDTO> importExcel(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal LoginUser loginUser) {
        
        // 验证文件类型
        String filename = file.getOriginalFilename();
        if (filename == null || 
            (!filename.toLowerCase().endsWith(".xlsx") && 
             !filename.toLowerCase().endsWith(".xls") && 
             !filename.toLowerCase().endsWith(".csv"))) {
            return Result.fail(400, "请上传 Excel 文件（.xlsx）或 CSV 文件");
        }
        
        try {
            byte[] fileData = file.getBytes();
            ImportResultDTO result = importExportService.importExcel(
                    loginUser.getUserId(),
                    fileData,
                    filename
            );
            return Result.success(result);
        } catch (IOException e) {
            return Result.fail(400, "文件读取失败：" + e.getMessage());
        }
    }

    /**
     * 导出数据到 Excel
     * GET /api/app/accounting/export
     * @param month 月份过滤
     * @param type 类型过滤
     * @param keyword 关键词过滤
     * @param loginUser 当前登录用户
     * @return Excel 文件
     */
    @GetMapping("/accounting/export")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam(required = false, defaultValue = "") String month,
            @RequestParam(required = false, defaultValue = "") String type,
            @RequestParam(required = false, defaultValue = "") String keyword,
            @AuthenticationPrincipal LoginUser loginUser) {
        
        List<Integer> userIds = Collections.singletonList(loginUser.getUserId());
        
        String filteredMonth = month != null ? month.trim() : "";
        String filteredType = type != null ? type.trim() : "";
        String filteredKeyword = keyword != null ? keyword.trim() : "";
        
        // 查询数据
        List<AppRecord> records = recordMapper.selectRecordsByUserIds(
                userIds,
                null,  // item
                null,  // category
                filteredType.isEmpty() ? null : filteredType,
                null,  // startDate
                null,  // endDate
                filteredMonth.isEmpty() ? null : filteredMonth,
                filteredKeyword.isEmpty() ? null : filteredKeyword
        );
        
        // 导出 Excel
        byte[] data = importExportService.exportExcel(records);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "accounting-records.xlsx");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }

    /**
     * 导出数据到 CSV
     * GET /api/app/export.csv
     * @param month 月份过滤
     * @param type 类型过滤
     * @param keyword 关键词过滤
     * @param loginUser 当前登录用户
     * @return CSV 文件
     */
    @GetMapping("/export.csv")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false, defaultValue = "") String month,
            @RequestParam(required = false, defaultValue = "") String type,
            @RequestParam(required = false, defaultValue = "") String keyword,
            @AuthenticationPrincipal LoginUser loginUser) {
        
        List<Integer> userIds = Collections.singletonList(loginUser.getUserId());
        
        String filteredMonth = month != null ? month.trim() : "";
        String filteredType = type != null ? type.trim() : "";
        String filteredKeyword = keyword != null ? keyword.trim() : "";
        
        // 查询数据
        List<AppRecord> records = recordMapper.selectRecordsByUserIds(
                userIds,
                null,  // item
                null,  // category
                filteredType.isEmpty() ? null : filteredType,
                null,  // startDate
                null,  // endDate
                filteredMonth.isEmpty() ? null : filteredMonth,
                filteredKeyword.isEmpty() ? null : filteredKeyword
        );
        
        // 导出 CSV
        byte[] data = importExportService.exportCsv(records);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=utf-8"));
        headers.setContentDispositionFormData("attachment", "accounting-records.csv");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }
}