

// To be mapped to an existing JVM class
struct jvm_rect {
  int ref;
};

// To be mapped to an existing JVM static method
int jvm_area(struct jvm_rect *r);


int test(struct jvm_rect *p) {
  return jvm_area(p);
}



