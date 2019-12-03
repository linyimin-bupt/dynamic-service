package com.linyimin.util.sqlparser;

import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;

import java.util.HashMap;
import java.util.Map;

import static com.linyimin.util.Constant.SQL_SELECT;
import static com.linyimin.util.Constant.SQL_TABLE_SOURCE;

/**
 * 自定义AST访问器
 * 获取SQLStatement及SQLTableSource的所有信息
 */
public class ExportTableSourceAndSQLSelectVisitor extends MySqlASTVisitorAdapter {
    /**
     * 存放SQLSelect及SQLTableSource
     * sqlSelect --> SQLSelect变量
     * sqlTableSource --> SQLTableSource变量
     */
    private HashMap<String, Object> map = new HashMap<>();

    // table name --> SQLTableSource
    private HashMap<String, SQLTableSource> aliasTable = new HashMap();

    // 获取SQLStatement
    public boolean visit(SQLSelectStatement x) {
        map.put(SQL_SELECT, x.getSelect());
        return true;
    }

    // 获取SQLTableSource
    public boolean visit(SQLExprTableSource x) {
        // 如果不存在别名,使用真实表名, 以免alias为null, 解析不完全
        String name = x.getName().getSimpleName();
        aliasTable.put(name, x);
        return true;
    }

    public Map<String, Object> getTableSourceAndSQLSelect() {
        map.put(SQL_TABLE_SOURCE, aliasTable);
        return map;
    }
}