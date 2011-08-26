
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
    
}
