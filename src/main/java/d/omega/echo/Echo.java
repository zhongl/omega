package d.omega.echo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Echo implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Echo.class);

    private final String message;

    public Echo(String message) {this.message = message;}

    public void run() {
        LOGGER.info(message);
    }
}
