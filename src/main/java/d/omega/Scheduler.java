package d.omega;

import java.util.List;
import java.util.Optional;

public interface Scheduler<C, P> {
    /**
     * @return none if task is not existed, or previous task.
     */
    Optional<Task<C, P>> plan(Task<C, P> task);

    /**
     * @return latest execution of the task.
     */
    List<Execution<C, P>> executionsOf(Task<C, P> task);
}
