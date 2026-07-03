package com.xiao.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiao.sys.common.BusinessException;
import com.xiao.sys.dto.PageResult;
import com.xiao.sys.entity.AppTodo;
import com.xiao.sys.mapper.AppTodoMapper;
import com.xiao.sys.service.AppTodoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 待办事项 Service 实现类
 */
@Service
public class AppTodoServiceImpl extends ServiceImpl<AppTodoMapper, AppTodo> implements AppTodoService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageResult<AppTodo> getTodoPage(Integer userId, String status, String priority, Integer page, Integer size) {
        // 查询该用户的所有待办事项
        LambdaQueryWrapper<AppTodo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppTodo::getUserId, userId);

        // 按完成状态排序：未完成的排在前面
        // 按优先级排序：high->normal->low
        // 按创建时间倒序
        List<AppTodo> allTodos = this.list(wrapper);

        // 过滤状态
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

        // 过滤优先级
        if (priority != null && !priority.isEmpty()) {
            allTodos = allTodos.stream()
                .filter(t -> priority.equals(t.getPriority()))
                .collect(Collectors.toList());
        }

        // 排序：completed ASC, priority(high=0, normal=1, low=2), created_at DESC
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

        // 分页
        int total = allTodos.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);
        List<AppTodo> pageList = start < total ? allTodos.subList(start, end) : List.of();

        return PageResult.of(pageList, (long) total, (long) page, (long) size);
    }

    @Override
    @Transactional
    public AppTodo createTodo(Integer userId, AppTodo todo) {
        // 验证标题
        if (todo.getTitle() == null || todo.getTitle().trim().isEmpty()) {
            throw new BusinessException("标题不能为空");
        }

        // 设置默认值
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

        // 设置时间
        String now = LocalDateTime.now().format(DATE_FORMATTER);
        todo.setCreatedAt(now);
        todo.setUpdatedAt(now);

        this.save(todo);
        return todo;
    }

    @Override
    @Transactional
    public AppTodo updateTodo(Integer userId, Integer todoId, AppTodo todo) {
        // 验证标题
        if (todo.getTitle() == null || todo.getTitle().trim().isEmpty()) {
            throw new BusinessException("标题不能为空");
        }

        // 查询待办事项是否存在
        LambdaQueryWrapper<AppTodo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppTodo::getId, todoId).eq(AppTodo::getUserId, userId);
        AppTodo existing = this.getOne(wrapper);

        if (existing == null) {
            throw new BusinessException("待办不存在");
        }

        // 更新字段
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

        // 更新时间
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
        // 查询待办事项是否存在
        LambdaQueryWrapper<AppTodo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppTodo::getId, todoId).eq(AppTodo::getUserId, userId);
        AppTodo existing = this.getOne(wrapper);

        if (existing == null) {
            throw new BusinessException("待办不存在");
        }

        // 更新完成状态
        existing.setCompleted(completed != null ? completed : 0);
        existing.setUpdatedAt(LocalDateTime.now().format(DATE_FORMATTER));

        this.updateById(existing);
        return existing;
    }
}