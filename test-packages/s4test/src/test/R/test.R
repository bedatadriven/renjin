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

library(stats4)
library(org.renjin.test.s4test)
library(hamcrest)


test.seqType.ClassExport.ToNamespace = function(){
  
  d = new("seq", sequence = "ACCATCG", name = "Kozak", type = "")
  r = new("seq", sequence = "AUG", name = "Startcodon", type = "")
  p = new("seq", sequence = "VNAGNVQELHIG", name = "ProtTest", type = "")
  
  d = findType(d)
  r = findType(r)
  p = findType(p)
  
  assertThat(
    d@type,
    identicalTo("DNA")
  )
  
  assertThat(
    r@type,
    identicalTo("RNA")
  )
  
  assertThat(
    p@type,
    identicalTo("protein")
    )
}

test.ExtendClassFromMethodAndExportToNamespace = function() {
  
  a = new("mle")
  b = new("mle_ext")
  
  assertThat( class(b@input_file), identicalTo("character") )
  
  assertThat( class(a@input_file), throwsError() )
  
  assertThat( class(a@coef), identicalTo("numeric") )
  
  assertThat( class(a@coef), identicalTo(class(b@coef)) )

  
  
  setClass("new_ext_mle",
           prototype = c(B = 100, c= "character"),
           contains = "mle_ext"
           )
  
  e = new("new_ext_mle")
  
  assertThat( e@names, identicalTo(c("B","c")))
  
}
