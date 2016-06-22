
// point_t is *not* a unit pointer because it we declare contiguous blocks
// of records
struct point_t {
    double x;
    double y;
};

struct point_t * malloc_path(int count) {
    return malloc(sizeof(struct point_t) * count);
}

