package org.samokat.performance.mockserver.core.initializer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;

public class Utils {

    public static String getFile(String local) throws IOException {
        InputStream in = CommandSwitcher.class.getClassLoader().getResourceAsStream(local);
        String file = null;
        if (in != null) {
            file = IOUtils.toString(in, StandardCharsets.UTF_8);
        }
        return file;
    }
}
