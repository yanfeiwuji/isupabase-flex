package io.github.yanfeiwuji.isupabase;

import io.github.yanfeiwuji.isupabase.entity.SysRole;
import io.github.yanfeiwuji.isupabase.entity.SysRoleUser;
import io.github.yanfeiwuji.isupabase.entity.SysUser;
import io.github.yanfeiwuji.isupabase.entity.table.SysUserExtTableDef;
import io.github.yanfeiwuji.isupabase.entity.table.SysUserTableDef;
import io.github.yanfeiwuji.isupabase.mapper.SysRoleMapper;
import io.github.yanfeiwuji.isupabase.mapper.SysRoleUserMapper;
import io.github.yanfeiwuji.isupabase.mapper.SysUserMapper;
import io.github.yanfeiwuji.isupabase.request.IReqHandler;
import io.github.yanfeiwuji.isupabase.request.select.NewReq;
import lombok.AllArgsConstructor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.function.*;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.relation.RelationManager;

import cn.hutool.extra.expression.engine.qlexpress.QLExpressEngine;

import static io.github.yanfeiwuji.isupabase.entity.table.SysUserTableDef.SYS_USER;

import java.util.List;

import javax.management.Query;

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
        NewReq newReq = new NewReq();
        newReq.setQueryColumns(List.of(SYS_USER.ALL_COLUMNS));
        newReq.setQueryTable(SYS_USER);
        newReq.setJoinRelations(RelationManager.getRelations(SysUser.class));
        newReq.setOrders(
                List.of(SYS_USER.ID.desc().nullsFirst(), SysUserExtTableDef.SYS_USER_EXT.ID.desc().nullsLast()));

        newReq.setLimit(1);
        newReq.setOffset(6);
        return sysUserMapper.selectListByQuery(newReq.handler(QueryWrapper.create()));
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
