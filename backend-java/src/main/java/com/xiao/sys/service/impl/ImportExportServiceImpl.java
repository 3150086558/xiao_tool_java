package com.xiao.sys.service.impl;

import com.xiao.sys.dto.ImportResultDTO;
import com.xiao.sys.entity.AppRecord;
import com.xiao.sys.mapper.AppRecordMapper;
import com.xiao.sys.service.ImportExportService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 导入导出服务实现类
 */
@Service
public class ImportExportServiceImpl implements ImportExportService {

    private final AppRecordMapper recordMapper;

    public ImportExportServiceImpl(AppRecordMapper recordMapper) {
        this.recordMapper = recordMapper;
    }

    @Override
    public byte[] exportExcel(List<AppRecord> records) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("记账记录");

            // 表头样式
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // 创建表头
            String[] headers = {"ID", "日期", "类型", "项目", "金额", "消费分类", "账户", "备注", "创建时间", "更新时间"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 填充数据
            for (int i = 0; i < records.size(); i++) {
                AppRecord record = records.get(i);
                Row row = sheet.createRow(i + 1);

                row.createCell(0).setCellValue(record.getId() != null ? record.getId() : 0);
                row.createCell(1).setCellValue(record.getRecordDate() != null ? record.getRecordDate() : "");
                row.createCell(2).setCellValue("income".equals(record.getType()) ? "收入" : "支出");
                row.createCell(3).setCellValue(record.getCategory() != null ? record.getCategory() : "");
                row.createCell(4).setCellValue(record.getAmount() != null ? record.getAmount() : 0);
                row.createCell(5).setCellValue(record.getSubCategory() != null ? record.getSubCategory() : "");
                row.createCell(6).setCellValue(record.getAccount() != null ? record.getAccount() : "");
                row.createCell(7).setCellValue(record.getNote() != null ? record.getNote() : "");
                row.createCell(8).setCellValue(record.getCreatedAt() != null ? record.getCreatedAt() : "");
                row.createCell(9).setCellValue(record.getUpdatedAt() != null ? record.getUpdatedAt() : "");
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("导出 Excel 失败", e);
        }
    }

