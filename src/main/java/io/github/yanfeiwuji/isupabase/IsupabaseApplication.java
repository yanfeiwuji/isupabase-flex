package io.github.yanfeiwuji.isupabase;

import com.mybatisflex.core.query.QueryWrapper;
import io.github.yanfeiwuji.isupabase.entity.SysRole;
import io.github.yanfeiwuji.isupabase.entity.SysRoleUser;
import io.github.yanfeiwuji.isupabase.entity.SysUser;
import io.github.yanfeiwuji.isupabase.entity.table.SysUserTableDef;
import io.github.yanfeiwuji.isupabase.mapper.SysRoleMapper;
import io.github.yanfeiwuji.isupabase.mapper.SysRoleUserMapper;
import io.github.yanfeiwuji.isupabase.mapper.SysUserMapper;
import io.github.yanfeiwuji.isupabase.request.IReqHandler;
import lombok.AllArgsConstructor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.function.*;

import java.util.List;
import java.util.Map;

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
    public List user() {
        return sysUserMapper.selectListByQueryAs(
                QueryWrapper.create().select(SysUserTableDef.SYS_USER.ID.as("ID"))
                , Map.class
        );

    }

    @GetMapping("/role")
    public List<SysRole> roleList() {

        return List.of();
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

    @Bean
    RouterFunction<ServerResponse> routerFunction(IReqHandler reqHandler) {
        return reqHandler.routerFunction();
    }

}
