package io.github.yanfeiwuji.isupabase.request.ex;

import java.util.function.Supplier;

public interface IExManagers {
    ExResTemp getExResTemp();

    default Supplier<ReqEx> supplierReqEx(ExResArgs args) {
        return () -> new ReqEx(getExResTemp(), args);
    }

    default Supplier<ReqEx> supplierReqExWith(ExResArgs args) {
        return () -> new ReqEx(getExResTemp(), args);
    }

    default ReqEx reqEx(ExResArgs args) {
        return new ReqEx(getExResTemp(), args);
    }

    default ReqEx reqEx(Object... args) {
        return new ReqEx(getExResTemp(), null);
    }
}
