package io.github.yanfeiwuji.isupabase.request.filter;

import cn.hutool.core.collection.ListUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface IToken {

    String mark();

    Pattern pattern();

    default boolean find(String input) {
        return pattern().matcher(input).find();
    }

    default Optional<String> value(String input) {

        return Optional.of(input)
                .map(pattern()::matcher)
                .filter(Matcher::find)
                .map(it -> it.group(it.groupCount()));
    }

    default Optional<String> first(String input) {
        return group(input, 1);
    }

    default Optional<String> group(String input, int group) {
        return Optional.of(input)
                .map(pattern()::matcher)
                .filter(Matcher::find)
                .filter(it -> it.groupCount() >= group)
                .map(it -> it.group(group));
    }

    default List<String> groups(String input) {

        return Optional.of(input)
                .map(pattern()::matcher)
                .filter(Matcher::find)
                .map(it -> IntStream.rangeClosed(0, it.groupCount()).mapToObj(it::group).toList())
                .orElse(List.of());
    }

    // reduce matcher create
    default Optional<KeyValue> keyValue(String input) {

        return Optional.of(input)
                .map(pattern()::matcher)
                .filter(Matcher::find)
                .filter(it -> it.groupCount() >= 1)
                .map(it -> new KeyValue(it.group(1), it.group(it.groupCount())));
    }

}
