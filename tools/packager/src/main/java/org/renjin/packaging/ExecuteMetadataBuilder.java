/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${$file.lastModified.year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */

package org.renjin.packaging;

import org.json.JSONArray;
import org.json.JSONObject;
import org.renjin.sexp.*;

import java.util.Date;

public class ExecuteMetadataBuilder {



  public static JSONObject composeMetadata(PackageSource packageSource, Closure executeFunction) {
    JSONObject metadata = new JSONObject();
    metadata.put("name", packageSource.getPackageName());
    metadata.put("groupId", packageSource.getGroupId());
    metadata.put("description", packageSource.getDescription().getDescription());
    metadata.put("version", packageSource.getDescription().getVersion());
    metadata.put("published", new Date().toInstant().toString());
    metadata.put("parameters", composeParameterMetadata(executeFunction));

    return metadata;
  }

  static JSONArray composeParameterMetadata(Closure executeFunction) {
    JSONArray parameterArray = new JSONArray();
    for (PairList.Node formal : executeFunction.getFormals().nodes()) {
      parameterArray.put(composeParameterMetadata(formal));
    }
    return parameterArray;
  }

  private static JSONObject composeParameterMetadata(PairList.Node formal) {
    JSONObject parameterObject = new JSONObject();
    parameterObject.put("name", formal.getTag().getPrintName());
    if(formal.getValue() instanceof FunctionCall) {
      FunctionCall parameterType = (FunctionCall) formal.getValue();
      if(parameterType.getFunction() instanceof Symbol) {
        parameterObject.put("type", ((Symbol) parameterType.getFunction()).getPrintName());
      }
      for (PairList.Node typeArgument : parameterType.getArguments().nodes()) {
        if(typeArgument.hasName()) {
          parameterObject.put(typeArgument.getName(), toParameterTypeArgument(typeArgument.getValue()));
        }
      }
    }
    return parameterObject;
  }

  private static Object toParameterTypeArgument(SEXP value) {
    if(value instanceof StringVector && value.length() > 0) {
      return ((StringVector) value).getElementAsString(0);
    } else if((value instanceof IntVector || value instanceof DoubleVector) && value.length() > 0) {
      return ((AtomicVector) value).getElementAsDouble(0);
    } else {
      return null;
    }
  }
}
