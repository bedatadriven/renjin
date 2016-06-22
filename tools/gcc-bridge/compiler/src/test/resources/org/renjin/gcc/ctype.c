
#include <ctype.h>
#include <stdio.h>

int count_whitespace(const char * str) {
  int count = 0;
  int i;
  for(i=0;i<strlen(str);++i) {
    if(isspace(str[i])) {
      count ++;
    }
  }
  return count;
}