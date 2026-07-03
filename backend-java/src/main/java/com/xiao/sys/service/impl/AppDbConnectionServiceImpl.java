package com.xiao.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiao.sys.entity.AppDbConnection;
import com.xiao.sys.mapper.AppDbConnectionMapper;
import com.xiao.sys.service.AppDbConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 数据库连接服务实现
 */
@Service
public class AppDbConnectionServiceImpl implements AppDbConnectionService {

    @Autowired
    private AppDbConnectionMapper dbConnectionMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<AppDbConnection> listConnections(Integer userId) {
        QueryWrapper<AppDbConnection> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).orderByDesc("id");
        List<AppDbConnection> list = dbConnectionMapper.selectList(wrapper);
        // 密码不返回前端
        list.forEach(c -> c.setPassword(null));
        return list;
    }

    @Override
    public AppDbConnection createConnection(AppDbConnection conn, Integer userId) {
        String now = LocalDateTime.now().format(FORMATTER);
        conn.setUserId(userId);
        conn.setCreatedAt(now);
        conn.setUpdatedAt(now);
        dbConnectionMapper.insert(conn);
        conn.setPassword(null);
        return conn;
    }

    @Override
    public AppDbConnection updateConnection(Integer id, AppDbConnection conn, Integer userId) {
        AppDbConnection existing = getConnectionById(id, userId);
        if (existing == null) {
            throw new RuntimeException("连接不存在");
        }
        conn.setId(id);
        conn.setUserId(userId);
        conn.setUpdatedAt(LocalDateTime.now().format(FORMATTER));
        // 如果密码为空，保持原密码
        if (conn.getPassword() == null || conn.getPassword().isEmpty()) {
            conn.setPassword(existing.getPassword());
        }
        dbConnectionMapper.updateById(conn);
        conn.setPassword(null);
        return conn;
    }

    @Override
    public boolean deleteConnection(Integer id, Integer userId) {
        AppDbConnection existing = getConnectionById(id, userId);
        if (existing == null) {
            return false;
        }
        return dbConnectionMapper.deleteById(id) > 0;
    }

    @Override
    public AppDbConnection getConnectionById(Integer id, Integer userId) {
        QueryWrapper<AppDbConnection> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id).eq("user_id", userId);
        return dbConnectionMapper.selectOne(wrapper);
    }
}
