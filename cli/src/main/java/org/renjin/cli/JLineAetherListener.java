package org.renjin.cli;

import jline.console.ConsoleReader;
import org.eclipse.aether.transfer.TransferCancelledException;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transfer.TransferListener;


public class JLineAetherListener implements TransferListener {
    
    private ConsoleReader console;
    private String oldPrompt;

    public JLineAetherListener(ConsoleReader console) {
        this.console = console;
    }

    @Override
    public void transferInitiated(TransferEvent event) throws TransferCancelledException {
        console.setPrompt("Starting transfer of " + event.getResource().getResourceName());
    }

    @Override
    public void transferStarted(TransferEvent event) throws TransferCancelledException {
        console.setPrompt("Starting transfer of " + event.getResource().getResourceName());
    }

    @Override
    public void transferProgressed(TransferEvent event) throws TransferCancelledException {
        console.setPrompt(String.format("Transferred %dl bytes of %s", event.getTransferredBytes(),
                event.getResource().getResourceName()));
    }

    @Override
    public void transferCorrupted(TransferEvent event) throws TransferCancelledException {

    }

    @Override
    public void transferSucceeded(TransferEvent event) {

    }

    @Override
    public void transferFailed(TransferEvent event) {

    }

}
