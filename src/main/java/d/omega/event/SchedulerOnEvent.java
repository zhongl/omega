package d.omega.event;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import d.omega.State;
import d.omega.Task;
import d.omega.Execution;
import d.omega.Scheduler;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

public class SchedulerOnEvent<C extends Runnable, E> implements Scheduler<C, PreconditionOnEvent<E>> {
    private final Function<E, State> handler;
    private final Consumer<Execution<C, PreconditionOnEvent<E>>> onFinished;
    private final Map<String, Task<C, PreconditionOnEvent<E>>> tasks;
    private final Map<String, List<ExecutionOnEvent<C, E>>> pending;
    private final Map<String, List<ExecutionOnEvent<C, E>>> finished;

    public SchedulerOnEvent(Function<E, State> handler, Consumer<Execution<C, PreconditionOnEvent<E>>> onFinished) {
        this.handler = handler;
        this.onFinished = onFinished;
        tasks = Maps.newHashMap();
        finished = Maps.newHashMap();
        pending = Maps.newHashMap();
    }

    @Override
    public synchronized Optional<Task<C, PreconditionOnEvent<E>>> plan(Task<C, PreconditionOnEvent<E>> task) {
        removePendingAndStartNew(task);
        return Optional.ofNullable(tasks.put(task.name(), task));
    }

    @Override
    public synchronized List<Execution<C, PreconditionOnEvent<E>>> executionsOf(Task<C, PreconditionOnEvent<E>> task) {
        List<Execution<C, PreconditionOnEvent<E>>> all = Lists.newArrayList();
        all.addAll(pending.computeIfAbsent(task.name(), k -> Collections.emptyList()));
        all.addAll(finished.computeIfAbsent(task.name(), k -> Collections.emptyList()));
        return all;
    }

    @Subscribe
    public synchronized void on(E event) {
        // TODO improve concurrency without sync method.
        for (String name : pending.keySet()) {
            pending.compute(name, (k, v) -> v.stream().map(e -> on(event, e)).collect(toList()));
        }
    }

    private ExecutionOnEvent<C, E> on(E event, ExecutionOnEvent<C, E> execution) {
        final ExecutionOnEvent<C, E> e = execution.on(event);

        if (isPending(e)) return e;

        finished.computeIfAbsent(e.task.name(), k -> Lists.newArrayList()).add(e);
        onFinished.accept(e);
        return ExecutionOnEvent.newInstance(e.task(), handler);
    }

    private boolean isPending(ExecutionOnEvent<C, E> execution) {
        final State s = execution.state();
        return s == State.PLANNING || s == State.RUNNING;
    }

    private void removePendingAndStartNew(Task<C, PreconditionOnEvent<E>> task) {
        final List<ExecutionOnEvent<C, E>> executions =
                pending.computeIfAbsent(task.name(), k -> Lists.newArrayList());
        executions.removeIf(e -> e.state() == State.PLANNING);
        executions.add(ExecutionOnEvent.newInstance(task, handler));
    }
}
