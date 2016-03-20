
#include <stdlib.h>

// For this struct, the unit pointer assumptions hold, and 
// we can use the RecordUnitPtrStrategy
struct simple_t {
    int count;
};

int get_count(struct simple_t *p) {
    return p->count;
}

int simple_value() {
    struct simple_t s;
    s.count = 10;
    return get_count(&s);
}

int simple_malloc() {
    struct simple_t *p = malloc(sizeof(struct simple_t));
    p->count = 50;
    return get_count(p);
}





