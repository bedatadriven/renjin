/****************************************************************************
 * Safe signed integer arithmetic                                           *
 * ------------------------------                                           *
 * TODO: Extend to support safe double arithmetic when the need arises.     *
 ****************************************************************************/
#include "S4Vectors.h"
#include <limits.h> /* for INT_MAX and INT_MIN */


static int ovflow_flag;

void _reset_ovflow_flag()
{
	ovflow_flag = 0;
	return;
}

int _get_ovflow_flag()
{
	return ovflow_flag;
}

/* Reference:
 *   The CERT C Secure Coding Standard
 *     Rule INT32-C. Ensure that operations on signed integers do not result
 *     in overflow
 */

int _safe_int_add(int x, int y)
{
	if (x == NA_INTEGER || y == NA_INTEGER)
		return NA_INTEGER;
	if (((y > 0) && (x > (INT_MAX - y)))
	 || ((y < 0) && (x < (INT_MIN - y)))) {
		ovflow_flag = 1;
		return NA_INTEGER;
	}
	return x + y;
}

int _safe_int_mult(int x, int y)
{
	if (x == NA_INTEGER || y == NA_INTEGER)
		return NA_INTEGER;
	if (x > 0) { /* x is positive */
		if (y > 0) { /* x and y are positive */
			if (x > (INT_MAX / y)) {
				ovflow_flag = 1;
				return NA_INTEGER;
			}
		} else { /* x is positive, y is non-positive */
			if (y < (INT_MIN / x)) {
				ovflow_flag = 1;
				return NA_INTEGER;
			}
		}
	} else { /* x is non-positive */
		if (y > 0) { /* x is non-positive, y is positive */
			if (x < (INT_MIN / y)) {
				ovflow_flag = 1;
				return NA_INTEGER;
			}
	  	} else { /* x and y are non-positive */
			if ((x != 0) && (y < (INT_MAX / x))) {
				ovflow_flag = 1;
				return NA_INTEGER;
			}
		}
	}
	return x * y;
}

