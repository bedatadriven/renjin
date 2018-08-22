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

library(hamcrest)

test.call.replace.attr <- function() {
    call <- ~x
    class(call) <- 'foo'
    attr(call, 'bar') <- 'baz'
    call[-1] <- lapply(call[-1], function(x) x)

    assertThat(deparse(call), identicalTo("~x"))
    assertThat(class(call), identicalTo("foo"))
    assertThat(attr(call, 'bar'), identicalTo('baz'))
    assertThat(attr(call, '.Environment'), identicalTo(environment()))

    # Subsetting does not preserve attributes!
    assertThat(attributes(call[TRUE]), identicalTo(NULL))

    # Also not for vectors!
    vec <- structure(list(1), bar = 'baz')
    assertThat(attributes(vec[TRUE]), identicalTo(NULL))

    # But empty brackets DO retain attributes for lists
    assertThat(attr(vec[], 'bar'), identicalTo('baz'))
    assertThat(attr(call[], 'bar'), identicalTo('baz'))  # <--- GNU R drops attributes for calls but not lists
                                                         #      I think Renjin's behavior is more consistent.
}

test.call.replace.pairlist <- function() {

    listy <- pairlist(a=1,b=2)
    class(listy) <- 'foo'
    attr(listy, 'bar') <- 'baz'

    listy[-1] <- lapply(listy[-1], function(x) x)

    assertThat(class(listy), identicalTo("foo"))
    assertThat(attr(listy, 'bar'), identicalTo('baz'))
}