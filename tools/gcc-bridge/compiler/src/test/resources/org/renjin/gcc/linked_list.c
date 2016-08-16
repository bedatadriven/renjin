
#include <stdlib.h>

#include "assert.h"


struct node_t {
    int value;
    struct node_t *next;
};


struct node_t * alloc_list() {
    
    // oversized malloc to face non-unit pointer...
    struct node_t *n2 = malloc(sizeof(struct node_t)*2);
    n2->value = 92;
    n2->next = NULL;
    
    struct node_t *n1 = malloc(sizeof(struct node_t));
    n1->value = 91;
    n1->next = n2;
    
    return n1;
}

void test_loop() {

    struct node_t * head = alloc_list();
    struct node_t * node;
    
    int count;
    int sum;
    
    count = 0;
    sum = 0;
    
    for(node = head; node != NULL; node = node->next) {
        count++;
        sum += node->value;
    }
    
    ASSERT(count == 2);
    ASSERT(sum == (91+92));
}

