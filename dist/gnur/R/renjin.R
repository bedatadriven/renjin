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


renjin <- function(expr, envir = parent.frame(), serialize = FALSE, compile = TRUE) {
  
  # Grab the unevaluated expression
  expr <- substitute(expr)

  if(serialize) {
    bytesIn <- serialize(expr, connection = NULL)
    bytesOut <- .jcall(.renjin.session$renjin, "[B", "evalSerialized", bytesIn)
    return(unserialize(bytesOut))
  }
  
  # Now create a Java object to hold the pointer
  jexpr <- toJava(expr, .renjin.session$jengine)
  jenv <- toJava(envir, .renjin.session$jengine)
  
  # Currently a bit tortured to get a real R object
  # back from a Java method. As a workaround, we'll
  # pass this environment in as an argument, and the
  # result will be set there.
  
  resultEnv <- toJava(environment(), .renjin.session$jengine)
  
  .jcall(.renjin.session$renjin, "V", "eval", jexpr, resultEnv, jenv, compile)
  
  return(result)
}