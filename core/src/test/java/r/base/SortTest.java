
package r.base;

import org.junit.Test;

import r.EvalTestCase;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class SortTest extends EvalTestCase {
    
    @Test
    public void sortStringsDescending (){
        assertThat(eval(".Internal(sort(c('5','8','7','50'), decreasing=TRUE))"), equalTo(c("8","7","50","5")));
    }
    
    @Test
    public void sortNumericsDescending(){
        /* Needs to be implemented */
    }
    
    @Test
    public void testWhichMin(){
        assertThat (eval(".Internal(which.min(c(6,5,4,6,5,4,1)))"), equalTo(c_i(7)));
        assertThat (eval(".Internal(which.min(c()))"), equalTo(c_i()));
    }
    
    @Test
    public void testWhichMax(){
        assertThat (eval(".Internal(which.max(c(6,5,4,6,5,4,1,6)))"), equalTo(c_i(1)));
        assertThat (eval(".Internal(which.max(c()))"), equalTo(c_i()));
    }
    
    
    
}
