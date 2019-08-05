package d.omega.event;

import com.google.common.base.Objects;

import java.util.Set;
import java.util.function.Function;

public abstract class PreconditionOnEvent<E> {

    public static <E> PreconditionOnEvent<E> newInstance(Function<E, Boolean> handler) {
        return new Unsatisfied<>(handler);
    }

    public static <E> PreconditionOnEvent<E> compound(Set<PreconditionOnEvent<E>> preconditions) {
        return preconditions.stream().reduce(PreconditionOnEvent::compound).orElse(new Satisfied<>());
    }

    public static <E> PreconditionOnEvent<E> compound(PreconditionOnEvent<E> left, PreconditionOnEvent<E> right) {
        return new Compound<>(left, right);
    }

    abstract PreconditionOnEvent<E> on(E event);

    abstract boolean isSatisfied();

    final static class Satisfied<E> extends PreconditionOnEvent<E> {
        @Override
        PreconditionOnEvent<E> on(E event) {
            return this;
        }

        @Override
        boolean isSatisfied() {
            return true;
        }
    }

    final static class Unsatisfied<E> extends PreconditionOnEvent<E> {
        private final Function<E, Boolean> handler;

        Unsatisfied(Function<E, Boolean> handler) {
            this.handler = handler;
        }

        @Override
        PreconditionOnEvent<E> on(E event) {
            if (handler.apply(event)) {
                return new Satisfied<>();
            } else {
                return this;
            }
        }

        @Override
        boolean isSatisfied() {
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Unsatisfied)) return false;
            Unsatisfied<?> that = (Unsatisfied<?>) o;
            return Objects.equal(handler, that.handler);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(handler);
        }

        @Override
        public String toString() {
            return "Unsatisfied{handler=" + handler + '}';
        }
    }

    final static class Compound<E> extends PreconditionOnEvent<E> {
        private final PreconditionOnEvent<E> left;
        private final PreconditionOnEvent<E> right;

        Compound(PreconditionOnEvent<E> left, PreconditionOnEvent<E> right) {
            this.left = left;
            this.right = right;
        }

        @Override
        PreconditionOnEvent<E> on(E event) {
            return new Compound<>(left.on(event), right.on(event));
        }

        @Override
        boolean isSatisfied() {
            return left.isSatisfied() && right.isSatisfied();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Compound)) return false;
            Compound<?> compound = (Compound<?>) o;
            return Objects.equal(left, compound.left) &&
                    Objects.equal(right, compound.right);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(left, right);
        }

        @Override
        public String toString() {
            return "Compound{left=" + left + ", right=" + right + '}';
        }
    }
}
