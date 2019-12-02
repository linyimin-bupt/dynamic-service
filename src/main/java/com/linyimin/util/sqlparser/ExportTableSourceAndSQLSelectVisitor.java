package com.linyimin.util.sqlparser;

import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;

import java.util.HashMap;
import java.util.Map;

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

    // alias --> SQLTableSource
    private HashMap<String, SQLTableSource> aliasTable = new HashMap();

    // 获取SQLStatement
    public boolean visit(SQLSelectStatement x) {
        map.put("sqlSelect", x.getSelect());
        return true;
    }

    // 获取SQLTableSource
    public boolean visit(SQLExprTableSource x) {
        aliasTable.put(x.getAlias(), x);
        return true;
    }

    public Map<String, Object> getTableSourceAndSQLSelect() {
        map.put("sqlTableSource", aliasTable);
        return map;
    }
}