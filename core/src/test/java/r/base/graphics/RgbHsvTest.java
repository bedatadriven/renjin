package r.base.graphics;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

import r.EvalTestCase;

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
	 public void hsv(){
		 assertThat(eval("hsv(c(0.5,1),1,1,1,1)"), equalTo(c("#00FFFFFF", "#FF0000FF")));
	 }
}
