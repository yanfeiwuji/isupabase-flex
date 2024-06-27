package io.github.yanfeiwuji.isupabase;

import io.github.yanfeiwuji.isupabase.auth.entity.AppMetaData;
import io.github.yanfeiwuji.isupabase.config.EnableSupabase;
import io.github.yanfeiwuji.isupabase.request.anno.Rpc;
import io.github.yanfeiwuji.isupabase.request.anno.RpcMapping;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * @author yanfeiwuji
 * @date 2024/6/27 09:27
 */
@SpringBootApplication
@EnableSupabase
@RpcMapping
public class Start {

    public record Ass(String a, List<AppMetaData> appMetaData) {
    }

    public static void main(String[] args) {
        SpringApplication.run(Start.class, args);
    }

    @Rpc("a")
    public void rpc() {
        System.out.println("1");
    }

    @Rpc("b")
    public List<Ass> rpc2() {
        System.out.println("1");
        return List.of();
    }

    @Rpc("c")
    public List<Map<String, String>> rpc3() {
        return List.of(Map.of());
    }

    @Rpc("d")
    public List<Map<String, String>> rpc4(@RequestBody List<AppMetaData> appMetaData) {
        return List.of(Map.of());
    }

    @Rpc("e")
    public List<Map<String, String>> rpc5(@RequestBody List<Map<String,String>> appMetaData) {
        return List.of(Map.of());
    }
}
