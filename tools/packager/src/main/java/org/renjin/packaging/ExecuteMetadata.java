/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
package org.renjin.packaging;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.renjin.repackaged.guava.io.CharSource;

import java.io.IOException;
import java.io.Reader;

public class ExecuteMetadata {

  private final JSONObject object;

  private ExecuteMetadata(JSONObject object) {
    this.object = object;
  }

  public String getName() {
    return object.getString("name");
  }

  public String getGroupId() {
    return object.getString("groupId");
  }

  public String getVersion() {
    return object.getString("version");
  }

  public int getParameterCount() {
    return object.getJSONArray("parameters").length();
  }

  public String getParameterType(int index) {
    return object.getJSONArray("parameters").getString(index);
  }

  public ExecuteMetadata read(CharSource charSource) throws IOException {
    JSONObject object;
    try(Reader reader = charSource.openStream()) {
      JSONTokener tokener = new JSONTokener(reader);
      object = new JSONObject(tokener);
    }
    
    return new ExecuteMetadata(object);    
  }
  
}
