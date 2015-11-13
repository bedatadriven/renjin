

// To be mapped to an existing JVM class
struct jvm_rect {
  int width;
  int height;
};

// To be mapped to an existing JVM static method
int jvm_area(struct jvm_rect *r);

int jvm_areas(struct jvm_rect *r);

int test(struct jvm_rect *p) {
  return jvm_area(p);
}

int test_multiple() {
  struct jvm_rect rects[2];
  rects[0].width = 10;
  rects[0].height = 15;
  rects[1].width = 20;
  rects[1].height = 35;
  
  return jvm_areas(rects);
  
}

static const struct jvm_rect global_rects[] = {
  {2, 4},
  {3, 5},
  {6, 8},
  {10, 10},
  {0, 0} // "zero-terminated"
  
};

int test_globals() {
  return jvm_areas(global_rects);
}