

static int Power2 [ ] =
{
/*  0  1  2  3  4  5  6  7  8  9  10*/
    0, 1, 2, 4, 4, 8, 8, 8, 8, 8,  8
} ;


int test() {
  int x[10];
  x[0] = 41;
  x[1] = 33;


  sum10(x);

  return x[0];
}

int test_static() {
  return sum10(Power2);
}

int sum10(int values[10]) {
  int i;
  int sum = 0;
  for(i=0;i<10;++i) { sum += values[i]; }
  values[0] = 342;
  return sum;
}
