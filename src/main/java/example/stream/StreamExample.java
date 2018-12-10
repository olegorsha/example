package example.stream;

import one.util.streamex.StreamEx;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

public class StreamExample {
    public static Stream<Integer> randomizeStream(final int from, final int to) {
        List<Integer> list = IntStream.range(from, to).boxed().collect(toList());
        Collections.shuffle(list);
        return list.stream();
    }

    public static boolean hasOption(String[] args) {
        boolean present = Stream.of(args)
                .filter(x -> x.startsWith("--"))
                .findFirst()
                .isPresent();

        return Stream.of(args).anyMatch(x -> x.startsWith("--"));
    }

    public static String joinString(List<String> list) {
        return list.stream().collect(joining());
    }

    public static int max(List<Integer> list) {
        return list.stream().mapToInt(x -> x).max().orElseThrow();
    }

    public static Map<Integer, String> stringByLength(List<String> list) {
        return list.stream().collect(groupingBy(String::length, joining("+")));
    }


//    https://www.youtube.com/watch?v=vxikpWnnnCU&t=308s
    public static Stream<String> декартовоПроизведение(List<List<String>> list) {
         return list.get(0).stream().flatMap(a ->
                 list.get(1).stream().flatMap(b ->
                  list.get(2).stream().map(c -> a + b + c)));
    }

    public static Stream<String> декартовоПроизведение2(List<List<String>> input) {
        Supplier<Stream<String>> s = input.stream()
                .<Supplier<Stream<String>>>map(list -> list::stream)
                .reduce((sup1, sup2) -> () -> sup1.get()
                .flatMap(e1 -> sup2.get().map(e2 -> e1+e2)))
                .orElse(() -> Stream.of(""));
        return s.get();
    }

    //Выбрать из стрима все элементы заданного класса
    public static <T, TT> Stream<TT> select(Stream<T> stream, Class<TT> clazz) {
        return stream.filter(clazz::isInstance).map(clazz::cast);
    }

    public static <T, TT> Function<T, Stream<TT>> select2(Class<TT> clazz) {
        return e -> clazz.isInstance(e) ? Stream.of(clazz.cast(e)) : null;
    }


    //Оставить значения, которые повоторяются не менее n раз
    public static <T> Predicate<T> distinct(long atLeast) {
        Map<T, Long> map = new ConcurrentHashMap<>();
        System.out.println("!");
        return t -> map.merge(t, 1L, Long::sum) == atLeast;
    }

    public static void main(String[] args) {
        randomizeStream(1, 50).forEach(System.out::println);
        randomizeStream(1, 10).collect(toCollection(LinkedList::new));
        Map<? extends Class<?>, Byte> collect = randomizeStream(1, 10)
                .collect(toMap(Object::getClass, Integer::byteValue, (aByte, aByte2) -> {
                    return aByte;
                }, LinkedHashMap::new));
        System.out.println((stringByLength(Arrays.asList("a", "bb", "c", "dd", "eee"))));

        List<List<String>> input = Arrays.asList(Arrays.asList("a", "b", "c"), Arrays.asList("x" , "y"), Arrays.asList("1", "2", "3"));
//        декартовоПроизведение(input).forEach(System.out::println);
        декартовоПроизведение2(input).forEach(System.out::println);

        Stream<?> ss = Stream.of("aa", 1, 2.2d);
        select(ss, String.class).forEach(s ->{
            System.out.println("value=" + s + ", class=" + s.getClass().getName());
        });

        Stream.of("aa", 1, 2.2d)
                .flatMap(select2(String.class)).forEach(System.out::println);
        StreamEx.of("aa", 1, 2.2d)
                .select(String.class)
                .forEach(System.out::println);
        List<String> list = Arrays.asList("1", "1", "2", "2", "2", "3");
        list.stream().filter(distinct(3)).forEach(System.out::println);
    }
}


