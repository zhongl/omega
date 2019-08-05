package d.omega.event;

import d.omega.State;
import org.junit.Test;
import d.omega.Execution;

import java.util.Optional;
import java.util.function.Consumer;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class SchedulerOnEventTest {

    @Test
    public void should_schedule_single_task() {
        final MockTask task = new MockTask("a", () -> {}, s -> true);
        final Consumer<Execution<Runnable, PreconditionOnEvent<String>>> consumer = mock(Consumer.class);

        final SchedulerOnEvent<Runnable, String> scheduler = new SchedulerOnEvent<>(s -> State.SUCCESS, consumer);
        assertThat(scheduler.plan(task), is(Optional.empty()));

        scheduler.on("start");
        scheduler.on("done");
        verify(consumer).accept(new ExecutionOnEvent.Finished<>(task, State.SUCCESS));
    }

    @Test
    public void should_schedule_tasks_with_dependency() {
        final MockTask a = new MockTask("a", () -> {}, s -> s.equals("s"));
        final MockTask b = new MockTask("b", () -> {}, s -> s.equals("a"));
        final ExecutionOnEvent.Finished<Runnable, String> aFinished = new ExecutionOnEvent.Finished<>(a, State.SUCCESS);
        final ExecutionOnEvent.Finished<Runnable, String> bFinished = new ExecutionOnEvent.Finished<>(b, State.SUCCESS);
        final Consumer<Execution<Runnable, PreconditionOnEvent<String>>> consumer = mock(Consumer.class);

        final SchedulerOnEvent<Runnable, String> scheduler = new SchedulerOnEvent<>(s -> State.SUCCESS, consumer);
        scheduler.plan(a);
        scheduler.plan(b);

        scheduler.on("s");
        scheduler.on("a done");
        verify(consumer).accept(aFinished);
        verify(consumer, never()).accept(bFinished);

        scheduler.on("a");
        scheduler.on("b done");
        verify(consumer).accept(bFinished);

    }
}