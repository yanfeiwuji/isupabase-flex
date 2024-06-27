package io.github.yanfeiwuji.isupabase.storage.vo;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mybatisflex.core.query.*;
import io.github.yanfeiwuji.isupabase.constants.PgrstStrPool;
import io.github.yanfeiwuji.isupabase.storage.ex.StorageExFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static io.github.yanfeiwuji.isupabase.storage.entity.table.StorageObjectTableDef.STORAGE_OBJECT;

/**
 * select max(id)                            as id,
 * <p>
 * instr(substr(name, 5), '/') > 0    as pos,
 * (case
 * when instr(substr(name, 5), '/') > 0 then left(name, instr(substr(name, 5), '/') + 5)
 * else name end)                as dir,
 * max(case
 * when instr(substr(name, 5), '/') > 0 then null
 * else created_at end)       as created_at,
 * max(case
 * when instr(substr(name, 5), '/') > 0 then null
 * else updated_at end)       as updated_at,
 * max(case
 * when instr(substr(name, 5), '/') > 0 then null
 * else last_accessed_at end) as last_accessed_at
 * <p>
 * from storage_object
 * where name like 'ass/%'
 * group by dir, pos
 * order by pos desc, substr(dir, 5) desc;
 *
 * @author yanfeiwuji
 * @date 2024/6/20 17:37
 */
public record ObjectListParam(Long limit, Long offset, String prefix,
                              @JsonProperty("sortBy") ObjectListParamOrder sortBy) {
    private static final Set<String> ALLOW_SORT_ORDER = CollUtil.set(false, PgrstStrPool.ASC, PgrstStrPool.DESC);
    private static final Set<String> ALLOW_SORT_COLUMN = CollUtil.set(false, "name", "updated_at", "created_at", "last_accessed_at");

    /// todo gen querywrapper
    public QueryWrapper toQueryWrapper(String bucketId) {

        validSort(sortBy);
        final String needPrefix = Optional.ofNullable(prefix).orElse(CharSequenceUtil.EMPTY);
        final int prefixLength = needPrefix.length() + 1;
        QueryColumn POS_QUERY_COLUMN = new RawQueryColumn("instr(substr(name, %d), '/') > 0".formatted(prefixLength));

        QueryColumn POS_CONDITION_COLUMN = QueryMethods.instr(QueryMethods.substring(STORAGE_OBJECT.NAME, prefixLength), QueryMethods.string("/"));

        QueryColumn DIR_COLUMN = new RawQueryColumn("left(name, instr(substr(name, %d), '/'))".formatted(prefixLength));
        QueryMethods.instr(QueryMethods.substring(STORAGE_OBJECT.NAME, prefixLength), new RawQueryColumn("/"))
                .ge(0);

        return QueryWrapper.create().select(
                        QueryMethods.max(STORAGE_OBJECT.ID).as("id"),
                        POS_QUERY_COLUMN.as("pos"),
                        QueryMethods.case_().when(POS_CONDITION_COLUMN.clone().gt(0))
                                .then(DIR_COLUMN)
                                .else_(STORAGE_OBJECT.NAME)
                                .end().as("dir"),
                        QueryMethods.max(QueryMethods.case_().when(POS_CONDITION_COLUMN.clone().gt(0))
                                .then(QueryMethods.null_())
                                .else_(STORAGE_OBJECT.CREATED_AT)
                                .end()).as("created_at"),
                        QueryMethods.max(QueryMethods.case_().when(POS_CONDITION_COLUMN.clone().gt(0))
                                .then(QueryMethods.null_())
                                .else_(STORAGE_OBJECT.UPDATED_AT)
                                .end()).as("updated_at"),
                        QueryMethods.max(QueryMethods.case_().when(POS_CONDITION_COLUMN.clone().gt(0))
                                .then(QueryMethods.null_())
                                .else_(STORAGE_OBJECT.LAST_ACCESSED_AT)
                                .end()).as("last_accessed_at")

                ).from(STORAGE_OBJECT)
                .where(STORAGE_OBJECT.NAME.likeLeft(CharSequenceUtil.isEmpty(prefix) ? prefix : prefix + StrPool.SLASH))
                .and(STORAGE_OBJECT.BUCKET_ID.eq(bucketId))
                .groupBy("pos", "dir")
                .orderBy("pos", false)
                .orderBy(sortBy.column(), CharSequenceUtil.equals(sortBy.order(), PgrstStrPool.ASC))
                .limit(Optional.ofNullable(limit).orElse(100L))
                .offset(Optional.ofNullable(offset).orElse(0L));


    }

    public void validSort(ObjectListParamOrder sortBy) {
        if (Objects.isNull(sortBy)) {
            return;
        }
        final boolean validOrder = ALLOW_SORT_ORDER.contains(sortBy.order());
        if (!validOrder) {
            throw StorageExFactory.SORT_ORDER_NOT_ALLOW;
        }
        final boolean validColumn = ALLOW_SORT_COLUMN.contains(sortBy.column());
        if (!validColumn) {
            throw StorageExFactory.SORT_COLUMN_NOT_ALLOW;
        }
    }
}