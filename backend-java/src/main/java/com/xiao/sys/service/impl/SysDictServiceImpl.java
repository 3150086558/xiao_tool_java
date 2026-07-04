package com.xiao.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiao.sys.dto.PageResult;
import com.xiao.sys.entity.SysDictData;
import com.xiao.sys.entity.SysDictType;
import com.xiao.sys.mapper.SysDictDataMapper;
import com.xiao.sys.mapper.SysDictTypeMapper;
import com.xiao.sys.service.SysDictService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SysDictServiceImpl implements SysDictService {

    @Autowired
    private SysDictTypeMapper dictTypeMapper;

    @Autowired
    private SysDictDataMapper dictDataMapper;

    @Override
    public PageResult<SysDictType> listDictTypes(String name, Integer page, Integer size) {
        QueryWrapper<SysDictType> wrapper = new QueryWrapper<>();
        if (name != null && !name.trim().isEmpty()) {
            wrapper.like("dict_name", name.trim()).or().like("dict_code", name.trim());
        }
        wrapper.orderByDesc("id");
        Page<SysDictType> pageParam = new Page<>(page, size);
        IPage<SysDictType> result = dictTypeMapper.selectPage(pageParam, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public List<SysDictType> listAllDictTypes() {
        QueryWrapper<SysDictType> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1).orderByAsc("id");
        return dictTypeMapper.selectList(wrapper);
    }

    @Override
    public SysDictType getDictTypeById(Integer id) {
        return dictTypeMapper.selectById(id);
    }

    @Override
    public SysDictType createDictType(SysDictType dictType) {
        LocalDateTime now = LocalDateTime.now();
        dictType.setCreatedAt(now);
        dictType.setUpdatedAt(now);
        if (dictType.getStatus() == null) dictType.setStatus(1);
        dictTypeMapper.insert(dictType);
        return dictType;
    }

    @Override
    public SysDictType updateDictType(Integer id, SysDictType dictType) {
        dictType.setId(id);
        dictType.setUpdatedAt(LocalDateTime.now());
        dictTypeMapper.updateById(dictType);
        return dictTypeMapper.selectById(id);
    }

    @Override
    public boolean deleteDictType(Integer id) {
        dictDataMapper.delete(new QueryWrapper<SysDictData>().eq("dict_code",
                dictTypeMapper.selectById(id).getDictCode()));
        return dictTypeMapper.deleteById(id) > 0;
    }

    @Override
    public boolean updateDictTypeStatus(Integer id, Integer status) {
        SysDictType dictType = new SysDictType();
        dictType.setId(id);
        dictType.setStatus(status);
        dictType.setUpdatedAt(LocalDateTime.now());
        return dictTypeMapper.updateById(dictType) > 0;
    }

    @Override
    public PageResult<SysDictData> listDictData(String dictCode, Integer page, Integer size) {
        QueryWrapper<SysDictData> wrapper = new QueryWrapper<>();
        if (dictCode != null && !dictCode.trim().isEmpty()) {
            wrapper.eq("dict_code", dictCode.trim());
        }
        wrapper.orderByAsc("sort_order", "id");
        Page<SysDictData> pageParam = new Page<>(page, size);
        IPage<SysDictData> result = dictDataMapper.selectPage(pageParam, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public List<SysDictData> listDictDataByCode(String dictCode) {
        QueryWrapper<SysDictData> wrapper = new QueryWrapper<>();
        wrapper.eq("dict_code", dictCode).eq("status", 1).orderByAsc("sort_order", "id");
        return dictDataMapper.selectList(wrapper);
    }

    @Override
    public SysDictData getDictDataById(Integer id) {
        return dictDataMapper.selectById(id);
    }

    @Override
    public SysDictData createDictData(SysDictData dictData) {
        LocalDateTime now = LocalDateTime.now();
        dictData.setCreatedAt(now);
        dictData.setUpdatedAt(now);
        if (dictData.getStatus() == null) dictData.setStatus(1);
        if (dictData.getSortOrder() == null) dictData.setSortOrder(0);
        dictDataMapper.insert(dictData);
        return dictData;
    }

    @Override
    public SysDictData updateDictData(Integer id, SysDictData dictData) {
        dictData.setId(id);
        dictData.setUpdatedAt(LocalDateTime.now());
        dictDataMapper.updateById(dictData);
        return dictDataMapper.selectById(id);
    }

    @Override
    public boolean deleteDictData(Integer id) {
        return dictDataMapper.deleteById(id) > 0;
    }

    @Override
    public boolean updateDictDataStatus(Integer id, Integer status) {
        SysDictData dictData = new SysDictData();
        dictData.setId(id);
        dictData.setStatus(status);
        dictData.setUpdatedAt(LocalDateTime.now());
        return dictDataMapper.updateById(dictData) > 0;
    }

    @Override
    public byte[] exportDictTypes() {
        List<SysDictType> list = dictTypeMapper.selectList(null);
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("字典类型");
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            String[] headers = {"字典编码", "字典名称", "描述", "状态"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            for (int i = 0; i < list.size(); i++) {
                SysDictType item = list.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(item.getDictCode() != null ? item.getDictCode() : "");
                row.createCell(1).setCellValue(item.getDictName() != null ? item.getDictName() : "");
                row.createCell(2).setCellValue(item.getDescription() != null ? item.getDescription() : "");
                row.createCell(3).setCellValue(item.getStatus() != null && item.getStatus() == 1 ? "启用" : "禁用");
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
    public byte[] exportDictData(String dictCode) {
        QueryWrapper<SysDictData> wrapper = new QueryWrapper<>();
        if (dictCode != null && !dictCode.trim().isEmpty()) {
            wrapper.eq("dict_code", dictCode.trim());
        }
        wrapper.orderByAsc("sort_order", "id");
        List<SysDictData> list = dictDataMapper.selectList(wrapper);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("字典数据");
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            String[] headers = {"字典编码", "字典标签", "字典键值", "排序", "状态"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            for (int i = 0; i < list.size(); i++) {
                SysDictData item = list.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(item.getDictCode() != null ? item.getDictCode() : "");
                row.createCell(1).setCellValue(item.getItemLabel() != null ? item.getItemLabel() : "");
                row.createCell(2).setCellValue(item.getItemValue() != null ? item.getItemValue() : "");
                row.createCell(3).setCellValue(item.getSortOrder() != null ? item.getSortOrder() : 0);
                row.createCell(4).setCellValue(item.getStatus() != null && item.getStatus() == 1 ? "启用" : "禁用");
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
    public byte[] downloadDictTypeTemplate() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("字典类型");
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            String[] headers = {"字典编码*", "字典名称*", "描述", "状态(启用/禁用)"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            Row sampleRow = sheet.createRow(1);
            sampleRow.createCell(0).setCellValue("sample_code");
            sampleRow.createCell(1).setCellValue("示例字典");
            sampleRow.createCell(2).setCellValue("这是一个示例");
            sampleRow.createCell(3).setCellValue("启用");

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
    public byte[] downloadDictDataTemplate() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("字典数据");
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            String[] headers = {"字典编码*", "字典标签*", "字典键值*", "排序", "状态(启用/禁用)"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            Row sampleRow = sheet.createRow(1);
            sampleRow.createCell(0).setCellValue("sample_code");
            sampleRow.createCell(1).setCellValue("示例标签");
            sampleRow.createCell(2).setCellValue("sample_value");
            sampleRow.createCell(3).setCellValue(1);
            sampleRow.createCell(4).setCellValue("启用");

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
    public String importDictTypes(byte[] fileData, String fileName) {
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(fileData))) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String dictCode = getCellString(row.getCell(0));
                String dictName = getCellString(row.getCell(1));
                String description = getCellString(row.getCell(2));
                String statusStr = getCellString(row.getCell(3));

                if (dictCode.isEmpty() || dictName.isEmpty()) {
                    failCount++;
                    errors.add("第" + (i + 1) + "行：字典编码和字典名称不能为空");
                    continue;
                }

                try {
                    QueryWrapper<SysDictType> wrapper = new QueryWrapper<>();
                    wrapper.eq("dict_code", dictCode);
                    SysDictType existing = dictTypeMapper.selectOne(wrapper);

                    Integer status = "禁用".equals(statusStr) ? 0 : 1;

                    if (existing != null) {
                        existing.setDictName(dictName);
                        existing.setDescription(description);
                        existing.setStatus(status);
                        existing.setUpdatedAt(now);
                        dictTypeMapper.updateById(existing);
                    } else {
                        SysDictType dictType = new SysDictType();
                        dictType.setDictCode(dictCode);
                        dictType.setDictName(dictName);
                        dictType.setDescription(description);
                        dictType.setStatus(status);
                        dictType.setCreatedAt(now);
                        dictType.setUpdatedAt(now);
                        dictTypeMapper.insert(dictType);
                    }
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    errors.add("第" + (i + 1) + "行：" + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("文件解析失败", e);
        }

        StringBuilder result = new StringBuilder();
        result.append("导入完成：成功").append(successCount).append("条，失败").append(failCount).append("条");
        if (!errors.isEmpty()) {
            result.append("。失败原因：").append(String.join("；", errors));
        }
        return result.toString();
    }

    @Override
    public String importDictData(String dictCode, byte[] fileData, String fileName) {
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(fileData))) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String code = getCellString(row.getCell(0));
                String itemLabel = getCellString(row.getCell(1));
                String itemValue = getCellString(row.getCell(2));
                String sortOrderStr = getCellString(row.getCell(3));
                String statusStr = getCellString(row.getCell(4));

                String finalCode = (dictCode != null && !dictCode.isEmpty()) ? dictCode : code;

                if (finalCode.isEmpty() || itemLabel.isEmpty() || itemValue.isEmpty()) {
                    failCount++;
                    errors.add("第" + (i + 1) + "行：字典编码、标签、键值不能为空");
                    continue;
                }

                try {
                    Integer sortOrder = 0;
                    try {
                        sortOrder = Integer.parseInt(sortOrderStr);
                    } catch (NumberFormatException ignored) {}

                    Integer status = "禁用".equals(statusStr) ? 0 : 1;

                    QueryWrapper<SysDictData> wrapper = new QueryWrapper<>();
                    wrapper.eq("dict_code", finalCode).eq("item_value", itemValue);
                    SysDictData existing = dictDataMapper.selectOne(wrapper);

                    if (existing != null) {
                        existing.setItemLabel(itemLabel);
                        existing.setSortOrder(sortOrder);
                        existing.setStatus(status);
                        existing.setUpdatedAt(now);
                        dictDataMapper.updateById(existing);
                    } else {
                        SysDictData dictData = new SysDictData();
                        dictData.setDictCode(finalCode);
                        dictData.setItemLabel(itemLabel);
                        dictData.setItemValue(itemValue);
                        dictData.setSortOrder(sortOrder);
                        dictData.setStatus(status);
                        dictData.setCreatedAt(now);
                        dictData.setUpdatedAt(now);
                        dictDataMapper.insert(dictData);
                    }
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    errors.add("第" + (i + 1) + "行：" + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("文件解析失败", e);
        }

        StringBuilder result = new StringBuilder();
        result.append("导入完成：成功").append(successCount).append("条，失败").append(failCount).append("条");
        if (!errors.isEmpty()) {
            result.append("。失败原因：").append(String.join("；", errors));
        }
        return result.toString();
    }

    private String getCellString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
}
