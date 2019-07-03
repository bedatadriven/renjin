#
# Renjin : JVM-based interpreter for the R language for the statistical analysis
# Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, a copy is available at
# https://www.gnu.org/licenses/gpl-2.0.txt
#


library(hamcrest)

test.csv <- function() {

    df <- read.csv("tables/simple.csv")
    assertThat(names(df), identicalTo(c("A", "B", "C")))
}

test.noquote <- function() {

    df <- read.csv("tables/simple.csv", quote = "")
    assertThat(names(df), identicalTo(c("A", "B", "C")))
}

test.txt.with.comments <- function() {

    df <- read.table("tables/comments.txt", header = TRUE)
    assertThat(names(df), identicalTo(c("buf", "pH", "NaCl", "con", "ra", "det",
        "MgCl2", "temp", "prot.act1", "prot.act2", "prot.act3", "prot.act4")))

}

test.issue.102 <- function() {

    df <- read.csv("tables/issue102.txt", stringsAsFactors = FALSE)
    
    expected <- data.frame(
        X_id = c("3909D56A13834DBDAA7A5F637A48B441",
                "3151D4C10A17440584D1363747F933B2",
                "A01F4909C00542BD919602F6922DCC0C", 
                "993E4E765C5947519D89B8C9068346AF",  
                "235122AAA820439EAE043CA3DD8BF0DF"), 
        operation = c("OP_SELL",  "OP_BUY", "OP_SELL", "OP_BUY", "OP_SELL"), 
        orderOpenPrice = c(1.13427,  1.13866, 1.13983, 1.14088, 1.13923), 
        orderClosePrice = c(1.1373,  1.13834, 1.14088, 1.13923, 1.13977), 
        orderOpenTime = c("2015-02-17T08:30:00Z",  
                "2015-02-17T09:30:00Z", 
                "2015-02-18T02:00:03Z", 
                "2015-02-18T03:00:01Z",  
                "2015-02-18T07:30:00Z"), 
        orderCloseTime = c("2015-02-17T09:08:57Z",  
                "2015-02-17T15:20:24Z", 
                "2015-02-18T03:00:00Z", 
                "2015-02-18T07:30:00Z",
                "2015-02-18T19:00:49Z"), 
        orderProfit = c(-151.5, -16, -52.5,  -82.5, -27), 
        orderSwap = c(0L, 0L, 0L, 0L, 0L), 
        symbol = c("EURUSD",  "EURUSD", "EURUSD", "EURUSD", "EURUSD"), 
        orderTicket = c(122570932L,  122575039L, 122629206L, 122631062L, 122637014L), 
        volume = c(0.5,  0.5, 0.5, 0.5, 0.5), 
        status = c("CLOSED", "CLOSED", "CLOSED",  "CLOSED", "CLOSED"),
        stringsAsFactors = FALSE)
        
    assertThat(df, identicalTo(expected))
}

test.issue.257 <- function() {

    df <- read.csv2("tables/tableau.txt", sep=";", dec=",", stringsAsFactors=FALSE)

    assertThat(df$typo, identicalTo(c("A", "B", "C")))
    assertThat(df$quantite, identicalTo(c(1.5, 2.6, 3.8)))

}