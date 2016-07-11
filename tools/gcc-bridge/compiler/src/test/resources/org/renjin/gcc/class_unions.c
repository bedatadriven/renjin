
#include "assert.h"

struct s32_mat {
    int nrow;
    int ncol;
    int nelem;
    
    int* data;
};

struct dbl_mat {
    int nrow;
    int ncol;
    int nelem;
    
    double *data;
};

union mat_union {
    struct s32_mat s32mat;
    struct dbl_mat dblmat;
};

struct D {
    double x;
    double y;
};

union E {
    int *i;
    struct D *d;
};

struct univ_mat {
    union mat_union mat;
    int type;
};

void test_simple_fields() {
    union mat_union c;
    struct s32_mat *a = &c.s32mat;
    struct dbl_mat *b = &c.dblmat;
    
    a->nrow = 14;
    a->ncol = 3;
    
    ASSERT(b->nrow == 14)
    ASSERT(b->ncol == 3)
}

void test_unioned_primitive_pointers() {
    union mat_union c;
    struct s32_mat *a = &c.s32mat;
    struct dbl_mat *b = &c.dblmat;
    
    int data[3] = {91, 92, 93};
    c.s32mat.data = &data[0];
    
    ASSERT(a->data[0] == 91)
    ASSERT(a->data[1] == 92)
    ASSERT(a->data[2] == 93);
}

void test_unioned_pointers() {
    union E e1;

    int data[] = { 41, 42, 43 };
    e1.i = data;
}

void test_copy_unioned_pointers() {
    union E e1;
    union E e2;
    
    int data[] = { 41, 42, 43 };
    e1.i = data;

    e2 = e1;
    
    ASSERT(e1.i[0] == 41);
    ASSERT(e2.i[0] == 41);
}

int get_data(struct univ_mat *mat1, int i) {
    return mat1->mat.s32mat.data[i];
}

void test_matrix() {
    struct univ_mat mat1;
    int data[] = {1, 2, 3};
    mat1.mat.s32mat.data = &data;
    
    ASSERT(get_data(&mat1, 2) == 3);
}