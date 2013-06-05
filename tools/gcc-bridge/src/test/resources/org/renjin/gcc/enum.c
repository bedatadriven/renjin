

typedef enum { FALSE = 0, TRUE } Rboolean;


Rboolean test(int x) {
    Rboolean b;
    if(x > 0) {
        b = TRUE;
    } else {
        b = FALSE;
    }

    if(b) {
        return x*45 > 0;
    } else {
        return FALSE;
    }
}
