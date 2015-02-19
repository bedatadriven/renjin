package org.renjin.cli;

import com.google.common.collect.Lists;
import jline.console.ConsoleReader;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.transfer.TransferCancelledException;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transfer.TransferListener;
import org.eclipse.aether.transfer.TransferResource;
import org.renjin.aether.PackageListener;
import org.renjin.primitives.packaging.FqPackageName;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class JLineAetherListener implements TransferListener, PackageListener {

    private ConsoleReader reader;

    private Map<TransferResource, Long> downloads = new ConcurrentHashMap<TransferResource, Long>();

    private List<String> failedDownloads = Lists.newArrayList();


    public JLineAetherListener(ConsoleReader console) {
        this.reader = console;
    }

    @Override
    public void transferInitiated(TransferEvent event) throws TransferCancelledException {
        downloads.put(event.getResource(), event.getTransferredBytes());
        updateProgress(event);
    }

    @Override
    public void transferStarted(TransferEvent event) throws TransferCancelledException {
        downloads.put(event.getResource(), event.getTransferredBytes());
        updateProgress(event);
    }

    @Override
    public void transferProgressed(TransferEvent event) throws TransferCancelledException {
        downloads.put(event.getResource(), event.getTransferredBytes());
        updateProgress(event);
    }

    @Override
    public void transferCorrupted(TransferEvent event) throws TransferCancelledException {
        transferFailed(event);
    }

    @Override
    public void transferSucceeded(TransferEvent event) {
        downloads.remove(event.getResource());
        updateProgress(event);
    }

    @Override
    public void transferFailed(TransferEvent event) {
        // Aether generates a lot of failed transfers while looking through repos. Only print error
        // list if the package load fails overall
        failedDownloads.add(event.getResource().getRepositoryUrl() + event.getResource().getResourceName());
        downloads.remove(event.getResource());
        updateProgress(event);
    }

    private synchronized void updateProgress(TransferEvent event) {
        long total = 0;
        long completed = 0;

        for (Map.Entry<TransferResource, Long> entry : downloads.entrySet()) {
            total += entry.getKey().getContentLength();
            completed += entry.getValue();
        }
        
        String url = event.getResource().getRepositoryUrl() + event.getResource().getResourceName();

        switch (event.getType()) {

            case INITIATED:
            case STARTED:
                updateStatus(String.format("Downloading %s...", url));
                break;
            case PROGRESSED:
                updateStatus(String.format("Downloaded %s of %s...",
                        getStatus(event.getTransferredBytes(), event.getResource().getContentLength()),
                        url));
                break;
            case CORRUPTED:
                updateStatus(String.format("%s is corrupted.", url));
                break;
            case SUCCEEDED:
                updateStatus(String.format("Downloaded %s.", url));
                break;
        }
    }

    private void clearProgressBar() {
        try {
            reader.resetPromptLine("", "", 0);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    private void updateStatus(String statusMessage) {
        try {

            int terminalWidth = reader.getTerminal().getWidth();
            if(statusMessage.length() > terminalWidth) {
                statusMessage = statusMessage.substring(0, terminalWidth-6) + "...";
            }
            reader.resetPromptLine(null, statusMessage, statusMessage.length());
        } catch (Exception ignored) {
        }
    }


    private String getStatus(long complete, long total) {
        if (total >= 1024) {
            return toKB(complete) + "/" + toKB(total) + " KB ";
        } else if (total >= 0) {
            return complete + "/" + total + " B ";
        } else if (complete >= 1024) {
            return toKB(complete) + " KB ";
        } else {
            return complete + " B ";
        }
    }

    private long toKB(long bytes) {
        return (bytes + 1023) / 1024;
    }

    @Override
    public void packageLoading(FqPackageName packageName) {
        failedDownloads.clear();
    }

    @Override
    public void packageResolved(FqPackageName packageName, String version) {
    }

    @Override
    public void packageVersionResolutionFailed(FqPackageName packageName) {
        clearProgressBar();
        try {
            reader.println("Could not resolve " + packageName);
            reader.println("Tried:");
            for (String failedDownload : failedDownloads) {
                reader.println("* " + failedDownload);
            }
        } catch (IOException ignored) {
        }
    }

    @Override
    public void packageResolveFailed(DependencyResolutionException exception) {
        clearProgressBar();
        Artifact artifact = exception.getResult().getRoot().getArtifact();
        try {
            reader.println(String.format("Could not resolve dependencies for %s.%s version %s",
                    artifact.getGroupId(),
                    artifact.getArtifactId(),
                    artifact.getVersion()));
            reader.println("The following artifacts could not be located:");
            for (ArtifactResult result : exception.getResult().getArtifactResults()) {
                if (result.isMissing()) {
                    reader.println("* " + result.getRequest().getArtifact());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void packageLoadSucceeded(FqPackageName name, String version) {
        clearProgressBar();
        try {
            reader.println(String.format("Loaded %s.%s verson %s.", name.getGroupId(), name.getPackageName(), version));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
