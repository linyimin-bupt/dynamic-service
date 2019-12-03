package com.linyimin.util.sqlparser;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLLimit;
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.SQLOrderingSpecification;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.util.JdbcConstants;

import java.util.*;

import static com.linyimin.util.Constant.DESC;
import static com.linyimin.util.Constant.SQL_SELECT;
import static com.linyimin.util.Constant.SQL_TABLE_SOURCE;

public class SqlParser  {

    private ExportTableSourceAndSQLSelectVisitor visitor;

    // 保存表别名 alias --> real name
    private HashMap<String, String> tableAlias = new HashMap<>();

    // 保存列别名 alias --> real name
    private HashMap<String, String> fieldAlias = new HashMap<>();

    SqlParser(String sql) {
        // 清除多余的空格
        sql = sql.replaceAll("\\s+", " ").trim();
        List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, JdbcConstants.MYSQL);
        visitor = new ExportTableSourceAndSQLSelectVisitor();
        for (SQLStatement stmt : stmtList) {
            stmt.accept(visitor);
        }

        /**
         * 获取from下的所有表名称及其别名
         */
        HashMap<String, SQLTableSource> aliasTables = (HashMap<String, SQLTableSource>) visitor.getTableSourceAndSQLSelect().get(SQL_TABLE_SOURCE);
        for (Map.Entry<String, SQLTableSource> entry : aliasTables.entrySet()) {
            String name = entry.getKey();
            String alias = entry.getValue().getAlias();
            alias = alias !=null ? alias : name;
            tableAlias.put(alias, name);
        }

        /**
         * 获取select下所有字段及其别名
         */
        SQLSelect sqlSelect = (SQLSelect) visitor.getTableSourceAndSQLSelect().get(SQL_SELECT);
        List<SQLSelectItem> items = sqlSelect.getQueryBlock().getSelectList();

        for(SQLSelectItem item : items) {
            String alias = item.getAlias();
            String name = item.getExpr().toString();
            alias = alias != null ? alias : name;
            fieldAlias.put(alias, name);
        }
    }

    /**
     * 获取select下所有的字段名称
     * @return
     */
    public List<String> parseSelect() {
        List<String> fields = new ArrayList<>();
        for (Map.Entry<String, String> entry : fieldAlias.entrySet()) {
            String name = entry.getValue();
            // 将别名替换成真实表名
            name = aliasToReal(name);
            fields.add(name);
        }
        return fields;
    }

    /**
     * 获取from下的所有表名称
     * @return
     */
    public List<String> parseFrom() {
        List<String> tables = new ArrayList<>();
        for (Map.Entry<String, String> entry : tableAlias.entrySet()) {
            String name = entry.getValue();
            tables.add(name);
        }
        return tables;
    }

    /**
     * 返回limit的解析
     * @return
     */
    public Map<String, Integer> parseLimit() {
        Map<String, Integer> map = new HashMap<>();
        SQLSelect sqlSelect = (SQLSelect) visitor.getTableSourceAndSQLSelect().get(SQL_SELECT);
        SQLLimit limit = sqlSelect.getQueryBlock().getLimit();
        if (limit.getRowCount() != null) {
            map.put("limit", Integer.valueOf(limit.getRowCount().toString()));
        }
        if (limit.getOffset() != null) {
            map.put("offset", Integer.valueOf(limit.getOffset().toString()));
        }
        return map;
    }

    /**
     * 返回order的解析
     * 索引序号表示排序的顺序
     * name --> order
     * @return
     */
    public List<Map<String, String>> parseOrder() {
        List<Map<String, String>> list = new ArrayList<>();
        SQLSelect sqlSelect = (SQLSelect) visitor.getTableSourceAndSQLSelect().get(SQL_SELECT);
        SQLOrderBy orderBy = sqlSelect.getQueryBlock().getOrderBy();
        if (orderBy != null) {
            List<SQLSelectOrderByItem> items = orderBy.getItems();
            for (SQLSelectOrderByItem item : items) {
                Map<String, String> map = new HashMap<>();
                String name = item.getExpr().toString();
                SQLOrderingSpecification orderType = item.getType();
                String type = orderType == null ? DESC : orderType.name_lcase;
                map.put(name, type);
                list.add(map);
            }
        }
        return list;
    }

    /**
     * 将select后的含表别名的字段映射成真实表名的字段
     * s.name --> mysql.user.name
     * @param field
     * @return
     */
    private String aliasToReal(String field) {
        int index = field.lastIndexOf(".");
        String alias = field.substring(0, index);
        String fieldName = field.substring(index+1);
        String table = tableAlias.get(alias);
        if (table != null) {
            return table + "." + fieldName;
        }
        return field;
    }


    /**
     *
     * @param args
     * @throws Exception
     */

    public static void  main(String [] args) throws Exception {
        String sql = "SELECT s.STUDENTID as test " +
                    ",s.STUDENTNAME,s.STUDENTCODE FROM tbl_student_info s ," +
                "tbl_parents_student_map t WHERE s.STUDENTID=t.STUDENTID AND t.USERID='?' " +
                "ORDER BY expression DESC, test ASC LIMIT 10 OFFSET 2;";
        SqlParser parser = new SqlParser(sql);

        System.out.println("-------select-----------");
        System.out.println(parser.parseSelect());
        System.out.println("-------select-----------");

        System.out.println("------- from -----------");
        System.out.println(parser.parseFrom());
        System.out.println("------- from -----------");

        System.out.println("------- order ----------");
        System.out.println(parser.parseOrder());
        System.out.println("------- order ----------");

        System.out.println("------- limit -----------");
        System.out.println(parser.parseLimit());
        System.out.println("------- limit -----------");
    }
}
