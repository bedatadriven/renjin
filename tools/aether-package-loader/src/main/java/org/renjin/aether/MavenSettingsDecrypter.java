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
package org.renjin.aether;


import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.building.DefaultSettingsProblem;
import org.apache.maven.settings.building.SettingsProblem;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.apache.maven.settings.crypto.SettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecryptionResult;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException;

import java.util.ArrayList;
import java.util.List;

public class MavenSettingsDecrypter implements SettingsDecrypter {

  // Copied from Maven as there was no constructor to set the security Dispatcher
  private final SecDispatcher securityDispatcher = new MavenSecDispatcher();

  public SettingsDecryptionResult decrypt(SettingsDecryptionRequest request) {
    List<SettingsProblem> problems = new ArrayList<>();

    List<Server> servers = new ArrayList<>();

    for (Server server : request.getServers()) {
      server = server.clone();

      servers.add(server);

      try {
        server.setPassword(decrypt(server.getPassword()));
      } catch (SecDispatcherException e) {
        problems.add(new DefaultSettingsProblem("Failed to decrypt password for server " + server.getId()
                + ": " + e.getMessage(), SettingsProblem.Severity.ERROR, "server: " + server.getId(), -1, -1, e));
      }

      try {
        server.setPassphrase(decrypt(server.getPassphrase()));
      } catch (SecDispatcherException e) {
        problems.add(new DefaultSettingsProblem("Failed to decrypt passphrase for server " + server.getId()
                + ": " + e.getMessage(), SettingsProblem.Severity.ERROR, "server: " + server.getId(), -1, -1, e));
      }
    }

    List<Proxy> proxies = new ArrayList<>();

    for (Proxy proxy : request.getProxies()) {
      proxy = proxy.clone();

      proxies.add(proxy);

      try {
        proxy.setPassword(decrypt(proxy.getPassword()));
      } catch (SecDispatcherException e) {
        problems.add(new DefaultSettingsProblem("Failed to decrypt password for proxy " + proxy.getId()
                + ": " + e.getMessage(), SettingsProblem.Severity.ERROR, "proxy: " + proxy.getId(), -1, -1, e));
      }
    }

    return new DefaultSettingsDecryptionResult(servers, proxies, problems);
  }

  private String decrypt(String str)
          throws SecDispatcherException {
    return (str == null) ? null : securityDispatcher.decrypt(str);
  }

  class DefaultSettingsDecryptionResult
          implements SettingsDecryptionResult {

    private List<Server> servers;

    private List<Proxy> proxies;

    private List<SettingsProblem> problems;

    public DefaultSettingsDecryptionResult(List<Server> servers, List<Proxy> proxies, List<SettingsProblem> problems) {
      this.servers = (servers != null) ? servers : new ArrayList<>();
      this.proxies = (proxies != null) ? proxies : new ArrayList<>();
      this.problems = (problems != null) ? problems : new ArrayList<>();
    }

    public Server getServer() {
      return servers.isEmpty() ? null : servers.get(0);
    }

    public List<Server> getServers() {
      return servers;
    }

    public Proxy getProxy() {
      return proxies.isEmpty() ? null : proxies.get(0);
    }

    public List<Proxy> getProxies() {
      return proxies;
    }

    public List<SettingsProblem> getProblems() {
      return problems;
    }

  }

}
