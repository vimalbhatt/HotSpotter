package org.vb.hotspotter;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Properties;

public class ReporterTest {

    Reporter reporter = new Reporter(new Properties());
    
    @Test
    public void shouldFindCountOfLines() {
        int linesCount = reporter.findCountOfLines("src/main/resources/HM.properties");
        Assert.assertEquals(linesCount, 6);
    }


    @Test
    public void shouldHandleUnavailableFile() {
        
        int linesCount = reporter.findCountOfLines("src/main/resources/HM.properties1");
        Assert.assertEquals(linesCount, -1);
    }
    
    @Test
    public void shouldExtractFileNameFromPath(){
        String fileName = reporter.getFileName("services/hedgemanagement/hedgemanagement-service/src/main/java/com/trafigura/hedgingservice/config/HedgingSupportServiceConfig.java");
        Assert.assertEquals(fileName, "HedgingSupportServiceConfig.java");
    }

}
