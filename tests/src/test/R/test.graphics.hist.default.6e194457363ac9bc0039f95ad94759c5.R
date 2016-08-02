library(hamcrest)

 expected <- structure(list(breaks = c(0x1.9p+6, 0x1.2cp+7, 0x1.9p+7, 0x1.f4p+7, 
0x1.2cp+8, 0x1.5ep+8, 0x1.9p+8, 0x1.c2p+8, 0x1.f4p+8, 0x1.13p+9, 
0x1.2cp+9, 0x1.45p+9), counts = c(24L, 24L, 21L, 13L, 21L, 13L, 
13L, 8L, 4L, 1L, 2L), density = c(0x1.b4e81b4e81b4fp-9, 0x1.b4e81b4e81b4fp-9, 
0x1.7e4b17e4b17e5p-9, 0x1.d950c83fb72eap-10, 0x1.7e4b17e4b17e5p-9, 
0x1.d950c83fb72eap-10, 0x1.d950c83fb72eap-10, 0x1.23456789abcdfp-10, 
0x1.23456789abcdfp-11, 0x1.23456789abcdfp-13, 0x1.23456789abcdfp-12
), mids = c(0x1.f4p+6, 0x1.5ep+7, 0x1.c2p+7, 0x1.13p+8, 0x1.45p+8, 
0x1.77p+8, 0x1.a9p+8, 0x1.dbp+8, 0x1.068p+9, 0x1.1f8p+9, 0x1.388p+9
), xname = "structure(c(112, 118, 132, 129, 121, 135, 148, 148, 136, 119, 104, 118, 115, 126, 141, 135, 125, 149, 170, 170, 158, 133, 114, 140, 145, 150, 178, 163, 172, 178, 199, 199, 184, 162, 146, 166, 171, 180, 193, 181, 183, 218, 230, 242, 209, 191, 172, 194, 196, 196, 236, 235, 229, 243, 264, 272, 237, 211, 180, 201, 204, 188, 235, 227, 234, 264, 302, 293, 259, 229, 203, 229, 242, 233, 267, 269, 270, 315, 364, 347, 312, 274, 237, 278, 284, 277, 317, 313, 318, 374, 413, 405, 355, 306, 271, 306, 315, 301, \n    356, 348, 355, 422, 465, 467, 404, 347, 305, 336, 340, 318, 362, 348, 363, 435, 491, 505, 404, 359, 310, 337, 360, 342, 406, 396, 420, 472, 548, 559, 463, 407, 362, 405, 417, 391, 419, 461, 472, 535, 622, 606, 508, 461, 390, 432), .Tsp = c(1949, 1960.91666666667, 12), class = \"ts\")", 
    equidist = TRUE), .Names = c("breaks", "counts", "density", 
"mids", "xname", "equidist"), class = "histogram") 
 expected <- structure(list(breaks = c(0x0p+0, 0x1.f4p+10, 0x1.f4p+11, 0x1.77p+12, 
0x1.f4p+12, 0x1.388p+13, 0x1.77p+13, 0x1.b58p+13, 0x1.f4p+13, 
0x1.194p+14), counts = c(41L, 2L, 1L, 1L, 1L, 1L, 0L, 0L, 1L), 
    density = c(0x1.bfd44f3078264p-12, 0x1.5d867c3ece2a5p-16, 
    0x1.5d867c3ece2a5p-17, 0x1.5d867c3ece2a5p-17, 0x1.5d867c3ece2a5p-17, 
    0x1.5d867c3ece2a5p-17, 0x0p+0, 0x0p+0, 0x1.5d867c3ece2a5p-17
    ), mids = c(0x1.f4p+9, 0x1.77p+11, 0x1.388p+12, 0x1.b58p+12, 
    0x1.194p+13, 0x1.57cp+13, 0x1.964p+13, 0x1.d4cp+13, 0x1.09ap+14
    ), xname = "structure(c(11506, 5500, 16988, 2968, 16, 184, 23, 280, 84, 73, 25, 43, 21, 82, 3745, 840, 13, 30, 30, 89, 40, 33, 49, 14, 42, 227, 16, 36, 29, 15, 306, 44, 58, 43, 9390, 32, 13, 29, 6795, 16, 15, 183, 14, 26, 19, 13, 12, 82), .Names = c(\"Africa\", \"Antarctica\", \"Asia\", \"Australia\", \"Axel Heiberg\", \"Baffin\", \"Banks\", \"Borneo\", \"Britain\", \"Celebes\", \"Celon\", \"Cuba\", \"Devon\", \"Ellesmere\", \"Europe\", \"Greenland\", \"Hainan\", \"Hispaniola\", \"Hokkaido\", \"Honshu\", \"Iceland\", \"Ireland\", \"Java\", \"Kyushu\", \"Luzon\", \n    \"Madagascar\", \"Melville\", \"Mindanao\", \"Moluccas\", \"New Britain\", \"New Guinea\", \"New Zealand (N)\", \"New Zealand (S)\", \"Newfoundland\", \"North America\", \"Novaya Zemlya\", \"Prince of Wales\", \"Sakhalin\", \"South America\", \"Southampton\", \"Spitsbergen\", \"Sumatra\", \"Taiwan\", \"Tasmania\", \"Tierra del Fuego\", \"Timor\", \"Vancouver\", \"Victoria\"))", 
    equidist = TRUE), .Names = c("breaks", "counts", "density", 
"mids", "xname", "equidist"), class = "histogram") 




assertThat(graphics:::hist.default(col="gray",labels=TRUE,plot=FALSE,x=structure(c(11506, 5500, 16988, 2968, 16, 184, 23, 280, 84, 73, 
25, 43, 21, 82, 3745, 840, 13, 30, 30, 89, 40, 33, 49, 14, 42, 
227, 16, 36, 29, 15, 306, 44, 58, 43, 9390, 32, 13, 29, 6795, 
16, 15, 183, 14, 26, 19, 13, 12, 82), .Names = c("Africa", "Antarctica", 
"Asia", "Australia", "Axel Heiberg", "Baffin", "Banks", "Borneo", 
"Britain", "Celebes", "Celon", "Cuba", "Devon", "Ellesmere", 
"Europe", "Greenland", "Hainan", "Hispaniola", "Hokkaido", "Honshu", 
"Iceland", "Ireland", "Java", "Kyushu", "Luzon", "Madagascar", 
"Melville", "Mindanao", "Moluccas", "New Britain", "New Guinea", 
"New Zealand (N)", "New Zealand (S)", "Newfoundland", "North America", 
"Novaya Zemlya", "Prince of Wales", "Sakhalin", "South America", 
"Southampton", "Spitsbergen", "Sumatra", "Taiwan", "Tasmania", 
"Tierra del Fuego", "Timor", "Vancouver", "Victoria")))[-5]
,  identicalTo( expected[-5] ) )
