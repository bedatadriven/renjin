
int test() {
  int x[10];
  x[0] = 41;
  x[1] = 33;


  sum10(x);

  return x[0];
}

int sum10(int values[10]) {
  int i;
  int sum = 0;
  for(i=0;i<10;++i) { sum += values[i]; }
  values[0] = 342;
  return sum;
}
