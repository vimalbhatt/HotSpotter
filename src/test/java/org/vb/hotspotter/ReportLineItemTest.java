package org.vb.hotspotter;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ReportLineItemTest {
    private ReportLineItem reportLineItem = new ReportLineItem();
    
    @Test
    public void shouldCalculateHotness(){
        
        reportLineItem.setCommitsAge(2000);
        reportLineItem.setCommitCount(4);
        reportLineItem.calculateAvgCommitAge();
        Assert.assertEquals(500.0,reportLineItem.getAvgCommitAge());
    }
    
    @Test
    public void shouldAbbreviatePackageName(){
        Assert.assertEquals("c.t.t.h.Hedge.java", reportLineItem.abbreviatePackage("com/trafigura/titan/hedgemanagement/Hedge.java"));
    }
}