    @Override
    public byte[] exportCsv(List<AppRecord> records) {
        StringBuilder sb = new StringBuilder();
        // 添加 BOM 头，使 Excel 正确识别 UTF-8
        sb.append("\ufeff");

        // 表头
        sb.append("ID,日期,类型,项目,金额,消费分类,账户,备注,创建时间,更新时间\n");

        for (AppRecord record : records) {
            sb.append(record.getId() != null ? record.getId() : 0).append(",");
            sb.append(record.getRecordDate() != null ? record.getRecordDate() : "").append(",");
            sb.append("income".equals(record.getType()) ? "收入" : "支出").append(",");
            sb.append(escapeCsv(record.getCategory())).append(",");
            sb.append(record.getAmount() != null ? record.getAmount() : 0).append(",");
            sb.append(escapeCsv(record.getSubCategory())).append(",");
            sb.append(escapeCsv(record.getAccount())).append(",");
            sb.append(escapeCsv(record.getNote())).append(",");
            sb.append(record.getCreatedAt() != null ? record.getCreatedAt() : "").append(",");
            sb.append(record.getUpdatedAt() != null ? record.getUpdatedAt() : "").append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * CSV 字段转义
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        // 如果包含逗号、引号或换行符，需要用引号包裹并转义内部引号
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    @Override
    public byte[] downloadTemplate() {
        try (Workbook workbook = new XSSFWorkbook()) {
            // ====== 第1页：填写表格 ======
            Sheet sheet1 = workbook.createSheet("导入数据");

            // 表头样式
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setFontHeightInPoints((short) 12);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // 必填样式
            CellStyle requiredStyle = workbook.createCellStyle();
            requiredStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            requiredStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            requiredStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle optionalStyle = workbook.createCellStyle();
            optionalStyle.setAlignment(HorizontalAlignment.CENTER);

            // 表头
            String[] headers = {"日期", "类型", "项目", "金额", "消费分类", "账户", "备注", "星期几", "是否取消"};
            Row headerRow = sheet1.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 第二行提示
            Row tipRow = sheet1.createRow(1);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = tipRow.createCell(i);
                if (i < 4) {
                    cell.setCellValue("* 必填");
                    cell.setCellStyle(requiredStyle);
                } else {
                    cell.setCellValue("选填");
                    cell.setCellStyle(optionalStyle);
                }
            }

            // 示例数据
            Object[][] sampleData = {
                    {"2025/06/17", "支出", "午餐", 6.85, "餐饮", "微信", "", "星期四", ""},
                    {"2025/06/17", "支出", "地铁", 4, "交通", "支付宝", "上班通勤", "星期四", ""},
                    {"2025/06/17", "收入", "工资", 15000, "收入", "银行卡", "2025年6月工资", "星期五", ""}
            };

            for (int i = 0; i < sampleData.length; i++) {
                Row row = sheet1.createRow(i + 2);
                for (int j = 0; j < sampleData[i].length; j++) {
                    Cell cell = row.createCell(j);
                    Object value = sampleData[i][j];
                    if (value instanceof Number) {
                        cell.setCellValue(((Number) value).doubleValue());
                    } else {
                        cell.setCellValue(value != null ? value.toString() : "");
                    }
                }
            }

            // 设置列宽
            sheet1.setColumnWidth(0, 14 * 256);
            sheet1.setColumnWidth(1, 10 * 256);
            sheet1.setColumnWidth(2, 28 * 256);
            sheet1.setColumnWidth(3, 12 * 256);
            sheet1.setColumnWidth(4, 18 * 256);
            sheet1.setColumnWidth(5, 12 * 256);
            sheet1.setColumnWidth(6, 25 * 256);

            // 冻结窗格
            sheet1.createFreezePane(0, 2);

            // ====== 第2页：字段说明 ======
            Sheet sheet2 = workbook.createSheet("字段说明");

            // 标题
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);

            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);

            Cell titleCell = sheet2.createRow(0).createCell(0);
            titleCell.setCellValue("记账导入模板 - 字段说明");
            titleCell.setCellStyle(titleStyle);

            // 字段说明表头
            Font descHeaderFont = workbook.createFont();
            descHeaderFont.setBold(true);
            descHeaderFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle descHeaderStyle = workbook.createCellStyle();
            descHeaderStyle.setFont(descHeaderFont);
            descHeaderStyle.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
            descHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            descHeaderStyle.setAlignment(HorizontalAlignment.CENTER);

            String[] descHeaders = {"字段名称", "是否必填", "格式要求", "示例"};
            Row descHeaderRow = sheet2.createRow(2);
            for (int i = 0; i < descHeaders.length; i++) {
                Cell cell = descHeaderRow.createCell(i);
                cell.setCellValue(descHeaders[i]);
                cell.setCellStyle(descHeaderStyle);
            }

            // 字段说明数据
            String[][] fieldDescriptions = {
                    {"日期", "必填", "支持多种日期格式\nYYYY/MM/DD\nYYYY-MM-DD\nYYYY年MM月DD日", "2025/06/17\n2025-06-17\n2025年6月17日"},
                    {"类型", "必填", "收入 / 支出", "收入\n支出"},
                    {"项目", "必填", "消费项目或收入来源", "午餐\n工资\n地铁"},
                    {"金额", "必填", "数字格式\n负数自动识别为支出", "6.85\n-4.00\n15000"},
                    {"消费分类", "选填", "更详细的分类标签", "餐饮\n收入\n交通"},
                    {"账户", "选填", "支付或收款账户", "微信\n支付宝\n银行卡"},
                    {"备注", "选填", "额外说明文字", "和同事聚餐"}
            };

            CellStyle wrapStyle = workbook.createCellStyle();
            wrapStyle.setWrapText(true);
            wrapStyle.setVerticalAlignment(VerticalAlignment.TOP);

            CellStyle requiredDescStyle = workbook.createCellStyle();
            requiredDescStyle.setWrapText(true);
            requiredDescStyle.setVerticalAlignment(VerticalAlignment.TOP);
            requiredDescStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            requiredDescStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            for (int i = 0; i < fieldDescriptions.length; i++) {
                Row row = sheet2.createRow(i + 3);
                for (int j = 0; j < fieldDescriptions[i].length; j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(fieldDescriptions[i][j]);
                    if (j == 1 && "必填".equals(fieldDescriptions[i][j])) {
                        cell.setCellStyle(requiredDescStyle);
                    } else {
                        cell.setCellStyle(wrapStyle);
                    }
                }
                row.setHeightInPoints(45);
            }

            // 设置列宽
            sheet2.setColumnWidth(0, 14 * 256);
            sheet2.setColumnWidth(1, 12 * 256);
            sheet2.setColumnWidth(2, 35 * 256);
            sheet2.setColumnWidth(3, 30 * 256);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("生成模板失败", e);
        }
    }

    @Override
    public ImportResultDTO importExcel(Integer userId, byte[] fileData, String fileName) {
        List<String> errors = new ArrayList<>();
        int successCount = 0;

        try {
            Workbook workbook;
            String fileExt = fileName != null ? fileName.toLowerCase() : "";

            if (fileExt.endsWith(".csv")) {
                // CSV 文件处理
                return importCsv(userId, fileData);
            } else {
                // Excel 文件处理
                workbook = WorkbookFactory.create(new ByteArrayInputStream(fileData));
            }

            Sheet sheet = workbook.getSheetAt(0);

            // 自动定位表头行：包含"日期"和"项目"列
            int headerRow = -1;
            java.util.Map<String, Integer> headers = new java.util.HashMap<>();

            for (int row = 0; row < Math.min(sheet.getPhysicalNumberOfRows(), 10); row++) {
                Row currentRow = sheet.getRow(row);
                if (currentRow == null) continue;

                java.util.Map<String, Integer> rowHeaders = new java.util.HashMap<>();
                for (int col = 0; col < currentRow.getPhysicalNumberOfCells(); col++) {
                    Cell cell = currentRow.getCell(col);
                    if (cell != null) {
                        String value = getCellValueAsString(cell).trim();
                        if (!value.isEmpty()) {
                            rowHeaders.put(value, col);
                        }
                    }
                }

                if (rowHeaders.containsKey("日期") && rowHeaders.containsKey("项目")) {
                    headerRow = row;
                    headers = rowHeaders;
                    break;
                }
            }

            if (headerRow == -1) {
                return new ImportResultDTO(0, List.of("找不到有效的表头行，请确保包含【日期】和【项目】列"));
            }

            // 检查必填列
            String[] requiredCols = {"日期", "类型", "项目", "金额"};
            for (String col : requiredCols) {
                if (!headers.containsKey(col)) {
                    return new ImportResultDTO(0, List.of("缺少必须列：" + col));
                }
            }

            // 类型映射
            java.util.Map<String, String> typeMap = new java.util.HashMap<>();
            typeMap.put("收入", "income");
            typeMap.put("支出", "expense");

            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // 处理数据行
            for (int row = headerRow + 1; row <= sheet.getPhysicalNumberOfRows(); row++) {
                Row currentRow = sheet.getRow(row);
                if (currentRow == null) continue;

                try {
                    String recordDate = getColumnValue(currentRow, headers, "日期");
                    String recordType = getColumnValue(currentRow, headers, "类型");
                    String category = getColumnValue(currentRow, headers, "项目");
                    String amountStr = getColumnValue(currentRow, headers, "金额");
                    String subCategory = getColumnValue(currentRow, headers, "消费分类");
                    String account = getColumnValue(currentRow, headers, "账户");
                    String note = getColumnValue(currentRow, headers, "备注");

                    // 跳过空行或示例/标记行
                    if ((recordDate == null || recordDate.isEmpty()) && (category == null || category.isEmpty())) {
                        continue;
                    }

                    if ("* 必填".equals(recordDate) || "选填".equals(recordDate) ||
                        "* 必填".equals(category) || "选填".equals(category)) {
                        continue;
                    }

                    // 解析日期
                    String parsedDate = parseDate(recordDate);
                    if (parsedDate == null) {
                        errors.add("第 " + (row + 1) + " 行：日期格式无效");
                        continue;
                    }

                    // 解析类型
                    String type = typeMap.get(recordType);
                    if (type == null) {
                        errors.add("第 " + (row + 1) + " 行：类型只能是收入或支出");
                        continue;
                    }

                    // 解析金额
                    Double amount;
                    try {
                        BigDecimal bd = new BigDecimal(amountStr != null && !amountStr.isEmpty() ? amountStr : "0");
                        amount = bd.doubleValue();
                    } catch (NumberFormatException e) {
                        errors.add("第 " + (row + 1) + " 行：金额格式无效");
                        continue;
                    }

                    // 负数自动转为支出
                    if (amount < 0) {
                        amount = Math.abs(amount);
                        if ("income".equals(type)) {
                            type = "expense";
                        }
                    }

                    if (category == null || category.isEmpty()) {
                        errors.add("第 " + (row + 1) + " 行：项目不能为空");
                        continue;
                    }

                    // 保存记录
                    AppRecord record = new AppRecord();
                    record.setUserId(userId);
                    record.setRecordDate(parsedDate);
                    record.setType(type);
                    record.setCategory(category);
                    record.setSubCategory(subCategory);
                    record.setAmount(amount);
                    record.setAccount(account);
                    record.setNote(note);
                    record.setCreatedAt(now);
                    record.setUpdatedAt(now);

                    recordMapper.insert(record);
                    successCount++;

                } catch (Exception e) {
                    errors.add("第 " + (row + 1) + " 行：" + e.getMessage());
                }
            }

            workbook.close();

        } catch (Exception e) {
            return new ImportResultDTO(0, List.of("文件解析失败：" + e.getMessage()));
        }

        return new ImportResultDTO(successCount, errors);
    }

    /**
     * CSV 文件导入
     */
    private ImportResultDTO importCsv(Integer userId, byte[] fileData) {
        List<String> errors = new ArrayList<>();
        int successCount = 0;

        try {
            // 处理 BOM 头
            String content = new String(fileData, StandardCharsets.UTF_8);
            if (content.startsWith("\ufeff")) {
                content = content.substring(1);
            }

            BufferedReader reader = new BufferedReader(new StringReader(content));
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return new ImportResultDTO(0, List.of("CSV 文件为空"));
            }

            // 解析表头
            String[] headers = parseCsvLine(headerLine);
            java.util.Map<String, Integer> headerIndex = new java.util.HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerIndex.put(headers[i].trim(), i);
            }

            // 类型映射
            java.util.Map<String, String> typeMap = new java.util.HashMap<>();
            typeMap.put("收入", "income");
            typeMap.put("支出", "expense");

            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String line;
            int rowNum = 1;

            while ((line = reader.readLine()) != null) {
                rowNum++;
                try {
                    String[] fields = parseCsvLine(line);

                    String recordDate = getCsvField(fields, headerIndex, "日期", "date");
                    String recordType = getCsvField(fields, headerIndex, "类型", "type");
                    String category = getCsvField(fields, headerIndex, "项目", "item", "category");
                    String amountStr = getCsvField(fields, headerIndex, "金额", "amount");
                    String subCategory = getCsvField(fields, headerIndex, "消费分类", "sub_category");
                    String account = getCsvField(fields, headerIndex, "账户", "account");
                    String note = getCsvField(fields, headerIndex, "备注", "note");

                    // 跳过空行
                    if ((recordDate == null || recordDate.isEmpty()) && (category == null || category.isEmpty())) {
                        continue;
                    }

                    // 解析日期
                    String parsedDate = parseDate(recordDate);
                    if (parsedDate == null) {
                        errors.add("第 " + rowNum + " 行：日期格式无效");
                        continue;
                    }

                    // 解析类型
                    String type = typeMap.get(recordType);
                    if (type == null) {
                        errors.add("第 " + rowNum + " 行：类型只能是收入或支出");
                        continue;
                    }

                    // 解析金额
                    Double amount;
                    try {
                        BigDecimal bd = new BigDecimal(amountStr != null && !amountStr.isEmpty() ? amountStr : "0");
                        amount = bd.doubleValue();
                    } catch (NumberFormatException e) {
                        errors.add("第 " + rowNum + " 行：金额格式无效");
                        continue;
                    }

                    // 负数自动转为支出
                    if (amount < 0) {
                        amount = Math.abs(amount);
                        if ("income".equals(type)) {
                            type = "expense";
                        }
                    }

                    if (category == null || category.isEmpty()) {
                        errors.add("第 " + rowNum + " 行：项目不能为空");
                        continue;
                    }

                    // 保存记录
                    AppRecord record = new AppRecord();
                    record.setUserId(userId);
                    record.setRecordDate(parsedDate);
                    record.setType(type);
                    record.setCategory(category);
                    record.setSubCategory(subCategory);
                    record.setAmount(amount);
                    record.setAccount(account);
                    record.setNote(note);
                    record.setCreatedAt(now);
                    record.setUpdatedAt(now);

                    recordMapper.insert(record);
                    successCount++;

                } catch (Exception e) {
                    errors.add("第 " + rowNum + " 行：" + e.getMessage());
                }
            }

        } catch (Exception e) {
            return new ImportResultDTO(0, List.of("CSV 文件解析失败：" + e.getMessage()));
        }

        return new ImportResultDTO(successCount, errors);
    }

    /**
     * 解析 CSV 行
     */
    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString().trim());

        return fields.toArray(new String[0]);
    }

    /**
     * 获取单元格值
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                } else {
                    // 避免科学计数法
                    double value = cell.getNumericCellValue();
                    if (value == (long) value) {
                        return String.valueOf((long) value);
                    } else {
                        return String.valueOf(value);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }

    /**
     * 获取指定列的值
     */
    private String getColumnValue(Row row, java.util.Map<String, Integer> headers, String colName) {
        Integer colIndex = headers.get(colName);
        if (colIndex == null) {
            return "";
        }
        Cell cell = row.getCell(colIndex);
        return getCellValueAsString(cell).trim();
    }

    /**
     * 获取 CSV 字段值
     */
    private String getCsvField(String[] fields, java.util.Map<String, Integer> headerIndex, String... possibleNames) {
        for (String name : possibleNames) {
            Integer index = headerIndex.get(name);
            if (index != null && index < fields.length) {
                return fields[index];
            }
        }
        return "";
    }

    /**
     * 解析日期，返回 yyyy-MM-dd 格式字符串
     */
    private String parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }

        dateStr = dateStr.trim();

        // 尝试多种日期格式解析
        String[] patterns = {
                "yyyy-MM-dd",
                "yyyy/MM/dd",
                "yyyy年M月d日",
                "yyyy年MM月dd日"
        };

        for (String pattern : patterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                java.time.LocalDate date = java.time.LocalDate.parse(dateStr, formatter);
                return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (Exception ignored) {
            }
        }

        // 如果已经是 yyyy-MM-dd 格式，直接返回
        if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return dateStr;
        }

        return null;
    }
}