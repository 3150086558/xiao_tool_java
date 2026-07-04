package com.xiao.sys;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

@SpringBootApplication
@MapperScan("com.xiao.sys.mapper")
public class XiaoSysApplication {

    public static void main(String[] args) {
        SpringApplication.run(XiaoSysApplication.class, args);
        System.out.println("====== XiaoSys 后端启动成功 ======");
    }

    @Bean
    public ApplicationRunner x2026070323xhjSchemaUpgradeRunner(DataSource dataSource) {
        return args -> {
            // 兼容旧库，启动时补齐当前版本依赖的字段和字典数据。
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.setSqlScriptEncoding("UTF-8");
            populator.setIgnoreFailedDrops(true);
            populator.addScript(new ClassPathResource("db/migration/V2__add_dict_and_fields.sql"));
            populator.addScript(new ClassPathResource("db/migration/V3__add_dict_menu.sql"));
            populator.execute(dataSource);

            // 兼容旧版 PostgreSQL 库里 completed_at 被建成 timestamp 的情况。
            try (Connection connection = dataSource.getConnection()) {
                String productName = connection.getMetaData().getDatabaseProductName();
                if (productName != null && productName.toLowerCase().contains("postgresql")) {
                    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
                    List<String> columnTypes = jdbcTemplate.queryForList(
                            "SELECT data_type FROM information_schema.columns WHERE table_name = 'todos' AND column_name = 'completed_at'",
                            String.class
                    );
                    if (!columnTypes.isEmpty() && !"character varying".equalsIgnoreCase(columnTypes.get(0))) {
                        jdbcTemplate.execute(
                                "ALTER TABLE todos ALTER COLUMN completed_at TYPE VARCHAR(30) USING CASE WHEN completed_at IS NULL THEN NULL ELSE TO_CHAR(completed_at, 'YYYY-MM-DD HH24:MI:SS') END"
                        );
                    }
                }
            }
        };
    }
}