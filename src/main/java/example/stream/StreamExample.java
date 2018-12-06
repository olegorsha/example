package example.stream;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

interface User {
    String name();
}

interface Department {
    String title();

    User chief();

    Stream<User> users();
}

interface Company {
    Stream<Department> departments();
}

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

    static Map<User, List<Department>> departmentByChief(Company company) {
        return company.departments().collect(groupingBy(Department::chief));
    }

    static Map<User, List<String>> departmentNameByChief(Company company) {
        return company.departments().collect(groupingBy(Department::chief, mapping(Department::title, toList())));
    }

    static Map<User, Set<User>> supervisors(Company company) {
        return company.departments().collect(groupingBy(Department::chief, flatMapping(Department::users, toSet())));
    }


    public static void main(String[] args) {
        randomizeStream(1, 50).forEach(System.out::println);
        randomizeStream(1, 10).collect(toCollection(LinkedList::new));
        Map<? extends Class<?>, Byte> collect = randomizeStream(1, 10)
                .collect(toMap(Object::getClass, Integer::byteValue, (aByte, aByte2) -> {
                    return aByte;
                }, LinkedHashMap::new));
        System.out.println((stringByLength(Arrays.asList("a", "bb", "c", "dd", "eee"))));
    }
}


