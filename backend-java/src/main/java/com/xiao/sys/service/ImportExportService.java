package com.xiao.sys.service;

import com.xiao.sys.dto.ImportResultDTO;
import com.xiao.sys.entity.AppRecord;

import java.util.List;

/**
 * 导入导出服务接口
 */
public interface ImportExportService {

    /**
     * 导出 Excel 文件
     * @param records 记录列表
     * @return Excel 文件字节数组
     */
    byte[] exportExcel(List<AppRecord> records);

    /**
     * 导出 CSV 文件
     * @param records 记录列表
     * @return CSV 文件字节数组
     */
    byte[] exportCsv(List<AppRecord> records);

    /**
     * 下载导入模板
     * @return 模板文件字节数组
     */
    byte[] downloadTemplate();

    /**
     * 导入 Excel/CSV 文件
     * @param userId 用户ID
     * @param fileData 文件数据
     * @param fileName 文件名
     * @return 导入结果
     */
    ImportResultDTO importExcel(Integer userId, byte[] fileData, String fileName);
}