library(hamcrest)

expected <- structure(list(
        breaks = c(0x1.8p+3, 0x1.4p+4, 0x1.2p+5, 0x1.4p+6, 
                   0x1.9p+7, 0x1.f4p+9, 0x1.09ap+14), 
        counts = c(12L, 11L, 8L, 6L, 4L, 7L), 
        density = c(0x1p-5, 0x1.d555555555555p-7, 0x1.f07c1f07c1f08p-9, 
                     0x1.1111111111111p-10, 0x1.b4e81b4e81b4fp-14, 0x1.31d5acb6f4651p-17), 
        mids = c(0x1p+4, 0x1.cp+4, 0x1.dp+5, 0x1.18p+7, 0x1.2cp+9, 0x1.194p+13), 
        xname = "structure(c(11506, 5500, 16988, 2968, 16, 184, 23, 280, 84, 73, 25, 43, 21, 82, 3745, 840, 13, 30, 30, 89, 40, 33, 49, 14, 42, 227, 16, 36, 29, 15, 306, 44, 58, 43, 9390, 32, 13, 29, 6795, 16, 15, 183, 14, 26, 19, 13, 12, 82), .Names = c(\"Africa\", \"Antarctica\", \"Asia\", \"Australia\", \"Axel Heiberg\", \"Baffin\", \"Banks\", \"Borneo\", \"Britain\", \"Celebes\", \"Celon\", \"Cuba\", \"Devon\", \"Ellesmere\", \"Europe\", \"Greenland\", \"Hainan\", \"Hispaniola\", \"Hokkaido\", \"Honshu\", \"Iceland\", \"Ireland\", \"Java\", \"Kyushu\", \"Luzon\", \n    \"Madagascar\", \"Melville\", \"Mindanao\", \"Moluccas\", \"New Britain\", \"New Guinea\", \"New Zealand (N)\", \"New Zealand (S)\", \"Newfoundland\", \"North America\", \"Novaya Zemlya\", \"Prince of Wales\", \"Sakhalin\", \"South America\", \"Southampton\", \"Spitsbergen\", \"Sumatra\", \"Taiwan\", \"Tasmania\", \"Tierra del Fuego\", \"Timor\", \"Vancouver\", \"Victoria\"))", 
        equidist = FALSE), 
    .Names = c("breaks", "counts", "density", 
               "mids", "xname", "equidist"), 
    class = "histogram") 


assertThat(graphics:::hist.default(
    breaks=c(12, 20, 36, 80, 200, 1000, 17000),
    freq=TRUE,
    main="WRONG histogram",
    plot=FALSE,
    x=structure(c(11506, 5500, 16988, 2968, 16, 184, 23, 280, 84, 73, 
                  25, 43, 21, 82, 3745, 840, 13, 30, 30, 89, 40, 33, 49, 14, 42, 
                  227, 16, 36, 29, 15, 306, 44, 58, 43, 9390, 32, 13, 29, 6795, 
                  16, 15, 183, 14, 26, 19, 13, 12, 82), 
                .Names = c("Africa", "Antarctica", 
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
