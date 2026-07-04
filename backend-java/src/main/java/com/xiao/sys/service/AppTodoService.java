package com.xiao.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiao.sys.dto.PageResult;
import com.xiao.sys.entity.AppTodo;

import java.util.Map;

/**
 * 待办事项 Service 接口
 */
public interface AppTodoService extends IService<AppTodo> {

    /**
     * 分页查询待办事项
     * @param userId 用户ID
     * @param status 状态过滤：undone/done
     * @param priority 优先级过滤：low/normal/high
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    PageResult<AppTodo> getTodoPage(Integer userId, String status, String priority, Integer page, Integer size);

    /**
     * 创建待办事项
     * @param userId 用户ID
     * @param todo 待办事项对象
     * @return 创建后的待办事项
     */
    AppTodo createTodo(Integer userId, AppTodo todo);

    /**
     * 更新待办事项
     * @param userId 用户ID
     * @param todoId 待办ID
     * @param todo 待办事项对象
     * @return 更新后的待办事项
     */
    AppTodo updateTodo(Integer userId, Integer todoId, AppTodo todo);

    /**
     * 删除待办事项
     * @param userId 用户ID
     * @param todoId 待办ID
     * @return 是否删除成功
     */
    boolean deleteTodo(Integer userId, Integer todoId);

    /**
     * 切换待办完成状态
     * @param userId 用户ID
     * @param todoId 待办ID
     * @param completed 完成状态：0-未完成，1-已完成
     * @return 更新后的待办事项
     */
    AppTodo toggleDone(Integer userId, Integer todoId, Integer completed);

    /**
     * 导出待办事项
     * @param userId 用户ID
     * @param keyword 关键词
     * @param status 状态过滤
     * @param priority 优先级过滤
     * @return Excel 文件字节数组
     */
    byte[] exportTodos(Integer userId, String keyword, String status, String priority);

    /**
     * 下载待办事项导入模板
     * @return Excel 文件字节数组
     */
    byte[] downloadTodoTemplate();

    /**
     * 导入待办事项
     * @param userId 用户ID
     * @param fileData 文件数据
     * @param fileName 文件名
     * @return 导入结果，包含成功数量 count
     */
    Map<String, Object> importTodos(Integer userId, byte[] fileData, String fileName);
}