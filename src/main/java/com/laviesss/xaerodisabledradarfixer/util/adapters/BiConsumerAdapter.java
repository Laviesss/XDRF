package com.laviesss.xaerodisabledradarfixer.util.adapters;

import java.util.function.BiConsumer;
import java.util.function.Function;

@SuppressWarnings("unused")
public class BiConsumerAdapter {
    @SuppressWarnings("unchecked")
    public static <S, T> Function<Object, BiConsumer<S, T>> wrapper(Function<Object, S> wrapperS, Function<S, Object> unwrapperS, Function<Object, T> wrapperT, Function<T, Object> unwrapperT) {
        return object -> {
            BiConsumer<Object, Object> bicon = (BiConsumer<Object, Object>) object;
            return (s, t) -> bicon.accept(unwrapperS.apply(s), unwrapperT.apply(t));
        };
    }

    public static <S, T> Function<BiConsumer<S, T>, Object> unwrapper(Function<Object, S> wrapperS, Function<S, Object> unwrapperS, Function<Object, T> wrapperT, Function<T, Object> unwrapperT) {
        return bicon -> (BiConsumer<?, ?>) (obj1, obj2) -> bicon.accept(wrapperS.apply(obj1), wrapperT.apply(obj2));
    }
}
