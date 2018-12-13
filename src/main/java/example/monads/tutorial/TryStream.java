package example.monads.tutorial;

import example.monads.Try;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TryStream {

    private static Stream<Try<String>> testReadFirstLine5(Stream<String> files) {

        return files
                .map(f -> Try.of(() -> new FileReader(f))
                        .map(BufferedReader::new)
                        .map(r -> Try.of(() -> r.readLine()).onSuccess(l -> r.close()).get()));
    }

    public static void main(String[] args) throws Throwable {

        Stream<String> files = Stream.of("/tmp/file1.txt", "file2.txt", "file3.txt");

        Map<Boolean, List<Try<String>>> splited =
                testReadFirstLine5(files).collect(Collectors.partitioningBy(Try::isSuccess));

        splited.get(Boolean.TRUE)
                .stream()
                .map(Try::getUnchecked)
                .forEach(System.out::println);

        splited.get(Boolean.FALSE)
                .stream()
                .map(t -> Try.of(() -> t.get())
                        .recoverWith(tt -> Try.of(() -> tt.getMessage()))
                )
                .map(Try::getUnchecked)
                .forEach(System.out::println);
    }

    private Stream<String> testReadFirstLine1(Stream<String> files) {

        return files
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

    private static Stream<Try<String>> testReadFirstLine4(Stream<String> files) {
        return files
                .map(fn -> Try.of(() -> new FileReader(fn)))
                .map(t -> {
                    if(t.isSuccess()){
                        return Try.of(() -> new BufferedReader(t.getUnchecked()));
                    }else {
                        final String[] reasonOfFailure = new String[1];
                        t.onFailure(tt -> {
                            reasonOfFailure[0] = tt.getMessage();
                        });
                        return Try.of(() -> new BufferedReader(new StringReader(reasonOfFailure[0])));
                    }
                })
                .map(t -> {
                    if (t.isSuccess()) {
                        return Try.of(() -> t.getUnchecked().readLine());
                    } else {
                        final String[] reasonOfFailure = new String[1];
                        t.onFailure(tt -> {
                            reasonOfFailure[0] = tt.getMessage();
                        });
                        return Try.of(() -> new String(reasonOfFailure[0]));
                    }
                });
    }

    private Stream<String> testReadFirstLine2(Stream<String> files) {

        return files
                .map(File::new)
                .filter(File::exists)
                .map(fn -> {
                    try {
                        return Optional.of(new FileReader(fn));
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
                    }
                })
                .filter(Optional::isPresent).map(Optional::get);
    }

    private static Stream<Try<String>> testReadFirstLine6(Stream<String> files) {

        return files
                .map(fn ->
                        Try.of(() -> new FileReader(fn))
                                .map(BufferedReader::new)
                                .map(BufferedReader::readLine)
                )
                .map(t -> {
                    if (t.isSuccess()) {
                        return Try.of(() -> t.get());
                    } else {
                        final String[] reasonOfFailure = new String[1];
                        t.onFailure(tt -> {
                            reasonOfFailure[0] = tt.getMessage();
                        });
                        return Try.of(() -> new String(reasonOfFailure[0]));
                    }
                });
    }

    private Stream<String> testReadFirstLine3(Stream<String> files) {

        return files
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
}
