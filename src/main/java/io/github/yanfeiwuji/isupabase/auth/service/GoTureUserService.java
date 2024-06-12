package io.github.yanfeiwuji.isupabase.auth.service;


import io.github.yanfeiwuji.isupabase.auth.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static io.github.yanfeiwuji.isupabase.auth.entity.table.UserTableDef.USER;


/**
 * @author yanfeiwuji
 * @date 2024/6/12 12:04
 */
@Service
@RequiredArgsConstructor
public class GoTureUserService implements UserDetailsService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        return userMapper.selectOneByCondition(USER.EMAIL.eq(username));

    }

}
