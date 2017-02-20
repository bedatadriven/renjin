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


# Compiles unit tests from
# https://hackage.haskell.org/package/regex-posix-unittest into a single CSV data table

sources <- list.files(path = "~/dev/regex-posix-unittest-1.1/data-dir/", full.names = TRUE)
tables <- lapply(sources, function(source) {
  cat(sprintf("file = %s\n", source))
  data <- read.table(file = source, col.names = c("id", "pattern", "string", "expected"), stringsAsFactors = FALSE) 
  
  for(i in 2:nrow(data)) {
    if(identical(data[i, "pattern"], "SAME")) {
      data[i, "pattern"] <- data[i-1, "pattern"]
    }
  }
  data
})

table <- do.call("rbind", tables)
table$id <- NULL

write.csv(table, file = "src/testgen/regex-posix-unittest-1.1.csv", row.names = FALSE, quote = TRUE)
