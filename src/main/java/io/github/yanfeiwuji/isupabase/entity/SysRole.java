package io.github.yanfeiwuji.isupabase.entity;

import com.mybatisflex.annotation.*;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.Data;

import java.util.List;

@Data
@Table("sys_role")
public class SysRole {
    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;
    private String roleName;

    @RelationManyToMany(joinTable = "sys_role_user", // 中间表
            selfField = "id", joinSelfColumn = "rid", targetField = "id", joinTargetColumn = "uid")
    private List<SysUser> users;
}
