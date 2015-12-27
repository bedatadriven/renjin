
#include <math.h>


const char * circle_name() {
	
	char * foo = "Hello world";
	return foo;
}

int first_char(char *str) {
   return str[0];
}

int test_first_char() {
   return first_char("hello world");
}

int unmarshall() {
   char *c = circle_name();
   return c[1];
}