package example.monads;

import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class FuturesTest {
    @Test
    public void itShouldConvertAListOfFuturesToAFutureWithAList() throws Exception {
        //given a list of futures,
        List<Integer> list = IntStream.range(0, 100).boxed().collect(Collectors.toList());
        int size = list.size();
        List<CompletableFuture<Integer>> futures = list
                .stream()
                .map(x -> CompletableFuture.supplyAsync(() -> x))
                .collect(Collectors.toList());

        //when we call sequence,
        CompletableFuture<List<Integer>> futureList = Futures.sequence(futures);

        //then we should get a future with a list
        List<Integer> collectedIntegers = Futures.sequence(futures).get();
        assert (collectedIntegers.size() == size);
        assert (list.get(5) == collectedIntegers.get(5));
    }

    <A, B, R> Optional<R> compute(BiFunction<A, B, R> operation, Optional<A> oa, Optional<B> ob) {
        return oa.flatMap(a -> ob.map(b -> operation.apply(a, b)));
    }

    public void test() {

        BiFunction<Integer, Integer, Integer> times = (x, y) -> x + y;

        Optional<Integer> one = Optional.of(1);
        Stream<Optional<Integer>> stream = Stream.of(1, 2, 3, 4).map(Optional::of);
        stream.reduce(one, (acc, elem) -> compute(times, acc, elem));  // Optional[24]
        stream = Stream.of(Optional.of(10), Optional.empty());
        stream.reduce(one, (acc, elem) -> compute(times, acc, elem));  // Optional.empty
    }

    public String getDrinkForEmployee(Optional<Employee> employe) {
        return employe.filter(employee -> employee.getAge() >= 21)
                .flatMap(employee -> employee.getFavoriteBeer())
                .orElse("Sprite");
    }

    private class Employee {
        public Integer getAge() {
            return 20;
        }

        public Optional<String> getFavoriteBeer() {
            return Optional.of("");
        }
    }

    static String getUsernameFromMessage(String message) {
        final String fieldName = "\"screen_name\":\"";
        final int indexOfFieldValue = message.indexOf(fieldName) + fieldName.length();
        final int indexOfEndOfFieldValue = message.indexOf("\"", indexOfFieldValue);
        return message.substring(indexOfFieldValue, indexOfEndOfFieldValue);
    }
}


