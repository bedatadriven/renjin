/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.invoke.codegen.args;

import org.renjin.invoke.codegen.GeneratorDefinitionException;
import org.renjin.invoke.model.JvmMethod;


public class ArgConverterStrategies {

  public static ArgConverterStrategy findArgConverterStrategy(JvmMethod.Argument formal) {
    if(Recyclable.accept(formal)) {
      return new Recyclable(formal);
    
    } else if(UsingAsCharacter.accept(formal)) {
      return new UsingAsCharacter(formal);

    } else if(UnwrapS4Environment.accept(formal)) {
      return new UnwrapS4Environment(formal);
    
    } else if(SexpSubclass.accept(formal)) {
      return new SexpSubclass(formal);
    
    } else if(ToScalar.accept(formal)) {
      return new ToScalar(formal);
    
    } else if(UnwrapExternalObject.accept(formal)) {
      return new UnwrapExternalObject(formal);
    }
    
    throw new GeneratorDefinitionException("Could not find a strategy for converting to argument " + formal.getIndex() + " of type " + formal.getClazz());
  }

}
