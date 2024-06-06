package io.github.yanfeiwuji.isupabase.request.ex;

import org.springframework.http.HttpStatus;

import java.util.function.Supplier;

public record ExCodeStatus(String code, HttpStatus status) {

    public static final ExCodeStatus DB_UNDEFINED_TABLE = new ExCodeStatus("42P01", HttpStatus.NOT_FOUND);
    public static final ExCodeStatus DB_UNDEFINED_COLUMN = new ExCodeStatus("42703", HttpStatus.BAD_REQUEST);

    public static final ExCodeStatus DB_INVALID_INPUT = new ExCodeStatus("22P02", HttpStatus.BAD_REQUEST);
    public static final ExCodeStatus DB_DATATYPE_MISMATCH = new ExCodeStatus("42804", HttpStatus.BAD_REQUEST);
    public static final ExCodeStatus DB_CANNOT_COERCE = new ExCodeStatus("42846", HttpStatus.BAD_REQUEST);

    public static final ExCodeStatus DB_UNDEFINED_OBJECT = new ExCodeStatus("42704", HttpStatus.BAD_REQUEST);


    public static final ExCodeStatus PGRST_PARSE_ERROR = new ExCodeStatus("PGRST100", HttpStatus.BAD_REQUEST);
    public static final ExCodeStatus PGRST_FUNCTION_ONLY_GET_OR_POST = new ExCodeStatus("PGRST101",
            HttpStatus.METHOD_NOT_ALLOWED);
    public static final ExCodeStatus PGRST_INVALID_REQUEST_BODY = new ExCodeStatus("PGRST102", HttpStatus.BAD_REQUEST);
    public static final ExCodeStatus PGRST_INVALID_RANGE = new ExCodeStatus("PGRST103",
            HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE);
    // public static final ExCodeStatus PGRST_1 = new ExCodeStatus("PGRST104",
    // HttpStatus.BAD_REQUEST);
    public static final ExCodeStatus PGRST_INVALID_PUT_HAS_DOWN = new ExCodeStatus("PGRST105",
            HttpStatus.METHOD_NOT_ALLOWED);
    public static final ExCodeStatus PGRST_SCHEMA_NOT_CONFIG = new ExCodeStatus("PGRST106", HttpStatus.NOT_ACCEPTABLE);
    public static final ExCodeStatus PGRST_INVALID_CONTENT_TYPE = new ExCodeStatus("PGRST107",
            HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    public static final ExCodeStatus PGRST_FILTER_APPLY_EMBEDDED_NOT_IN_SELECT = new ExCodeStatus("PGRST108",
            HttpStatus.BAD_REQUEST);
    public static final ExCodeStatus PGRST_UPDATE_DELETE_USE_LIMIT_MUST_HAS_ORDER_UNI_COL = new ExCodeStatus("PGRST109",
            HttpStatus.BAD_REQUEST);
    public static final ExCodeStatus PGRST_UPDATE_DELETE_USE_LIMIT_GT_MAX_ROWS = new ExCodeStatus("PGRST110",
            HttpStatus.BAD_REQUEST);
    public static final ExCodeStatus PGRST_INVALID_HEADERS = new ExCodeStatus("PGRST111",
            HttpStatus.INTERNAL_SERVER_ERROR);

    public static final ExCodeStatus PGRST_STATUS_CODE_MUST_INT = new ExCodeStatus("PGRST112",
            HttpStatus.INTERNAL_SERVER_ERROR);
    // public static final ExCodeStatus PGRST_1 = new ExCodeStatus("PGRST113",
    // HttpStatus.BAD_REQUEST);
    public static final ExCodeStatus PGRST_FOR_UPSET_PUT_HAS_LIMIT = new ExCodeStatus("PGRST114",
            HttpStatus.BAD_REQUEST);
    public static final ExCodeStatus PGRST_FOR_UPSET_PK_NE_BODY = new ExCodeStatus("PGRST115", HttpStatus.BAD_REQUEST);
    public static final ExCodeStatus PGRST_MORE_THEN_ONE_WHEN_SIGNAL_RES = new ExCodeStatus("PGRST116",
            HttpStatus.NOT_ACCEPTABLE);
    public static final ExCodeStatus PGRST_HTTP_VERB_NOT_SUPPORTED = new ExCodeStatus("PGRST117",
            HttpStatus.METHOD_NOT_ALLOWED);
    public static final ExCodeStatus PGRST_CAN_NOT_ORDER_REL_FOR_MANY_END = new ExCodeStatus("PGRST118",
            HttpStatus.BAD_REQUEST);

    public static final ExCodeStatus PGRST_CAN_NOT_SPREAD_REL_FOR_MANY_END = new ExCodeStatus("PGRST119",
            HttpStatus.BAD_REQUEST);
    public static final ExCodeStatus PGRST_AN_EMBEDDED_ONLY_USE_FILTER_IS = new ExCodeStatus("PGRST120",
            HttpStatus.BAD_REQUEST);
    public static final ExCodeStatus PGRST_CAN_NOT_PARSE_JSON_IN_RAISE = new ExCodeStatus("PGRST121",
            HttpStatus.BAD_REQUEST);
    public static final ExCodeStatus PGRST_INVALID_PREFERENCES = new ExCodeStatus("PGRST122", HttpStatus.BAD_REQUEST);

    public static final ExCodeStatus PGRST_REL_NOT_EXIST = new ExCodeStatus("PGRST200", HttpStatus.BAD_REQUEST);
    public static final ExCodeStatus PGRST_AMBIGUOUS_EMBEDDED_WAS_MADE = new ExCodeStatus("PGRST201", HttpStatus.BAD_REQUEST);
    public static final ExCodeStatus PGRST_FUNCTION_NOT_EXIST = new ExCodeStatus("PGRST202", HttpStatus.NOT_FOUND);
    public static final ExCodeStatus PGRST_FUNCTION_NOT_UNI = new ExCodeStatus("PGRST203", HttpStatus.MULTIPLE_CHOICES);
    public static final ExCodeStatus PGRST_PUT_COLUMNS_NOT_FOUND = new ExCodeStatus("PGRST204", HttpStatus.BAD_REQUEST);

    // ext error
    public static final ExCodeStatus PGRST_EXT_TABLE_NO_PK = new ExCodeStatus("PGRSTY001", HttpStatus.BAD_REQUEST);

    public ExRes toExRes(ExInfo info) {
        return new ExRes(code, info.details(), info.hint(), info.message());
    }

    public PgrstEx toEx(ExInfo info) {
        return new PgrstEx(this, info);
    }

    public Supplier<PgrstEx> toSupplierEx(ExInfo info) {
        return () -> new PgrstEx(this, info);
    }
}
