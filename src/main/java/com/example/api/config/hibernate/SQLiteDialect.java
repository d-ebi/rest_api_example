package com.example.api.config.hibernate;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.dialect.identity.IdentityColumnSupport;
import org.hibernate.dialect.identity.IdentityColumnSupportImpl;

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

    /** SQLite専用のIDENTITYサポートを返します。 */
    @Override
    public IdentityColumnSupport getIdentityColumnSupport() {
        return SQLiteIdentityColumnSupport.INSTANCE;
    }
    /** LIMIT句をサポートするためtrueを返します。 */
    @Override
    public boolean supportsLimit() { return true; }
    /**
     * SQLite用にLIMIT句（必要に応じてOFFSETを含む）を構築します。
     *
     * @param query     元のSQL
     * @param hasOffset OFFSET指定が必要かどうか
     * @return LIMIT付きクエリ
     */
    public String getLimitString(String query, boolean hasOffset) {
        return query + (hasOffset ? " limit ? offset ?" : " limit ?");
    }

    private static final class SQLiteIdentityColumnSupport extends IdentityColumnSupportImpl {
        private static final SQLiteIdentityColumnSupport INSTANCE = new SQLiteIdentityColumnSupport();

        @Override
        public boolean supportsIdentityColumns() {
            return true;
        }

        @Override
        public boolean hasDataTypeInIdentityColumn() {
            return false;
        }

        @Override
        public String getIdentityColumnString(int type) {
            return "integer";
        }

        @Override
        public String getIdentitySelectString(String table, String column, int type) {
            return "select last_insert_rowid()";
        }
    }
}
