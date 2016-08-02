library(hamcrest)

 expected <- structure(list(breaks = c(0x0p+0, 0x1p+2, 0x1p+3, 0x1.8p+3, 0x1p+4, 
0x1.4p+4, 0x1.ep+4, 0x1.4p+5, 0x1.9p+5, 0x1.18p+6, 0x1.9p+6, 
0x1.18p+7), counts = c(11L, 19L, 5L, 3L, 2L, 1L, 0L, 0L, 2L, 
3L, 2L), density = c(0x1.d555555555555p-5, 0x1.9555555555555p-4, 
0x1.aaaaaaaaaaaabp-6, 0x1p-6, 0x1.5555555555555p-7, 0x1.1111111111111p-9, 
0x0p+0, 0x0p+0, 0x1.1111111111111p-9, 0x1.1111111111111p-9, 0x1.1111111111111p-10
), mids = c(0x1p+1, 0x1.8p+2, 0x1.4p+3, 0x1.cp+3, 0x1.2p+4, 0x1.9p+4, 
0x1.18p+5, 0x1.68p+5, 0x1.ep+5, 0x1.54p+6, 0x1.ep+6), xname = "structure(c(107.266024443903, 74.1619848709566, 130.338022081049, 54.4793538875049, 4, 13.5646599662505, 4.79583152331272, 16.7332005306815, 9.16515138991168, 8.54400374531753, 5, 6.557438524302, 4.58257569495584, 9.05538513813742, 61.196405123177, 28.9827534923789, 3.60555127546399, 5.47722557505166, 5.47722557505166, 9.4339811320566, 6.32455532033676, 5.74456264653803, 7, 3.74165738677394, 6.48074069840786, 15.0665191733194, 4, 6, 5.3851648071345, 3.87298334620742, 17.4928556845359, 6.6332495807108, \n    7.61577310586391, 6.557438524302, 96.9020123630051, 5.65685424949238, 3.60555127546399, 5.3851648071345, 82.4317899842045, 4, 3.87298334620742, 13.5277492584687, 3.74165738677394, 5.09901951359278, 4.35889894354067, 3.60555127546399, 3.46410161513775, 9.05538513813742), .Names = c(\"Africa\", \"Antarctica\", \"Asia\", \"Australia\", \"Axel Heiberg\", \"Baffin\", \"Banks\", \"Borneo\", \"Britain\", \"Celebes\", \"Celon\", \"Cuba\", \"Devon\", \"Ellesmere\", \"Europe\", \"Greenland\", \"Hainan\", \"Hispaniola\", \"Hokkaido\", \"Honshu\", \n    \"Iceland\", \"Ireland\", \"Java\", \"Kyushu\", \"Luzon\", \"Madagascar\", \"Melville\", \"Mindanao\", \"Moluccas\", \"New Britain\", \"New Guinea\", \"New Zealand (N)\", \"New Zealand (S)\", \"Newfoundland\", \"North America\", \"Novaya Zemlya\", \"Prince of Wales\", \"Sakhalin\", \"South America\", \"Southampton\", \"Spitsbergen\", \"Sumatra\", \"Taiwan\", \"Tasmania\", \"Tierra del Fuego\", \"Timor\", \"Vancouver\", \"Victoria\"))", 
    equidist = FALSE), .Names = c("breaks", "counts", "density", 
"mids", "xname", "equidist"), class = "histogram") 


assertThat(graphics:::hist.default(breaks=c(0, 4, 8, 12, 16, 20, 30, 40, 50, 70, 100, 140),col="blue1",plot=FALSE,x=structure(c(107.266024443903, 74.1619848709566, 130.338022081049, 
54.4793538875049, 4, 13.5646599662505, 4.79583152331272, 16.7332005306815, 
9.16515138991168, 8.54400374531753, 5, 6.557438524302, 4.58257569495584, 
9.05538513813742, 61.196405123177, 28.9827534923789, 3.60555127546399, 
5.47722557505166, 5.47722557505166, 9.4339811320566, 6.32455532033676, 
5.74456264653803, 7, 3.74165738677394, 6.48074069840786, 15.0665191733194, 
4, 6, 5.3851648071345, 3.87298334620742, 17.4928556845359, 6.6332495807108, 
7.61577310586391, 6.557438524302, 96.9020123630051, 5.65685424949238, 
3.60555127546399, 5.3851648071345, 82.4317899842045, 4, 3.87298334620742, 
13.5277492584687, 3.74165738677394, 5.09901951359278, 4.35889894354067, 
3.60555127546399, 3.46410161513775, 9.05538513813742), .Names = c("Africa", 
"Antarctica", "Asia", "Australia", "Axel Heiberg", "Baffin", 
"Banks", "Borneo", "Britain", "Celebes", "Celon", "Cuba", "Devon", 
"Ellesmere", "Europe", "Greenland", "Hainan", "Hispaniola", "Hokkaido", 
"Honshu", "Iceland", "Ireland", "Java", "Kyushu", "Luzon", "Madagascar", 
"Melville", "Mindanao", "Moluccas", "New Britain", "New Guinea", 
"New Zealand (N)", "New Zealand (S)", "Newfoundland", "North America", 
"Novaya Zemlya", "Prince of Wales", "Sakhalin", "South America", 
"Southampton", "Spitsbergen", "Sumatra", "Taiwan", "Tasmania", 
"Tierra del Fuego", "Timor", "Vancouver", "Victoria")))[-5]
,  identicalTo( expected[-5] ) )
