package io.github.yanfeiwuji.isupabase.stroage.vo;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.github.yanfeiwuji.isupabase.constants.StorageStrPool;

/**
 * @author yanfeiwuji
 * @date 2024/6/23 16:13
 */
@JsonNaming

public record SignedPathsVo(String error, String path, String signedURL) {
    public static SignedPathsVo EMPTY_NOT_EXIST = new SignedPathsVo(StorageStrPool.ERROR_PATHS_NOT_EXIST, CharSequenceUtil.EMPTY, null);


    public static SignedPathsVo ofNotExist(String path) {
        return new SignedPathsVo(
                "Either the object does not exist or you do not have access to it",
                path,
                null);
    }
}
