package com.xiao.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiao.sys.common.BusinessException;
import com.xiao.sys.dto.PageResult;
import com.xiao.sys.entity.AppTodo;
import com.xiao.sys.mapper.AppTodoMapper;
import com.xiao.sys.service.AppTodoService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 待办事项服务实现。
 */
@Service
public class AppTodoServiceImpl extends ServiceImpl<AppTodoMapper, AppTodo> implements AppTodoService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageResult<AppTodo> getTodoPage(Integer userId, String status, String priority, Integer page, Integer size) {
        LambdaQueryWrapper<AppTodo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppTodo::getUserId, userId);

        List<AppTodo> allTodos = this.list(wrapper);

        if (status != null && !status.isEmpty() && !"all".equals(status)) {
            if ("undone".equals(status) || "pending".equals(status)) {
                allTodos = allTodos.stream()
                        .filter(t -> t.getCompleted() == 0)
                        .collect(Collectors.toList());
            } else if ("done".equals(status) || "completed".equals(status)) {
                allTodos = allTodos.stream()
                        .filter(t -> t.getCompleted() == 1)
                        .collect(Collectors.toList());
            }
        }

        if (priority != null && !priority.isEmpty()) {
            allTodos = allTodos.stream()
                    .filter(t -> priority.equals(t.getPriority()))
                    .collect(Collectors.toList());
        }

        allTodos = allTodos.stream()
                .sorted(Comparator.comparing(AppTodo::getCompleted)
                        .thenComparing((t1, t2) -> {
                            String p1 = t1.getPriority() != null ? t1.getPriority() : "normal";
                            String p2 = t2.getPriority() != null ? t2.getPriority() : "normal";
                            int order1 = "high".equals(p1) ? 0 : ("normal".equals(p1) ? 1 : 2);
                            int order2 = "high".equals(p2) ? 0 : ("normal".equals(p2) ? 1 : 2);
                            return order1 - order2;
                        })
                        .thenComparing(Comparator.comparing(AppTodo::getCreatedAt).reversed()))
                .collect(Collectors.toList());

        int total = allTodos.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);
        List<AppTodo> pageList = start < total ? allTodos.subList(start, end) : List.of();

        return PageResult.of(pageList, (long) total, (long) page, (long) size);
    }

    @Override
    @Transactional
    public AppTodo createTodo(Integer userId, AppTodo todo) {
        if (todo.getTitle() == null || todo.getTitle().trim().isEmpty()) {
            throw new BusinessException("标题不能为空");
        }

        todo.setUserId(userId);
        todo.setTitle(todo.getTitle().trim());
        if (todo.getCompleted() == null) {
            todo.setCompleted(0);
        }
        if (todo.getPriority() == null || todo.getPriority().trim().isEmpty()) {
            todo.setPriority("normal");
        } else {
            todo.setPriority(todo.getPriority().trim());
        }
        if (todo.getRemark() == null) {
            todo.setRemark("");
        } else {
            todo.setRemark(todo.getRemark().trim());
        }
        if (todo.getAssignee() == null) {
            todo.setAssignee("");
        } else {
            todo.setAssignee(todo.getAssignee().trim());
        }
        if (todo.getCreator() == null) {
            todo.setCreator("");
        } else {
            todo.setCreator(todo.getCreator().trim());
        }

        String now = LocalDateTime.now().format(DATE_FORMATTER);
        todo.setCreatedAt(now);
        todo.setUpdatedAt(now);

        this.save(todo);
        return todo;
    }

    @Override
    @Transactional
    public AppTodo updateTodo(Integer userId, Integer todoId, AppTodo todo) {
        if (todo.getTitle() == null || todo.getTitle().trim().isEmpty()) {
            throw new BusinessException("标题不能为空");
        }

        LambdaQueryWrapper<AppTodo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppTodo::getId, todoId).eq(AppTodo::getUserId, userId);
        AppTodo existing = this.getOne(wrapper);

        if (existing == null) {
            throw new BusinessException("待办不存在");
        }

        existing.setTitle(todo.getTitle().trim());
        if (todo.getCompleted() != null) {
            existing.setCompleted(todo.getCompleted());
        }
        if (todo.getPriority() != null && !todo.getPriority().trim().isEmpty()) {
            existing.setPriority(todo.getPriority().trim());
        } else {
            existing.setPriority("normal");
        }
        if (todo.getDueDate() != null) {
            existing.setDueDate(todo.getDueDate().trim());
        } else {
            existing.setDueDate(null);
        }
        if (todo.getRemark() != null) {
            existing.setRemark(todo.getRemark().trim());
        } else {
            existing.setRemark("");
        }
        if (todo.getAssignee() != null) {
            existing.setAssignee(todo.getAssignee().trim());
        } else {
            existing.setAssignee("");
        }

        existing.setUpdatedAt(LocalDateTime.now().format(DATE_FORMATTER));

        this.updateById(existing);
        return existing;
    }

    @Override
    @Transactional
    public boolean deleteTodo(Integer userId, Integer todoId) {
        LambdaQueryWrapper<AppTodo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppTodo::getId, todoId).eq(AppTodo::getUserId, userId);
        return this.remove(wrapper);
    }

    @Override
    @Transactional
    public AppTodo toggleDone(Integer userId, Integer todoId, Integer completed) {
        LambdaQueryWrapper<AppTodo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppTodo::getId, todoId).eq(AppTodo::getUserId, userId);
        AppTodo existing = this.getOne(wrapper);

        if (existing == null) {
            throw new BusinessException("待办不存在");
        }

        int newStatus = completed != null ? completed : 0;
        existing.setCompleted(newStatus);
        existing.setUpdatedAt(LocalDateTime.now().format(DATE_FORMATTER));

        if (newStatus == 1) {
            existing.setCompletedAt(LocalDateTime.now().format(DATE_FORMATTER));
        } else {
            existing.setCompletedAt(null);
        }

        this.updateById(existing);
        return existing;
    }

    @Override
    public byte[] exportTodos(Integer userId, String keyword, String status, String priority) {
        LambdaQueryWrapper<AppTodo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppTodo::getUserId, userId);
        List<AppTodo> allTodos = this.list(wrapper);

        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim().toLowerCase();
            allTodos = allTodos.stream().filter(t ->
                    (t.getTitle() != null && t.getTitle().toLowerCase().contains(kw)) ||
                    (t.getRemark() != null && t.getRemark().toLowerCase().contains(kw))
            ).collect(Collectors.toList());
        }

        if (status != null && !status.isEmpty() && !"all".equals(status)) {
            if ("undone".equals(status) || "pending".equals(status)) {
                allTodos = allTodos.stream().filter(t -> t.getCompleted() == 0).collect(Collectors.toList());
            } else if ("done".equals(status) || "completed".equals(status)) {
                allTodos = allTodos.stream().filter(t -> t.getCompleted() == 1).collect(Collectors.toList());
            }
        }

        if (priority != null && !priority.isEmpty()) {
            allTodos = allTodos.stream().filter(t -> priority.equals(t.getPriority())).collect(Collectors.toList());
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("待办事项");
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            String[] headers = {"标题", "状态", "优先级", "截止日期", "创建人", "负责人", "完成时间", "备注", "创建时间", "更新时间"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            for (int i = 0; i < allTodos.size(); i++) {
                AppTodo todo = allTodos.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(todo.getTitle() != null ? todo.getTitle() : "");
                row.createCell(1).setCellValue(todo.getCompleted() != null && todo.getCompleted() == 1 ? "已完成" : "未完成");
                String priorityLabel = "中";
                if ("high".equals(todo.getPriority())) priorityLabel = "高";
                else if ("low".equals(todo.getPriority())) priorityLabel = "低";
                row.createCell(2).setCellValue(priorityLabel);
                row.createCell(3).setCellValue(todo.getDueDate() != null ? todo.getDueDate() : "");
                row.createCell(4).setCellValue(todo.getCreator() != null ? todo.getCreator() : "");
                row.createCell(5).setCellValue(todo.getAssignee() != null ? todo.getAssignee() : "");
                row.createCell(6).setCellValue(todo.getCompletedAt() != null ? todo.getCompletedAt() : "");
                row.createCell(7).setCellValue(todo.getRemark() != null ? todo.getRemark() : "");
                row.createCell(8).setCellValue(todo.getCreatedAt() != null ? todo.getCreatedAt() : "");
                row.createCell(9).setCellValue(todo.getUpdatedAt() != null ? todo.getUpdatedAt() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("导出失败", e);
        }
    }

    @Override
    public byte[] downloadTodoTemplate() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("待办事项");
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            String[] headers = {"标题*", "状态(未完成/已完成)", "优先级(高/中/低)", "截止日期", "创建人", "负责人", "备注"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            Row sampleRow = sheet.createRow(1);
            sampleRow.createCell(0).setCellValue("示例待办");
            sampleRow.createCell(1).setCellValue("未完成");
            sampleRow.createCell(2).setCellValue("中");
            sampleRow.createCell(3).setCellValue("2025-01-01");
            sampleRow.createCell(4).setCellValue("张三");
            sampleRow.createCell(5).setCellValue("李四");
            sampleRow.createCell(6).setCellValue("这是备注");

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("模板生成失败", e);
        }
    }

    @Override
    @Transactional
    public Map<String, Object> importTodos(Integer userId, byte[] fileData, String fileName) {
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();
        String now = LocalDateTime.now().format(DATE_FORMATTER);

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(fileData))) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String title = getCellString(row.getCell(0));
                String statusStr = getCellString(row.getCell(1));
                String priorityStr = getCellString(row.getCell(2));
                String dueDate = getCellString(row.getCell(3));
                String creator = getCellString(row.getCell(4));
                String assignee = getCellString(row.getCell(5));
                String remark = getCellString(row.getCell(6));

                if (title.isEmpty()) {
                    failCount++;
                    errors.add("第" + (i + 1) + "行：标题不能为空");
                    continue;
                }

                try {
                    AppTodo todo = new AppTodo();
                    todo.setUserId(userId);
                    todo.setTitle(title);
                    todo.setCompleted("已完成".equals(statusStr) ? 1 : 0);
                    String priority = "normal";
                    if ("高".equals(priorityStr)) priority = "high";
                    else if ("低".equals(priorityStr)) priority = "low";
                    todo.setPriority(priority);
                    todo.setDueDate(dueDate.isEmpty() ? null : dueDate);
                    todo.setCreator(creator);
                    todo.setAssignee(assignee);
                    todo.setRemark(remark);
                    todo.setCreatedAt(now);
                    todo.setUpdatedAt(now);
                    if ("已完成".equals(statusStr)) {
                        todo.setCompletedAt(now);
                    }
                    this.save(todo);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    errors.add("第" + (i + 1) + "行：" + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("文件解析失败", e);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("count", successCount);
        result.put("failCount", failCount);
        result.put("errors", errors);
        result.put("message", "导入完成：成功" + successCount + "条，失败" + failCount + "条");
        return result;
    }

    private String getCellString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().format(DATE_FORMATTER);
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
}