

// For this struct, the unit pointer assumptions hold, and 
// we can use the RecordUnitPtrStrategy
struct simple_t {
    int count;
}

// point_t is *not* a unit pointer because it we declare contiguous blocks
// of records
struct point_t {
    double x;
    double y;
}


int get_count(struct simple_t *p) {
    return p->count;
}

int simple_value() {
    struct simple_t s;
    s.count = 10;
    return get_count(&s);
}

int simple_malloc() {
    struct simple_t *p = malloc(sizeof(simple_t))
    p->count = 50;
    return get_count(p);
}

double calc_distance(struct t1 *p, int count) {
    double dist = 0;
    int i=0;
    for(i=0;i<count;++i) {
        dist += p[i].x;
    }
    return dist;
}

double point_array() {
    struct point_t path[] = { {0, 0}, {2, 3}, {5, 5}};
    return calc_distance(path, 3);
}

double point_malloc() {
    struct point_t *p = malloc(sizeof(point_t)*5);
    return calc_distance(p, 5);
}




