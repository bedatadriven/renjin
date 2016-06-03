
#include <inttypes.h>

uint8_t * do_cast() {
    uint32_t *p = malloc(sizeof(uint32_t)*8);
    return (uint8_t*)p;
}