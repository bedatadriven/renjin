/*******************************************************************************
 * Copyright (c) 2010, 2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sonatype, Inc. - initial API and implementation
 *******************************************************************************/
package org.renjin.aether;

import org.eclipse.aether.AbstractRepositoryListener;
import org.eclipse.aether.RepositoryEvent;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.repository.ArtifactRepository;

import java.io.PrintStream;
import java.util.logging.Logger;

/**
 * A simplistic repository listener that logs events to the console.
 */
public class ConsoleRepositoryListener
        extends AbstractRepositoryListener {

  private static final Logger LOGGER = Logger.getLogger(ConsoleRepositoryListener.class.getName());

  private PrintStream out;

  public ConsoleRepositoryListener() {
    this(null);
  }

  public ConsoleRepositoryListener(PrintStream out) {
    this.out = (out != null) ? out : System.out;
  }

  public void artifactDeployed(RepositoryEvent event) {
    LOGGER.info("Deployed " + event.getArtifact() + " to " + event.getRepository());
  }

  public void artifactDeploying(RepositoryEvent event) {
    LOGGER.fine("Deploying " + event.getArtifact() + " to " + event.getRepository());
  }

  public void artifactDescriptorInvalid(RepositoryEvent event) {
    LOGGER.warning("Invalid artifact descriptor for " + event.getArtifact() + ": "
        + event.getException().getMessage());
  }

  public void artifactDescriptorMissing(RepositoryEvent event) {
    LOGGER.warning("Missing artifact descriptor for " + event.getArtifact());
  }

  public void artifactInstalled(RepositoryEvent event) {
    LOGGER.warning("Installed " + event.getArtifact() + " to " + event.getFile());
  }

  public void artifactInstalling(RepositoryEvent event) {
    LOGGER.warning("Installing " + event.getArtifact() + " to " + event.getFile());
  }

  public void artifactResolved(RepositoryEvent event) {
    out.println("Resolved package " + toString(event.getArtifact()) + " from " + toString(event.getRepository()));
  }

  private String toString(ArtifactRepository repository) {
    if(repository.getId().equals("renjin")) {
      return "nexus.bedatadriven.com";
    } else {
      return repository.toString();
    }
  }

  public void artifactDownloading(RepositoryEvent event) {
    if(concernsPackage(event)) {
      out.println("Trying to download package " +
          toString(event.getArtifact()) + " from " + event.getRepository());
    }
    out.println("Downloading artifact " + event.getArtifact() + " from " + event.getRepository());

  }

  private boolean concernsPackage(RepositoryEvent event) {
    return event.getArtifact().getExtension().equals("jar");
  }

  private String toString(Artifact artifact) {
    return artifact.getGroupId() + ":" + artifact.getArtifactId() + " (version " +
        artifact.getVersion() + ")";
  }

  public void artifactDownloaded(RepositoryEvent event) {
    out.println("Downloaded package " + toString(event.getArtifact()) + " from " + event.getRepository());
  }

  public void artifactResolving(RepositoryEvent event) {
    LOGGER.info("Resolving artifact " + event.getMetadata() + " to " + event.getRepository());
  }

  public void metadataDeployed(RepositoryEvent event) {
    LOGGER.info("Deployed " + event.getMetadata() + " to " + event.getRepository());
  }

  public void metadataDeploying(RepositoryEvent event) {
    LOGGER.fine("Deploying " + event.getMetadata() + " to " + event.getRepository());
  }

  public void metadataInstalled(RepositoryEvent event) {
    LOGGER.info("Installed " + event.getMetadata() + " to " + event.getFile());
  }

  public void metadataInstalling(RepositoryEvent event) {
    LOGGER.fine("Installing " + event.getMetadata() + " to " + event.getFile());
  }

  public void metadataInvalid(RepositoryEvent event) {
    LOGGER.warning("Invalid metadata " + event.getMetadata());
  }

  public void metadataResolved(RepositoryEvent event) {
    LOGGER.fine("Resolved metadata " + event.getMetadata() + " from " + event.getRepository());
  }

  public void metadataResolving(RepositoryEvent event) {
    LOGGER.fine("Resolving metadata " + event.getMetadata() + " from " + event.getRepository());
  }

}