package org.renjin.primitives.graphics;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.renjin.EvalTestCase;


public class RgbHsvTest extends EvalTestCase{

	 @Before
	  public void setupPackages() {
	    assumingBasePackagesLoad();
	  }
	 
	 @Test
	 public void rgb(){
		 assertThat(eval("a<-rgb(c(0.5,1.0),0,0,0,c('Red1','Red2'), maxColorValue=1)"), equalTo(c("#7F000000" , "#FF000000")));
		 assertThat(eval("names(a)"), equalTo(c("Red1" , "Red2")));
	 }
	 
	 
	 @Test
	 public void rgb256(){
		 assertThat(eval("a<-rgb(c(127,255),0,0,0,c('Red1','Red2'), maxColorValue=255)"), equalTo(c("#7F000000" , "#FF000000")));
		 assertThat(eval("names(a)"), equalTo(c("Red1" , "Red2")));
	 }
	 
	 @Test 
	 public void gray(){
		 assertThat(eval("gray(seq(0,1,0.40))"), equalTo(c("#000000", "#666666" ,"#CCCCCC")));
	 }
	 
	 @Test 
	 public void hsv() {
		 assertThat(eval("hsv(c(0.5,1),1,1,1)"), equalTo(c("#00FFFFFF", "#FF0000FF")));
	 }
	 
	 @Test
	 public void col2rgb(){
		 assertThat(eval("col2rgb('aliceblue')"), equalTo(c_i(240, 248, 255)));
		 assertThat(eval("col2rgb(c('aliceblue', 'blue'))"), equalTo(c_i(240,248,255,0,0,255)));
	 }
	 
	 
	 @Test
	 public void rgb2hsv(){
		 assertThat(eval("rgb2hsv(1,1,0)"), closeTo(
				 matrix(row( 0.166666667, 1, 0.003921569)), 0.01));
	 }
}
