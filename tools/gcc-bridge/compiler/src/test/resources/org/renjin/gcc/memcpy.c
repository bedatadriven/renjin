

int test_memcpy() {
  char x[10];
  strcpy(x, "foobar");
  if(x[3] == 'b') {
    return 1;
  } else {
    return 0; // failed
  }
}