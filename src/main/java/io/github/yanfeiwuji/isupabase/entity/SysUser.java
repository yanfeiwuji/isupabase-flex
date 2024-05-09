package io.github.yanfeiwuji.isupabase.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.RelationManyToMany;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Table("sys_user")
public class SysUser {

    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;
    private String userName;
    private Integer age;
    private Date birthday;

    @RelationManyToMany(
            joinTable = "sys_role_user", // 中间表
            selfField = "id", joinSelfColumn = "uid",
            targetField = "id", joinTargetColumn = "rid"
    )
    private List<SysRole> roles;
}
