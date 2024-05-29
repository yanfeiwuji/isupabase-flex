package io.github.yanfeiwuji.isupabase.request.ex;

import com.mybatisflex.core.relation.AbstractRelation;
import com.mybatisflex.core.table.TableInfo;
import lombok.experimental.UtilityClass;

import java.util.function.Supplier;

@UtilityClass
public class PgrstExFactory {


    public Supplier<PgrstEx> exDataInvalidInput(String dbType, String value) {
        return ExCodeStatus.DB_INVALID_INPUT.toSupplierEx(
                new ExInfo(
                        null,
                        null,
                        "invalid input syntax for type %s: \\\"%s\\\" ".formatted(dbType, value))
        );
    }

    public Supplier<PgrstEx> exIsBoolButNotMatch(String sqlBool, String columnType) {
        return ExCodeStatus.DB_DATATYPE_MISMATCH.toSupplierEx(
                new ExInfo(
                        null,
                        null,
                        "argument of IS %s must be type boolean, not type %s".formatted(sqlBool, columnType)));
    }

    public Supplier<PgrstEx> exFilterApplyButNotInSelect(String embedded) {
        return ExCodeStatus.PGRST_FILTER_APPLY_EMBEDDED_NOT_IN_SELECT
                .toSupplierEx(
                        new ExInfo(
                                null,
                                "Verify that '%s' is included in the 'select' query".formatted(embedded),
                                "'%s' is not an embedded resource in this request".formatted(embedded)
                        )
                );
    }

    public Supplier<PgrstEx> exParseFilterError(String value) {
        return ExCodeStatus.PGRST_PARSE_ERROR
                .toSupplierEx(new ExInfo(
                        "unexpected \\\"%s\\\" expecting operator (eq, gt, ...)".formatted(value),
                        "",
                        "\"failed to parse filter (%s)\"".formatted(value)));
    }

    public Supplier<PgrstEx> exParseLogicTreeError(String value) {
        return ExCodeStatus.PGRST_PARSE_ERROR
                .toSupplierEx(new ExInfo(
                        "unexpected \\\"%s\\\" expecting operator (eq, gt, ...)".formatted(value),
                        "",
                        "\"failed to parse logic tree (%s)\"".formatted(value)));
    }

    public Supplier<PgrstEx> exRelNotExist(String root, String sub) {
        return ExCodeStatus.PGRST_REL_NOT_EXIST.toSupplierEx(new ExInfo(
                "Searched relationship between '%s' and '%s' in database ,but no matches were found.".formatted(root, sub),
                null,
                "Could not find a relationship between '%s' and '%s' in the schema cache".formatted(root, sub)
        ));
    }

    public Supplier<PgrstEx> exIsValueNotFound(String value) {
        return ExCodeStatus.PGRST_PARSE_ERROR
                .toSupplierEx(
                        new ExInfo(
                                "unexpected \\\"%s\\\" expecting null or trilean value (unknown, true, false)  ",
                                null,
                                "\\\"failed to parse filter (is.%s) \"  ".formatted(value)));

    }

    public Supplier<PgrstEx> exColumnNotFound(TableInfo tableInfo, String key) {
        return ExCodeStatus.DB_UNDEFINED_COLUMN
                .toSupplierEx(
                        new ExInfo(
                                null,
                                null,
                                "column \"%s.%s\" does not exist".formatted(tableInfo.getTableName(), key)));

    }

    public Supplier<PgrstEx> exColumnNotFound(AbstractRelation<?> relation) {
        TableInfo tableInfo = relation.getTargetTableInfo();
        String column = tableInfo.getPropertyColumnMapping().get(relation.getTargetField().getName());
        return exColumnNotFound(tableInfo, column);

    }

    public Supplier<PgrstEx> exTableNotFound(String tableName) {
        return ExCodeStatus
                .DB_UNDEFINED_TABLE
                .toSupplierEx(new ExInfo(
                        null,
                        null,
                        "relation \"%s\" does not exist".formatted(tableName)));
    }
}