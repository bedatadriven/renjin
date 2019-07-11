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

test.formattingStringsWithPadding <- function() {
    assertThat(format("foobar", width = 8, justify = "left"), identicalTo("foobar  "))
    assertThat(format("foobar", width = 8, justify = "centre"), identicalTo(" foobar "))
    assertThat(format("foobar", width = 8, justify = "right"), identicalTo("  foobar"))
    assertThat(format("foobar", width = 8, justify = "none"), identicalTo("foobar"))

    # padding is unequal on both sides (smallest on the left, largest on the right):
    assertThat(format("foobar", width = 9, justify = "centre"), identicalTo(" foobar  "))

    # extra spaces in original element(s) are preserved:
    assertThat(format(" foobar", width = 9, justify = "centre"), identicalTo("  foobar "))

    assertThat(format(c(NA, "a")), identicalTo(c("NA", "a ")))

    # test encoding of NA's:
    assertThat(format(NA, na.encode = TRUE), identicalTo("NA"))
    assertThat(format(NA, na.encode = FALSE), identicalTo("NA"))
    assertThat(format(NA_character_, na.encode = TRUE), identicalTo("NA"))
    assertThat(format(NA_character_, na.encode = FALSE), identicalTo(NA_character_))

    # if NA_character_ is not encoded, it doesn't have a width:
    assertThat(format(c(NA_character_, "a"), na.encode = FALSE), identicalTo(c(NA_character_, "a")))

}
