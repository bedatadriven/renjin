library(hamcrest)
library(utils)
library(org.renjin.test.dataTest)

data(wiod05)
assertThat(ls(), identicalTo(c("countries", "final05", "industries", "inter05", "output05")))
assertThat(countries, identicalTo(
        c("AUS", "AUT", "BEL", "BGR", "BRA", "CAN", "CHN", "CYP", "CZE",  "DEU", "DNK", 
          "ESP", "EST", "FIN", "FRA", "GBR", "GRC", "HUN",  "IDN", "IND", "IRL", "ITA", 
          "JPN", "KOR", "LTU", "LUX", "LVA",  "MEX", "MLT", "NLD", "POL", "PRT", "ROM", 
          "RUS", "SVK", "SVN",  "SWE", "TUR", "TWN", "USA", "RoW")))
          
assertThat(output05[1:5], identicalTo(c(37935L, 83639L, 54522L, 4718L, 1217L)))

