package io.github.yanfeiwuji.isupabase.auth.action.param;

import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author yanfeiwuji
 * @date 2024/6/12 10:07
 */
@Data
public class RecoverParam {
    private String email;
}
