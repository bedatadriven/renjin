
#include "assert.h"

//void test_pointer() {
//  char buffer[100];
//
//  sprintf(buffer, "%p", (void*)0x1234U);
//  assertStringsEqual(buffer, "00001234");
//}

void test_long() {
 char buffer[100];
  sprintf(buffer, "%lu", 0xFFFFFFFFL);
  assertStringsEqual(buffer, "4294967295");

}