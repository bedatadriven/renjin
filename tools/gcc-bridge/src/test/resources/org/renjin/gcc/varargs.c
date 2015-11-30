
#include <stdlib.h>
#include <stdio.h>

char * test_sprintf(char *name, int messageCount) {
  char * x = malloc(100);
  sprintf(x, "Hello %s, you have %d messages", name, messageCount);
  return x;
}