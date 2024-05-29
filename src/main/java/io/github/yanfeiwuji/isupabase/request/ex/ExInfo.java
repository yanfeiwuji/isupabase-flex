package io.github.yanfeiwuji.isupabase.request.ex;

public record ExInfo(String details, String hint, String message) {
    public static final String TABLE_NOT_FOUND_TEMP = "relation \"%s\" does not exist";
    public static final String UNDEFINED_COLUMN_TEMP = "column \"%s.%s\" does not exist";
}
