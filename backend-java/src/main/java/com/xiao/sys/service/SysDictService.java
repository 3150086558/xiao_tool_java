package com.xiao.sys.service;

import com.xiao.sys.dto.PageResult;
import com.xiao.sys.entity.SysDictData;
import com.xiao.sys.entity.SysDictType;

import java.util.List;

public interface SysDictService {

    PageResult<SysDictType> listDictTypes(String name, Integer page, Integer size);

    List<SysDictType> listAllDictTypes();

    SysDictType getDictTypeById(Integer id);

    SysDictType createDictType(SysDictType dictType);

    SysDictType updateDictType(Integer id, SysDictType dictType);

    boolean deleteDictType(Integer id);

    boolean updateDictTypeStatus(Integer id, Integer status);

    PageResult<SysDictData> listDictData(String dictCode, Integer page, Integer size);

    List<SysDictData> listDictDataByCode(String dictCode);

    SysDictData getDictDataById(Integer id);

    SysDictData createDictData(SysDictData dictData);

    SysDictData updateDictData(Integer id, SysDictData dictData);

    boolean deleteDictData(Integer id);

    boolean updateDictDataStatus(Integer id, Integer status);

    byte[] exportDictTypes();

    byte[] exportDictData(String dictCode);

    byte[] downloadDictTypeTemplate();

    byte[] downloadDictDataTemplate();

    String importDictTypes(byte[] fileData, String fileName);

    String importDictData(String dictCode, byte[] fileData, String fileName);
}
