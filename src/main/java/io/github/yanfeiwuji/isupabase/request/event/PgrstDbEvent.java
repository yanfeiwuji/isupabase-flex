package io.github.yanfeiwuji.isupabase.request.event;


import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * only set this event
 *
 * @author yanfeiwuji
 * @date 2024/6/21 09:20
 */
@Getter
public class PgrstDbEvent<T> extends ApplicationEvent {

    private interface EventStrPool {
        String EVENT_INSERT = "insert";
        String EVENT_UPDATE = "update";
        String EVENT_DELETE = "delete";

        String TYPE_BEFORE = "before";
        String TYPE_AFTER = "after";
    }

    // table name with schema;
    private String table;
    //
    private String event; // insert , update ,delete
    private String type;// before .after
    // can use combine to filter
    private String combine;

    private List<T> newEntities;
    private List<T> oldEntities;

    // only use of to gen
    private PgrstDbEvent(Object source, String table, String event, String type, List<T> newEntities, List<T> oldEntities) {
        super(source);
        this.table = table;
        this.event = event;
        this.type = type;
        this.combine = "%s.%s.%s".formatted(table, event, type);
        this.newEntities = newEntities;
        this.oldEntities = oldEntities;
    }

    public static <E> PgrstDbEvent<E> ofInsertBefore(Object source, String table, List<E> newEntities) {
        return new PgrstDbEvent<>(source, table, EventStrPool.EVENT_INSERT, EventStrPool.TYPE_BEFORE, newEntities, List.of());
    }

    public static <E> PgrstDbEvent<E> ofInsertAfter(Object source, String table, List<E> newEntities) {
        return new PgrstDbEvent<>(source, table, EventStrPool.EVENT_INSERT, EventStrPool.TYPE_AFTER, newEntities, List.of());
    }

    public static <E> PgrstDbEvent<E> ofUpdateBefore(Object source, String table, List<E> newEntities, List<E> oldEntities) {
        return new PgrstDbEvent<>(source, table, EventStrPool.EVENT_UPDATE, EventStrPool.TYPE_BEFORE, newEntities, oldEntities);
    }

    public static <E> PgrstDbEvent<E> ofUpdateAfter(Object source, String table, List<E> newEntities, List<E> oldEntities) {
        return new PgrstDbEvent<>(source, table, EventStrPool.EVENT_UPDATE, EventStrPool.TYPE_AFTER, newEntities, oldEntities);
    }

    public static <E> PgrstDbEvent<E> ofDeleteBefore(Object source, String table, List<E> oldEntities) {
        return new PgrstDbEvent<>(source, table, EventStrPool.EVENT_DELETE, EventStrPool.TYPE_BEFORE, List.of(), oldEntities);
    }

    public static <E> PgrstDbEvent<E> ofDeleteAfter(Object source, String table, List<E> oldEntities) {
        return new PgrstDbEvent<>(source, table, EventStrPool.EVENT_DELETE, EventStrPool.TYPE_AFTER, List.of(), oldEntities);
    }


}
