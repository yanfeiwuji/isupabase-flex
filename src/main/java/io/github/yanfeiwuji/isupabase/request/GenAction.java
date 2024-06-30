package io.github.yanfeiwuji.isupabase.request;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mybatisflex.annotation.EnumValue;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.relation.RelationManager;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;
import io.github.yanfeiwuji.isupabase.auth.entity.AuthBase;
import io.github.yanfeiwuji.isupabase.request.anno.Rpc;
import io.github.yanfeiwuji.isupabase.request.anno.RpcMapping;
import io.github.yanfeiwuji.isupabase.request.validate.Valid;
import io.github.yanfeiwuji.isupabase.storage.entity.StorageBase;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.security.util.FieldUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author yanfeiwuji
 * @date 2024/6/25 16:33
 */
@RequestMapping("meta/generators")
@RestController
@RequiredArgsConstructor
public class GenAction {
    private final ApplicationContext applicationContext;
    private static final String FIELD_PRE = CharSequenceUtil.repeat("    ", 5);
    private static final String ENUM_PRE = CharSequenceUtil.repeat("    ", 3);
    private static final String COMPOSITE_TYPE_PRE = CharSequenceUtil.repeat("    ", 3);
    private static final String COMPOSITE_TYPE_FIELD_PRE = CharSequenceUtil.repeat("    ", 4);

    private static final String FUNC_PRE = CharSequenceUtil.repeat("    ", 4);
    private static final String FUNC_NULL_PRE = CharSequenceUtil.repeat("    ", 3);
    private static final String FUNC_ITEM_PRE = CharSequenceUtil.repeat("    ", 5);

    private static final String NON_NULL_FIELD_TEMP = FIELD_PRE + "%s: %s";
    private static final String NON_NULL_VALUE_NULL_FIELD_TEMP = FIELD_PRE + "%s: %s | null";

    private static final String NULL_FIELD_TEMP = FIELD_PRE + "%s?: %s | null";
    private static final String NULL_VAL_NOT_NULL_FIELD_TEMP = FIELD_PRE + "%s?: %s";

    private static final String NON_NULL_COMPOSITE_TYPE_TEMP = COMPOSITE_TYPE_FIELD_PRE + "%s: %s";
    private static final String NON_NULL_VALUE_NULL_COMPOSITE_TYPE_TEMP = COMPOSITE_TYPE_FIELD_PRE + "%s: %s | null";

    private static final String NON_NULL_FUNC_ARG_TEMP = FUNC_ITEM_PRE + "%s: %s";
    private static final String NON_NULL_VALUE_NULL_FUNC_ARG_TEMP = FUNC_ITEM_PRE + "%s: %s | null";

    private static final String FUNC_NULL = "Record<PropertyKey, never>";
    private static final String FUNC_NULL_ARRAY = "Record<PropertyKey, never>[]";
    public static final String UNDERFINED = "undefined";

    private static final String NEVER = "[_ in never]: never;";

    private static final String ENUM_TEMP = ENUM_PRE + "%s: %s";

    private static final String HEADER = """
            export type Json =
                | string
                | number
                | boolean
                | null
                | { [key: string]: Json | undefined }
                | Json[]
            """;
    private static final String DATABASE_TEMP = """
            export type Database = {
                public: {
                    Tables: {
                        %s
                    };
                    Views: {
                        [_ in never]: never;
                    };
                    Functions: {
            %s
                    };
                    Enums: {
            %s
                    };
                    CompositeTypes: {
            %s
                    };
                };
            };
            """;
    private static final String COMPOSITE_TYPE_TEMP = """
                        
                        %s: {
            %s
                        }
            """;
    private static final String TABLE_TEMP = """
                        
                        %s: {
                            Row: {
            %s
                            };
                            Insert: {
            %s
                            };
                            Update: {
            %s
                            };
                            Relationships: [
            %s
                            ];
                        };
            """;
    private static final String FUNC_TEMP = """
                        %s: {
                            Args: %s;
                            Returns: %s;
                        };
            """;

