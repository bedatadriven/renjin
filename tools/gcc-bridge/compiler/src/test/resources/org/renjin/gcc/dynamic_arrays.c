
#include "assert.h"
#include <stdio.h>

int do_dynamic_array(int size) {

    char * a[size];
    int i;
    for(i=0;i<size;++i) {
        a[i] = "Hello world";
    }
    
    ASSERT(strcmp(a[size-1], "Hello world") == 0);
}

void test_dynamic_array() {
    do_dynamic_array(10);
    do_dynamic_array(10000);;       
}

void test_large_static_array() {

    char * a[5000];
    
    int i;
    for(i=0;i<5000;++i) {
        if(i % 2 == 0) {
            a[i] = "Hello";
        } else {
            a[i] = "Goodbye";
        }
    }
    ASSERT(strcmp(a[0], "Hello") == 0);
    ASSERT(strcmp(a[33], "Goodbye") == 0);
}