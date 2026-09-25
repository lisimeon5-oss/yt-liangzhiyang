package com.zbkj.common.utils;

import org.junit.Test;
import java.math.BigDecimal;
import static org.junit.Assert.*;

public class BargainCalculatorTest {
    @Test public void everySequenceEndsExactlyAtFloorWithoutZeroCuts() {
        for (int people : new int[]{2,3,10,100,1000}) {
            for (int run=0;run<100;run++) {
                BigDecimal floor=new BigDecimal("0.01");
                BigDecimal current=new BigDecimal("999.99");
                for(int left=people;left>0;left--) {
                    BigDecimal cut=BargainCalculator.cut(current,floor,left);
                    assertEquals(2,cut.scale());
                    assertTrue(cut.signum()>0);
                    current=current.subtract(cut);
                    assertTrue(current.subtract(floor).compareTo(BigDecimal.valueOf(left-1,2))>=0);
                }
                assertEquals(floor,current);
            }
        }
    }
    @Test public void minimalDifferenceStillGivesEveryPersonOneCent() {
        BigDecimal current=new BigDecimal("0.04");
        for(int left=3;left>0;left--) {
            BigDecimal cut=BargainCalculator.cut(current,new BigDecimal("0.01"),left);
            assertEquals(new BigDecimal("0.01"),cut);current=current.subtract(cut);
        }
    }
    @Test(expected=IllegalArgumentException.class) public void impossiblePeopleCountIsRejected() {
        BargainCalculator.cut(new BigDecimal("0.03"),new BigDecimal("0.01"),3);
    }
}
