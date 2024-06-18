package io.github.yanfeiwuji.isupabase.request.flex.policy;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import com.mybatisflex.core.dialect.OperateType;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;
import io.github.yanfeiwuji.isupabase.entity.SysUser;
import io.github.yanfeiwuji.isupabase.request.flex.AuthContext;
import io.github.yanfeiwuji.isupabase.request.flex.TableOneOperateConfig;
import lombok.experimental.UtilityClass;
import org.springframework.context.ApplicationContext;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author yanfeiwuji
 * @date 2024/6/16 18:38
 */
@UtilityClass
public class TableConfigUtils {
    private static final String POLICY_ALL = "ALL";
    private static final String POLICY_OTHER = "OTHER";
    private static final String POLICY_SELECT = "SELECT";
    private static final String POLICY_INSERT = "INSERT";
    private static final String POLICY_UPDATE = "UPDATE";
    private static final String POLICY_DELETE = "DELETE";

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <C extends AuthContext> Map<String, Map<OperateType, TableOneOperateConfig<C, Object>>> load(ApplicationContext context) {

        Map<String, Map<OperateType, TableOneOperateConfig<C, Object>>> config = new ConcurrentHashMap<>();
        final List<PolicyBase> policies = context.getBeansOfType(PolicyBase.class).values().stream().toList();
        // only last policy can handler
        TableInfoFactory.ofEntityClass(SysUser.class);
        final Map<String, List<PolicyBase>> policyGrouping = policies.stream().collect(Collectors.groupingBy(TableConfigUtils::operateType));


        final Map<Class<?>, TableInfo> entityTableMap = (Map<Class<?>, TableInfo>) ReflectUtil.getFieldValue(TableInfoFactory.class, "entityTableMap");

        final Map<Class<?>, List<Class<?>>> clazzSuperWithSelfList =
                entityTableMap.keySet().stream().collect(Collectors.toMap(it -> it, TableConfigUtils::superClassesWithSelf));

        final Map<Class<?>, PolicyBase> allPolicy =
                policyGrouping.get(POLICY_ALL).stream().collect(Collectors.toMap(
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


        configOneOp(POLICY_SELECT, OperateType.SELECT, policyGrouping, clazzSuperWithSelfList, config);
        configOneOp(POLICY_INSERT, OperateType.INSERT, policyGrouping, clazzSuperWithSelfList, config);
        configOneOp(POLICY_UPDATE, OperateType.UPDATE, policyGrouping, clazzSuperWithSelfList, config);
        configOneOp(POLICY_DELETE, OperateType.DELETE, policyGrouping, clazzSuperWithSelfList, config);


        return config;
    }

    private <C extends AuthContext> void configOneOp(
            String groupName,
            OperateType operateType,
            Map<String, List<PolicyBase>> policyGrouping, Map<Class<?>,
            List<Class<?>>> clazzSuperWithSelfList,
            Map<String, Map<OperateType, TableOneOperateConfig<C, Object>>> config
    ) {
        final Map<Class<?>, PolicyBase> selectPolicy = Optional.ofNullable(policyGrouping.get(groupName)).orElse(List.of())
                .stream().collect(Collectors.toMap(
                        it -> ClassUtil.getTypeArgument(it.getClass(), 1), it -> it
                ));

        clazzSuperWithSelfList.forEach((k, v) -> v.stream().filter(selectPolicy::containsKey)
                .map(selectPolicy::get)
                .findFirst().ifPresent(policyBase -> {
                    final String tableNameWithSchema = TableInfoFactory.ofEntityClass(k).getTableNameWithSchema();
                    final Map<OperateType, TableOneOperateConfig<C, Object>> innerMap = config.computeIfAbsent(tableNameWithSchema, key -> new ConcurrentHashMap<>());
                    innerMap.put(operateType, policyBase.config());
                })

        );
    }

    private <C extends AuthContext> String operateType(PolicyBase<C, Object> policy) {

        final boolean isSelect = policy instanceof SelectPolicyBase;
        if (isSelect) {
            return POLICY_SELECT;
        }
        final boolean isInsert = policy instanceof InsertPolicyBase;
        if (isInsert) {
            return POLICY_INSERT;
        }
        final boolean isUpdate = policy instanceof UpdatePolicyBase;
        if (isUpdate) {
            return POLICY_UPDATE;
        }
        final boolean isDelete = policy instanceof DeletePolicyBase;
        if (isDelete) {
            return POLICY_DELETE;
        }
        final boolean isAll = policy instanceof AllPolicyBase;
        if (isAll) {
            return POLICY_ALL;
        }
        return POLICY_OTHER;
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
