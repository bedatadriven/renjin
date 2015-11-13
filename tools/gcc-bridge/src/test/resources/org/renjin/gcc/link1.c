
int magic_number1 = 41;

extern int shared_triple(int x);


// This function is private to the link1 compilation unit
static int get_unit_number() {
  return 1;
}

static int test() {
  return shared_triple(get_unit_number());
}
