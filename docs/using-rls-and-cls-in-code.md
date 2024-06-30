# Using Rls and Cls in code
You can use PgrstDb Bean to use query with rls and cls

```java
@Service
@RequiredArgsConstructor
public class RpcFunction {
    final PostMapper postMapper;

    private final PgrstDb pgrstDb;

    public void plus() {
        pgrstDb.selectListByQuery(postMapper, QueryWrapper.create());
    }
}
```
