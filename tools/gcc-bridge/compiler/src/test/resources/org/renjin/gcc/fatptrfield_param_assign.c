
#include "assert.h"

#include <stdio.h>
#include <stdlib.h>

struct node {
    int id;    
    double weight;
};

struct edge {
    double weight;
    struct node *head;
    struct node *tail;
};


struct edge * makeEdge(struct node * n) {
    struct edge * newEdge = malloc(sizeof(struct edge));
    newEdge->tail = n;
    return newEdge;
}

void test_assign() {
    
    struct node *pn = malloc(sizeof(struct node)*3);
    struct node *pq = pn+1;
    
    pq->id = 41;
    
    struct edge *pe = makeEdge(pq);
    
    ASSERT(pe->tail->id == 41);
}