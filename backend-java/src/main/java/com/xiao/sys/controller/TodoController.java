package com.xiao.sys.controller;

import com.xiao.sys.common.Result;
import com.xiao.sys.dto.PageResult;
import com.xiao.sys.entity.AppTodo;
import com.xiao.sys.security.LoginUser;
import com.xiao.sys.service.AppTodoService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * 待办事项 Controller
 */
@RestController
@RequestMapping("/api/app/todo")
public class TodoController {

    private final AppTodoService appTodoService;

    public TodoController(AppTodoService appTodoService) {
        this.appTodoService = appTodoService;
    }

    /**
     * 分页查询待办事项
     */
    @GetMapping("/page")
    public Result<PageResult<AppTodo>> getTodoPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority) {
        Integer userId = getCurrentUserId();
        PageResult<AppTodo> result = appTodoService.getTodoPage(userId, status, priority, page, size);
        return Result.success(result);
    }

    /**
     * 创建待办事项
     */
    @PostMapping
    public Result<AppTodo> createTodo(@RequestBody AppTodo todo) {
        Integer userId = getCurrentUserId();
        AppTodo result = appTodoService.createTodo(userId, todo);
        return Result.success(result);
    }

    /**
     * 编辑待办事项
     */
    @PutMapping("/{id}")
    public Result<AppTodo> updateTodo(@PathVariable Integer id, @RequestBody AppTodo todo) {
        Integer userId = getCurrentUserId();
        AppTodo result = appTodoService.updateTodo(userId, id, todo);
        return Result.success(result);
    }

    /**
     * 删除待办事项
     */
    @DeleteMapping("/{id}")
    public Result<Map<String, Boolean>> deleteTodo(@PathVariable Integer id) {
        Integer userId = getCurrentUserId();
        boolean deleted = appTodoService.deleteTodo(userId, id);
        return Result.success(Map.of("ok", deleted));
    }

    /**
     * 切换完成状态
     */
    @PutMapping("/{id}/done")
    public Result<AppTodo> toggleDone(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        Integer userId = getCurrentUserId();
        // 兼容 completed 和 done 字段
        Integer completed = null;
        if (body.containsKey("completed")) {
            completed = Integer.valueOf(body.get("completed").toString());
        } else if (body.containsKey("done")) {
            completed = Integer.valueOf(body.get("done").toString());
        } else {
            completed = 0;
        }
        AppTodo result = appTodoService.toggleDone(userId, id, completed);
        return Result.success(result);
    }

    /**
     * 导出待办事项
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportTodos(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority) {
        Integer userId = getCurrentUserId();
        byte[] data = appTodoService.exportTodos(userId, keyword, status, priority);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "todos.xlsx");
        return ResponseEntity.ok().headers(headers).body(data);
    }

    /**
     * 下载待办事项导入模板
     */
    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTodoTemplate() {
        byte[] data = appTodoService.downloadTodoTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "todo-template.xlsx");
        return ResponseEntity.ok().headers(headers).body(data);
    }

    /**
     * 导入待办事项
     */
    @PostMapping("/import")
    public Result<Map<String, Object>> importTodos(@RequestParam("file") MultipartFile file) {
        Integer userId = getCurrentUserId();
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.toLowerCase().endsWith(".xlsx") && !filename.toLowerCase().endsWith(".xls"))) {
            return Result.fail(400, "请上传 Excel 文件（.xlsx）");
        }
        try {
            byte[] fileData = file.getBytes();
            Map<String, Object> result = appTodoService.importTodos(userId, fileData, filename);
            return Result.success(result);
        } catch (IOException e) {
            return Result.fail(400, "文件读取失败：" + e.getMessage());
        }
    }

    /**
     * 获取当前登录用户ID
     */
    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser.getUserId();
        }
        return null;
    }
}