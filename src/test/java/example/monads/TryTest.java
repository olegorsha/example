package example.monads;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Optional;

import static org.junit.Assert.*;

    public class TryTest {
        @Test
        public void itShouldBeSuccessOnSuccess() throws Throwable{
            Try<String> t = Try.of(() -> "hey");
            assertTrue(t.isSuccess());
        }

        @Test
        public void itShouldHoldValueOnSuccess() throws Throwable{
            Try<String> t = Try.of(() -> "hey");
            assertEquals("hey", t.get());
        }

        @Test
        public void itShouldMapOnSuccess() throws Throwable{
            Try<String> t = Try.of(() -> "hey");
            Try<Integer> intT = t.map((x) -> 5);
            intT.get();
            assertEquals(5, intT.get().intValue());
        }

        @Test
        public void itShouldFlatMapOnSuccess() throws Throwable {
            Try<String> t = Try.of(() -> "hey");
            Try<Integer> intT = t.flatMap((x) -> Try.of(() -> 5));
            intT.get();
            assertEquals(5, intT.get().intValue());
        }

        @Test
        public void itShouldOrElseOnSuccess() {
            String t = Try.of(() -> "hey").orElse("jude");
            assertEquals("hey", t);

        }

        @Test
        public void itShouldReturnValueWhenRecoveringOnSuccess() {
            String t = Try.of(() -> "hey").recover((e) -> "jude");
            assertEquals("hey", t);
        }


        @Test
        public void itShouldReturnValueWhenRecoveringWithOnSuccess() throws Throwable {
            String t = Try.of(() -> "hey")
                    .recoverWith((x) ->
                            Try.of(() -> "Jude")
                    ).get();
            assertEquals("hey", t);
        }

        @Test
        public void itShouldOrElseTryOnSuccess() throws Throwable {
            Try<String> t = Try.of(() -> "hey").orElseTry(() -> "jude");

            assertEquals("hey", t.get());
        }

        @Test
        public void itShouldBeFailureOnFailure(){
            Try<String> t = Try.of(() -> {
                throw new Exception("e");
            });
            assertFalse(t.isSuccess());
        }

        @Test(expected = IllegalArgumentException.class)
        public void itShouldThrowExceptionOnGetOfFailure() throws Throwable{
            Try<String> t = Try.of(() -> {
                throw new IllegalArgumentException("e");
            });
            t.get();
        }

        @Test
        public void itShouldMapOnFailure(){
            Try<String> t = Try.of(() -> {
                throw new Exception("e");
            }).map((x) -> "hey" + x);

            assertFalse(t.isSuccess());
        }

        @Test
        public void itShouldFlatMapOnFailure(){
            Try<String> t = Try.of(() -> {
                throw new Exception("e");
            }).flatMap((x) -> Try.of(() -> "hey"));

            assertFalse(t.isSuccess());
        }

        @Test
        public void itShouldOrElseOnFailure() {
            String t = Try.<String>of(() -> {
                throw new IllegalArgumentException("e");
            }).orElse("jude");

            assertEquals("jude", t);
        }

        @Test
        public void itShouldOrElseTryOnFailure() throws Throwable {
            Try<String> t = Try.<String>of(() -> {
                throw new IllegalArgumentException("e");
            }).orElseTry(() -> "jude");

            assertEquals("jude", t.get());
        }

        @Test(expected = RuntimeException.class)
        public void itShouldGetAndThrowUncheckedException() throws Throwable {
            Try.<String>of(() -> {
                throw new Exception();
            }).getUnchecked();

        }

        @Test
        public void itShouldGetValue() throws Throwable {
            final String result = Try.<String>of(() -> "test").getUnchecked();

            assertEquals("test", result);
        }

        @Test
        public void itShouldReturnRecoverValueWhenRecoveringOnFailure() {
            String t = Try.of(() -> "hey")
                    .<String>map((x) -> {
                        throw new Exception("fail");
                    })
                    .recover((e) -> "jude");
            assertEquals("jude", t);
        }


        @Test
        public void itShouldReturnValueWhenRecoveringWithOnFailure() throws Throwable {
            String t = Try.<String>of(() -> {
                throw new Exception("oops");
            })
                    .recoverWith((x) ->
                            Try.of(() -> "Jude")
                    ).get();
            assertEquals("Jude", t);
        }

        @Test
        public void itShouldHandleComplexChaining() throws Throwable {
            Try.of(() -> "1").<Integer>flatMap((x) -> Try.of(() -> Integer.valueOf(x))).recoverWith((t) -> Try.successful(1));
        }

        @Test
        public void itShouldPassFailureIfPredicateIsFalse() throws Throwable {
            Try t1 = Try.of(() -> {
                throw new RuntimeException();
            }).filter(o -> false);

            Try t2 = Try.of(() -> {
                throw new RuntimeException();
            }).filter(o -> true);

            assertEquals(t1.isSuccess(), false);
            assertEquals(t2.isSuccess(), false);
        }

        @Test
        public void isShouldPassSuccessOnlyIfPredicateIsTrue() throws Throwable {
            Try t1 = Try.<String>of(() -> "yo mama").filter(s -> s.length() > 0);
            Try t2 = Try.<String>of(() -> "yo mama").filter(s -> s.length() < 0);

            assertEquals(t1.isSuccess(), true);
            assertEquals(t2.isSuccess(), false);
        }

        @Test
        public void itShouldReturnEmptyOptionalIfFailureOrNullSuccess() throws Throwable {
            Optional<String> opt1 = Try.<String>of(() -> {
                throw new IllegalArgumentException("Expected exception");
            }).toOptional();
            Optional<String> opt2 = Try.<String>of(() -> null).toOptional();

            assertFalse(opt1.isPresent());
            assertFalse(opt2.isPresent());
        }

        @Test
        public void isShouldReturnTryValueWrappedInOptionalIfNonNullSuccess() throws Throwable {
            Optional<String> opt1 = Try.<String>of(() -> "yo mama").toOptional();

            assertTrue(opt1.isPresent());
        }

        @Test(expected = IllegalArgumentException.class)
        public void itShouldThrowExceptionFromTryConsumerOnSuccessIfSuccess() throws Throwable {
            Try<String> t = Try.of(() -> "hey");

            t.onSuccess(s -> {
                throw new IllegalArgumentException("Should be thrown.");
            });
        }

        @Test
        public void itShouldNotThrowExceptionFromTryConsumerOnSuccessIfFailure() throws Throwable {
            Try<String> t = Try.of(() -> {
                throw new IllegalArgumentException("Expected exception");
            });

            t.onSuccess(s -> {
                throw new IllegalArgumentException("Should NOT be thrown.");
            });
        }

        @Test
        public void itShouldNotThrowExceptionFromTryConsumerOnFailureIfSuccess() throws Throwable {
            Try<String> t = Try.of(() -> "hey");

            t.onFailure(s -> {
                throw new IllegalArgumentException("Should NOT be thrown.");
            });
        }

        @Test(expected = IllegalArgumentException.class)
        public void itShouldThrowExceptionFromTryConsumerOnFailureIfFailure() throws Throwable {
            Try<String> t = Try.of(() -> {
                throw new IllegalArgumentException("Expected exception");
            });

            t.onFailure(s -> {
                throw new IllegalArgumentException("Should be thrown.");
            });
        }

        @Test(expected = IllegalArgumentException.class)
        public void itShouldThrowNewExceptionWhenInvokingOrElseThrowOnFailure() throws Throwable {
            Try<String> t = Try.of(() -> {
                throw new Exception("Oops");
            });

//            t.<Integer>get();

            t.<IllegalArgumentException>orElseThrow(() -> {
                throw new IllegalArgumentException("Should be thrown.");
            });
        }

        public void itShouldNotThrowNewExceptionWhenInvokingOrElseThrowOnSuccess() throws Throwable {
            Try<String> t = Try.of(() -> "Ok");

            String result = t.<IllegalArgumentException>orElseThrow(() -> {
                throw new IllegalArgumentException("Should be thrown.");
            });

            assertEquals(result, "Ok");
        }



    @Test
    public void testTryInteger() {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

        Try<Integer> dividend = Try.of(() -> {

            System.out.println("Enter an Int that you'd like to divide:");

            return Integer.parseInt("1111");
//            return Integer.parseInt(bufferedReader.readLine());

        } );

        Try<Integer> divisor = Try.of(() -> {

            System.out.println("Enter an Int that you'd like to divide by:");

            return Integer.parseInt("1aa");
//            return Integer.parseInt(bufferedReader.readLine());

        });

        Try<Integer> result = dividend.<Integer>flatMap(x -> divisor.<Integer>map(y -> x/y));

        if(result.isSuccess()) {
            ;
            System.out.println(result.getUnchecked());

         //   System.out.println("Result of " + dividend.get() + "/"+ divisor.get() +" is: " + result.get());

        } else {

            System.out.println("You must've divided by zero or entered something that's not an Int. Try again!");

            final String[] str = {""};
            result.onFailure((e) -> { str[0] = e.toString(); });

            System.out.println("Info from the exception: " + result.onFailure((e) -> {
                System.out.println(e);
            }));
        }

    }



}