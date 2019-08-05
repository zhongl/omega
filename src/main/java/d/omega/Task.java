package d.omega;

public interface Task<C, P> {
    String name();

    String description();

    C command();

    P precondition();
}
