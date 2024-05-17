package io.github.yanfeiwuji.isupabase.request.filter;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

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

    // reduce matcher create
    default Optional<KeyValue> keyValue(String input) {

        return Optional.of(input)
                .map(pattern()::matcher)
                .filter(Matcher::find)
                .filter(it -> it.groupCount() >= 1)
                .map(it -> new KeyValue(it.group(1), it.group(it.groupCount())));
    }

}
