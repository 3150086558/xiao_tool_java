package com.xiao.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiao.sys.dto.PageResult;
import com.xiao.sys.dto.RecordDTO;
import com.xiao.sys.entity.AppRecord;

import java.util.List;

/**
 * 记账服务接口
 */
public interface AppRecordService extends IService<AppRecord> {

    /**
     * 分页查询记账记录
     */
    PageResult<RecordDTO> getRecordPage(Integer userId, String item, String category,
                                        String startDate, String endDate,
                                        Integer page, Integer size);

    /**
     * 创建记账记录
     */
    AppRecord createRecord(Integer userId, RecordDTO dto);

    /**
     * 更新记账记录
     */
    AppRecord updateRecord(Integer userId, Integer recordId, RecordDTO dto);

    /**
     * 删除记账记录
     */
    boolean deleteRecord(Integer userId, Integer recordId);

    /**
     * 删除所有记账记录
     */
    int deleteAllRecords(Integer userId);
}