package example.monads.tutorial;

import org.junit.Test;

import java.util.List;

import static example.monads.tutorial.Validation.failure;
import static example.monads.tutorial.Validation.success;
import static example.monads.tutorial.ValidationDSL.valid;
import static example.monads.tutorial.Validator.using;
import static example.monads.tutorial.Validator.validate;

public class ValidationTest {

    public static Validation<String, Person> validAge(Person p) {
        return isValidAge(p) ? success(p) : failure("Age must be less than 130", p);
    }

    public static boolean isValidAge(Person p) {
        return p.getAge() < 130;
    }

    public static Validation<String, Person> validName(Person p) {
        return isValidName(p) ? success(p) : failure("Name must start with an uppercase", p);
    }

    public static boolean isValidName(Person p) {
        return Character.isUpperCase(p.getName().charAt(0));
    }

    @Test
    public void testValidation() {
        Person person = new Person("Mario", 40);
        Validation<List<Object>, Person> validatedPerson =
                success(person).failList()
                        .flatMap(ValidationTest::validAge)
                        .flatMap(ValidationTest::validName);

        System.out.println(validatedPerson);
    }

    @Test
    public void testValidationDSLLambda() {
        Person person = new Person("mario", 140);
        Validation<? extends List<Object>, Person> validatedPerson = valid(person,
                p -> p.getAge() < 130 ?
                        success(p) :
                        failure("Age must be less than 130", p),
                p -> Character.isUpperCase(p.getName().charAt(0)) ?
                        success(p) :
                        failure("Name must start with an uppercase", p));
        System.out.println(validatedPerson);
    }

    @Test
    public void testValidationDSL() {
        Person person = new Person("mario", 137);
        Validation<? extends List<Object>, Person> validatedPerson = valid(person,
                ValidationTest::validAge,
                ValidationTest::validName);
        System.out.println(validatedPerson);
    }

    @Test
    public void testValidator() {
        Person person = new Person("mario", 137);
        Validation<List<Object>, Person> validatedPerson = validate(person,
                new Validator<Person>(ValidationTest::isValidAge, "Age must be less than 130"),
                new Validator<Person>(ValidationTest::isValidName, "Name must start with an uppercase"));
        System.out.println(validatedPerson);
    }

    @Test
    public void testValidatorDSL() {
        Person person = new Person("mario", 137);
        Validation<List<Object>, Person> validatedPerson = validate(person,
                using((Person p) -> p.getAge() < 130)
                        .withError("Age must be less than 130"),
                using((Person p) -> Character.isUpperCase(p.getName().charAt(0)))
                        .withError("Name must start with an uppercase"));
        System.out.println(validatedPerson);
    }

    @Test
    public void testValidatorLambda() {
        Person person = new Person("mario", 137);
        Validation<List<Object>, Person> validatedPerson = validate(person,
                new Validator<Person>(p -> p.getAge() < 130, "Age must be less than 130"),
                new Validator<Person>(p -> Character.isUpperCase(p.getName().charAt(0)), "Name must start with an uppercase"));
        System.out.println(validatedPerson);
    }

    public static class Person {
        private final String name;
        private final int age;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public int getAge() {
            return age;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "Person{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    '}';
        }
    }
}