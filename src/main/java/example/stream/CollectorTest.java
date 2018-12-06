package example.stream;

import java.net.SocketException;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class CollectorTest {
    /*
public interface Collector<T, A, R> {
    //Саздать накопитель
    Supplier<A> supplier();

    //Добавить в накопитель
    BiConsumer<A, T> accumulator();

    //Склеить два накопителя
    BinaryOperator<A> combiner();

    //Преобразовать накопитель в результат
    Function<A, R> finisher();

    //не важно
    Set<Characteristics> characteristics();
}
    public static<T, A, R> Collector<T, A, R> of(Supplier<A> supplier,
                                                 BiConsumer<A, T> accumulator,
                                                 BinaryOperator<A> combiner,
                                                 Function<A, R> finisher,
                                                 Characteristics... characteristics) {
    }
 */

    //https://youtu.be/Pk7atYm8bX0?t=5095
    static <T, R> Collector<T, ?, Optional<R>> minMax(Comparator<? super T> cmp,
                                                      BiFunction<? super T, ? super T, ? extends R> finisher) {

        class Acc {
            T min;
            T max;
            boolean present;

            void add(T t) {
                if (present) {
                    if (cmp.compare(t, min) < 0) min = t;
                    if (cmp.compare(t, min) > 0) max = t;
                } else {
                    min = max = t;
                    present = true;
                }
            }

            Acc combine(Acc other) {
                if (!other.present) return this;
                if (!present) return other;
                if (cmp.compare(other.min, min) < 0) min = other.min;
                if (cmp.compare(other.max, max) > 0) max = other.max;
                return this;
            }
        }

        return Collector.of(
                Acc::new,
                Acc::add,
                Acc::combine,
                acc -> acc.present ? Optional.of(finisher.apply(acc.min, acc.max)) :
                        Optional.empty()
        );
    }

    public static void minMaxCollector() {
        Stream.of("one", "two", "three")
                .collect(
                        minMax(
                                Comparator.comparingInt(String::length),
                                (min, max) -> min + "|" + max))
                .ifPresent(System.out::println);
    }

    public static void main(String[] args) throws SocketException {
        minMaxCollector();
    }
}
