package org.vb.hotspotter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

public class Utils {

    public static String getFileName(String filePath) {
        return filePath.substring(filePath.lastIndexOf('/') + 1);
    }

    public static int findCountOfLines(String filePath) {
        final File file = new File(filePath);
        if (!file.exists()) return -1;

        try (final LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(filePath))) {
            while (lineNumberReader.readLine() != null) {
            }
            ;
            return lineNumberReader.getLineNumber();
        } catch (IOException e) {
        }
        return 0;
    }
}
