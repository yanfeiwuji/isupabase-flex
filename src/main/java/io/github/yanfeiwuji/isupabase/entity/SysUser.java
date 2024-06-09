package io.github.yanfeiwuji.isupabase.entity;

import com.mybatisflex.annotation.*;
import com.mybatisflex.core.keygen.KeyGenerators;
import io.github.yanfeiwuji.isupabase.request.validate.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Table("sys_user")
public class SysUser {

    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;
    @NotBlank(groups = Valid.Insert.class, message = "not blank")
    @NotNull(groups = Valid.Insert.class, message = "not nul")
    private String userName;

    private Integer age;
    @Column(onInsertValue = "now()", onUpdateValue = "now()")
    private Date birthday;

    @RelationOneToOne(selfField = "id", targetField = "uid")
    private SysUserExt sysUserExt;

    @RelationManyToMany(joinTable = "sys_role_user", // 中间表
            selfField = "id", targetField = "id", joinSelfColumn = "uid", joinTargetColumn = "rid")
    private List<SysRole> roles;
}
