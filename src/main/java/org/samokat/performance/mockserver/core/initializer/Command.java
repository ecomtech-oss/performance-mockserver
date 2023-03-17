package org.samokat.performance.mockserver.core.initializer;

import java.io.IOException;
import org.mockserver.mock.Expectation;

/**
 * Configures expectations of mock for Command
 */
public interface Command {

    /**
     * Init list of Expectations
     *
     * @return Expectation[]
     * @throws IOException
     * @see Expectation
     */
    default Expectation[] initializeExpectations() throws IOException {
        return new Expectation[]{};
    }
}
