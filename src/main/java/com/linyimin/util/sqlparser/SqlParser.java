package com.linyimin.util.sqlparser;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;
import com.alibaba.druid.util.JdbcConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqlParser {
    public static void  main(String [] args) throws Exception {
        String sql = "SELECT s.STUDENTID,s.STUDENTNAME,s.STUDENTCODE FROM tbl_student_info s,tbl_parents_student_map t WHERE s.STUDENTID=t.STUDENTID AND t.USERID='?'";

        List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, JdbcConstants.MYSQL);

        ExportTableSourceAndSQLSelectVisitor visitor = new ExportTableSourceAndSQLSelectVisitor();
        for (SQLStatement stmt : stmtList) {
            stmt.accept(visitor);
        }

        SQLSelect sqlSelect = (SQLSelect) visitor.getTableSourceAndSQLSelect().get("sqlSelect");
        HashMap<String, SQLTableSource> aliasTables = (HashMap<String, SQLTableSource>) visitor.getTableSourceAndSQLSelect().get("sqlTableSource");

        System.out.println(sqlSelect.getFirstQueryBlock().getSelectList().toString());

        System.out.println(aliasTables.keySet());

    }


}
