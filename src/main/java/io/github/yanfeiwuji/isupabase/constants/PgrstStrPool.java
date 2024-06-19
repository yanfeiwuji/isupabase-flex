package io.github.yanfeiwuji.isupabase.constants;


public final class PgrstStrPool {



    private PgrstStrPool() {
    }

    public static final String API_REQ_PGRST_DB_BEAN = "API_REQ_PGRST_DB_BEAN";


    public static final String UPDATE_TEMP_TABLE = "$update_temp_table";

    public static final String DELETE_TEMP_TABLE = "$delete_temp_table";

    public static final String STAR = "*";
    public static final String SELECT_INNER_MARK = "!inner";
    public static final String ONE = "1";

    public static final String NULLS_FIRST = "nullsfirst";
    public static final String NULLS_LAST = "nullslast";

    public static final String ASC = "asc";
    public static final String DESC = "desc";


    public static final String SELECT = "select";
    public static final String LIMIT = "limit";
    public static final String OFFSET = "offset";
    public static final String ORDER = "order";
    public static final String COLUMNS = "columns";
    public static final String ON_CONFLICT = "on_conflict";


    public static final String PERCENT = "%";

    // op
    public static final String SPREAD_MARK = "...";

    public static final String IS_VALUE_NULL = "null";
    public static final String IS_VALUE_UNKNOWN = "unknown";
    public static final String IS_VALUE_TRUE = "true";
    public static final String IS_VALUE_FALSE = "false";

    public static final String SQL_NULL = " NULL ";
    public static final String SQL_UNKNOWN = " UNKNOWN ";
    public static final String SQL_TRUE = " TRUE ";
    public static final String SQL_FALSE = " FALSE ";
    // logic
    public static final String AND = "and";
    public static final String OR = "or";
    public static final String NOT_AND = "not.and";
    public static final String NOT_OR = "not.or";

    public static final String NOT_DOT = "not.";

    public static final String MODIFIER_ALL = "(all)";
    public static final String MODIFIER_ANY = "(any)";
    // ext op
    public static final String IS_SQL_OP = " IS ";
    public static final String MATCH_SQL_OP = " ~ ";

    public static final String IMATCH_SQL_OP = " ~* ";
    public static final String ISDISTINCT_SQL_OP = " IS DISTINCT FROM ";

    public static final String FTS_SQL_OP = " @@ ";
    public static final String PLFTS_SQL_OP = " @@ ";
    public static final String PHFTS_SQL_OP = " @@ ";
    public static final String WFTS_SQL_OP = " @@ ";

    public static final String CS_SQL_OP = " @> ";
    public static final String CD_SQL_OP = " <@ ";
    public static final String OV_SQL_OP = " && ";
    public static final String SL_SQL_OP = " << ";
    public static final String SR_SQL_OP = " >> ";
    public static final String NXR_SQL_OP = " &< ";
    public static final String NXL_SQL_OP = " &> ";
    public static final String ADJ_SQL_OP = " -|- ";

    public static final String IS_NULL = "is.null";
    public static final String NOT_IS_NULL = "not.is.null";

    public static final String PREFER_HEADER_KEY = "Prefer";

    public static final String HEADER_RANGE_KEY = "Content-Range";
    public static final String HEADER_PREFERENCE_APPLIED_KEY = "preference-applied";
    public static final String HEADER_RANGE_VALUE_FORMAT = "%s/%s";
    public static final String HEADER_RANGE_VALUE_RANGE_FORMAT = "%d-%d";
    // handling
    public static final String PREFER_HANDLING_STRICT = "handling=strict";
    public static final String PREFER_HANDLING_LENIENT = "handling=lenient";
    // timezone
    public static final String PREFER_TIMEZONE = "timezone="; // not impl
    // return
    public static final String PREFER_RETURN_MINIMAL = "return=minimal";
    public static final String PREFER_RETURN_HEADERS_ONLY = "return=headers-only"; // not impl
    public static final String PREFER_RETURN_REPRESENTATION = "return=representation";
    // count
    public static final String PREFER_COUNT_EXACT = "count=exact";
    public static final String PREFER_COUNT_PLANNED = "count=planned"; // not impl
    public static final String PREFER_COUNT_ESTIMATED = "count=estimated"; // not impl

    // resolution
    public static final String PREFER_RESOLUTION_MERGE_DUPLICATES = "resolution=merge-duplicates";

    public static final String PREFER_RESOLUTION_IGNORE_DUPLICATES = "resolution=ignore-duplicates";  // not impl

    // missing
    public static final String PREFER_MISSION_DEFAULT = "missing=default"; // not impl

    // api version
    public static final String API_VERSION = "v1";

    // restPath
    public static final String REST_PATH = "rest/v1";
    // restRpc
    public static final String REST_RPC_PATH = "rest/v1/rpc";
    // auth path
    public static final String AUTH_PATH = "auth/v1";


    public static final String[] EXPOSE_HEADERS =
            new String[]{
                    "Content-Encoding",
                    "Content-Location",
                    PgrstStrPool.HEADER_RANGE_KEY,
                    "Content-Type",
                    "Date",
                    "Location", "Server", "Transfer-Encoding", "Range-Unit"};


}
