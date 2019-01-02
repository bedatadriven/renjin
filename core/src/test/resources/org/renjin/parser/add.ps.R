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

add.ps <- function(data, col.id, col.offset, col.x, col.value, fun.aggregate = "mean", ref.offset = 1, prefixes,  alternative = c("two.sided", "less", "greater"), mu = 0, paired = FALSE, var.equal = FALSE, lty = 0, ... ) {  round.ps <- function(x) {		as.character(ifelse(x < 0.001, "<.001", ifelse(x < 0.01, paste("=", substr(round(x, 3),2,5), sep =""),ifelse((x < 0.05) & (round(x, 2) == 0.05), "<.05", ifelse(round(x,2) == 1, ">.99", paste("=", substr(round(x, 2), 2, 4), sep = ""))))))	}  if(!is.data.frame(data)) stop("data must be a data.frame")  columns <- c(col.id, col.offset, col.x, col.value)  if (any(!(columns %in% colnames(data)))) stop("column not matching the data")  formula.agg <- as.formula(paste(col.value, "~", col.id, "+", col.offset, "+", col.x))  d.new <- aggregate(formula.agg, data = data, FUN = fun.aggregate)  #so far same as in rm.plot2  l.offset <- levels(factor(d.new[,col.offset]))  l.offset.test <- l.offset[-ref.offset]  if (missing(prefixes)) prefixes <- paste("p(", l.offset.test, ")", sep = "")  l.x <- levels(factor(d.new[,col.x]))  for (c.offset in seq_along(l.offset.test)) {    tmp.ps <- vector("numeric", length(l.x))    for (c.x in seq_along(l.x)) {      tmpx <- d.new[d.new[,col.offset] == l.offset[ref.offset] & d.new[,col.x] == l.x[c.x], ]	  tmpx <- tmpx[order(tmpx[,col.id]),]	  tmpy <- d.new[d.new[,col.offset] == l.offset.test[c.offset] & d.new[,col.x] == l.x[c.x], ]	  tmpy <- tmpy[order(tmpy[,col.id]),]      tmp.ps[c.x] <- t.test(tmpx[,col.value], tmpy[,col.value], alternative = alternative, mu = mu, paired = paired, var.equal = var.equal)$p.value    }    tmp.labels <- paste(prefixes[c.offset], round.ps(tmp.ps), sep = "")    axis(1,seq_along(l.x), labels = tmp.labels, line = c.offset, lty = lty,  ...)  }      }