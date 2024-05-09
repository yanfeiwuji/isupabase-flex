package io.github.yanfeiwuji.isupabase;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.relation.RelationManager;
import io.github.yanfeiwuji.isupabase.entity.SysRole;
import io.github.yanfeiwuji.isupabase.entity.SysRoleUser;
import io.github.yanfeiwuji.isupabase.entity.SysUser;
import io.github.yanfeiwuji.isupabase.entity.table.SysRoleTableDef;
import io.github.yanfeiwuji.isupabase.entity.table.SysUserTableDef;
import io.github.yanfeiwuji.isupabase.mapper.SysRoleMapper;
import io.github.yanfeiwuji.isupabase.mapper.SysRoleUserMapper;
import io.github.yanfeiwuji.isupabase.mapper.SysUserMapper;
import lombok.AllArgsConstructor;
import org.apache.ibatis.annotations.Delete;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@SpringBootApplication
@AllArgsConstructor
@MapperScan("io.github.yanfeiwuji.isupabase.mapper")
public class IsupabaseApplication {

    private final SysUserMapper sysUserMapper;

    private final SysRoleMapper sysRoleMapper;

    private final SysRoleUserMapper sysRoleUserMapper;

    public static void main(String[] args) {
        SpringApplication.run(IsupabaseApplication.class, args);
    }

    @GetMapping
    public List<SysUser> user() {
        RelationManager.addQueryRelations("roles");

        sysRoleMapper.selectListWithRelationsByQuery(
                QueryWrapper.create()
                        .where(SysRoleTableDef.SYS_ROLE.ROLE_NAME.like("2"))

        );
        return sysUserMapper.selectAllWithRelations();
    }


    @GetMapping("/role")
    public List<SysRole> roleList() {
        return sysRoleMapper.selectAllWithRelations();
    }

    @PostMapping
    public void post() {
        SysRole sysRole = new SysRole();
        sysRole.setRoleName("test");
        sysRoleMapper.insert(sysRole);

        SysUser sysUser = new SysUser();
        sysUser.setUserName("test user name");
        sysUserMapper.insert(sysUser);

        SysRoleUser sysRoleUser = new SysRoleUser();
        sysRoleUser.setUid(sysUser.getId());
        sysRoleUser.setRid(sysRole.getId());
        sysRoleUserMapper.insert(sysRoleUser);

    }
}
