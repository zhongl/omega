package d.omega.event;

import com.google.common.base.Objects;
import d.omega.Task;

import java.util.function.Function;

final class MockTask implements Task<Runnable, PreconditionOnEvent<String>> {
    private final Runnable command;
    private final Function<String, Boolean> handler;
    private final String name;

    MockTask(String name, Runnable command, Function<String, Boolean> handler) {
        this.name = name;
        this.command = command;
        this.handler = handler;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String description() {
        return "mock";
    }

    @Override
    public Runnable command() {
        return command;
    }

    @Override
    public PreconditionOnEvent<String> precondition() {
        return PreconditionOnEvent.newInstance(handler);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MockTask)) return false;
        MockTask mockTask = (MockTask) o;
        return Objects.equal(command, mockTask.command) &&
                Objects.equal(handler, mockTask.handler) &&
                Objects.equal(name, mockTask.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(command, handler, name);
    }
}
