package io.github.yanfeiwuji.isupabase.request.ex;

import org.springframework.http.HttpStatus;

import java.util.function.Supplier;

public record PgrstExCodeStatus(String code, HttpStatus status) {

    public static final PgrstExCodeStatus DB_UNDEFINED_TABLE = new PgrstExCodeStatus("42P01", HttpStatus.NOT_FOUND);
    public static final PgrstExCodeStatus DB_UNDEFINED_COLUMN = new PgrstExCodeStatus("42703", HttpStatus.BAD_REQUEST);

    public static final PgrstExCodeStatus DB_INVALID_INPUT = new PgrstExCodeStatus("22P02", HttpStatus.BAD_REQUEST);
    public static final PgrstExCodeStatus DB_DATATYPE_MISMATCH = new PgrstExCodeStatus("42804", HttpStatus.BAD_REQUEST);
    public static final PgrstExCodeStatus DB_CANNOT_COERCE = new PgrstExCodeStatus("42846", HttpStatus.BAD_REQUEST);

    public static final PgrstExCodeStatus DB_UNDEFINED_OBJECT = new PgrstExCodeStatus("42704", HttpStatus.BAD_REQUEST);

    public static final PgrstExCodeStatus DB_NOT_NULL_VIOLATION = new PgrstExCodeStatus("23502", HttpStatus.BAD_REQUEST);

    public static final PgrstExCodeStatus PGRST_PARSE_ERROR = new PgrstExCodeStatus("PGRST100", HttpStatus.BAD_REQUEST);
    public static final PgrstExCodeStatus PGRST_FUNCTION_ONLY_GET_OR_POST = new PgrstExCodeStatus("PGRST101",
            HttpStatus.METHOD_NOT_ALLOWED);
    public static final PgrstExCodeStatus PGRST_INVALID_REQUEST_BODY = new PgrstExCodeStatus("PGRST102", HttpStatus.BAD_REQUEST);
    public static final PgrstExCodeStatus PGRST_INVALID_RANGE = new PgrstExCodeStatus("PGRST103",
            HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE);

    public static final PgrstExCodeStatus PGRST_INVALID_PUT_HAS_DOWN = new PgrstExCodeStatus("PGRST105",
            HttpStatus.METHOD_NOT_ALLOWED);
    public static final PgrstExCodeStatus PGRST_SCHEMA_NOT_CONFIG = new PgrstExCodeStatus("PGRST106", HttpStatus.NOT_ACCEPTABLE);
    public static final PgrstExCodeStatus PGRST_INVALID_CONTENT_TYPE = new PgrstExCodeStatus("PGRST107",
            HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    public static final PgrstExCodeStatus PGRST_FILTER_APPLY_EMBEDDED_NOT_IN_SELECT = new PgrstExCodeStatus("PGRST108",
            HttpStatus.BAD_REQUEST);
    public static final PgrstExCodeStatus PGRST_UPDATE_DELETE_USE_LIMIT_MUST_HAS_ORDER_UNI_COL = new PgrstExCodeStatus("PGRST109",
            HttpStatus.BAD_REQUEST);
    public static final PgrstExCodeStatus PGRST_UPDATE_DELETE_USE_LIMIT_GT_MAX_ROWS = new PgrstExCodeStatus("PGRST110",
            HttpStatus.BAD_REQUEST);
    public static final PgrstExCodeStatus PGRST_INVALID_HEADERS = new PgrstExCodeStatus("PGRST111",
            HttpStatus.INTERNAL_SERVER_ERROR);

    public static final PgrstExCodeStatus PGRST_STATUS_CODE_MUST_INT = new PgrstExCodeStatus("PGRST112",
            HttpStatus.INTERNAL_SERVER_ERROR);
    // public static final ExCodeStatus PGRST_1 = new ExCodeStatus("PGRST113",
    // HttpStatus.BAD_REQUEST);
    public static final PgrstExCodeStatus PGRST_FOR_UPSET_PUT_HAS_LIMIT = new PgrstExCodeStatus("PGRST114",
            HttpStatus.BAD_REQUEST);
    public static final PgrstExCodeStatus PGRST_FOR_UPSET_PK_NE_BODY = new PgrstExCodeStatus("PGRST115", HttpStatus.BAD_REQUEST);
    public static final PgrstExCodeStatus PGRST_MORE_THEN_ONE_WHEN_SIGNAL_RES = new PgrstExCodeStatus("PGRST116",
            HttpStatus.NOT_ACCEPTABLE);
    public static final PgrstExCodeStatus PGRST_HTTP_VERB_NOT_SUPPORTED = new PgrstExCodeStatus("PGRST117",
            HttpStatus.METHOD_NOT_ALLOWED);
    public static final PgrstExCodeStatus PGRST_CAN_NOT_ORDER_REL_FOR_MANY_END = new PgrstExCodeStatus("PGRST118",
            HttpStatus.BAD_REQUEST);

    public static final PgrstExCodeStatus PGRST_CAN_NOT_SPREAD_REL_FOR_MANY_END = new PgrstExCodeStatus("PGRST119",
            HttpStatus.BAD_REQUEST);
    public static final PgrstExCodeStatus PGRST_AN_EMBEDDED_ONLY_USE_FILTER_IS = new PgrstExCodeStatus("PGRST120",
            HttpStatus.BAD_REQUEST);
    public static final PgrstExCodeStatus PGRST_CAN_NOT_PARSE_JSON_IN_RAISE = new PgrstExCodeStatus("PGRST121",
            HttpStatus.BAD_REQUEST);
    public static final PgrstExCodeStatus PGRST_INVALID_PREFERENCES = new PgrstExCodeStatus("PGRST122", HttpStatus.BAD_REQUEST);

    public static final PgrstExCodeStatus PGRST_REL_NOT_EXIST = new PgrstExCodeStatus("PGRST200", HttpStatus.BAD_REQUEST);
    public static final PgrstExCodeStatus PGRST_AMBIGUOUS_EMBEDDED_WAS_MADE = new PgrstExCodeStatus("PGRST201", HttpStatus.BAD_REQUEST);
    public static final PgrstExCodeStatus PGRST_FUNCTION_NOT_EXIST = new PgrstExCodeStatus("PGRST202", HttpStatus.NOT_FOUND);
    public static final PgrstExCodeStatus PGRST_FUNCTION_NOT_UNI = new PgrstExCodeStatus("PGRST203", HttpStatus.MULTIPLE_CHOICES);
    public static final PgrstExCodeStatus PGRST_PUT_COLUMNS_NOT_FOUND = new PgrstExCodeStatus("PGRST204", HttpStatus.BAD_REQUEST);

    // ext error
    public static final PgrstExCodeStatus PGRST_EXT_TABLE_NO_PK = new PgrstExCodeStatus("PGRSTY001", HttpStatus.BAD_REQUEST);

    public PgrstExRes toExRes(PgrstExInfo info) {
        return new PgrstExRes(code, info.details(), info.hint(), info.message(), null);
    }

    public PgrstExRes toExRes(PgrstExInfo info, Object extInfo) {
        return new PgrstExRes(code, info.details(), info.hint(), info.message(), extInfo);
    }

    public PgrstEx toEx(PgrstExInfo info) {
        return new PgrstEx(this, info, null);
    }

    public Supplier<PgrstEx> toSupplierEx(PgrstExInfo info) {
        return () -> new PgrstEx(this, info, null);
    }

    public Supplier<PgrstEx> toSupplierEx(PgrstExInfo info, Object extInfo) {
        return () -> new PgrstEx(this, info, extInfo);
    }
}
