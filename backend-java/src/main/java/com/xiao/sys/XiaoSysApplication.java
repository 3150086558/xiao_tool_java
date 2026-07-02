package com.xiao.sys;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.xiao.sys.mapper")
public class XiaoSysApplication {

    public static void main(String[] args) {
        SpringApplication.run(XiaoSysApplication.class, args);
        System.out.println("====== XiaoSys 后端启动成功 ======");
    }
}
