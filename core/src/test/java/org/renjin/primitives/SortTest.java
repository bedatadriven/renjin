/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.renjin.primitives;

import org.junit.Test;
import org.renjin.EvalTestCase;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class SortTest extends EvalTestCase {
  
    @Test
    public void sortInts (){
      assertThat(eval(".Internal(sort(c(1L,5L,2L,9L,1L), decreasing=TRUE))"), equalTo(c_i(9,5,2,1,1)));
      assertThat(eval(".Internal(sort(c(1L,5L,2L,9L,1L), decreasing=FALSE))"), equalTo(c_i(1,1,2,5,9)));
    }

    @Test
    public void sortLogical (){
        assertThat(eval(".Internal(psort(c(TRUE,FALSE,FALSE,TRUE,FALSE), 1:3))"), equalTo(c(false,false,false,true,true)));
    }

    @Test
    public void rank () {
        assertThat(eval(".Internal(rank(c(2, 3, 1, 1, 2, 2, 2), \"min\"))"), equalTo(c_i(3, 7, 1, 1, 3, 3, 3)));
        assertThat(eval(".Internal(rank(c(2, 3, 1, 1, 2, 2, 2), \"max\"))"), equalTo(c_i(6, 7, 2, 2, 6, 6, 6)));
        assertThat(eval(".Internal(rank(c(2, 3, 1, 1, 2, 2, 2), \"average\"))"), equalTo(c(4.5, 7.0, 1.5, 1.5, 4.5, 4.5, 4.5)));
        assertThat(eval(".Internal(rank(c(2, 3, 1, 1, 2, 2, 2), \"min\"))"), equalTo(c_i(3, 7, 1, 1, 3, 3, 3)));
        assertThat(eval(".Internal(rank(c(2, 3, 1, 1, 2, 2, 2), \"max\"))"), equalTo(c_i(6, 7, 2, 2, 6, 6, 6)));
        assertThat(eval(".Internal(rank(c(2, 3, 1, 1, 2, 2, 2), \"average\"))"), equalTo(c(4.5, 7.0, 1.5, 1.5, 4.5, 4.5, 4.5)));
        assertThat(eval(".Internal(rank(c('a','b','a','d','e'), \"min\"))"), equalTo(c_i(1, 3, 1, 4, 5)));
        assertThat(eval(".Internal(rank(c('A','B','A','D','E'), \"min\"))"), equalTo(c_i(1, 3, 1, 4, 5)));
        assertThat(eval(".Internal(rank(c(2L, 3L, 1L, 1L, 2L, 2L, 2L), \"min\"))"), equalTo(c_i(3, 7, 1, 1, 3, 3, 3)));
        assertThat(eval(".Internal(rank(c(2L, 3L, 1L, 1L, 2L, 2L, 2L), \"max\"))"), equalTo(c_i(6, 7, 2, 2, 6, 6, 6)));
        assertThat(eval(".Internal(rank(c(2L, 3L, 1L, 1L, 2L, 2L, 2L), \"average\"))"), equalTo(c(4.5, 7.0, 1.5, 1.5, 4.5, 4.5, 4.5)));
        assertThat(eval(".Internal(rank(integer(0), \"min\"))"), equalTo(c_i()));
        assertThat(eval(".Internal(rank(integer(0), \"max\"))"), equalTo(c_i()));
        assertThat(eval(".Internal(rank(integer(0), \"average\"))"), equalTo(c(new double[0])));

        assertThat(eval(".Internal(rank(c(2, 3, 1, 1, 2, 2, 2, 3), \"max\"))"), equalTo(c_i(6, 8, 2, 2, 6, 6, 6, 8)));
    }


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
    
    @Test
    public void orderTest() {
      
        /*  1 2 3
         *  -----
         *  1 1 3  
         *  1 2 9
         *  1 1 1 
         */
      
        assertThat( eval(".Internal(order(TRUE,FALSE,c(1,1,1), c(1,2,1), c(3,9,1)))"), equalTo(c_i(3,1,2)));
        assertThat( eval(".Internal(order(TRUE,TRUE,c(1,1,1), c(1,2,1), c(3,9,1)))"), equalTo(c_i(2,1,3)));
    }

    @Test
    public void qsort() {
      assertThat( eval(".Internal(qsort(c(3,1,5,0), FALSE))"), equalTo(c(0,1,3,5)));
    }

    @Test
    public void unsorted() {
      assertThat( eval(".Internal(is.unsorted(c(1,2,3), TRUE))"), equalTo(c(false)) );
      assertThat( eval(".Internal(is.unsorted(c(3,2,1), FALSE))"), equalTo(c(true)) );
      assertThat( eval(".Internal(is.unsorted(c(1,1,1), TRUE))"), equalTo(c(true)) );
      assertThat( eval(".Internal(is.unsorted(c(1,1,1), FALSE))"), equalTo(c(false)) );
    }
}
