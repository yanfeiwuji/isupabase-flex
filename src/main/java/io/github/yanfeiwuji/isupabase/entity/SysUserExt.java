package io.github.yanfeiwuji.isupabase.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.Data;

@Table("sys_user_ext")
@Data
public class SysUserExt {
    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;
    private Long uid;
    private String avatar;


}
