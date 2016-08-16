
#include "assert.h"

// From https://github.com/Bioconductor-mirror/CGEN/blob/release-3.3/src/hcl.c

typedef struct Tnode
{
	unsigned int g : 4 ; 
	unsigned int h : 4 ;
	struct Tnode *child ;
	struct Tnode *next ;
} tnode ;


tnode * init() {
    tnode *n = malloc(sizeof(tnode));
    n->g = 3;
    n->h = 14;
    return n;
}

void test_bit_fields() {
    
    tnode *p = init();
    
    ASSERT(p->g == 3);
    ASSERT(p->h == 14);
}