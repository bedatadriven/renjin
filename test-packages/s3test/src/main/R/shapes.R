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



new.circle <- function(radius) structure(radius, class = "circle")
new.square <- function(width) structure(width, class = "square")

as.character.circle <- function(radius) sprintf("circle of radius %d", radius)
as.character.square <- function(width) sprintf("%dx%d square", width, width)


area <- function(animal) UseMethod("area")
area.circle <- function(radius) radius*radius*pi
area.square <- function(width) width*width

`%/%.circle` <- function(e1, e2) {
    # Long method to test serialization to external file
    if(e1 == 1) {
        return(e1+e2)
    }
    if(e1 == 2) {
        return(42)
    }
    if(e1 == 3) {
        return(43)
    }
    if(e1 == 4) {
        return(44)
    }
    if(e1 == 3) {
        return(43)
    }
    if(e1 == 4) {
        return(44)
    }
    if(e1 == 3) {
        return(43)
    }
    if(e1 == 4) {
        return(44)
    }
}