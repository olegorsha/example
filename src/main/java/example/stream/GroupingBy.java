package example.stream;

import java.util.List;
import java.util.Map;
import java.util.Set;
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


public class GroupingBy {
    public static Map<User, List<Department>> departmentByChief(Company company) {
        return company.departments().collect(groupingBy(Department::chief));
    }

    public static Map<User, List<String>> departmentNameByChief(Company company) {
        return company.departments().collect(groupingBy(Department::chief, mapping(Department::title, toList())));
    }

    public static Map<User, Set<User>> supervisors(Company company) {
        return company.departments().collect(groupingBy(Department::chief, flatMapping(Department::users, toSet())));
    }
}
