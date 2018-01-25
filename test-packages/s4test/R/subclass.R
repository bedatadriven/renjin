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



setClass("NSBS",
    representation(
        "VIRTUAL",
        upper_bound="integer",            # single integer >= 0
        upper_bound_is_strict="logical",  # TRUE or FALSE
        ## 'subscript' is an object that holds integer values >= 1 and
        ## <= upper_bound. The precise type of the object depends on the NSBS
        ## subclass and is specified in the subclass definition.
        subscript="ANY"
    ),
    prototype(
        upper_bound=0L,
        upper_bound_is_strict=TRUE
    )
)


setClass("NativeNSBS",  # not exported
    contains="NSBS",
    representation(
        subscript="integer"
    ),
    prototype(
        subscript=integer(0)
    )
)