package io.github.yanfeiwuji.isupabase.constants;

import lombok.experimental.UtilityClass;

/**
 * @author yanfeiwuji
 * @date 2024/6/18 16:37
 */
@UtilityClass
public class StorageStrPool {
    public static final String STORAGE_PATH = "storage/v1";
    public static final String EX_STATUS_CODE = "statusCode";

    public static final String UPLOAD_SIGNED_URL_TEMP = "/object/upload/sign/%s/%s?token=%s";


    public static final String SIGNED_URL_TEMP = "/object/sign/%s/%s?token=%s";
    public static final String TOKEN_KEY_OWNER = "owner";
    public static final String TOKEN_KEY_URL = "url";
    public static final String TOKEN_KEY_UPSERT = "upsert";

    public static final String ERROR_PATHS_NOT_EXIST = "Either the object does not exist or you do not have access to it";
}
