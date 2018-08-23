library(grDevices)
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

test.rgbWithoutNames <- function() {
    assertThat(rgb(1,0,0,0), identicalTo("#FF000000"))
}

test.hsv.range <- function() {
    h <- c(0.35,0.32777777,0.30555555, 0.28333333, 0.2611111, 0.2388888, 0.2166666,0.1944444,0.1722222,0.15)
    s <- c(1.0)
    v <- c(0.5, 0.533333, 0.566666,0.6,0.6333333,0.666660,0.700000,0.733333,0.76666666,0.8)


    assertThat(hsv(h,s,v), identicalTo(c(
        "#00800D", "#058800", "#189000", "#2E9900", "#46A100",
        "#60AA00",  "#7DB300", "#9CBB00", "#BDC300", "#CCB800")))
}


test.hsv.bug <- function() {
    assertThat(hsv(0.3277777,1.00000,0.533333), identicalTo("#058800"))
}