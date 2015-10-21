#include <math.h>
#include <float.h>

#define NA_REAL NAN
#define NULL 0

#define EUCLIDEAN   1
#define MAXIMUM     2
#define MANHATTAN   3
#define CANBERRA    4
#define BINARY      5


int R_distance(int *method, int x, int y)
{
    int dist = 0;

    switch(*method) {
    //case EUCLIDEAN:
	//dist = abs(x - y);
	//break;
    case MAXIMUM:
	dist = x - y;
	break;
    case MANHATTAN:
	dist = x*x;
	break;
    case CANBERRA:
	dist = x+y;
	break;
    case BINARY:
	dist = (x == y);
	break;
	}

	return dist;
}
