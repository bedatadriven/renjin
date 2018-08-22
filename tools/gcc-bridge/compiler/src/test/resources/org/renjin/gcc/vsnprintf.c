
#include "assert.h"
#include<stdarg.h>

char buffer[256];


char * call_vsnprintf( const char * format, ... )
{
  va_list args;
  va_start (args, format);
  vsnprintf (buffer,256,format, args);
  va_end (args);
  return buffer;
}

void test_vsnprintf() {
  assertStringsEqual("Hello World", call_vsnprintf("Hello %s", "World"));
}

void test_long_int() {
  assertStringsEqual("Hello Bob at coordinate 42", call_vsnprintf("Hello %2$s at coordinate %1$d", 42L, "Bob"));
}



