package io.github.yanfeiwuji.isupabase.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.Data;

@Data
@Table("sys_role_ext")
public class SysRoleExt {
    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    private Long rid;

    private String roleExt;
}
