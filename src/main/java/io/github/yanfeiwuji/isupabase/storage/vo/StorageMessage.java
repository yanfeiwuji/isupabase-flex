package io.github.yanfeiwuji.isupabase.storage.vo;

/**
 * @author yanfeiwuji
 * @date 2024/6/18 17:39
 */

public record StorageMessage(String message) {
    public static final StorageMessage SUCCESS_DELETED = new StorageMessage("Successfully deleted");
    public static final StorageMessage SUCCESS_EMPTIED = new StorageMessage("Successfully emptied");
    public static final StorageMessage SUCCESS_UPDATED = new StorageMessage("Successfully updated");
    public static final StorageMessage SUCCESS_MOVED = new StorageMessage("Successfully moved");

}
