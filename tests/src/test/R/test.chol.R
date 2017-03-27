#
# Renjin : JVM-based interpreter for the R language for the statistical analysis
# Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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

test.chol <- function() {

    assertThat(chol(matrix(c(8,1,1,4),2,2)), identicalTo(
        structure(c(2.82842712474619, 0, 0.353553390593274, 1.96850196850295 ),
            .Dim = c(2L, 2L)), tol = 1e-6))

}

test.chol.00a7adfad0bf08ae955117a64220fd83 <- function() {


     expected <- structure(c(0x1.2a5cca6939b4dp+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0,
    0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x1.2cee731d0d6f4p+0,
    0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0,
    0x0p+0, 0x1.210ca9da5e189p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0,
    0x0p+0, 0x0p+0, 0x0p+0, 0x1.8d81671a3d322p-3, 0x0p+0, 0x1.2608e1c3d8665p+0,
    0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0,
    0x0p+0, 0x1.49cfde760bd81p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0,
    0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x1.1801c1e0a6ebap+0,
    0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x1.3aa5296179d85p-13, 0x0p+0,
    0x1.a425f7107e5bfp-3, 0x0p+0, 0x0p+0, 0x1.1175f9417330fp+0, 0x0p+0,
    0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0,
    0x1.02cc6bce03ed8p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0,
    0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x1.01e97a866cfd8p+0), .Dim = c(9L,
    9L))


    assertThat(base:::chol.default(x=structure(c(1.3583398384708, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1.38183187840811,
    0, 0.228160663474221, 0, 0, 0.000176367579302974, 0, 0, 0, 0,
    1.27486563459899, 0, 0, 0, 0, 0, 0, 0, 0.228160663474221, 0,
    1.35689266246681, 0, 0, 0.235659494904759, 0, 0, 0, 0, 0, 0,
    1.65978923927347, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1.19634772057518,
    0, 0, 0, 0, 0.000176367579302974, 0, 0.235659494904759, 0, 0,
    1.18315164681959, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1.02198293973711,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 1.01499348556733), .Dim = c(9L, 9L
    )))
    ,  identicalTo( expected, tol = 1e-6 ) )
}