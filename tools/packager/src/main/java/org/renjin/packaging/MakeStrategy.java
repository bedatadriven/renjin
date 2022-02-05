package org.renjin.packaging;

/**
 * Alternative strategies for running 'make' on packages with
 * natives sources.
 */
public enum MakeStrategy {
    /**
     * Run make within a VirtualBox virtual machine with a preconfigured environment.
     */
    VAGRANT,

    /**
     * Run make in the local filesystem. Requires a unix-like system with the correct
     * version of gcc installed.
     */
    LOCAL

}
