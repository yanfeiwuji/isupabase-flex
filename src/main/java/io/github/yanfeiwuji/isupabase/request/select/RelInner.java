package io.github.yanfeiwuji.isupabase.request.select;

import com.mybatisflex.core.relation.AbstractRelation;
import com.mybatisflex.core.table.TableInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// TODO remove change to list use pid to get tree
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RelInner {
    TableInfo tableInfo;
    AbstractRelation<?> abstractRelation;
}