package d.omega;

public interface Execution<C, P> {
    Task<C, P> task();

    State state();
}
