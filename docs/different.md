# Something Different

## Database Relation

The relation is configured using [MyBatis-Flex](https://mybatis-flex.com/). For more information, refer
to [Mybatis-Flex Relation Query](https://mybatis-flex.com/zh/base/relations-query.html#%E4%B8%80%E5%AF%B9%E4%B8%80-relationonetoone).
It's similar to...

```java

@EqualsAndHashCode(callSuper = true)
@Data
@Table(value = "post")
public class Post extends BaseEntity {
    private String title;
    private String content;

    @RelationManyToMany(joinTable = "post_tag",
            selfField = "id",
            joinSelfColumn = "pid",
            targetField = "id",
            joinTargetColumn = "tid"
    )
    private List<Tag> tags;

    @RelationOneToOne(selfField = "id", targetField = "pid")
    private PostDetail detail;
}

```

You can use the property's underscored name in `SELECT` queries only if it's defined as a relation on the entity.

## Database Rls and Cls

You can configure RLS (Row-Level Security) and CLS (Column-Level Security) by implements from interfaces
`AllPolicyBase`, `SelectPolicyBase`, `InsertPolicyBase`, `UpdatePolicyBase`, and `DeletePolicyBase`.

```java
// T is used to configure entity classes.
public interface AllPolicyBase<T> extends PolicyBase<T> {
    default QueryCondition using(PgrstContext context) {
        // Customize the QueryCondition based on the provided context.
        return QueryCondition.createEmpty();
    }

    default void checking(PgrstContext context, List<T> entities) {
        // Implement logic to validate or modify the list of entities.
        // You may throw an exception to interrupt the process.
    }

    default List<QueryColumn> columns(PgrstContext context) {
        // Return null to allow access to all columns, or customize the list of columns based on the context.
        return null;
    }

    default TableSetting<T> config() {
        return new TableSetting<>(
                this::using,
                this::checking,
                this::columns
        );
    }
}
```

1. Using a superclass of entities enables unified permission configuration.
2. Only one configuration, based on the nearest entity class, will take effect.
3. The `AllPolicy` configures `select`, `insert`, `update`, and `delete` operations.
4. Implemented policies for these operations override the `AllPolicy` configuration.
5. This setup ensures all permissions are effective through a single unified entry point.

### example

Deny All

```java

@Policy
public class AuthPolicy implements AllPolicyBase<AuthBase> {

    @Override
    public QueryCondition using(PgrstContext context) {
        // it will return 
        /**
         {
         "code": "42501",
         "details": null,
         "hint": null,
         "message": "new row violates row-level security policy for table \"post\""
         }
         */
        throw deniedOnRowEx();
    }

    @Override
    public void checking(PgrstContext context, List<AuthBase> entities) {
        throw deniedOnRowEx();
    }
}

```

## Database Check

For simple field validations, you can use JSR 303. For more complex logic, you can write custom validation annotations
or implement your own validation logic in the `checking` method.

```java

@Data
@Table("tb_account")
public class Account {

    @Id(keyType = KeyType.Auto)
    private Long id;

    @Length(min = 1, max = 20, message = "Must be between 1 and 20 characters") // Used in Update and Insert
    @NotNull(groups = Valid.Update.class, message = "Username is required for update") // Used in Update
    private String userName;

    @Max(value = 200, message = "Age cannot be greater than 200") // Used in Update
    @Min(value = 0, message = "Age cannot be less than 0") // Used in Update
    @NotNull(groups = Valid.Insert.class, message = "Age is required for insert") // Used in Update
    private Integer age;

    private Date birthday;
}
```

## Database Trigger

Database triggers are implemented using Spring event listeners to execute corresponding logic.

```java

@Component
public class PostListener {
    // Ensure correct configuration of the condition (tableName.[insert|update|delete].[before|after])
    // to correctly handle events with entity signatures, preventing type conversion errors.
    @EventListener(value = PgrstDbEvent.class, condition = "#event.combine == 'post.insert.before'")
    public void onPostInsert(PgrstDbEvent<Post> event) {
        event.getNewEntities().forEach(System.out::println);
    }
}
```

## Database Enums

It will use [Mybatis-Flex](https://mybatis-flex.com/zh/core/enum-property.html).
You need to add `@JsonValue` so that it can be read into the entity; otherwise, Jackson will use the enum name.s

```java
public enum TypeEnum {
    TYPE1(1, "类型1"),
    TYPE2(2, "类型2"),
    TYPE3(3, "类型3"),
    ;

    @EnumValue
    @JsonValue
    private int code;

    private String desc;

    TypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    //getter
}
```

## Rpc Function

```java

@RpcMapping
public class RpcFunction {
    public record PlusArg(Integer a, Integer b) {

    }

    // Using a list for the body will not be read into types; this is consistent with Supabase behavior
    @Rpc("plus")
    public Integer plus(@RequestBody PlusArg arg) {
        return arg.a + arg.b;
    }
}
```

The RPC function is essentially a regular Spring MVC POST endpoint. By default, it is open, but you can secure it using
Spring Security. Note that all endpoints must use JWT tokens, and the default role for users is anon.