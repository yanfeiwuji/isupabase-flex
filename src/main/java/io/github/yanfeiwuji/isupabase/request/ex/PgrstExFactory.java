package io.github.yanfeiwuji.isupabase.request.ex;

import com.mybatisflex.core.relation.AbstractRelation;
import com.mybatisflex.core.table.TableInfo;
import lombok.experimental.UtilityClass;

import java.util.function.Supplier;

@UtilityClass
public class PgrstExFactory {
    public Supplier<PgrstEx> exColumnNotFound(TableInfo tableInfo, String key) {
        return ExCodeStatus.DB_UNDEFINED_COLUMN
                .toSupplierEx(
                        new ExInfo(
                                null,
                                null,
                                ExInfo.UNDEFINED_COLUMN_TEMP.formatted(tableInfo.getTableName(), key)));

    }

    public Supplier<PgrstEx> exColumnNotFound(AbstractRelation<?> relation) {
        TableInfo tableInfo = relation.getTargetTableInfo();
        String column = tableInfo.getPropertyColumnMapping().get(relation.getTargetField().getName());
        return exColumnNotFound(tableInfo, column);

    }

    public Supplier<PgrstEx> exTableNotFound(String tableName) {
        return ExCodeStatus
                .DB_UNDEFINED_TABLE
                .toSupplierEx(new ExInfo(null, null, ExInfo.TABLE_NOT_FOUND_TEMP.formatted(tableName)));
    }
}
