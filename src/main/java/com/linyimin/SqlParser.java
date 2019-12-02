package com.linyimin;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.util.JdbcConstants;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;
import com.linyimin.util.sqlparser.ExportTableSourceAndSQLSelectVisitor;

import java.util.*;

public class SqlParser  {


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

        System.out.println(sqlSelect.getFirstQueryBlock().getInto());

    }
}
