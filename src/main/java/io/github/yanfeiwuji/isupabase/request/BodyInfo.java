package io.github.yanfeiwuji.isupabase.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BodyInfo<T> {
    private T single;
    private List<T> array;

    public BodyInfo(T single) {
        this(single, null);
    }

    public BodyInfo(List<T> array) {
        this(null, array);
    }

}
