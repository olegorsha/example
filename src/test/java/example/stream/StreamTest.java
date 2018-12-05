package example.stream;

import example.monads.Try;
import example.monads.TryConsumer;

import static org.junit.Assert.*;

import java.io.*;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.Collectors;

public class StreamTest {

    private void testReadFirstLine1() {

        Stream<String> files = Stream.of("file1.txt", "file2.txt", "file3.txt");

        Stream<String> firstLine = files
                .map(File::new)
                .filter(File::exists)
                .map(fn -> {
                    try {
                        return new FileReader(fn);
                    } catch (FileNotFoundException e) {
                        throw new UncheckedIOException(e);
                    }
                })
                .map(BufferedReader::new)
                .map(r -> {
                    try {
                        return r.readLine();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
    }

    private void testReadFirstLine2() {

        Stream<String> files = Stream.of("file1.txt", "file2.txt", "file3.txt");

        Stream<String> firstLine = files
                .map(File::new)
                .filter(File::exists)
                .map(fn -> {
                    try {
                        return Optional.of( new FileReader(fn) );
                    } catch (FileNotFoundException e) {
                        return Optional.<FileReader>empty();
                    }
                })
                .filter(Optional::isPresent).map(Optional::get)
                .map(BufferedReader::new)
                .map(r -> {
                    try {
                        return Optional.of(r.readLine());
                    } catch (IOException e) {
                        return Optional.<String>empty();
                    }})
                .filter(Optional::isPresent).map(Optional::get);
    }

    private void testReadFirstLine3() {

        Stream<String> files = Stream.of("file1.txt", "file2.txt", "file3.txt");

        Stream<String> firstLine = files
                .flatMap(fn -> {
                    try {
                        return Stream.of(new FileReader(fn));
                    } catch (FileNotFoundException e) {
                        return Stream.<FileReader>empty();
                    }
                })
                .map(BufferedReader::new)
                .flatMap(r -> {
                    try {
                        return Stream.of(r.readLine());
                    } catch (IOException e) {
                        return Stream.<String>empty();
                    }
                });
    }

    private static Stream<Try<String>> testReadFirstLine4(Stream<String> files) {
        return files
                .map(fn -> Try.ofFailable(() -> new FileReader(fn)))
                .map(t -> {
                    if(t.isSuccess()){
                        return Try.ofFailable(() -> new BufferedReader(t.getUnchecked()));
                    }else {
                        final String[] reasonOfFailure = new String[1];
                        t.onFailure(tt -> {
                            reasonOfFailure[0] = tt.getMessage();
                        });
                        return Try.ofFailable(() -> new BufferedReader(new StringReader(reasonOfFailure[0])));
                    }
                })
                .map(t -> {
                    if (t.isSuccess()) {
                        return Try.ofFailable(() -> t.getUnchecked().readLine());
                    } else {
                        final String[] reasonOfFailure = new String[1];
                        t.onFailure(tt -> {
                            reasonOfFailure[0] = tt.getMessage();
                        });
                        return Try.ofFailable(() -> new String(reasonOfFailure[0]));
                    }
                });
    }


    private static Stream<Try<String>> testReadFirstLine6(Stream<String> files) {

        return files
                .map(fn ->
                        Try.ofFailable(() -> new FileReader(fn))
                            .map(BufferedReader::new)
                            .map(BufferedReader::readLine)
                )
                .map(t -> {
                    if (t.isSuccess()) {
                        return Try.ofFailable(() -> t.get());
                    } else {
                        final String[] reasonOfFailure = new String[1];
                        t.onFailure(tt -> {
                            reasonOfFailure[0] = tt.getMessage();
                        });
                        return Try.ofFailable(() -> new String(reasonOfFailure[0]));
                    }
                });
    }

    public static void itShouldHandleComplexChaining() throws Throwable {
        Try<BufferedReader> t1 =
                Try.ofFailable(() -> new FileReader(""))
                .<BufferedReader>flatMap(x -> Try.ofFailable(() -> new BufferedReader(x)))
                .recoverWith(t -> Try.<BufferedReader>failure(t));

        System.out.println(t1.isSuccess());
        System.out.println(t1.get().readLine());

    }

    public static void betterWay() {
        List<File> files =
                Stream.of(new File(".").listFiles())
                        .flatMap(file -> file.listFiles() == null ?
                                Stream.of(file) : Stream.of(file.listFiles()))
                        .map(file -> {
                            System.out.println(file.getAbsoluteFile());
                            return file;
                        })
                        .collect(Collectors.toList());
        System.out.println("Count: " + files.size());
    }
    public static void main(String[] args) throws Throwable {
//        itShouldHandleComplexChaining();
//        betterWay();
        Stream<String> files = Stream.of("/tmp/file1.txt", "file2.txt", "file3.txt");
        List<Try<String>> collect = testReadFirstLine6(files).collect(Collectors.toList());
        for (Try<String> stringTry : collect) {
            if (stringTry.isSuccess()) {
                System.out.println(stringTry.get());
            }

        }
    }
}
