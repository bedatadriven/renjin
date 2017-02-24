

#include <stdlib.h>
#include "assert.h"

struct head_t {
    struct head_t *prev;
    struct head_t *next;
    double *data;
    int len;
};

struct Cache {
    struct head_t *head;
    struct head_t lru_head;
    int size;
};

void init_cache(struct Cache *pcache, int l) {
    pcache->head = (struct head_t *)calloc(l,sizeof(struct head_t));	// initialized to 0
    pcache->lru_head.next = pcache->lru_head.prev = &pcache->lru_head;
}

void test_cache() {
    struct Cache cache;
    init_cache(&cache, 100);

    cache.head[3].len = 4;
    cache.head[4].len = 40;

    ASSERT(cache.head[3].len == 4);
    ASSERT(cache.head[4].len == 40);

    cache.lru_head.next->len = 99;

    ASSERT(cache.lru_head.len == 99);
}