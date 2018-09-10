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
package org.renjin.packaging;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.renjin.repackaged.guava.net.UrlEscapers;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Client for querying the Renjin package repository
 */
public class PackageRepoClient {

  public Map<String, ResolvedDependency> resolve(Iterable<String> packageNames) throws IOException {

    // Use super-simple JDK classes to avoid dependency conflicts...
    StringBuilder queryString = new StringBuilder();
    for (String packageName : packageNames) {
      if(queryString.length() > 0) {
        queryString.append("&");
      }
      queryString.append("p=").append(UrlEscapers.urlFormParameterEscaper().escape(packageName));
    }

    URL url = null;
    try {
      url = new URL("http://packages.renjin.org/packages/resolve?" + queryString.toString());
    } catch (MalformedURLException e) {
      throw new IOException(e);
    }

    Map<String, ResolvedDependency> resultMap = new HashMap<>();

    try(Reader reader = new InputStreamReader(url.openStream())) {
      JSONArray array = new JSONArray(new JSONTokener(reader));
      for (int i = 0; i < array.length(); i++) {
        JSONObject result = array.getJSONObject(i);
        String name = result.getString("package");
        String groupId = result.getString("groupId");
        String version = result.getString("version");

        resultMap.put(name, new ResolvedDependency(name, groupId, version));
      }
    }

    return resultMap;
  }

}
