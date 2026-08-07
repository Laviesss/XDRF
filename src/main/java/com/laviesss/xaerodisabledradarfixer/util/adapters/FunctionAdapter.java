package com.laviesss.xaerodisabledradarfixer.util.adapters;

import java.util.function.Function;

@SuppressWarnings("unused")
public class FunctionAdapter {
    @SuppressWarnings("unchecked")
    public static <S, T> Function<Object, Function<S, T>> wrapper(Function<Object, S> wrapperS, Function<S, Object> unwrapperS, Function<Object, T> wrapperT, Function<T, Object> unwrapperT) {
        return object -> {
            Function<Object, Object> func = (Function<Object, Object>) object;
            return s -> wrapperT.apply(func.apply(unwrapperS.apply(s)));
        };
    }

    public static <S, T> Function<Function<S, T>, Object> unwrapper(Function<Object, S> wrapperS, Function<S, Object> unwrapperS, Function<Object, T> wrapperT, Function<T, Object> unwrapperT) {
        return func -> (Function<?, ?>) obj -> unwrapperT.apply(func.apply(wrapperS.apply(obj)));
    }
}
