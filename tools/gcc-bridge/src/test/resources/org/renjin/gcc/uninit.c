

// GCC will happily allow the use of an uninitialized value,
// especially of one on the stack
// but JVM byte code verifier chokes on this, so we need to make sure all variables 
// are initialized.

int test_uninitialized() {
  int sum; // not initialized;
  int values[] = {1, 2, 3, 4};
  int i;
  for(i=0;i<4;++i) {
    sum += values[i];
  }
  return sum;
}

const char * test_ptrarray() {
    const char *rnames[]= {"nrisk","nevent","ncensor", "prev", 
                           "cumhaz", "var", ""};
                           
    return rnames[2];                           
}