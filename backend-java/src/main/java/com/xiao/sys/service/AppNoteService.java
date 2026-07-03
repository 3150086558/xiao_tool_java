package com.xiao.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiao.sys.dto.NoteDTO;
import com.xiao.sys.dto.PageResult;
import com.xiao.sys.entity.AppNote;

/**
 * 备忘录服务接口
 */
public interface AppNoteService extends IService<AppNote> {

    /**
     * 分页查询备忘录
     */
    PageResult<NoteDTO> getNotePage(Integer userId, String keyword, Integer page, Integer size);

    /**
     * 创建备忘录
     */
    NoteDTO createNote(Integer userId, NoteDTO dto);

    /**
     * 更新备忘录
     */
    NoteDTO updateNote(Integer userId, Integer noteId, NoteDTO dto);

    /**
     * 删除备忘录
     */
    boolean deleteNote(Integer userId, Integer noteId);
}