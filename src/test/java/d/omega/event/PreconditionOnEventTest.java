package d.omega.event;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static d.omega.event.PreconditionOnEvent.compound;
import static d.omega.event.PreconditionOnEvent.newInstance;

public class PreconditionOnEventTest {
    @Test
    public void should_compound_two_preconditions() {
        final PreconditionOnEvent<String> compound = compound(newInstance(String::isEmpty), newInstance(s -> true));
        assertThat(compound.isSatisfied(), is(false));
        assertThat(compound.on("").isSatisfied(), is(true));
    }
}