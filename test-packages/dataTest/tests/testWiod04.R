library(hamcrest)
library(utils)
library(org.renjin.test.dataTest)

data(wiod04)
assertThat(ls(), identicalTo(c("countries", "final04", "industries", "inter04", "output04")))
assertThat(countries, identicalTo(
        c("AUS", "AUT", "BEL", "BGR", "BRA", "CAN", "CHN", "CYP", "CZE",  "DEU", "DNK", 
          "ESP", "EST", "FIN", "FRA", "GBR", "GRC", "HUN",  "IDN", "IND", "IRL", "ITA", 
          "JPN", "KOR", "LTU", "LUX", "LVA",  "MEX", "MLT", "NLD", "POL", "PRT", "ROM", 
          "RUS", "SVK", "SVN",  "SWE", "TUR", "TWN", "USA", "RoW")))
          
assertThat(output04[1:5], identicalTo(c(36361L, 55969L, 51905L, 4492L, 1158L)))

