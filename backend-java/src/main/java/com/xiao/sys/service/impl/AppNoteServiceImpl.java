package com.xiao.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiao.sys.common.BusinessException;
import com.xiao.sys.dto.NoteDTO;
import com.xiao.sys.dto.PageResult;
import com.xiao.sys.entity.AppNote;
import com.xiao.sys.mapper.AppNoteMapper;
import com.xiao.sys.service.AppNoteService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 备忘录服务实现类
 */
@Service
public class AppNoteServiceImpl extends ServiceImpl<AppNoteMapper, AppNote> implements AppNoteService {

    private final AppNoteMapper appNoteMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AppNoteServiceImpl(AppNoteMapper appNoteMapper) {
        this.appNoteMapper = appNoteMapper;
    }

    @Override
    public PageResult<NoteDTO> getNotePage(Integer userId, String keyword, String type, Integer page, Integer size) {
        // 设置默认分页参数
        int pageNum = page == null || page < 1 ? 1 : page;
        int pageSize = size == null || size < 1 ? 10 : size;

        // 构建查询条件
        LambdaQueryWrapper<AppNote> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppNote::getUserId, userId);

        // 按类型筛选
        if (type != null && !type.trim().isEmpty()) {
            wrapper.eq(AppNote::getNoteType, type.trim());
        }

        // 关键字搜索（标题或内容）
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim().toLowerCase();
            wrapper.and(w -> w.like(AppNote::getTitle, kw).or().like(AppNote::getContent, kw));
        }

        // 按更新时间倒序排列
        wrapper.orderByDesc(AppNote::getUpdatedAt);

        // 分页查询
        Page<AppNote> pageObj = new Page<>(pageNum, pageSize);
        Page<AppNote> resultPage = this.page(pageObj, wrapper);

        // 转换为 DTO
        List<NoteDTO> dtoList = resultPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResult.of(dtoList, resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize());
    }

    @Override
    @Transactional
    public NoteDTO createNote(Integer userId, NoteDTO dto) {
        // 验证标题不能为空
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new BusinessException("标题不能为空");
        }

        AppNote note = new AppNote();
        note.setUserId(userId);
        note.setOrgId(dto.getOrgId());
        note.setTitle(dto.getTitle().trim());
        note.setContent(dto.getContent() != null ? dto.getContent().trim() : "");
        note.setNoteType(dto.getNoteType() != null ? dto.getNoteType().trim() : "");
        // tags 直接存储前端传来的 JSON 字符串
        note.setTags(dto.getTags() != null ? dto.getTags() : "");

        String now = LocalDateTime.now().format(DATE_FORMATTER);
        note.setCreatedAt(now);
        note.setUpdatedAt(now);

        this.save(note);
        return convertToDTO(note);
    }

    @Override
    @Transactional
    public NoteDTO updateNote(Integer userId, Integer noteId, NoteDTO dto) {
        // 验证标题不能为空
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new BusinessException("标题不能为空");
        }

        // 查询备忘录并验证所属用户
        AppNote note = this.getById(noteId);
        if (note == null || !note.getUserId().equals(userId)) {
            throw new BusinessException("备忘录不存在");
        }

        note.setTitle(dto.getTitle().trim());
        note.setContent(dto.getContent() != null ? dto.getContent().trim() : "");
        note.setNoteType(dto.getNoteType() != null ? dto.getNoteType().trim() : "");
        // tags 直接存储前端传来的 JSON 字符串
        note.setTags(dto.getTags() != null ? dto.getTags() : "");
        note.setUpdatedAt(LocalDateTime.now().format(DATE_FORMATTER));

        this.updateById(note);
        return convertToDTO(note);
    }

    @Override
    @Transactional
    public boolean deleteNote(Integer userId, Integer noteId) {
        // 查询备忘录并验证所属用户
        AppNote note = this.getById(noteId);
        if (note == null || !note.getUserId().equals(userId)) {
            return false;
        }

        return this.removeById(noteId);
    }

    @Override
    public byte[] exportNotes(Integer userId, String keyword, String type) {
        LambdaQueryWrapper<AppNote> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppNote::getUserId, userId);

        if (type != null && !type.trim().isEmpty()) {
            wrapper.eq(AppNote::getNoteType, type.trim());
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim().toLowerCase();
            wrapper.and(w -> w.like(AppNote::getTitle, kw).or().like(AppNote::getContent, kw));
        }

        wrapper.orderByDesc(AppNote::getUpdatedAt);
        List<AppNote> notes = this.list(wrapper);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("备忘录");
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            String[] headers = {"标题", "类型", "内容", "标签", "创建时间", "更新时间"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            for (int i = 0; i < notes.size(); i++) {
                AppNote note = notes.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(note.getTitle() != null ? note.getTitle() : "");
                row.createCell(1).setCellValue(note.getNoteType() != null ? note.getNoteType() : "");
                row.createCell(2).setCellValue(note.getContent() != null ? note.getContent() : "");
                row.createCell(3).setCellValue(note.getTags() != null ? note.getTags() : "");
                row.createCell(4).setCellValue(note.getCreatedAt() != null ? note.getCreatedAt() : "");
                row.createCell(5).setCellValue(note.getUpdatedAt() != null ? note.getUpdatedAt() : "");
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

    /**
     * 将实体转换为 DTO
     */
    private NoteDTO convertToDTO(AppNote note) {
        NoteDTO dto = new NoteDTO();
        dto.setId(note.getId());
        dto.setUserId(note.getUserId());
        dto.setOrgId(note.getOrgId());
        dto.setTitle(note.getTitle());
        dto.setContent(note.getContent());
        dto.setNoteType(note.getNoteType());
        dto.setTags(note.getTags());
        dto.setCreateTime(note.getCreatedAt());
        dto.setUpdateTime(note.getUpdatedAt());
        return dto;
    }
}