    private static final String FOOTER = """
                        
            type PublicSchema = Database[Extract<keyof Database, "public">];
                        
            export type Tables<
              PublicTableNameOrOptions extends
                | keyof (PublicSchema["Tables"] & PublicSchema["Views"])
                | { schema: keyof Database },
              TableName extends PublicTableNameOrOptions extends { schema: keyof Database }
                ? keyof (Database[PublicTableNameOrOptions["schema"]]["Tables"] &
                    Database[PublicTableNameOrOptions["schema"]]["Views"])
                : never = never
            > = PublicTableNameOrOptions extends { schema: keyof Database }
              ? (Database[PublicTableNameOrOptions["schema"]]["Tables"] &
                  Database[PublicTableNameOrOptions["schema"]]["Views"])[TableName] extends {
                  Row: infer R;
                }
                ? R
                : never
              : PublicTableNameOrOptions extends keyof (PublicSchema["Tables"] &
                  PublicSchema["Views"])
              ? (PublicSchema["Tables"] &
                  PublicSchema["Views"])[PublicTableNameOrOptions] extends {
                  Row: infer R;
                }
                ? R
                : never
              : never;
                        
            export type TablesInsert<
              PublicTableNameOrOptions extends
                | keyof PublicSchema["Tables"]
                | { schema: keyof Database },
              TableName extends PublicTableNameOrOptions extends { schema: keyof Database }
                ? keyof Database[PublicTableNameOrOptions["schema"]]["Tables"]
                : never = never
            > = PublicTableNameOrOptions extends { schema: keyof Database }
              ? Database[PublicTableNameOrOptions["schema"]]["Tables"][TableName] extends {
                  Insert: infer I;
                }
                ? I
                : never
              : PublicTableNameOrOptions extends keyof PublicSchema["Tables"]
              ? PublicSchema["Tables"][PublicTableNameOrOptions] extends {
                  Insert: infer I;
                }
                ? I
                : never
              : never;
                       
            export type TablesUpdate<
              PublicTableNameOrOptions extends
                | keyof PublicSchema["Tables"]
                | { schema: keyof Database },
              TableName extends PublicTableNameOrOptions extends { schema: keyof Database }
                ? keyof Database[PublicTableNameOrOptions["schema"]]["Tables"]
                : never = never
            > = PublicTableNameOrOptions extends { schema: keyof Database }
              ? Database[PublicTableNameOrOptions["schema"]]["Tables"][TableName] extends {
                  Update: infer U;
                }
                ? U
                : never
              : PublicTableNameOrOptions extends keyof PublicSchema["Tables"]
              ? PublicSchema["Tables"][PublicTableNameOrOptions] extends {
                  Update: infer U;
                }
                ? U
                : never
              : never;
                       
            export type Enums<
              PublicEnumNameOrOptions extends
                | keyof PublicSchema["Enums"]
                | { schema: keyof Database },
              EnumName extends PublicEnumNameOrOptions extends { schema: keyof Database }
                ? keyof Database[PublicEnumNameOrOptions["schema"]]["Enums"]
                : never = never
            > = PublicEnumNameOrOptions extends { schema: keyof Database }
              ? Database[PublicEnumNameOrOptions["schema"]]["Enums"][EnumName]
              : PublicEnumNameOrOptions extends keyof PublicSchema["Enums"]
              ? PublicSchema["Enums"][PublicEnumNameOrOptions]
              : never;
                        
            """;

    private String returnTypescript;
    private List<Class<?>> allFieldClazz;

