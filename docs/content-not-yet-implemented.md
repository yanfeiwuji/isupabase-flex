# Content Not Yet Implemented

## Database

JSON queries and geolocation queries are not yet implemented. Some databases do not support JSON or geolocation; you can
use [Mybatis-Flex TypeHandler](https://mybatis-flex.com/zh/core/column.html#typehandler) to store JSON data.

::: danger Note
Only Jackson type handlers are supported.
:::

```java

@Column(typeHandler = JacksonTypeHandler.class)
private Map<String, Object> map;
```

## Auth

The content related to SSO, phone number authentication, and multi-factor authentication in Auth has not yet been
implemented.

## Realtime

Real-time functionality has not yet been implemented.

## Edge Function

Edge Function functionality has not yet been implemented.<br/>
Implement the relevant content using RPC functions or within the code

