package io.github.yanfeiwuji.isupabase.sec;

import io.github.yanfeiwuji.isupabase.entity.SysRole;
import io.github.yanfeiwuji.isupabase.request.event.PgrstDbEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author yanfeiwuji
 * @date 2024/6/21 11:05
 */
@Component
public class Eventlisten {
    //
    @EventListener(value = PgrstDbEvent.class,condition = "#event.table.startsWith('sys_user')")
    public void as(PgrstDbEvent<SysRole> event) {
        System.out.println(event.getCombine());
    }
}
