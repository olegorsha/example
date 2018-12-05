package example.monads.example.lamda;

import example.monads.Futures;
import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LamdaTest extends TestCase{

    List<Map<String,String>> list = new ArrayList<>();

    @Override
    public void setUp() throws Exception {
        super.setUp();
        Map<String, String> map1 = new HashMap<>();
        map1.put("key1", "value1");
        map1.put("key2", "value2");
        map1.put("key3", "value3");

        Map<String, String> map2 = new HashMap<>();
        map2.put("key1", "value1");
        map2.put("key2", "value2");

        list.add(map1);
        list.add(map2);
    }

    @Test
    public void testContainsKey() {

        Function<String, Predicate<Map<String,String>>> containsKey =
                (String key) -> {
                    Predicate<Map<String,String>> ck =
                            map -> map.containsKey(key);
                    return ck;
                };

        Function<String, Predicate<Map<String, String>>> containsKey1 = key -> map -> map.containsKey(key);

        Map<String, String> result =  list.stream()
                .filter(containsKey("key1").and(containsKey1.apply("key2")))
                .collect(Collectors.toMap(v -> v.get("key1"), v -> v.get("key2"), (p1, p2) -> {
                    System.out.println("p1 = " + p1);
                    System.out.println("p2 = " + p2);
                    return p1;
                }));


        result.forEach((k,v)->System.out.println("Key : " + k + " value : " + v));

    }

    private Problem getSolution() {

//        return new Problem() {
//            @Override
//            public byte[] fetchFirst(String... urls) {
//                AtomicReference<byte[]> result = new AtomicReference<>();
                ForkJoinPool forkJoinPool = new ForkJoinPool();
//                forkJoinPool.submit(new Runnable() {
//                    @Override
//                    public void run() {
//                        result.compareAndSet(null,
//                                Arrays.asList(urls)
//                                .stream()
//                                .parallel()
//                                .map(url -> {
//                                    return new String("").getBytes();
//                                }).findAny().get()
//                        );
//                    }
//                });
//                return result.get();
//            }
//        };

        return urls -> {
            return Arrays.asList(urls)
                    .stream()
                    .parallel()
                    .map(url -> {
                        return new String("").getBytes();
                    }).findAny().get();
        };
    }

    private Problem getSolution2() {
        return urls -> {
            CompletableFuture result =
                    CompletableFuture.anyOf(
                            Arrays.asList(urls)
                                    .stream()
                                     .map(url -> CompletableFuture.supplyAsync(() -> getBytes(url))
                                    )
                                    .collect(Collectors.toList())
                                    .toArray(new CompletableFuture[0])
                    );
            try {
                return (byte[]) result.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

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

    private byte[] getBytes(String url) {
        return new String("").getBytes();
    }

    private static Predicate<Map<String, String>> containsKey(String key) {
        return map -> map.containsKey(key);
    }

    private static  <T, R, E extends Exception>
    Function<T, R> wrapper(FunctionWithException<T, R, E> fe) {
        return arg -> {
            try {
                return fe.apply(arg);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    @FunctionalInterface
    public interface FunctionWithException<T, R, E extends Exception> {
        R apply(T t) throws E;
    }

    public interface Problem{
        String[] urls = {"", "", ""};

        byte[] fetchFirst(String... urls);

        default  byte[] getFirst(){
            return fetchFirst(urls);
        }
    }
}
