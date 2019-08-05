package d.omega.event;

import d.omega.State;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class ExecutionOnEventTest {

    @Test
    public void should_run_successful() {
        final Runnable command = mock(Runnable.class);
        final MockTask task = new MockTask("mock", command, s1 -> true);
        final ExecutionOnEvent<Runnable, String> exec =
                ExecutionOnEvent.newInstance(task, s -> State.SUCCESS)
                                .on("start");

        assertThat(exec.state(), is(State.RUNNING));
        verify(command).run();
        assertThat(exec.on("done").state(), is(State.SUCCESS));
    }

    @Test
    public void should_be_planning_if_precondition_was_unsatisfied() {
        final Runnable command = mock(Runnable.class);
        final MockTask task = new MockTask("mock", command, s1 -> false);
        final ExecutionOnEvent<Runnable, String> exec =
                ExecutionOnEvent.newInstance(task, s -> State.SUCCESS)
                                .on("start");

        assertThat(exec.state(), is(State.PLANNING));
        verify(command, never()).run();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void should_complain_if_push_event_to_finished_execution() {
        final MockTask task = new MockTask("mock", mock(Runnable.class), s1 -> true);

        ExecutionOnEvent.newInstance(task, s -> State.SUCCESS)
                        .on("start")
                        .on("done")
                        .on("boom");
    }

}