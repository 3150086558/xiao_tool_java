package com.xiao.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiao.sys.common.BusinessException;
import com.xiao.sys.common.ResultCode;
import com.xiao.sys.dto.PageResult;
import com.xiao.sys.dto.RecordDTO;
import com.xiao.sys.entity.AppRecord;
import com.xiao.sys.mapper.AppRecordMapper;
import com.xiao.sys.service.AppRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 记账服务实现类
 */
@Service
public class AppRecordServiceImpl extends ServiceImpl<AppRecordMapper, AppRecord> implements AppRecordService {

    private final AppRecordMapper appRecordMapper;

    private static final Pattern DATE_PATTERN_YMD = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    private static final Pattern DATE_PATTERN_SLASH = Pattern.compile("^\\d{4}/\\d{2}/\\d{2}$");
    private static final Pattern DATE_PATTERN_CN = Pattern.compile("^\\d{4}年\\d{2}月\\d{2}日$");
    private static final DateTimeFormatter OUTPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AppRecordServiceImpl(AppRecordMapper appRecordMapper) {
        this.appRecordMapper = appRecordMapper;
    }

    @Override
    public PageResult<RecordDTO> getRecordPage(Integer userId, String item, String category,
                                               String startDate, String endDate,
                                               Integer page, Integer size) {
        // 当前用户只能查看自己的记录（数据权限简化处理）
        List<Integer> userIds = Collections.singletonList(userId);

        // 查询全部记录
        List<AppRecord> allRecords = appRecordMapper.selectRecordsByUserIds(
                userIds,
                item != null ? item.trim() : null,
                category != null ? category.trim() : null,
                null,
                startDate != null ? startDate.trim() : null,
                endDate != null ? endDate.trim() : null,
                null,
                null
        );

        long total = allRecords.size();
        int pageNum = page != null && page > 0 ? page : 1;
        int pageSize = size != null && size > 0 ? size : 10;

        // 手动分页
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, allRecords.size());
        List<AppRecord> pageRecords = start < allRecords.size()
                ? allRecords.subList(start, end)
                : Collections.emptyList();

