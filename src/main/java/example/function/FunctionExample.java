package example.function;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class FunctionExample {
    static <A, B, C> Function<? super B, ? extends C> bind(
            BiFunction<? super A, ? super B, ? extends C> fn, A a) {
        Objects.requireNonNull(fn);
        return b -> fn.apply(a, b);
    }

    static <A, B, C> Function<? super A, Function<? super B, ? extends C>> curry(BiFunction<? super A, ? super B, ? extends C> fn) {
        return a -> b -> fn.apply(a, b);
    }

    static Optional<Integer> toInteger(String opt) {
        try {
         return Optional.of(Integer.valueOf(opt));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static void main(String[] args) {
        Function<? super Integer, ? extends Integer> inc = bind(Integer::sum, 1);
        System.out.println("inc = " + inc.apply(10));

        System.out.println("curry = " + curry(Integer::sum).apply(5).apply(6));

        Integer num = Optional.of("1234").flatMap(x -> toInteger(x)).orElse(-1);
        System.out.println("num = " + num);

        Double random = Optional.<Double>empty().orElseGet(Math::random);
        System.out.println("random = " + random);
    }
}
