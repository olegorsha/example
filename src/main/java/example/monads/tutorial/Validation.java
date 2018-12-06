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

    public static <L, A> Success<L, A> success(A value) {
        return new Success<L, A>(value);
    }

    public static <L, A> Failure<L, A> failure(L left, A value) {
        return new Failure(left, value);
    }

    public abstract Validation<L, A> validate(Predicate<? super A> validation, String fail);

    public static <L, A> Validation<L, A> of(A value) {
        return new Success<>(Objects.requireNonNull(value));
    }

    public abstract boolean isSuccess();

    public abstract <B> Validation<L, B> map(Function<? super A, ? extends B> mapper);

    public abstract <B> Validation<L, B> flatMap(Function<? super A, Validation<?, ? extends B>> mapper);

    public abstract L failure();

    public A value() {
        return value;
    }

    public abstract Validation<L, A> onFailure(Consumer<L> action);

    public abstract Optional<L> toOptional();

    public static class Success<L, A> extends Validation<L, A> {
        Success(A value) {
            super(value);
        }

        public static <L, A> Success<L, A> success(A value) {
            return new Success<>(value);
        }

        @Override
        public <B> Validation<L, B> map(Function<? super A, ? extends B> mapper) {
            return success(mapper.apply(value));
        }

        @Override
        public <B> Validation<L, B> flatMap(Function<? super A, Validation<?, ? extends B>> mapper) {
            return (Validation<L, B>) mapper.apply(value);
        }

        @Override
        public Validation<L, A> validate(Predicate<? super A> validation, String fail) {
            if (!validation.test(value)) {
                return (Validation<L, A>) Failure.failure(value, fail);
            }
            return Success.success(value);
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public L failure() {
            return null;
        }


        public Success<List<L>, A> failList() {
            return new SuccessList<L, A>(value);
        }

        @Override
        public Validation<L, A> onFailure(Consumer<L> action) {
            return this;
        }

        public Optional<L> toOptional() {
            return Optional.empty();
        }

        @Override
        public String toString() {
            return "Success( " + value + " )";
        }
    }

    public static class Failure<L, A> extends Validation<L, A> {
        protected final L left;

        public Failure(L left, A value) {
            super(value);
            this.left = left;
        }

        @Override
        public <B> Validation<L, B> map(Function<? super A, ? extends B> mapper) {
            return failure(left, mapper.apply(value));
        }

        @Override
        public <B> Validation<L, B> flatMap(Function<? super A, Validation<?, ? extends B>> mapper) {

            Validation<?, ? extends B> result = mapper.apply(value);
            return result.isSuccess() ?
                    failure(left, result.value) :
                    failure(((Failure<L, B>) result).left, result.value);
        }

        @Override
        public Validation<L, A> validate(Predicate<? super A> validation, String fail) {
            return (Validation<L, A>) Failure.failure(fail, value);
        }

        public Optional<L> toOptional() {
            return Optional.ofNullable(left);
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public L failure() {
            return left;
        }

        public Validation<L, A> onFailure(Consumer<L> action) {
            action.accept(left);
            return this;
        }

        @Override
        public String toString() {
            return "Failure( " + left + " )";
        }
    }

    public static final class SuccessList<L, A> extends Success<List<L>, A> {
        public SuccessList(A value) {
            super(value);
        }

        public <B> Validation<List<L>, B> map(Function<? super A, ? extends B> mapper) {
            return new SuccessList(mapper.apply(value));
        }

        @Override
        public <B> Validation<List<L>, B> flatMap(Function<? super A, Validation<?, ? extends B>> mapper) {
            Validation<?, ? extends B> result = mapper.apply(value);
            return (Validation<List<L>, B>) (result.isSuccess() ?
                    new SuccessList(result.value) :
                    new FailureList<L, B>(((Failure<L, B>) result).left, result.value));
        }

        @Override
        public Validation<List<L>, A> validate(Predicate<? super A> validation, String fail) {
            return (Validation<List<L>, A>) (validation.test(value) ?
                    new SuccessList<>(value) :
                    new FailureList<L, A>(new ArrayList<L>() {{
                        add((L) fail);
                    }}, value)
            );
        }
    }

    public static final class FailureList<L, A> extends Failure<List<L>, A> {

        public FailureList(L left, A value) {
            super(new ArrayList<L>() {{
                add(left);
            }}, value);
        }

        private FailureList(List<L> left, A value) {
            super(left, value);
        }

        @Override
        public <B> Validation<List<L>, B> map(Function<? super A, ? extends B> mapper) {
            return new FailureList(left, mapper.apply(value));
        }

        @Override
        public <B> Validation<List<L>, B> flatMap(Function<? super A, Validation<?, ? extends B>> mapper) {
            Validation<?, ? extends B> result = mapper.apply(value);
            return (Validation<List<L>, B>) (result.isSuccess() ?
                    new FailureList(left, result.value) :
                    new FailureList<L, B>(new ArrayList<L>(left) {{
                        add(((Failure<L, B>) result).left);
                    }}, result.value));
        }
    }

}

