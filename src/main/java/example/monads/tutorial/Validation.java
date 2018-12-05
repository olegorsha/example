package example.monads.tutorial;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class Validation<L, A> {
    protected final A value;

    protected Validation(A value) {
        this.value = value;
    }

    public abstract  Validation<L, A> map(Function<? super A, ? extends A> mapper);

    public abstract Validation<L, A> flatMap(Function<? super A, Validation<?, ? extends A>> mapper);

    public abstract Validation<L, A> validate(Predicate<? super A> validation, String fail);

    public static <L, A> Validation<L, A> of(A value) {
        return new Success<>(Objects.requireNonNull(value));
    }

    public Validation<List<L>, A> faiList() {
        return new SuccessList<>(value);
    }

    public abstract boolean isSuccess();

    public abstract Validation<L, A> onFailure(Consumer<L> action);

    public abstract Optional<L> toOptional();


    public static void main(String[] args) {

        Validation<String, Person> person =
                Success.<String, Person>success(new Person("san", 134))
                        .flatMap(Person::validateName);

        if (person.toOptional().isPresent()) {
            System.out.println(person.toOptional().get());
        }

        Person person1 = new Person("aaa", 134);
        Validation.<String, Person>of(person1)
                .faiList()
                .flatMap(Person::validateAge)
                .flatMap(Person::validateName)
                .onFailure(System.out::println);

    }
}

class Success<L, A> extends Validation<L, A> {
    Success(A value) {
        super(value);
    }

    @Override
    public  Validation<L, A> map(Function<? super A, ? extends A> mapper) {
        return success(mapper.apply(value));
    }

    @Override
    public Validation<L, A> flatMap(Function<? super A, Validation<?, ? extends A>> mapper) {
        return (Validation<L, A>) mapper.apply(value);
    }

    @Override
    public Validation<L, A> validate(Predicate<? super A> validation, String fail) {
        if (!validation.test(value)) {
            return (Validation<L, A>) Failure.failure(value, fail);
        }
        return Success.success(value);
    }

    public static <L, A> Success<L, A> success(A value) {
        return new Success<>(value);
    }

    @Override
    public boolean isSuccess() {
        return true;
    }


    @Override
    public Validation<L, A> onFailure(Consumer<L> action) {
        return this;
    }

    public Optional<L> toOptional() {
        return Optional.empty();
    }
}

class Failure<L, A> extends Validation<L, A> {
    protected final L left;

    Failure(A value, L left) {
        super(value);
        this.left = left;
    }

    @Override
    public Validation<L, A> map(Function<? super A, ? extends A> mapper) {
        return failure(left, mapper.apply(value));
    }

    @Override
    public  Validation<L, A> flatMap(Function<? super A, Validation<?, ? extends A>> mapper) {

        Validation<?, ? extends A> result = mapper.apply(value);
        return result.isSuccess() ?
                failure(left, result.value) :
                failure(((Failure<L, A>) result).left, result.value);
    }

    @Override
    public Validation<L, A> validate(Predicate<? super A> validation, String fail) {
        return (Validation<L, A>) Failure.failure(fail, value);
    }

    public static <L, A> Failure<L, A> failure(L left, A value) {
        return new Failure<>(value, left);
    }

    public Optional<L> toOptional() {
        return Optional.ofNullable(left);
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    public Validation<L, A> onFailure(Consumer<L> action) {
        action.accept(left);
        return this;
    }
}

class SuccessList<L, A> extends Success<List<L>, A> {
    public SuccessList(A value) {
        super(value);
    }

    public Validation<List<L>, A> map(Function<? super A, ? extends A> mapper) {
        return new SuccessList<>(mapper.apply(value));
    }

    public Validation<List<L>, A> flatMap(Function<? super A, Validation<?, ? extends A>> mapper) {
        Validation<?, ? extends A> result = mapper.apply(value);

        return (Validation<List<L>, A>) (result.isSuccess() ?
                new SuccessList<>(result.value) :
                new FailureList<L, A>(new ArrayList<L>() {{
                    add(((Failure<L, A>) result).left);
                }}, result.value)
        );
    }

    @Override
    public Validation<List<L>, A> validate(Predicate<? super A> validation, String fail) {
        return (Validation<List<L>, A>) (validation.test(value) ?
                new SuccessList<>(value) :
                new FailureList<L, A>(new ArrayList<L>() {{
                    add((L)fail);
                }}, value)
        );
    }


}

class FailureList<L, A> extends Failure<List<L>, A> {
    public FailureList(List<L> left, A value) {
        super(value, left);
    }

    public Validation<List<L>, A> map(Function<? super A, ? extends A> mapper) {
        return new FailureList(left, mapper.apply(value));
    }

    public Validation<List<L>, A> flatMap(Function<? super A, Validation<?, ? extends A>> mapper) {
        Validation<?, ? extends A> result = mapper.apply(value);
        return (Validation<List<L>, A>) (result.isSuccess() ?
                new FailureList(left, result.value) :
                new FailureList<L, A>(new ArrayList<L>(left) {{
                    add(((Failure<L, A>) result).left);
                }}, result.value));
    }
}

class Person {
    private String name;
    private int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public static Validation<String, Person> validateAge(Person person) {
        Validation<String, Person> validation = (person.getAge() > 18 && person.getAge() < 67) ?
                Success.success(person) :
                Failure.failure("Age must be between 18 and 67", person);
        return validation;
    }

    public static Validation<String, Person> validateName(Person person) {
        return Character.isUpperCase(person.getName().charAt(0)) ?
                Success.success(person) :
                Failure.failure("Name must start with uppercase", person);
    }
}

