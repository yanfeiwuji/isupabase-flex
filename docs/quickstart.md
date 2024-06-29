# Quick Start

Before you begin, we assume that you are already:

- Familiar with Java environment setup and development
- Familiar with relational databases, such as MySQL
- Familiar with Spring Boot and related frameworks
- Familiar with Java build tools, such as Maven
- Familiar with MyBatis-Flex ORM ([MyBatis-Flex](https://mybatis-flex.com/))

By following these prerequisites, you'll be well-prepared to proceed with the setup and development tasks ahead.

## Initialize Spring Boot 3

Setting Up a Spring Boot 3 Maven Project than Add dependency

```xml

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>io.github.yanfeiwuji</groupId>
        <artifactId>isupabase-flex</artifactId>
        <version>0.0.1</version>
    </dependency>

    <dependency>
        <groupId>com.mybatis-flex</groupId>
        <artifactId>mybatis-flex-processor</artifactId>
        <version>1.9.3</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

## Create Database Tables

Create Database Tables

```sql
create table if not exists storage_bucket
(
    id                 varchar(255)         not null
        primary key,
    name               text                 not null,
    owner              text                 null,
    created_at         timestamp            null,
    updated_at         timestamp            null,
    public             tinyint(1) default 0 null,
    avif_autodetection tinyint(1) default 0 null,
    file_size_limit    bigint               null,
    allowed_mime_types text                 null,
    owner_id           bigint               null
);

create table if not exists storage_object
(
    id               bigint    not null
        primary key,
    bucket_id        text      null,
    name             text      null,
    owner            text      null,
    created_at       timestamp null,
    updated_at       timestamp null,
    last_accessed_at timestamp null,
    metadata         text      null,
    path_tokens      text      null,
    version          text      null,
    owner_id         text      null
);

create table if not exists yfwj_identity
(
    provider_id     text      null,
    user_id         bigint    null,
    identity_data   text      null,
    provider        text      null,
    last_sign_in_at timestamp null,
    created_at      timestamp null,
    updated_at      timestamp null,
    email           text      null,
    id              bigint    not null
        primary key
);

create table if not exists yfwj_one_time_token
(
    id         bigint    not null
        primary key,
    user_id    bigint    null,
    token_type text      null,
    token_hash text      null,
    relates_to text      null,
    created_at timestamp null,
    updated_at timestamp null
);

create table if not exists yfwj_refresh_token
(
    id          bigint       not null
        primary key,
    instance_id bigint       null,
    token       varchar(255) null,
    user_id     bigint       null,
    revoked     tinyint(1)   null,
    created_at  timestamp    null,
    updated_at  timestamp    null,
    parent      varchar(255) null,
    session_id  bigint       null
);

create table if not exists yfwj_session
(
    id           bigint    not null
        primary key,
    user_id      bigint    null,
    created_at   timestamp null,
    updated_at   timestamp null,
    factor_id    int       null,
    aal          text      null,
    not_after    timestamp null,
    refreshed_at timestamp null,
    user_agent   text      null,
    ip           text      null,
    tag          text      null
);

create table if not exists yfwj_user
(
    id                          bigint       not null
        primary key,
    instance_id                 int          null,
    aud                         varchar(255) null,
    role                        varchar(255) null,
    email                       varchar(255) null,
    encrypted_password          varchar(255) null,
    email_confirmed_at          timestamp    null,
    invited_at                  timestamp    null,
    confirmation_token          varchar(255) null,
    confirmation_sent_at        timestamp    null,
    recovery_token              varchar(255) null,
    recovery_sent_at            timestamp    null,
    email_change_token_new      varchar(255) null,
    email_change                varchar(255) null,
    email_change_sent_at        timestamp    null,
    last_sign_in_at             timestamp    null,
    raw_app_meta_data           text         null,
    raw_user_meta_data          text         null,
    is_super_admin              tinyint(1)   null,
    created_at                  timestamp    null,
    updated_at                  timestamp    null,
    phone                       text         null,
    phone_confirmed_at          timestamp    null,
    phone_change                text         null,
    phone_change_token          varchar(255) null,
    phone_change_sent_at        timestamp    null,
    confirmed_at                timestamp    null,
    email_change_token_current  varchar(255) null,
    email_change_confirm_status smallint     null,
    banned_until                timestamp    null,
    reauthentication_token      varchar(255) null,
    reauthentication_sent_at    timestamp    null,
    is_sso_user                 tinyint(1)   null,
    deleted_at                  timestamp    null,
    is_anonymous                tinyint(1)   null
);
```

## Config Spring

Config database and mail

```yaml
spring:
  application:
    name: isupabase-example
  datasource:
    url: jdbc:mysql://localhost:3306/example
    username: username
    password: password
  mail:
    host: email.examle.com
    port: 587
    username: username
    password: password
    test-connection: false
```

In the Spring Boot startup class, add the @MapperScan annotation to scan the 'Mapper' folder. and @EnableSupabase to
start isupabase

```java

@SpringBootApplication
@EnableSupabase
@MapperScan({
        "io.github.yanfeiwuji.isupabase.auth.mapper",
        "io.github.yanfeiwuji.isupabase.storage.mapper",
        "io.github.yanfeiwuji.isupabase.demo.blog.mapper"
})
public class IsupabaseExampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(IsupabaseExampleApplication.class, args);
    }
}
```

## Create Your Database Table and Entity

Create your table

```sql
CREATE TABLE IF NOT EXISTS `tb_account`
(
    `id`        INTEGER PRIMARY KEY auto_increment,
    `user_name` VARCHAR(100),
    `age`       INTEGER,
    `birthday`  DATETIME
    );

INSERT INTO tb_account(id, user_name, age, birthday)
VALUES (1, '张三', 18, '2020-01-11'),
       (2, '李四', 19, '2021-03-21');
```

Create your entity and mapper

```java

@Data
@Table("tb_account")
public class Account {

    @Id(keyType = KeyType.Auto)
    private Long id;
    private String userName;
    private Integer age;
    private Date birthday;
}
```

```java
public interface AccountMapper extends BaseMapper<Account> {

}
```

## Write a Rpc Function

```java

@RpcMapping
public class RpcFunction {
    @Rpc("plus")
    public Integer plus(@RequestBody List<Integer> numbers) {
        return numbers.stream().reduce(Integer::sum).orElse(0);
    }
}
```

## Get Client ApiKey and types

Start spring boot project you will look ApiKey
::: danger
The default API key is generated from a publicly available private key and public key. Deployment environments require
configuration of your own private and public keys.
:::

```
i.g.y.isupabase.config.ISupaConfig       : JWT token:${ token}
```

Visit http://localhost:8080/meta/generators/typescript to get Supabase types.

```ts
export type Database = {
    public: {
        Tables: {
            tb_account: {
                Row: {
                    id: string;
                    user_name: string | null;
                    age: number | null;
                    birthday: string | null;
                };
                Insert: {
                    id?: string;
                    user_name?: string | null;
                    age?: number | null;
                    birthday?: string | null
                };
                Update: {
                    id?: string;
                    user_name?: string | null;
                    age?: number | null;
                    birthday?: string | null
                };
                Relationships: [];
            };

        };
        Views: {
            [_ in never]: never;
        };
        Functions: {
            plus: {
                Args: number[];
                Returns: number;
            };

        };
        Enums: {
            [_ in never]: never;
        };
        CompositeTypes: {};
    };
};
```