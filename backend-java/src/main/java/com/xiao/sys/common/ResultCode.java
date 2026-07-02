package com.xiao.sys.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "success"),
    FAIL(500, "操作失败"),
    UNAUTHORIZED(401, "未认证或认证已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    BAD_REQUEST(400, "请求参数错误"),

    USER_NOT_FOUND(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "用户名或密码错误"),
    USER_DISABLED(1003, "账号已被禁用"),
    USERNAME_EXISTS(1004, "用户名已存在"),
    TOKEN_INVALID(1005, "Token无效"),
    TOKEN_EXPIRED(1006, "Token已过期"),
    OLD_PASSWORD_ERROR(1007, "原密码错误"),

    ORG_NOT_FOUND(2001, "组织不存在"),
    ORG_CODE_EXISTS(2002, "组织编码已存在"),
    ORG_HAS_CHILDREN(2003, "存在子组织，无法删除"),
    ORG_HAS_USERS(2004, "组织下存在人员，无法删除"),

    POSITION_NOT_FOUND(3001, "职位不存在"),
    POSITION_CODE_EXISTS(3002, "职位编码已存在"),
    POSITION_HAS_USERS(3003, "职位下存在人员，无法删除"),

    ROLE_NOT_FOUND(4001, "角色不存在"),
    ROLE_CODE_EXISTS(4002, "角色编码已存在"),

    MENU_NOT_FOUND(5001, "菜单不存在"),
    MENU_HAS_CHILDREN(5002, "存在子菜单，无法删除"),

    DATA_SCOPE_NOT_FOUND(6001, "数据权限配置不存在");

    private final Integer code;
    private final String message;
}
