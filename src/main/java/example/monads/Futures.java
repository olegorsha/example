package example.monads;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Futures {
    /**
     * Convert List of CompletableFutures to CompletableFuture with a List.
     * @param futures List of Futures
     * @param <T> type
     * @return CompletableFuture with a List
     */

    public static <T> CompletableFuture<List<T>> sequence(List<CompletableFuture<T>> futures) {
        return CompletableFuture.
                allOf(futures.toArray(new CompletableFuture[futures.size()])).
                thenApply(v ->
                        futures.stream().
                                map(CompletableFuture::join).
                                collect(Collectors.toList())
                );
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        List<Integer> list = IntStream.range(0, 100).boxed().collect(Collectors.toList());
        int size = list.size();
        List<CompletableFuture<Integer>> futures = list
                .stream()
                .map(x -> CompletableFuture.supplyAsync(() -> x))
                .collect(Collectors.toList());

        CompletableFuture<List<Integer>> futureList = Futures.sequence(futures);

        futureList.get().forEach(System.out::println);
    }
}