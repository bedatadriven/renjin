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

# Common inputs for functions


vectorLikeInputs <- list(
  NULL,

  # logical
  logical(0),
  c(TRUE, TRUE, FALSE, FALSE, TRUE),
  c(a=TRUE, FALSE),
  c(TRUE, FALSE, NA),

  # integer
  integer(0),
  structure(integer(0), .Names = character(0)),
  c(1L, 2L, 3L),
  c(1L, NA, 4L, NA, 999L),
  c(1L, 2L, 1073741824L, 1073741824L), # overflow

  # double
  double(0),
  signif(1:5 * pi),
  signif(1:5 * -pi),
  c(a = 1L, b = 2L),  # names
  c(a = 1.5, b = 2.5),
  c(1.5, 1.51, 0, 1.49, -30),
  c(1.5, 1.51, 0, 1.49, -30, NA),
  c(1.5, 1.51, 0, 1.49, -30, NaN),
  c(1.5, 1.51, 0, 1.49, -30, Inf),
  c(1.5, 1.51, 0, 1.49, -30, -Inf),


  # character
  character(0),
  c('4.1', 'blahh', '99.9', '-413', NA),

  # complex
  complex(0),


  # lists
  list(1, 2, 3),
  list(1, 2, NULL),
  list(1L, 2L, 3L),
  list(1L, 2L, NULL),
  list(1, 2, list(3, 4)),
  list(3, "a", list("b", z = list(TRUE, "c"))),
  structure(list(1,2,3), .Names = c(NA_character_, "", "b")),

  # pairlist
  pairlist(41, "a", 21L),
  pairlist(a = 41, 42),
  pairlist(a = 41, NULL),

  # matrices
  matrix(1:12, nrow = 3),
  matrix(1:12, nrow = 3, dimnames = list(x=letters[1:3], y=letters[4:7])),
  structure(1:3, rando.attrib=941L),

  #arrays
  array(1:3, dim = 3L, dimnames = list(c('a', 'b', 'c'))),
  array(1:3, dim = 3L, dimnames = list(z = c('a', 'b', 'c'))),

  # S3 dispatch?
  structure(list('foo'), class='foo'),
  structure(list('bar'), class='foo'),

  # Symbols
  as.name('xyz'),

  # Function Calls
  call('sin', 3.14)
)
