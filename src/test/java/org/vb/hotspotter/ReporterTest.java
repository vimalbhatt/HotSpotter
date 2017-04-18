package org.vb.hotspotter;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Properties;

public class ReporterTest {
    
    @Test
    public void shouldFindCountOfLines() {
        Reporter reporter = new Reporter(new Properties());
        int linesCount = reporter.findCountOfLines("src/main/resources/HM.properties");
        Assert.assertEquals(6, linesCount);
    }


    @Test
    public void shouldHandleUnavailableFile() {
        Reporter reporter = new Reporter(new Properties());
        int linesCount = reporter.findCountOfLines("src/main/resources/HM.properties1");
        Assert.assertEquals(-1, linesCount);
    }

}
