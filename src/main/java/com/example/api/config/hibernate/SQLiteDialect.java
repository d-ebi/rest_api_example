package com.example.api.config.hibernate;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.StandardBasicTypes;

import java.sql.Types;

/**
 * SQLite向けの簡易Hibernate Dialect。
 * IDENTITYとLIMITの最小実装、および基本関数の登録のみ行います。
 */
public class SQLiteDialect extends Dialect {
    public SQLiteDialect() {
        registerColumnType(Types.INTEGER, "integer");
        registerColumnType(Types.BIGINT, "bigint");
        registerColumnType(Types.FLOAT, "float");
        registerColumnType(Types.REAL, "real");
        registerColumnType(Types.DOUBLE, "double");
        registerColumnType(Types.NUMERIC, "numeric");
        registerColumnType(Types.DECIMAL, "decimal");
        registerColumnType(Types.CHAR, "char");
        registerColumnType(Types.VARCHAR, "varchar");
        registerColumnType(Types.LONGVARCHAR, "longvarchar");
        registerColumnType(Types.DATE, "date");
        registerColumnType(Types.TIME, "time");
        registerColumnType(Types.TIMESTAMP, "timestamp");
        registerColumnType(Types.BINARY, "blob");
        registerColumnType(Types.VARBINARY, "blob");
        registerColumnType(Types.LONGVARBINARY, "blob");
        registerColumnType(Types.BLOB, "blob");
        registerColumnType(Types.CLOB, "text");
        registerColumnType(Types.NCLOB, "text");

        registerFunction("lower", new StandardSQLFunction("lower"));
        registerFunction("upper", new StandardSQLFunction("upper"));
        registerFunction("length", new StandardSQLFunction("length", StandardBasicTypes.LONG));
        registerFunction("substr", new StandardSQLFunction("substr", StandardBasicTypes.STRING));
        registerFunction("coalesce", new StandardSQLFunction("coalesce"));
        registerFunction("concat", new SQLFunctionTemplate(StandardBasicTypes.STRING, "(?1 || ?2)"));
    }

    public boolean supportsIdentityColumns() { return true; }
    public boolean hasDataTypeInIdentityColumn() { return false; }
    public String getIdentityColumnString() { return "integer"; }
    public String getIdentitySelectString() { return "select last_insert_rowid()"; }
    public boolean supportsLimit() { return true; }
    public String getLimitString(String query, boolean hasOffset) {
        return query + (hasOffset ? " limit ? offset ?" : " limit ?");
    }
}
