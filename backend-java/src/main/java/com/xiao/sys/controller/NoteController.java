package com.xiao.sys.controller;

import com.xiao.sys.common.Result;
import com.xiao.sys.dto.NoteDTO;
import com.xiao.sys.dto.PageResult;
import com.xiao.sys.security.LoginUser;
import com.xiao.sys.service.AppNoteService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 备忘录控制器
 * 路径: /api/app/note/*
 */
@RestController
@RequestMapping("/api/app/note")
public class NoteController {

    private final AppNoteService appNoteService;

    public NoteController(AppNoteService appNoteService) {
        this.appNoteService = appNoteService;
    }

    /**
     * 分页查询备忘录
     * GET /api/app/note/page
     */
    @GetMapping("/page")
    public Result<PageResult<NoteDTO>> getNotePage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Integer userId = getCurrentUserId();
        PageResult<NoteDTO> result = appNoteService.getNotePage(userId, keyword, page, size);
        return Result.success(result);
    }

    /**
     * 新增备忘录
     * POST /api/app/note
     */
    @PostMapping
    public Result<NoteDTO> createNote(@RequestBody NoteDTO dto) {
        Integer userId = getCurrentUserId();
        NoteDTO result = appNoteService.createNote(userId, dto);
        return Result.success(result);
    }

    /**
     * 编辑备忘录
     * PUT /api/app/note/{id}
     */
    @PutMapping("/{id}")
    public Result<NoteDTO> updateNote(@PathVariable Integer id, @RequestBody NoteDTO dto) {
        Integer userId = getCurrentUserId();
        NoteDTO result = appNoteService.updateNote(userId, id, dto);
        return Result.success(result);
    }

    /**
     * 删除备忘录
     * DELETE /api/app/note/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Map<String, Object>> deleteNote(@PathVariable Integer id) {
        Integer userId = getCurrentUserId();
        boolean deleted = appNoteService.deleteNote(userId, id);
        Map<String, Object> result = new HashMap<>();
        result.put("ok", deleted);
        return Result.success(result);
    }

    /**
     * 获取当前登录用户 ID
     */
    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser)) {
            throw new RuntimeException("用户未登录");
        }
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return loginUser.getUserId();
    }
}