package example.monads;

import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class Promise<A> implements Future<A> {
    private final CompletableFuture<A> future;

    protected Promise(CompletableFuture<A> future) {
        this.future = future;
    }

    public static final <A> Promise<A> promise(Supplier<A> supplier) {
        return new Promise<>(CompletableFuture.supplyAsync(supplier));
    }

    public <B> Promise<B> map(Function<? super A, ? extends B> function) {
        return new Promise<>(future.thenApplyAsync(function));
    }

    public <B> Promise<B> flatMap(Function<? super A, Promise<B>> function) {
        return new Promise<>(future.thenComposeAsync(a -> function.apply(a).future));
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return future.isCancelled();
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

    @Override
    public A get() throws InterruptedException, ExecutionException {
        return future.get();
    }

    @Override
    public A get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return future.get(timeout, unit);
    }

    public static int slowLength(String s) {
        try {
            Thread.sleep(100);
            System.out.println("wait");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return s.length();
    }

    public static int slowDouble(int i) {
        try {
            Thread.sleep(100);
            System.out.println("wait");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return i*2;
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        String s = "Hello";

//        FunctionExample<Integer, Promise<Integer>> function = new FunctionExample<Integer, Promise<Integer>>() {
//            @Override
//            public Promise<Integer> apply(Integer integer) {
//                Supplier<Integer> supplier = new Supplier<Integer>() {
//                    @Override
//                    public Integer get() {
//                        return slowDouble(integer);
//                    }
//                };
//                return promise(supplier);
//            }
//        };
//        Promise<Integer> p = promise(() -> slowLength(s)).flatMap(function);

        Promise<Integer> p =
                promise(() -> slowLength(s))
                        .flatMap(i -> promise(() -> slowDouble(i)));

        System.out.println(p.get());
    }
}
