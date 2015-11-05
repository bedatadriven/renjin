#include <stdio.h>

typedef struct rng_state {
    int i;
}rng_state;

int Init() {
  rng_state s;
	update(&s->i);
	return  s.i;
}

void update(int * x) {
	*x = 41;
}