        List<RecordDTO> dtoList = pageRecords.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResult.of(dtoList, total, (long) pageNum, (long) pageSize);
    }

    @Override
    @Transactional
    public AppRecord createRecord(Integer userId, RecordDTO dto) {
        // 校验并规范化数据
        String recordDate = validateAndNormalizeDate(dto.getRecordDate());
        String recordType = validateAndNormalizeType(dto.getType());
        String category = validateCategory(dto.getCategory());
        Double amount = validateAmount(dto.getAmount(), recordType);

        String now = LocalDateTime.now().format(OUTPUT_FORMATTER);

        AppRecord record = new AppRecord();
        record.setUserId(userId);
        record.setRecordDate(recordDate);
        record.setType(recordType);
        record.setCategory(category);
        record.setSubCategory(dto.getSubCategory() != null ? dto.getSubCategory().trim() : "");
        record.setAmount(amount);
        record.setAccount(dto.getAccount() != null ? dto.getAccount().trim() : "");
        record.setNote(dto.getNote() != null ? dto.getNote().trim() : "");
        record.setCreatedAt(now);
        record.setUpdatedAt(now);

        this.save(record);
        return record;
    }

    @Override
    @Transactional
    public AppRecord updateRecord(Integer userId, Integer recordId, RecordDTO dto) {
        AppRecord record = this.getById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FAIL.getCode(), "记录不存在");
        }

        // 校验并规范化数据
        String recordDate = validateAndNormalizeDate(dto.getRecordDate());
        String recordType = validateAndNormalizeType(dto.getType());
        String category = validateCategory(dto.getCategory());
        Double amount = validateAmount(dto.getAmount(), recordType);

        String now = LocalDateTime.now().format(OUTPUT_FORMATTER);

        record.setRecordDate(recordDate);
        record.setType(recordType);
        record.setCategory(category);
        record.setSubCategory(dto.getSubCategory() != null ? dto.getSubCategory().trim() : "");
        record.setAmount(amount);
        record.setAccount(dto.getAccount() != null ? dto.getAccount().trim() : "");
        record.setNote(dto.getNote() != null ? dto.getNote().trim() : "");
        record.setUpdatedAt(now);

        this.updateById(record);
        return record;
    }

    @Override
    @Transactional
    public boolean deleteRecord(Integer userId, Integer recordId) {
        LambdaQueryWrapper<AppRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppRecord::getId, recordId).eq(AppRecord::getUserId, userId);
        return this.remove(wrapper);
    }

    @Override
    @Transactional
    public int deleteAllRecords(Integer userId) {
        // 先查询数量
        LambdaQueryWrapper<AppRecord> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(AppRecord::getUserId, userId);
        long count = this.count(countWrapper);

        // 执行删除
        LambdaQueryWrapper<AppRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppRecord::getUserId, userId);
        this.remove(wrapper);

        return (int) count;
    }

    /**
     * 校验并规范化日期
     */
    private String validateAndNormalizeDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            throw new BusinessException(ResultCode.FAIL.getCode(), "日期格式应为 YYYY-MM-DD");
        }

        String trimmedDate = date.trim();

        try {
            if (DATE_PATTERN_YMD.matcher(trimmedDate).matches()) {
                return trimmedDate;
            } else if (DATE_PATTERN_SLASH.matcher(trimmedDate).matches()) {
                return trimmedDate.replace("/", "-");
            } else if (DATE_PATTERN_CN.matcher(trimmedDate).matches()) {
                return trimmedDate.replace("年", "-")
                        .replace("月", "-")
                        .replace("日", "");
            } else {
                throw new BusinessException(ResultCode.FAIL.getCode(), "日期格式应为 YYYY-MM-DD");
            }
        } catch (Exception e) {
            throw new BusinessException(ResultCode.FAIL.getCode(), "日期格式应为 YYYY-MM-DD");
        }
    }

    /**
     * 校验并规范化类型
     */
    private String validateAndNormalizeType(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new BusinessException(ResultCode.FAIL.getCode(), "类型只能是收入或支出");
        }

        String trimmedType = type.trim();
        if ("收入".equals(trimmedType) || "income".equals(trimmedType)) {
            return "income";
        } else if ("支出".equals(trimmedType) || "expense".equals(trimmedType)) {
            return "expense";
        } else {
            throw new BusinessException(ResultCode.FAIL.getCode(), "类型只能是收入或支出");
        }
    }

    /**
     * 校验项目
     */
    private String validateCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new BusinessException(ResultCode.FAIL.getCode(), "项目不能为空");
        }
        return category.trim();
    }

    /**
     * 校验金额
     */
    private Double validateAmount(Double amount, String type) {
        if (amount == null) {
            throw new BusinessException(ResultCode.FAIL.getCode(), "金额必须是数字");
        }

        Double roundedAmount = Math.round(amount * 100.0) / 100.0;

        // 支持负数自动识别类型
        if (roundedAmount < 0) {
            roundedAmount = Math.abs(roundedAmount);
            if ("income".equals(type)) {
                type = "expense";
            }
        }

        return roundedAmount;
    }

    /**
     * 转换为DTO
     */
    private RecordDTO convertToDTO(AppRecord record) {
        RecordDTO dto = new RecordDTO();
        dto.setId(record.getId());
        dto.setRecordDate(record.getRecordDate());
        dto.setType(record.getType());
        dto.setCategory(record.getCategory());
        dto.setSubCategory(record.getSubCategory());
        dto.setAmount(record.getAmount());
        dto.setAccount(record.getAccount());
        dto.setNote(record.getNote());
        dto.setUserId(record.getUserId());
        dto.setOrgId(record.getOrgId());
        dto.setCreatedAt(record.getCreatedAt());
        dto.setUpdatedAt(record.getUpdatedAt());
        return dto;
    }
}