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

test.scales <- function() {
    x <- sample(3000000000, 10)
    assertThat(length(x), identicalTo(10L))
}

test.withReplacement <- function() {
    x<-1:5
    p<-c(0.00, 0.00, 0.00, 1, 0.00)
    assertThat(sample(x,1L,TRUE,p), identicalTo(4L))
}

test.withReplacementUniform <- function() {
    delta <- 0.1
    x <- 1:5
    assertThat(mean(sample(x, 10000L, TRUE, rep(1/5,5))), closeTo(3.0, delta))
}

test.withoutReplacement <- function() {
    x<-c(1,2,3,4,5,10,9,8,7,6)

    assertThat(sort(sample(x, 10L, FALSE)), equalTo(c(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)))
}

test.minimumParametersCall <- function() {

    x <- c(1,2,3,4,5,10,9,8,7,6)
    assertThat(sort(sample(x, 10L)), equalTo(c(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)))
}

test.without.replacement <- function() {
    set.seed(12345)
    assertThat(sample(c(1,2,3,4,5,6,7,8,9,10), 10), equalTo(c(8, 10, 7, 9, 3, 1, 2, 4, 6, 5)))
}

test.with.replacement <- function() {
    set.seed(12345)
    assertThat(sample(c(1,2,3,4,5,6,7,8,9,10), replace = T), equalTo(c(8, 9, 8, 9, 5, 2, 4, 6, 8, 10)));
}

test.testWithProb <- function() {
    set.seed(12345)
    assertThat(sample(c(1,2,3,4), prob = c(0.1, 0.15, 0.25, 0.50)), equalTo(c(3, 1, 4, 2)))
}


test.WithWeights <- function() {
    set.seed(12345)
    assertThat(sample(c(1,2,3,4), prob = c(1, 100, 10, 7)), equalTo(c(2,4,3,1)))
}


test.testSmallSample <- function() {
    set.seed(12345)
    assertThat(sample(c(20,30,40,38,27,29,32,100,24), 5), equalTo(c(32, 100, 29, 24, 40)))
}


test.sampleWithPresortedProbs <- function() {
    set.seed(12345)
    assertThat(as.double(sample(0:4, size = 1, prob = c(0.2356849, 0.2163148, 0.1985367, 0.1822197, 0.1672438))), identicalTo(3))
}

test.uniformSampleWithReplacement <- function() {
    set.seed(8024)
    n <- 10e6
    m <- 1e6
    x <- sample(m, n, replace=TRUE)

    # Expected result from R 3.2.0
    assertThat(x[1:5], identicalTo(c(855610L, 258712L, 357515L, 584505L, 949772L)))
}