

// Code from read.dbc
// Static local variables which reference each other



#define MAXBITS 10

struct huffman {
    short *count;       /* number of symbols of each length */
    short *symbol;      /* canonically ordered symbols */
};


struct huffman * test_inter_reference() {

    static int virgin = 1;                              /* build tables once */
    static short litcnt[MAXBITS+1], litsym[256];        /* litcode memory */
    static short lencnt[MAXBITS+1], lensym[16];         /* lencode memory */
    static short distcnt[MAXBITS+1], distsym[64];       /* distcode memory */
    static struct huffman litcode = {litcnt, litsym};   /* length code */
    static struct huffman lencode = {lencnt, lensym};   /* length code */
    static struct huffman distcode = {distcnt, distsym};/* distance code */
        /* bit lengths of literal codes */


    litcnt[0] = 99;
    distcnt[1] = 414;
    distsym[2] = 11;

    return &distcode;
}