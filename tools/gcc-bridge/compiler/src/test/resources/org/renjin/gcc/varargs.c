
#include <stdlib.h>
#include <stdio.h>
#include <stdarg.h>

#include "assert.h"

char * invoke_sprintf(char *name, int messageCount) {
  char * x = malloc(100);
  sprintf(x, "Hello %s, you have %d messages", name, messageCount);
  return x;
}

void test_sprintf() {

    ASSERT(strcmp(invoke_sprintf("Bob", 99), "Hello Bob, you have 99 messages") == 0);

}

int varargs_i(int which, ...) {
    va_list args;
    va_start(args, which);

    int x;
    while(which >= 0) {
        x = va_arg(args, int);
        which --;
    }

    va_end(args);

    return x;
}

void test_varargs_invocation() {

    ASSERT(varargs_i(0, 91, 92, 93) == 91);
    ASSERT(varargs_i(1, 91, 92, 93) == 92);
    ASSERT(varargs_i(2, 91, 92, 93) == 93);

}

char * invoke_vsnprintf(char *fmt, ...) {
    char *x = malloc(100);
    va_list args;
    va_start(args, fmt);
    vsnprintf(x, 100, fmt, args);
    va_end(args);
    return x;
}

void test_vsnprintf() {

    ASSERT(strcmp(invoke_vsnprintf("Hello %s, you have %d messages", "Bob", 99), "Hello Bob, you have 99 messages") == 0);

}
