
#include <stdio.h>

// To be mapped to an existing JVM class
struct jvm_rect {
  int width;
  int height;
};

struct jvm_interface {
  int dummy;
};

// To be mapped to an existing JVM static method
int jvm_area(struct jvm_rect *r);

int jvm_areas(struct jvm_rect *r);

int test(struct jvm_rect *p) {
  return jvm_area(p);
}

int test_multiple() {
  struct jvm_rect rects[3];
  rects[0].width = 10;
  rects[0].height = 15;
  rects[1].width = 20;
  rects[1].height = 35;
  rects[2].width = 0;
  
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

struct jvm_interface ** alloc_pointer_array() {
  struct jvm_interface ** p = malloc(sizeof(struct jvm_interface*) * 10);
  return p;
}
