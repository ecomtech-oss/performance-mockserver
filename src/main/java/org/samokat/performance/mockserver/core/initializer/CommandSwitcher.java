package org.samokat.performance.mockserver.core.initializer;

import org.samokat.performance.mockserver.mocks.BananaBread;
import org.samokat.performance.mockserver.mocks.Croissant;

public class CommandSwitcher {

    public static Command getCommand(String commandName) {
        switch (commandName) {
            case "bananabread":
                return new BananaBread();
            case "croissant":
                return new Croissant();
            default:
                return null;
        }
    }
}