    @GetMapping(value = "/typescript", produces = "text/plain")
    public String typescript() {
        if (returnTypescript != null) {
            return returnTypescript;
        }
        final Map<String, BaseMapper> beansOfType = applicationContext.getBeansOfType(BaseMapper.class);
        final Collection<BaseMapper> mappers = beansOfType.values();
        final List<TableInfo> tableInfos = mappers.stream()
                .map(Object::getClass).map(TableInfoFactory::ofMapperClass).filter(it -> {
                    final Class<?> entityClass = it.getEntityClass();
                    return clazzTableFilter(entityClass);
                }).toList();
        final String tables = tableInfos.stream()
                .map(tableInfo -> TABLE_TEMP.formatted(tableInfo.getTableName(), tableInfoToRow(tableInfo), tableInfoToInsert(tableInfo), tableInfoToUpdate(tableInfo), "")).collect(Collectors.joining());

        final String enums = enums();
        final String compositeTypes = compositeTypes();
        final String functions = functions();
        final String database = DATABASE_TEMP.formatted(tables, functions, enums, compositeTypes);
        returnTypescript = String.join("\n", HEADER, database, FOOTER);
        return returnTypescript;
    }

    public String tableInfoToRow(TableInfo tableInfo) {
        String ids = tableInfo.getPrimaryKeyList().stream().map(it -> NON_NULL_FIELD_TEMP.formatted(it.getColumn(), propertyToType(it.getPropertyType(), it.getProperty(), tableInfo.getEntityClass()))).collect(Collectors.joining(";\n"));
        final Class<?> entityClass = tableInfo.getEntityClass();

        String fields = tableInfo.getColumnInfoList().stream()
                .map(it -> {
                    final Field field = FieldUtils.getField(entityClass, it.getProperty());
                    if (field.isAnnotationPresent(NotNull.class)) {
                        return NON_NULL_FIELD_TEMP.formatted(it.getColumn(), propertyToType(it.getPropertyType(), it.getProperty(), tableInfo.getEntityClass()));
                    } else {
                        return NON_NULL_VALUE_NULL_FIELD_TEMP.formatted(it.getColumn(), propertyToType(it.getPropertyType(), it.getProperty(), tableInfo.getEntityClass()));
                    }
                }).collect(Collectors.joining(";\n"));

        String relFields = relFields(tableInfo.getEntityClass());
        return String.join(";\n", ids, fields, relFields);
    }

    public String tableInfoToInsert(TableInfo tableInfo) {
        String ids = tableInfo.getPrimaryKeyList().stream().map(it -> {
            final KeyType keyType = it.getKeyType();
            if (keyType.equals(KeyType.None)) {
                return NON_NULL_FIELD_TEMP.formatted(it.getColumn(), propertyToType(it.getPropertyType(), it.getProperty(), tableInfo.getEntityClass()));
            } else {
                return NULL_VAL_NOT_NULL_FIELD_TEMP.formatted(it.getColumn(), propertyToType(it.getPropertyType(), it.getProperty(), tableInfo.getEntityClass()));
            }
        }).collect(Collectors.joining(";\n"));
        final Class<?> entityClass = tableInfo.getEntityClass();
        String fields = tableInfo.getColumnInfoList().stream().map(it -> {
            final Field field = FieldUtils.getField(entityClass, it.getProperty());
            final NotNull[] annotationsByType = field.getAnnotationsByType(NotNull.class);
            final boolean hasInsertNotNull = Arrays.stream(annotationsByType).map(NotNull::groups).anyMatch(groups -> Arrays.asList(groups).contains(Valid.Insert.class));
            if (hasInsertNotNull) {
                return NON_NULL_FIELD_TEMP.formatted(it.getColumn(), propertyToType(it.getPropertyType(), it.getProperty(), tableInfo.getEntityClass()));
            } else {
                return NULL_FIELD_TEMP.formatted(it.getColumn(), propertyToType(it.getPropertyType(), it.getProperty(), tableInfo.getEntityClass()));
            }
        }).collect(Collectors.joining(";\n"));
        return String.join(";\n", ids, fields);
    }

