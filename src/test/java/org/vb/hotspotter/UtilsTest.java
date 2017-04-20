package org.vb.hotspotter;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class UtilsTest {
    
    @Test
    public void shouldExtractFileNameFromPath() {
        String fileName = Utils.getFileName("services/hedgemanagement/hedgemanagement-service/src/main/java/com/trafigura/hedgingservice/config/HedgingSupportServiceConfig.java");
        Assert.assertEquals(fileName, "HedgingSupportServiceConfig.java");
    }

    @Test
    public void shouldFindCountOfLines() throws IOException {
        File file = new File(System.nanoTime() + ".txt");
        final String lineSeparator = System.lineSeparator();
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.append("a").append(lineSeparator).append("b").append(lineSeparator).append("c");
        }
        int linesCount = Utils.findCountOfLines(file.getPath());
        Assert.assertEquals(linesCount, 3);
        file.deleteOnExit();
    }


    @Test
    public void shouldHandleUnavailableFile() {
        int linesCount = Utils.findCountOfLines(System.nanoTime() + ".txt");
        Assert.assertEquals(linesCount, -1);
    }
}
