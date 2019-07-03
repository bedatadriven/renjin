package org.renjin.packaging;

import io.airlift.airline.Option;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DefaultPackageList {

  @Option(name = "--default-packages", description = "Comma-separated list of packages to load")
  private String defaultPackageList;

  public List<String> getList() {
    if(defaultPackageList == null || defaultPackageList.trim().isEmpty()) {
      return Collections.emptyList();
    } else {
      return Arrays.asList(defaultPackageList.split("\\s*,\\s*"));
    }
  }
}
