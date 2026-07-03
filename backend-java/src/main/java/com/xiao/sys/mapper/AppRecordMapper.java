package com.xiao.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiao.sys.entity.AppRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 记账数据访问层
 */
@Mapper
public interface AppRecordMapper extends BaseMapper<AppRecord> {

    /**
     * 根据可见用户ID查询记录
     */
    @Select("""
            <script>
            SELECT * FROM records
            WHERE user_id IN
            <foreach collection="userIds" item="uid" open="(" separator="," close=")">
                #{uid}
            </foreach>
            <if test="item != null and item != ''">
                AND category LIKE CONCAT('%', #{item}, '%')
            </if>
            <if test="category != null and category != ''">
                AND sub_category LIKE CONCAT('%', #{category}, '%')
            </if>
            <if test="type != null and type != ''">
                AND type = #{type}
            </if>
            <if test="startDate != null and startDate != ''">
                AND record_date &gt;= #{startDate}
            </if>
            <if test="endDate != null and endDate != ''">
                AND record_date &lt;= #{endDate}
            </if>
            <if test="month != null and month != ''">
                AND SUBSTRING(record_date, 1, 7) = #{month}
            </if>
            <if test="keyword != null and keyword != ''">
                AND (category LIKE CONCAT('%', #{keyword}, '%')
                  OR account LIKE CONCAT('%', #{keyword}, '%')
                  OR note LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            ORDER BY record_date DESC, id DESC
            </script>
            """)
    List<AppRecord> selectRecordsByUserIds(@Param("userIds") List<Integer> userIds,
                                           @Param("item") String item,
                                           @Param("category") String category,
                                           @Param("type") String type,
                                           @Param("startDate") String startDate,
                                           @Param("endDate") String endDate,
                                           @Param("month") String month,
                                           @Param("keyword") String keyword);

    /**
     * 统计用户记录数量
     */
    @Select("SELECT COUNT(*) FROM records WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Integer userId);

    /**
     * 统计收入支出总额
     * @param userIds 可见的用户ID列表
     * @param month 月份过滤（可选）
     * @return 统计结果
     */
    @Select("<script>" +
            "SELECT type, COALESCE(SUM(amount), 0) AS total " +
            "FROM records " +
            "WHERE user_id IN " +
            "<foreach item='id' collection='userIds' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "<if test='month != null and month != \"\"'>" +
            " AND SUBSTRING(record_date, 1, 7) = #{month}" +
            "</if>" +
            " GROUP BY type" +
            "</script>")
    List<java.util.Map<String, Object>> summaryByType(@Param("userIds") List<Integer> userIds, @Param("month") String month);

    /**
     * 按分类统计
     * @param userIds 可见的用户ID列表
     * @param month 月份过滤（可选）
     * @param type 类型过滤（可选）
     * @return 分类统计结果
     */
    @Select("<script>" +
            "SELECT type, category, COALESCE(SUM(amount), 0) AS amount, COUNT(*) AS count " +
            "FROM records " +
            "WHERE user_id IN " +
            "<foreach item='id' collection='userIds' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "<if test='month != null and month != \"\"'>" +
            " AND SUBSTRING(record_date, 1, 7) = #{month}" +
            "</if>" +
            "<if test='type != null and type != \"\"'>" +
            " AND type = #{type}" +
            "</if>" +
            " GROUP BY type, category ORDER BY amount DESC" +
            "</script>")
    List<java.util.Map<String, Object>> summaryByCategory(@Param("userIds") List<Integer> userIds,
                                                          @Param("month") String month,
                                                          @Param("type") String type);

    /**
     * 按月份趋势统计
     * @param userIds 可见的用户ID列表
     * @param month 月份过滤（可选）
     * @return 趋势统计结果
     */
    @Select("<script>" +
            "SELECT SUBSTRING(record_date, 1, 7) AS month, type, COALESCE(SUM(amount), 0) AS amount " +
            "FROM records " +
            "WHERE user_id IN " +
            "<foreach item='id' collection='userIds' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "<if test='month != null and month != \"\"'>" +
            " AND SUBSTRING(record_date, 1, 7) = #{month}" +
            "</if>" +
            " GROUP BY SUBSTRING(record_date, 1, 7), type ORDER BY month" +
            "</script>")
    List<java.util.Map<String, Object>> trendByMonth(@Param("userIds") List<Integer> userIds, @Param("month") String month);
}