    public String tableInfoToUpdate(TableInfo tableInfo) {
        String ids = tableInfo.getPrimaryKeyList().stream().map(it -> {
            final KeyType keyType = it.getKeyType();
            if (keyType.equals(KeyType.None)) {
                return NON_NULL_FIELD_TEMP.formatted(it.getColumn(), propertyToType(it.getPropertyType(), it.getProperty(), tableInfo.getEntityClass()));
            } else {
                return NULL_VAL_NOT_NULL_FIELD_TEMP.formatted(it.getColumn(), propertyToType(it.getPropertyType(), it.getProperty(), tableInfo.getEntityClass()));
            }
        }).collect(Collectors.joining(";\n"));
        final Class<?> entityClass = tableInfo.getEntityClass();
        String fields = tableInfo.getColumnInfoList().stream().map(it -> {
            final Field field = FieldUtils.getField(entityClass, it.getProperty());
            final NotNull[] annotationsByType = field.getAnnotationsByType(NotNull.class);
            final boolean hasUpdateNotNull = Arrays.stream(annotationsByType).map(NotNull::groups).anyMatch(groups -> Arrays.asList(groups).contains(Valid.Update.class));
            if (hasUpdateNotNull) {
                return NON_NULL_FIELD_TEMP.formatted(it.getColumn(), propertyToType(it.getPropertyType(), it.getProperty(), tableInfo.getEntityClass()));
            } else {
                return NULL_FIELD_TEMP.formatted(it.getColumn(), propertyToType(it.getPropertyType(), it.getProperty(), tableInfo.getEntityClass()));
            }
        }).collect(Collectors.joining(";\n"));

        return String.join(";\n", ids, fields);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public String enums() {
        String enums = allFieldClazz().stream()
                .filter(Class::isEnum)
                .map(it -> (Class<Enum>) it).distinct().map(enumClass -> {
                    final String enumName = CharSequenceUtil.toUnderlineCase(enumClass.getSimpleName()).toLowerCase();

                    final LinkedHashMap<String, Enum> enumMap = EnumUtil.getEnumMap(enumClass);
                    final Field field = Arrays.stream(ClassUtil.getDeclaredFields(enumClass)).filter(it -> it.isAnnotationPresent(EnumValue.class)).findFirst().orElse(null);
                    if (Objects.nonNull(field)) {
                        final String ENUM_ITEM_TEMP = Optional.of(field).map(Field::getType).filter(it -> ClassUtil.isAssignable(Number.class, it)).map(it -> " %s ").orElse(" \"%s\" ");

                        final String enumsValues = enumMap.values().stream().map(it -> ReflectUtil.getFieldValue(it, field)).map(Object::toString).distinct().map(ENUM_ITEM_TEMP::formatted).collect(Collectors.joining("|"));
                        return ENUM_TEMP.formatted(enumName, enumsValues);
                    }
                    return null;
                }).filter(Objects::nonNull).collect(Collectors.joining(";\n"));
        return Optional.of(enums).filter(StrUtil::isNotBlank).orElse(ENUM_PRE + NEVER);
    }

    public String compositeTypes() {
        String compositeTypes = allFieldClazz().stream().filter(it -> !it.isAnnotationPresent(Table.class))
                .filter(it -> !it.equals(Object.class))
                .filter(it -> !it.equals(byte.class))
                .filter(it -> !TableInfoFactory.defaultSupportColumnTypes.contains(it))
                .filter(it -> !ClassUtil.isAssignable(Map.class, it))
                .filter(it -> !ClassUtil.isAssignable(Collection.class, it))
                .filter(it -> !it.isEnum())
                .map(it -> {
                    final String typeName = CharSequenceUtil.toUnderlineCase(it.getSimpleName()).toLowerCase();

                    final Field[] fields = ReflectUtil.getFields(it);
                    final String info = Arrays.stream(fields).filter(this::fieldShouldShow).map(field -> {
                        final String type = propertyToType(field.getType(), field.getName(), it);
                        final boolean notNullMark = field.isAnnotationPresent(NotNull.class);
                        final String temp = notNullMark ? NON_NULL_COMPOSITE_TYPE_TEMP : NON_NULL_VALUE_NULL_COMPOSITE_TYPE_TEMP;
                        return temp.formatted(CharSequenceUtil.toUnderlineCase(field.getName()).toLowerCase(), type);
                    }).collect(Collectors.joining(";\n"));
                    return COMPOSITE_TYPE_TEMP.formatted(typeName, info);
                }).collect(Collectors.joining());

        return Optional.of(compositeTypes)
                .filter(StrUtil::isNotEmpty)
                .orElse(COMPOSITE_TYPE_PRE + NEVER);
    }

    public String functions() {
        final Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(RpcMapping.class);
        String functions = beansWithAnnotation.values()
                .stream()
                .flatMap(rpcMapping -> Arrays.stream(ReflectUtil.getMethods(rpcMapping.getClass())))
                .filter(it -> it.isAnnotationPresent(Rpc.class))
                .filter(it -> Arrays.stream(it.getParameters())
                                      .noneMatch(parameter -> parameter.isAnnotationPresent(RequestMapping.class))
                              || Arrays.stream(it.getParameters())
                                      .filter(parameter -> parameter.isAnnotationPresent(RequestBody.class))
                                      .findFirst()
                                      .filter(parameter -> !ClassUtil.isAssignable(Collection.class, parameter.getType()))
                                      .filter(parameter -> !ClassUtil.isAssignable(Map.class, parameter.getType()))
                                      .isPresent()
                )
                .map(it -> {
                    final String[] value = it.getAnnotation(Rpc.class).value();
                    String funcName = value[0];
                    String args = Arrays.stream(it.getParameters()).filter(parameter -> parameter.isAnnotationPresent(RequestBody.class))
                            .findFirst().map(parameter -> {
                                String tableType = Optional.ofNullable(parameter.getAnnotation(Validated.class)).map(Validated::value)
                                        .map(groups -> {
                                            if (ArrayUtil.contains(groups, Valid.Update.class)) {
                                                return "Update";
                                            } else if (ArrayUtil.contains(groups, Valid.Insert.class)) {
                                                return "Insert";
                                            } else {
                                                return "Row";
                                            }
                                        }).orElse("Row");

                                final Class<?> clazz = parameter.getType();
                                final Type type = parameter.getParameterizedType();
                                return funcClazzTypeToString(clazz, type, tableType);
                            }).filter(StrUtil::isNotEmpty).orElse(FUNC_NULL);

                    final String returns = it.getReturnType().equals(Void.TYPE) ? UNDERFINED : funcClazzTypeToString(it.getReturnType(), it.getGenericReturnType(), "Row");

                    return FUNC_TEMP.formatted(funcName, args, returns);
                })
                .collect(Collectors.joining("\n"));
        return Optional.of(functions).filter(StrUtil::isNotBlank).orElse(FUNC_NULL_PRE + NEVER);
    }

    public String propertyToType(Class<?> propertyType, String property, Class<?> entityClass) {
        return propertyToType(propertyType, property, entityClass, "Row");
    }

    public String propertyToType(Class<?> propertyType, String property, Class<?> entityClass, String tableType) {

        if (ClassUtil.isAssignable(Collection.class, propertyType)) {
            final Type fieldType = TypeUtil.getFieldType(entityClass, property);

            final Type typeArgument = TypeUtil.getTypeArgument(fieldType);
            final Class<?> aClass = TypeUtil.getClass(typeArgument);
            return "%s[]".formatted(propertyToType(aClass, tableType));
        } else {
            return propertyToType(propertyType);
        }
    }

    public String propertyToType(Class<?> propertyType, Type type) {

        if (ClassUtil.isAssignable(Collection.class, propertyType)) {
            final Type typeArgument = TypeUtil.getTypeArgument(type);
            final Class<?> aClass = TypeUtil.getClass(typeArgument);
            return "%s[]".formatted(propertyToType(aClass));
        } else {
            return propertyToType(propertyType);
        }
    }


    private String propertyToType(Class<?> propertyType) {
        return propertyToType(propertyType, "Row");
    }


    /**
     * @param propertyType
     * @param tableType    Row  Insert or Update
     * @return
     */
    private String propertyToType(Class<?> propertyType, String tableType) {
        if (ClassUtil.isAssignable(CharSequence.class, propertyType)
            || propertyType.equals(Long.class) || propertyType.equals(long.class)
        ) {
            return "string";
        }
        if (ClassUtil.isAssignable(Boolean.class, propertyType)) {
            return "boolean";
        }

        if (ClassUtil.isAssignable(Number.class, propertyType)) {
            return "number";
        }

        if (ClassUtil.isAssignable(Map.class, propertyType)) {
            return "Json";
        }
        // should use clazz and sign type to handler
        if (propertyType.equals(Object.class)) {
            return "Json";
        }

        if (EnumUtil.isEnum(propertyType)) {
            final String enumName = CharSequenceUtil.toUnderlineCase(propertyType.getSimpleName()).toLowerCase();
            return "Database[\"public\"][\"Enums\"][\"%s\"]".formatted(enumName);
        }
        if (!TableInfoFactory.defaultSupportColumnTypes.contains(propertyType)) {
            final boolean isTable = propertyType.isAnnotationPresent(Table.class);
            // todo tableDef not handle
            if (isTable) {
                final String value = propertyType.getAnnotation(Table.class).value();
                return "Database[\"public\"][\"Tables\"][\"%s\"][\"%s\"]".formatted(value, tableType);
            } else {

                final String typeName = CharSequenceUtil.toUnderlineCase(propertyType.getSimpleName()).toLowerCase();

                return "Database[\"public\"][\"CompositeTypes\"][\"%s\"]".formatted(typeName);
            }
        }
        return "string";
    }

    private Stream<Class<?>> tableInfoToColNeedType(TableInfo tableInfo) {
        return tableInfo.getColumnInfoList().stream().map(col -> {
            final Class<?> propertyType = col.getPropertyType();
            if (ClassUtil.isAssignable(Collection.class, propertyType)) {
                final Type fieldType = TypeUtil.getFieldType(tableInfo.getEntityClass(), col.getProperty());
                final Type typeArgument = TypeUtil.getTypeArgument(fieldType);
                return TypeUtil.getClass(typeArgument);
            } else {
                return col.getPropertyType();
            }
        });
    }

    private boolean fieldShouldShow(Field field) {
        return !Modifier.isStatic(field.getModifiers()) && !field.isAnnotationPresent(JsonIgnore.class);
    }

    private String funcClazzTypeToString(Class<?> clazz, Type parameterizedType, String tableType) {

        final boolean isCollection = ClassUtil.isAssignable(Collection.class, clazz);
        Class needClazz;
        if (isCollection) {
            needClazz = TypeUtil.getClass(TypeUtil.getTypeArgument(parameterizedType));
        } else {
            needClazz = clazz;
        }
        final boolean isBaseType = TableInfoFactory.defaultSupportColumnTypes.contains(needClazz);
        final boolean isTable = needClazz.isAnnotationPresent(Table.class);
        final boolean isMap = ClassUtil.isAssignable(Map.class, needClazz);

        if (isBaseType || isTable || isMap) {
            final String rawType = propertyToType(needClazz, tableType);
            return isCollection ? "%s[]".formatted(rawType) : rawType;
        }

        String rawArgs = Arrays.stream(ReflectUtil.getFields(needClazz)).filter(this::fieldShouldShow).map(field -> {
            final boolean notNullMark = field.isAnnotationPresent(NotNull.class);
            final String typeMark = propertyToType(field.getType(), field.getName(), needClazz, tableType);
            final String temp = notNullMark ? NON_NULL_FUNC_ARG_TEMP : NON_NULL_VALUE_NULL_FUNC_ARG_TEMP;
            return temp.formatted(CharSequenceUtil.toUnderlineCase(field.getName()).toLowerCase(), typeMark);
        }).collect(Collectors.joining(";\n"));
        return Optional.of(rawArgs)
                .filter(StrUtil::isNotEmpty)
                .map(it -> "{\n%s\n%s}".formatted(it, FUNC_PRE))
                .map(raw -> isCollection ? "%s[]".formatted(raw) : raw).orElse(isCollection ? FUNC_NULL_ARRAY : FUNC_NULL);
    }

    private List<Class<?>> allFieldClazz() {
        if (Objects.nonNull(allFieldClazz)) {
            return allFieldClazz;
        }
        final Stream<Class<?>> tableFieldClazz = applicationContext
                .getBeansOfType(BaseMapper.class)
                .values().stream().map(Object::getClass)
                .map(TableInfoFactory::ofMapperClass)
                .filter(tableInfo -> this.clazzTableFilter(tableInfo.getEntityClass()))
                .flatMap(tableInfo -> clazzFieldsType(tableInfo.getEntityClass()));


        final Stream<Class<?>> rpcClazz = applicationContext.getBeansWithAnnotation(RpcMapping.class).values()
                .stream()
                .map(Object::getClass)
                .map(ReflectUtil::getMethods)
                .flatMap(methods -> Arrays.stream(methods).filter(it -> it.isAnnotationPresent(Rpc.class))
                        .flatMap(method -> {
                            final Stream<Class<?>> args = Arrays.stream(method.getParameters())
                                    .filter(it -> it.isAnnotationPresent(RequestBody.class))
                                    .findFirst().map(it -> flatTypes(it.getType(), it.getParameterizedType())).orElse(Stream.of());
                            final Stream<Class<?>> returns = flatTypes(method.getReturnType(), method.getGenericReturnType());
                            return Stream.concat(args, returns);
                        })
                ).flatMap(this::clazzFieldsType);

        allFieldClazz = Stream.concat(tableFieldClazz, rpcClazz)
                .distinct()
                .filter(Objects::nonNull).toList();
        return allFieldClazz;
    }

    private Stream<Class<?>> flatTypes(Class<?> clazz, Type parameterizedType) {

        return Stream.concat(Stream.of(clazz, Optional.ofNullable(parameterizedType)
                        .map(TypeUtil::getClass).orElse(null)),
                Optional.ofNullable(TypeUtil.getTypeArguments(parameterizedType))
                        .stream().flatMap(Arrays::stream)
                        .map(TypeUtil::getClass)
        ).filter(Objects::nonNull);
    }

    private Stream<Class<?>> clazzFieldsType(Class<?> clazz) {
        return Arrays.stream(ReflectUtil.getFields(clazz))
                .filter(it -> !Modifier.isStatic(it.getModifiers()))
                .flatMap(it -> {
                    final Class<?> type = it.getType();
                    final Type genericType = it.getGenericType();
                    return flatTypes(type, genericType);
                });
    }

    private boolean clazzTableFilter(Class<?> entityClass) {
        return !ClassUtil.isAssignable(AuthBase.class, entityClass) && !ClassUtil.isAssignable(StorageBase.class, entityClass);
    }

    private String relFields(Class<?> entityClass) {
        return RelationManager.getRelations(entityClass).stream().map(it -> {
            final Field relationField = it.getRelationField();
            return NON_NULL_VALUE_NULL_FIELD_TEMP.formatted(
                    CharSequenceUtil.toUnderlineCase(relationField.getName()).toLowerCase(),
                    propertyToType(relationField.getType(), relationField.getName(), entityClass));
        }).collect(Collectors.joining(";\n"));
    }


}
