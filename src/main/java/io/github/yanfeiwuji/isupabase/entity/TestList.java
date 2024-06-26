package io.github.yanfeiwuji.isupabase.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.handler.JacksonTypeHandler;
import lombok.Data;

import java.util.List;

/**
 * @author yanfeiwuji
 * @date 2024/6/26 10:03
 */
@Data
@Table("test_list")
public class TestList {
    @Id
    private Integer id;
    @Column(typeHandler = JacksonTypeHandler.class)
    private List<TestType> testTypes;

    @Column(typeHandler = JacksonTypeHandler.class)
    private Asd asd;

}
