package io.github.yanfeiwuji.isupabase;


import io.github.yanfeiwuji.isupabase.entity.SysRole;
import io.github.yanfeiwuji.isupabase.entity.SysRoleUser;
import io.github.yanfeiwuji.isupabase.entity.SysUser;
import io.github.yanfeiwuji.isupabase.mapper.SysRoleMapper;
import io.github.yanfeiwuji.isupabase.mapper.SysRoleUserMapper;
import io.github.yanfeiwuji.isupabase.mapper.SysUserMapper;
import io.github.yanfeiwuji.isupabase.request.IReqHandler;
import lombok.AllArgsConstructor;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.mapper.ClassPathMapperScanner;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.function.*;

import java.util.List;

import static io.github.yanfeiwuji.isupabase.entity.table.SysUserTableDef.SYS_USER;

@RestController
@SpringBootApplication
@AllArgsConstructor
@MapperScan({ "io.github.yanfeiwuji.isupabase.*.mapper","io.github.yanfeiwuji.isupabase.mapper",})
@EnableAspectJAutoProxy
@RegisterReflectionForBinding({ClassPathMapperScanner.class})
@EnableTransactionManagement
@EnableCaching
public class IsupabaseApplication {

    private final SysUserMapper sysUserMapper;

    private final SysRoleMapper sysRoleMapper;

    private final SysRoleUserMapper sysRoleUserMapper;

    public static void main(String[] args) {

        SpringApplication.run(IsupabaseApplication.class, args);
    }

    @GetMapping
    public List<SysUser> user() {

        final SysUser sysUser1 = new SysUser();
        sysUser1.setAge(1);
        sysUserMapper.updateByCondition(sysUser1,SYS_USER.ID.eq(38117646247000123L));
        // final List<SysUser> sysUsers = sysUserMapper.selectListByQuery(QueryWrapper.create());
        return List.of();
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
