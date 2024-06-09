package io.github.yanfeiwuji.isupabase.request.ex;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.relation.AbstractRelation;
import com.mybatisflex.core.table.TableInfo;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@UtilityClass
public class PgrstExFactory {

    public Supplier<PgrstEx> exInsertValidatorError(Set<ConstraintViolation<Object>> errors, String tableName) {

        final List<ViolationInfo> violationInfos = errors.stream()
                .map(ViolationInfo::of)
                .toList();

        return ExCodeStatus.DB_NOT_NULL_VIOLATION.toSupplierEx(
                new ExInfo("Failing row contains",
                        null,
                        "new row for relation  \"%s  \" violates check messages"
                                .formatted(tableName)

                ), violationInfos
        );
    }


    public Supplier<PgrstEx> exCasingError(String exMsg) {
        return ExCodeStatus.DB_INVALID_INPUT.toSupplierEx(
                new ExInfo(
                        null,
                        null,
                        "invalid input syntax %s".formatted(exMsg)));
    }

    public Supplier<PgrstEx> exNotCasingType(String value) {
        return ExCodeStatus.DB_UNDEFINED_OBJECT.toSupplierEx(
                new ExInfo(
                        null,
                        null,
                        "type \"%s\" does not exist".formatted(value)));
    }

    public Supplier<PgrstEx> exDataInvalidInput(String dbType, String value) {
        return ExCodeStatus.DB_INVALID_INPUT.toSupplierEx(
                new ExInfo(
                        null,
                        null,
                        "invalid input syntax for type %s: \\\"%s\\\" ".formatted(dbType,
                                value)));
    }

    public Supplier<PgrstEx> exIsBoolButNotMatch(String sqlBool, String columnType) {
        return ExCodeStatus.DB_DATATYPE_MISMATCH.toSupplierEx(
                new ExInfo(
                        null,
                        null,
                        "argument of IS %s must be type boolean, not type %s".formatted(sqlBool,
                                columnType)));
    }

    public Supplier<PgrstEx> exTableNoPk(String tableName) {
        return ExCodeStatus.PGRST_EXT_TABLE_NO_PK.toSupplierEx(
                new ExInfo(
                        null,
                        null,
                        "%s not has primary key".formatted(tableName)
                )
        );
    }

    public Supplier<PgrstEx> exUpdateOrDeleteUseLimitMustHasOrderUniCol() {
        return ExCodeStatus.PGRST_UPDATE_DELETE_USE_LIMIT_MUST_HAS_ORDER_UNI_COL.toSupplierEx(new ExInfo(
                null,
                "Apply an 'order' using unique column(s)",
                "A 'limit' was applied without an explicit 'order'"));
    }


    public Supplier<PgrstEx> exInvalidJson() {
        return ExCodeStatus.PGRST_INVALID_REQUEST_BODY.toSupplierEx(
                new ExInfo(
                        null,
                        null,
                        "Empty or invalid json"
                )
        );
    }

    public Supplier<PgrstEx> exInvalidPreferInStrict(List<String> invalids) {

        return ExCodeStatus.PGRST_INVALID_PREFERENCES.toSupplierEx(new ExInfo(
                "Invalid preferences: %s".formatted(invalids.stream().reduce(String::concat).orElse(CharSequenceUtil.EMPTY)),
                null,
                "Invalid preferences given with handling=strict"));
    }

    public Supplier<PgrstEx> exCanNotSpreadRelForManyEnd(String root, String sub) {
        return ExCodeStatus.PGRST_CAN_NOT_SPREAD_REL_FOR_MANY_END.toSupplierEx(
                new ExInfo(null,
                        "%s' and '%s' do not form a many-to-one or one-to-one relationship".formatted(root, sub),
                        "A spread operation on '%s' is not possible".formatted(sub))
        );
    }

    public Supplier<PgrstEx> exCanNotOrderRelForManyEnd(String root, String embedded) {
        return ExCodeStatus.PGRST_CAN_NOT_ORDER_REL_FOR_MANY_END.toSupplierEx(
                new ExInfo(
                        "'%s' and '%s' do not from a many-to-one or one-to-one relationship"
                                .formatted(root, embedded),
                        null,
                        "A related order on '%s' is not possible".formatted(embedded)));
    }

    public Supplier<PgrstEx> exEmbeddedApplyButNotInSelect(String embedded) {
        return ExCodeStatus.PGRST_FILTER_APPLY_EMBEDDED_NOT_IN_SELECT
                .toSupplierEx(
                        new ExInfo(
                                null,
                                "Verify that '%s' is included in the 'select' query"
                                        .formatted(embedded),
                                "'%s' is not an embedded resource in this request"
                                        .formatted(embedded)));
    }

    public Supplier<PgrstEx> exParseFilterError(String value) {
        return ExCodeStatus.PGRST_PARSE_ERROR
                .toSupplierEx(new ExInfo(
                        "unexpected \\\"%s\\\" expecting operator (eq, gt, ...)"
                                .formatted(value),
                        "",
                        "\"failed to parse filter (%s)\"".formatted(value)));
    }

    public Supplier<PgrstEx> exParseOrderError(String value) {
        return ExCodeStatus.PGRST_PARSE_ERROR
                .toSupplierEx(new ExInfo(
                        "unexpected \\\"%s\\\" expecting".formatted(value),
                        "",
                        "\"failed to parse order (%s)\"".formatted(value)));
    }

    public Supplier<PgrstEx> exParseLogicTreeError(String value) {
        return ExCodeStatus.PGRST_PARSE_ERROR
                .toSupplierEx(new ExInfo(
                        "unexpected \\\"%s\\\" expecting operator (eq, gt, ...)"
                                .formatted(value),
                        "",
                        "\"failed to parse logic tree (%s)\"".formatted(value)));
    }

    public Supplier<PgrstEx> exRelNotExist(String root, String sub) {
        return ExCodeStatus.PGRST_REL_NOT_EXIST.toSupplierEx(new ExInfo(
                "Searched relationship between '%s' and '%s' in database ,but no matches were found."
                        .formatted(root, sub),
                null,
                "Could not find a relationship between '%s' and '%s' in the schema cache"
                        .formatted(root, sub)));
    }

    public Supplier<PgrstEx> exIsValueNotFound(String value) {
        return ExCodeStatus.PGRST_PARSE_ERROR
                .toSupplierEx(
                        new ExInfo(
                                "unexpected \\\"%s\\\" expecting null or trilean value (unknown, true, false)  ",
                                null,
                                "\\\"failed to parse filter (is.%s) \"  "
                                        .formatted(value)));

    }

    public Supplier<PgrstEx> exColumnNotFound(TableInfo tableInfo, String key) {
        return ExCodeStatus.DB_UNDEFINED_COLUMN
                .toSupplierEx(
                        new ExInfo(
                                null,
                                null,
                                "column %s.%s does not exist".formatted(
                                        tableInfo.getTableName(), key)));

    }

    public Supplier<PgrstEx> exColumnNotFound(AbstractRelation<?> relation) {
        TableInfo tableInfo = relation.getTargetTableInfo();
        String column = tableInfo.getPropertyColumnMapping().get(relation.getTargetField().getName());
        return exColumnNotFound(tableInfo, column);

    }

    public Supplier<PgrstEx> exTableNotFound(String tableName) {
        return ExCodeStatus.DB_UNDEFINED_TABLE
                .toSupplierEx(new ExInfo(
                        null,
                        null,
                        "relation \"%s\" does not exist".formatted(tableName)));
    }


}
