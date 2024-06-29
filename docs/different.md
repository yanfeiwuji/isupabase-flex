# Something Different

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


## Database Trigger

## Rpc Function
