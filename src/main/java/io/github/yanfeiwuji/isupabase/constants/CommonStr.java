package io.github.yanfeiwuji.isupabase.constants;

import io.github.yanfeiwuji.isupabase.request.select.QueryConditionFactory;

public final class CommonStr {
    private CommonStr() {
    }

    public static final String STAR = "*";
    public static final String SELECT_INNER_MARK = "!inner";
    public static final String ONE = "1";

    public static final String NULL_FIRST = "nullsfirst";
    public static final String NULL_LAST = "nullslast";

    public static final String ASC = "asc";
    public static final String DESC = "desc";

    public static final String EMPTY_STRING = "";

    public static final String SELECT = "select";
    public static final String LIMIT = "limit";
    public static final String OFFSET = "offset";
    public static final String ORDER = "order";

    public static final String DOT_ORDER = ".order";
    public static final String DOT_LIMIT = ".limit";
    public static final String DOT_OFFSET = ".offset";

    public static final String PERCENT = "%";

    public static final String IS_VALUE_NULL = "null";
    public static final String IS_VALUE_UNKNOWN = "unknown";
    public static final String IS_VALUE_TRUE = "true";
    public static final String IS_VALUE_FALSE = "false";

    public static final String SQL_NULL = " NULL ";
    public static final String SQL_UNKNOWN = " UNKNOWN ";
    public static final String SQL_TRUE = " TRUE ";
    public static final String SQL_FALSE = " FALSE ";
    //
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


}
