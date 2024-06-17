package io.github.yanfeiwuji.isupabase.flex.policy;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import com.mybatisflex.core.dialect.OperateType;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;
import io.github.yanfeiwuji.isupabase.entity.SysUser;
import io.github.yanfeiwuji.isupabase.flex.AuthContext;
import io.github.yanfeiwuji.isupabase.flex.TableOneOperateConfig;
import lombok.experimental.UtilityClass;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author yanfeiwuji
 * @date 2024/6/16 18:38
 */
@UtilityClass
public class TableConfigUtils {
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <C extends AuthContext> Map<String, Map<OperateType, TableOneOperateConfig<C, Object>>> load(ApplicationContext context) {

        Map<String, Map<OperateType, TableOneOperateConfig<C, Object>>> config = new ConcurrentHashMap<>();
        final List<IPolicy> policies = context.getBeansOfType(IPolicy.class).values().stream().toList();
        // only last policy can handler
        TableInfoFactory.ofEntityClass(SysUser.class);

        final Map<Class<?>, TableInfo> entityTableMap = (Map<Class<?>, TableInfo>) ReflectUtil.getFieldValue(TableInfoFactory.class, "entityTableMap");

        final Map<Class<?>, List<Class<?>>> clazzSuperWithSelfList =
                entityTableMap.keySet().stream().collect(Collectors.toMap(it -> it, TableConfigUtils::superClassesWithSelf));

        final Map<Class<?>, IPolicy> allPolicy = policies.stream().filter(IAllPolicy.class::isInstance).collect(Collectors.toMap(
                it -> ClassUtil.getTypeArgument(it.getClass(), 1), it -> it
        ));

        clazzSuperWithSelfList.forEach((k, v) -> v.stream().filter(allPolicy::containsKey)
                .map(allPolicy::get)
                .findFirst()
                .ifPresent(policy -> {
                    final String tableNameWithSchema = TableInfoFactory.ofEntityClass(k).getTableNameWithSchema();
                    final Map<OperateType, TableOneOperateConfig<C, Object>> innerMap = config.computeIfAbsent(tableNameWithSchema, key -> new ConcurrentHashMap<>());
                    innerMap.put(OperateType.SELECT, policy.config());
                    innerMap.put(OperateType.INSERT, policy.config());
                    innerMap.put(OperateType.UPDATE, policy.config());
                    innerMap.put(OperateType.DELETE, policy.config());
                }));

        final Map<Class<?>, IPolicy> otherPolicies = policies.stream().filter(it -> !(it instanceof IAllPolicy)).collect(Collectors.toMap(
                it -> ClassUtil.getTypeArgument(it.getClass(), 1), it -> it
        ));

        clazzSuperWithSelfList.forEach((k, v) -> v.stream().filter(otherPolicies::containsKey).
                map(otherPolicies::get)
                .findFirst().ifPresent(policy -> {
                    final String tableNameWithSchema = TableInfoFactory.ofEntityClass(k).getTableNameWithSchema();
                    final Map<OperateType, TableOneOperateConfig<C, Object>> innerMap = config.computeIfAbsent(tableNameWithSchema, key -> new ConcurrentHashMap<>());
                    innerMap.put(operateType(policy), policy.config());
                })
        );


        return config;
    }

    private <C extends AuthContext> OperateType operateType(IPolicy<C, Object> policy) {

        final boolean isSelect = policy instanceof ISelectPolicy;
        if (isSelect) {
            return OperateType.SELECT;
        }
        final boolean isInsert = policy instanceof IInsertPolicy;
        if (isInsert) {
            return OperateType.INSERT;
        }
        final boolean isUpdate = policy instanceof IUpdatePolicy;
        if (isUpdate) {
            return OperateType.UPDATE;
        }
        final boolean isDelete = policy instanceof IDeletePolicy;
        if (isDelete) {
            return OperateType.DELETE;
        }

        throw new PolicyException();
    }


    private List<Class<?>> superClassesWithSelf(Class<?> clazz) {
        final ArrayList<Class<?>> classes = new ArrayList<>();
        classes.add(clazz);
        superClasses(clazz, classes);
        return classes;
    }

    private void superClasses(Class<?> clazz, List<Class<?>> superClasses) {
        clazz = clazz.getSuperclass();
        if (Objects.equals(clazz, Object.class)) {
            return;
        } else {
            superClasses.add(clazz);
        }
        superClasses(clazz, superClasses);

    }

}
