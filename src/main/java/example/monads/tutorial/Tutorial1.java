package example.monads.tutorial;

//https://www.slideshare.net/mariofusco/monadic-java

import java.util.Optional;

public class Tutorial1 {
    public class Person {
        private Optional<Car> car;

        public Person(Optional<Car> car) {
            this.car = car;
        }

        public Optional<Car> getCar() {
            return car;
        }
    }

    public class Car {
        private Optional<Insurance> insurance;

        public Car(Optional<Insurance> insurance) {
            this.insurance = insurance;
        }

        public Optional<Insurance> getInsurance() {
            return insurance;
        }
    }

    public class Insurance {
        private String name;

        public Insurance(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static String getCarInsurance(Optional<Person> person) {
        return person
                .flatMap(p -> p.getCar())
                .flatMap(car -> car.getInsurance())
                .map(insurance -> insurance.getName())
                .orElse("Unknown");
    }

    public static void main(String[] args) {
        Tutorial1 tutorial1 = new Tutorial1();

        Person p = tutorial1.new Person(Optional.of(
                        tutorial1.new Car(Optional.of(
                            tutorial1.new Insurance("name")))));

        System.out.println(Tutorial1.getCarInsurance(Optional.of(p)));
    }
}
