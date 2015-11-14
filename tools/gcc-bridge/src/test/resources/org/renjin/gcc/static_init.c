
struct method_t {
  const char *name;
  int (*fun)(int);
  int power;
};

static int cube(int x) {
  return x * x * x;
}

static int square(int x) {
  return x * x;
}

static const struct method_t CMethods[] = {
  {"cube", &cube, 3},
  {"square", &square, 2},
  {0, 0, 0}
};

const char * test_name() {
  return CMethods[1].name;
}