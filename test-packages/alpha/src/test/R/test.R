#
# Renjin : JVM-based interpreter for the R language for the statistical analysis
# Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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


library(org.renjin.test.alpha)
library(hamcrest)


test.jtsVersion <- function() {
    assertThat(alphaVersion(), identicalTo("1.13.0"))
}

test.invokeMethodsInJavaClasses <- function() {
    assertThat(alphaName(), identicalTo("Alpha"))
}

test.dependenciesLoadedOnClasspath <- function() {

    # This is currently an unfortunate side affect of the way were are mixing
    # R's concept of namespaces and java libraries.
    
    # We are still waiting for a standard for modules for the JVM,
    # which would give you the ability to keep dependencies truly private.
    
    # In the meantime, when an R Package depends on a JVM library, this library
    # will be on or added to the JVM's classpath, and so available to any R 
    # code that happens to run. 
    
    jv <- import(com.vividsolutions.jts.JTSVersion)
    assertThat(jv$CURRENT_VERSION$toString(), identicalTo("1.13.0"))
}

test.definedDuringLoad <- function() {
    assertThat(alpha:::defined.during.load, identicalTo(84))
}


test.packageRds <- function() {
    path <- file.path(find.package('alpha'), "Meta", "package.rds")

    assertTrue(file.exists(path))

}


test.activebinding.onload <- function() {
    x <- symbol
    y <- symbol

    assertTrue(is.numeric(x))
    assertTrue(is.numeric(y))
    assertTrue(x != y)
}