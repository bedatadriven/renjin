// Initial template generated from eventloop.h from R 3.2.2
package org.renjin.gnur.api;

@SuppressWarnings("unused")
public final class eventloop {

  private eventloop() { }



  // InputHandler* initStdinHandler (void)

  // void consoleInputHandler (unsigned char *buf, int len)

  // InputHandler* addInputHandler (InputHandler *handlers, int fd, InputHandlerProc handler, int activity)

  // InputHandler* getInputHandler (InputHandler *handlers, int fd)

  // int removeInputHandler (InputHandler **handlers, InputHandler *it)

  // InputHandler* getSelectedHandler (InputHandler *handlers, fd_set *mask)

  // fd_set* R_checkActivity (int usec, int ignore_stdin)

  // fd_set* R_checkActivityEx (int usec, int ignore_stdin, void(*intr)(void))

  // void R_runHandlers (InputHandler *handlers, fd_set *mask)

  // int R_SelectEx (int n, fd_set *readfds, fd_set *writefds, fd_set *exceptfds, struct timeval *timeout, void(*intr)(void))
}
