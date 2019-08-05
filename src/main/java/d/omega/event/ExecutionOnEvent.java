package d.omega.event;

import com.google.common.base.Objects;
import d.omega.Execution;
import d.omega.State;
import d.omega.Task;

import java.util.function.Function;

abstract class ExecutionOnEvent<C extends Runnable, E> implements Execution<C, PreconditionOnEvent<E>> {

    final Task<C, PreconditionOnEvent<E>> task;

    static <C extends Runnable, E> ExecutionOnEvent<C, E> newInstance(Task<C, PreconditionOnEvent<E>> task, Function<E, State> handler) {
        return new Planning<>(task, handler, task.precondition());
    }

    ExecutionOnEvent(Task<C, PreconditionOnEvent<E>> task) {
        this.task = task;
    }

    abstract ExecutionOnEvent<C, E> on(E event);

    @Override
    public Task<C, PreconditionOnEvent<E>> task() {
        return task;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExecutionOnEvent)) return false;
        ExecutionOnEvent<?, ?> that = (ExecutionOnEvent<?, ?>) o;
        return Objects.equal(task, that.task);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(task);
    }

    final static class Planning<C extends Runnable, E> extends ExecutionOnEvent<C, E> {
        private final Function<E, State> handler;
        private final PreconditionOnEvent<E> precondition;

        Planning(Task<C, PreconditionOnEvent<E>> task, Function<E, State> handler, PreconditionOnEvent<E> precondition) {
            super(task);
            this.handler = handler;
            this.precondition = precondition;
        }

        @Override
        ExecutionOnEvent<C, E> on(E event) {
            final PreconditionOnEvent<E> p = precondition.on(event);
            if (p.isSatisfied()) {
                task.command().run();
                return new Running<>(task, handler);
            } else {
                return new Planning<>(task, handler, p);
            }

        }

        @Override
        public State state() {
            return State.PLANNING;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Planning)) return false;
            if (!super.equals(o)) return false;
            Planning<?, ?> planning = (Planning<?, ?>) o;
            return Objects.equal(handler, planning.handler) &&
                    Objects.equal(precondition, planning.precondition);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(super.hashCode(), handler, precondition);
        }
    }

    final static class Running<C extends Runnable, E> extends ExecutionOnEvent<C, E> {
        private final Function<E, State> handler;

        Running(Task<C, PreconditionOnEvent<E>> task, Function<E, State> handler) {
            super(task);
            this.handler = handler;
        }

        @Override
        ExecutionOnEvent<C, E> on(E event) {
            return new Finished<>(task, handler.apply(event));
        }

        @Override
        public State state() {
            return State.RUNNING;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Running)) return false;
            if (!super.equals(o)) return false;
            Running<?, ?> running = (Running<?, ?>) o;
            return Objects.equal(handler, running.handler);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(super.hashCode(), handler);
        }
    }

    final static class Finished<C extends Runnable, E> extends ExecutionOnEvent<C, E> {
        private final State state;

        Finished(Task<C, PreconditionOnEvent<E>> task, State state) {
            super(task);
            this.state = state;
        }

        @Override
        ExecutionOnEvent<C, E> on(E event) {
            throw new UnsupportedOperationException("Execution was done");
        }

        @Override
        public State state() {
            return state;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Finished)) return false;
            if (!super.equals(o)) return false;
            Finished<?, ?> finished = (Finished<?, ?>) o;
            return state == finished.state;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(super.hashCode(), state);
        }
    }
}
