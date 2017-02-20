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


## Prepares test input from PCRE sources

source <- "~/dev/pcre/testdata/testinput1"
lines <- readLines(source)

table <- data.frame(pattern = character(0), string = character(0), expected = logical(0), stringsAsFactors = FALSE)


pattern <- ""
matchExpected <- TRUE

for(line in lines) {

  if(!all(charToRaw(line) <= 128)) {
    next;
  }

  firstChar <- substr(line, 1, 1) 
  if(firstChar == "/") {
    pattern <- substr(line, 2, nchar(line))
    matchExpected <- TRUE
    repeat {
      if(!nzchar(pattern)) {
        stop(sprintf("cannot parse line: %s\n", line))
      }
      lastChar <- substr(pattern, nchar(pattern), nchar(pattern))
      pattern <- substr(pattern, 1, nchar(pattern) - 1)
      if(lastChar == "/") {
        break;
      }
    }
  } else if(firstChar == "#") {
    next;
  } else if(line == "\\= Expect no match") {
    matchExpected <- FALSE
  } else {
    string <- sub(x = line, "^\\s+", "")
    string <- sub(x = string, "\\s+$", "")
    if(substr(string, 1, 1) == "\\") {
      string <- substr(string, 2, nchar(string))
    }
    if(nzchar(string)) {
      row <- nrow(table)+1
      table[row, "pattern"] <- pattern
      table[row, "string"] <- string
      table[row, "expected"] <- matchExpected
    }
  }
}