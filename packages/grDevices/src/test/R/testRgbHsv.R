library(hamcrest)

test.rgb <- function(){
	 assertThat(a<-rgb(c(0.5,1.0),0,0,0,c('Red1','Red2'), maxColorValue=1), equalTo(c("#80000000" , "#FF000000")))
	 assertThat(names(a), equalTo(c("Red1" , "Red2")))
}



test.rgb256 <- function(){
	 assertThat(a<-rgb(c(127,255),0,0,0,c('Red1','Red2'), maxColorValue=255), equalTo(c("#7F000000" , "#FF000000")))
	 assertThat(names(a), equalTo(c("Red1" , "Red2")))
}


test.gray <- function(){
	 assertThat(gray(seq(0,1,0.40)), equalTo(c("#000000", "#666666" ,"#CCCCCC")))
}


test.hsv <- function() {
	 assertThat(hsv(c(0.5,1),1,1,1), equalTo(c("#00FFFFFF", "#FF0000FF")))
}


test.col2rgb <- function(){
	 assertThat(col2rgb('aliceblue'), equalTo(c(240, 248, 255)))
	 assertThat(col2rgb(c('aliceblue', 'blue')), equalTo(c(240,248,255,0,0,255)))
}


test.rgb2hsv <- function(){
	 assertThat(rgb2hsv(1,1,0), closeTo(c( 0.166666667, 1, 0.003921569), 0.01))
}
