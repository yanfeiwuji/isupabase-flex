package io.github.yanfeiwuji.isupabase.stroage.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.yanfeiwuji.isupabase.stroage.ex.StorageExFactory;
import io.github.yanfeiwuji.isupabase.stroage.mapper.StorageObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author yanfeiwuji
 * @date 2024/6/20 16:43
 */
@Service
@RequiredArgsConstructor
public class ObjectService {
    private final StorageObjectMapper storageObjectMapper;



}
