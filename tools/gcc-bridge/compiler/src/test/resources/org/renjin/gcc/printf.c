///////////////////////////////////////////////////////////////////////////////
// \author (c) Marco Paland (info@paland.com)
//             2017-2018, PALANDesign Hannover, Germany
//
// \license The MIT License (MIT)
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
// 
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.
//
// \brief printf unit tests
//
///////////////////////////////////////////////////////////////////////////////

#include "assert.h"

#include <string.h>
#include <stdarg.h>
#include <inttypes.h>




void test_snprintf() {
  char buffer[100];

  snprintf(buffer, 100U, "%d", -1000);
  assertStringsEqual(buffer, "-1000");

  snprintf(buffer, 3U, "%d", -1000);
  assertStringsEqual(buffer, "-1");
}


static void vsnprintf_builder_1(char* buffer, ...)
{
  va_list args;
  va_start(args, buffer);
  vsnprintf(buffer, 100U, "%d", args);
  va_end(args);
}

static void vsnprintf_builder_3(char* buffer, ...)
{
  va_list args;
  va_start(args, buffer);
  vsnprintf(buffer, 100U, "%d %d %s", args);
  va_end(args);
}

void test_vsnprintf() {
  char buffer[100];

  vsnprintf_builder_1(buffer, -1);
  assertStringsEqual(buffer, "-1");

  vsnprintf_builder_3(buffer, 3, -1000, "test");
  assertStringsEqual(buffer, "3 -1000 test");
}


void test_space_flag() {
  char buffer[100];

  sprintf(buffer, "% d", 42);
  assertStringsEqual(buffer, " 42");

  sprintf(buffer, "% d", -42);
  assertStringsEqual(buffer, "-42");

  sprintf(buffer, "% 5d", 42);
  assertStringsEqual(buffer, "   42");

  sprintf(buffer, "% 5d", -42);
  assertStringsEqual(buffer, "  -42");

  sprintf(buffer, "% 15d", 42);
  assertStringsEqual(buffer, "             42");

  sprintf(buffer, "% 15d", -42);
  assertStringsEqual(buffer, "            -42");

  sprintf(buffer, "% 15d", -42);
  assertStringsEqual(buffer, "            -42");

  sprintf(buffer, "% 15.3f", -42.987);
  assertStringsEqual(buffer, "        -42.987");

  sprintf(buffer, "% 15.3f", 42.987);
  assertStringsEqual(buffer, "         42.987");

  sprintf(buffer, "% s", "Hello testing");
  assertStringsEqual(buffer, "Hello testing");

  sprintf(buffer, "% d", 1024);
  assertStringsEqual(buffer, " 1024");

  sprintf(buffer, "% d", -1024);
  assertStringsEqual(buffer, "-1024");

  sprintf(buffer, "% i", 1024);
  assertStringsEqual(buffer, " 1024");

  sprintf(buffer, "% i", -1024);
  assertStringsEqual(buffer, "-1024");

  sprintf(buffer, "% u", 1024);
  assertStringsEqual(buffer, "1024");

  sprintf(buffer, "% u", 4294966272U);
  assertStringsEqual(buffer, "4294966272");

  sprintf(buffer, "% o", 511);
  assertStringsEqual(buffer, "777");

  sprintf(buffer, "% o", 4294966785U);
  assertStringsEqual(buffer, "37777777001");

  sprintf(buffer, "% x", 305441741);
  assertStringsEqual(buffer, "1234abcd");

  sprintf(buffer, "% x", 3989525555U);
  assertStringsEqual(buffer, "edcb5433");

  sprintf(buffer, "% X", 305441741);
  assertStringsEqual(buffer, "1234ABCD");

  sprintf(buffer, "% X", 3989525555U);
  assertStringsEqual(buffer, "EDCB5433");

  sprintf(buffer, "% c", 'x');
  assertStringsEqual(buffer, "x");
}


void test_plus_flag() {
  char buffer[100];

  sprintf(buffer, "%+d", 42);
  assertStringsEqual(buffer, "+42");

  sprintf(buffer, "%+d", -42);
  assertStringsEqual(buffer, "-42");

  sprintf(buffer, "%+5d", 42);
  assertStringsEqual(buffer, "  +42");

  sprintf(buffer, "%+5d", -42);
  assertStringsEqual(buffer, "  -42");

  sprintf(buffer, "%+15d", 42);
  assertStringsEqual(buffer, "            +42");

  sprintf(buffer, "%+15d", -42);
  assertStringsEqual(buffer, "            -42");

  sprintf(buffer, "%+s", "Hello testing");
  assertStringsEqual(buffer, "Hello testing");

  sprintf(buffer, "%+d", 1024);
  assertStringsEqual(buffer, "+1024");

  sprintf(buffer, "%+d", -1024);
  assertStringsEqual(buffer, "-1024");

  sprintf(buffer, "%+i", 1024);
  assertStringsEqual(buffer, "+1024");

  sprintf(buffer, "%+i", -1024);
  assertStringsEqual(buffer, "-1024");

  sprintf(buffer, "%+u", 1024);
  assertStringsEqual(buffer, "1024");

  sprintf(buffer, "%+u", 4294966272U);
  assertStringsEqual(buffer, "4294966272");

  sprintf(buffer, "%+o", 511);
  assertStringsEqual(buffer, "777");

  sprintf(buffer, "%+o", 4294966785U);
  assertStringsEqual(buffer, "37777777001");

  sprintf(buffer, "%+x", 305441741);
  assertStringsEqual(buffer, "1234abcd");

  sprintf(buffer, "%+x", 3989525555U);
  assertStringsEqual(buffer, "edcb5433");

  sprintf(buffer, "%+X", 305441741);
  assertStringsEqual(buffer, "1234ABCD");

  sprintf(buffer, "%+X", 3989525555U);
  assertStringsEqual(buffer, "EDCB5433");

  sprintf(buffer, "%+c", 'x');
  assertStringsEqual(buffer, "x");
}


void test_0_flag() {
  char buffer[100];

  sprintf(buffer, "%0d", 42);
  assertStringsEqual(buffer, "42");

  sprintf(buffer, "%0ld", 42L);
  assertStringsEqual(buffer, "42");

  sprintf(buffer, "%0d", -42);
  assertStringsEqual(buffer, "-42");

  sprintf(buffer, "%05d", 42);
  assertStringsEqual(buffer, "00042");

  sprintf(buffer, "%05d", -42);
  assertStringsEqual(buffer, "-0042");

  sprintf(buffer, "%015d", 42);
  assertStringsEqual(buffer, "000000000000042");

  sprintf(buffer, "%015d", -42);
  assertStringsEqual(buffer, "-00000000000042");

  sprintf(buffer, "%015.2f", 42.1234);
  assertStringsEqual(buffer, "000000000042.12");

  sprintf(buffer, "%015.3f", 42.9876);
  assertStringsEqual(buffer, "00000000042.988");

  sprintf(buffer, "%015.5f", -42.9876);
  assertStringsEqual(buffer, "-00000042.98760");
}


void test_neg_flag() {
  char buffer[100];

  sprintf(buffer, "%-d", 42);
  assertStringsEqual(buffer, "42");

  sprintf(buffer, "%-d", -42);
  assertStringsEqual(buffer, "-42");

  sprintf(buffer, "%-5d", 42);
  assertStringsEqual(buffer, "42   ");

  sprintf(buffer, "%-5d", -42);
  assertStringsEqual(buffer, "-42  ");

  sprintf(buffer, "%-15d", 42);
  assertStringsEqual(buffer, "42             ");

  sprintf(buffer, "%-15d", -42);
  assertStringsEqual(buffer, "-42            ");

  sprintf(buffer, "%-0d", 42);
  assertStringsEqual(buffer, "42");

  sprintf(buffer, "%-0d", -42);
  assertStringsEqual(buffer, "-42");

  sprintf(buffer, "%-05d", 42);
  assertStringsEqual(buffer, "42   ");

  sprintf(buffer, "%-05d", -42);
  assertStringsEqual(buffer, "-42  ");

  sprintf(buffer, "%-015d", 42);
  assertStringsEqual(buffer, "42             ");

  sprintf(buffer, "%-015d", -42);
  assertStringsEqual(buffer, "-42            ");

  sprintf(buffer, "%0-d", 42);
  assertStringsEqual(buffer, "42");

  sprintf(buffer, "%0-d", -42);
  assertStringsEqual(buffer, "-42");

  sprintf(buffer, "%0-5d", 42);
  assertStringsEqual(buffer, "42   ");

  sprintf(buffer, "%0-5d", -42);
  assertStringsEqual(buffer, "-42  ");

  sprintf(buffer, "%0-15d", 42);
  assertStringsEqual(buffer, "42             ");

  sprintf(buffer, "%0-15d", -42);
  assertStringsEqual(buffer, "-42            ");
}


void test_specifier() {
  char buffer[100];

  sprintf(buffer, "Hello testing");
  assertStringsEqual(buffer, "Hello testing");

  sprintf(buffer, "%s", "Hello testing");
  assertStringsEqual(buffer, "Hello testing");

  sprintf(buffer, "%d", 1024);
  assertStringsEqual(buffer, "1024");

  sprintf(buffer, "%d", -1024);
  assertStringsEqual(buffer, "-1024");

  sprintf(buffer, "%i", 1024);
  assertStringsEqual(buffer, "1024");

  sprintf(buffer, "%i", -1024);
  assertStringsEqual(buffer, "-1024");

  sprintf(buffer, "%u", 1024);
  assertStringsEqual(buffer, "1024");

  sprintf(buffer, "%u", 4294966272U);
  assertStringsEqual(buffer, "4294966272");

  sprintf(buffer, "%o", 511);
  assertStringsEqual(buffer, "777");

  sprintf(buffer, "%o", 4294966785U);
  assertStringsEqual(buffer, "37777777001");

  sprintf(buffer, "%x", 305441741);
  assertStringsEqual(buffer, "1234abcd");

  sprintf(buffer, "%x", 3989525555U);
  assertStringsEqual(buffer, "edcb5433");

  sprintf(buffer, "%X", 305441741);
  assertStringsEqual(buffer, "1234ABCD");

  sprintf(buffer, "%X", 3989525555U);
  assertStringsEqual(buffer, "EDCB5433");

  sprintf(buffer, "%%");
  assertStringsEqual(buffer, "%");
}


void test_width() {
  char buffer[100];

  sprintf(buffer, "%1s", "Hello testing");
  assertStringsEqual(buffer, "Hello testing");

  sprintf(buffer, "%1d", 1024);
  assertStringsEqual(buffer, "1024");

  sprintf(buffer, "%1d", -1024);
  assertStringsEqual(buffer, "-1024");

  sprintf(buffer, "%1i", 1024);
  assertStringsEqual(buffer, "1024");

  sprintf(buffer, "%1i", -1024);
  assertStringsEqual(buffer, "-1024");

  sprintf(buffer, "%1u", 1024);
  assertStringsEqual(buffer, "1024");

  sprintf(buffer, "%1u", 4294966272U);
  assertStringsEqual(buffer, "4294966272");

  sprintf(buffer, "%1o", 511);
  assertStringsEqual(buffer, "777");

  sprintf(buffer, "%1o", 4294966785U);
  assertStringsEqual(buffer, "37777777001");

  sprintf(buffer, "%1x", 305441741);
  assertStringsEqual(buffer, "1234abcd");

  sprintf(buffer, "%1x", 3989525555U);
  assertStringsEqual(buffer, "edcb5433");

  sprintf(buffer, "%1X", 305441741);
  assertStringsEqual(buffer, "1234ABCD");

  sprintf(buffer, "%1X", 3989525555U);
  assertStringsEqual(buffer, "EDCB5433");

  sprintf(buffer, "%1c", 'x');
  assertStringsEqual(buffer, "x");
}


void test_width_20() {
  char buffer[100];

  sprintf(buffer, "%20s", "Hello");
  assertStringsEqual(buffer, "               Hello");

  sprintf(buffer, "%20d", 1024);
  assertStringsEqual(buffer, "                1024");

  sprintf(buffer, "%20d", -1024);
  assertStringsEqual(buffer, "               -1024");

  sprintf(buffer, "%20i", 1024);
  assertStringsEqual(buffer, "                1024");

  sprintf(buffer, "%20i", -1024);
  assertStringsEqual(buffer, "               -1024");

  sprintf(buffer, "%20u", 1024);
  assertStringsEqual(buffer, "                1024");

  sprintf(buffer, "%20u", 4294966272U);
  assertStringsEqual(buffer, "          4294966272");

  sprintf(buffer, "%20o", 511);
  assertStringsEqual(buffer, "                 777");

  sprintf(buffer, "%20o", 4294966785U);
  assertStringsEqual(buffer, "         37777777001");

  sprintf(buffer, "%20x", 305441741);
  assertStringsEqual(buffer, "            1234abcd");

  sprintf(buffer, "%20x", 3989525555U);
  assertStringsEqual(buffer, "            edcb5433");

  sprintf(buffer, "%20X", 305441741);
  assertStringsEqual(buffer, "            1234ABCD");

  sprintf(buffer, "%20X", 3989525555U);
  assertStringsEqual(buffer, "            EDCB5433");

  sprintf(buffer, "%20c", 'x');
  assertStringsEqual(buffer, "                   x");
}


void test_width_star_20() {
  char buffer[100];

  sprintf(buffer, "%*s", 20, "Hello");
  assertStringsEqual(buffer, "               Hello");

  sprintf(buffer, "%*d", 20, 1024);
  assertStringsEqual(buffer, "                1024");

  sprintf(buffer, "%*d", 20, -1024);
  assertStringsEqual(buffer, "               -1024");

  sprintf(buffer, "%*i", 20, 1024);
  assertStringsEqual(buffer, "                1024");

  sprintf(buffer, "%*i", 20, -1024);
  assertStringsEqual(buffer, "               -1024");

  sprintf(buffer, "%*u", 20, 1024);
  assertStringsEqual(buffer, "                1024");

  sprintf(buffer, "%*u", 20, 4294966272U);
  assertStringsEqual(buffer, "          4294966272");

  sprintf(buffer, "%*o", 20, 511);
  assertStringsEqual(buffer, "                 777");

  sprintf(buffer, "%*o", 20, 4294966785U);
  assertStringsEqual(buffer, "         37777777001");

  sprintf(buffer, "%*x", 20, 305441741);
  assertStringsEqual(buffer, "            1234abcd");

  sprintf(buffer, "%*x", 20, 3989525555U);
  assertStringsEqual(buffer, "            edcb5433");

  sprintf(buffer, "%*X", 20, 305441741);
  assertStringsEqual(buffer, "            1234ABCD");

  sprintf(buffer, "%*X", 20, 3989525555U);
  assertStringsEqual(buffer, "            EDCB5433");

  sprintf(buffer, "%*c", 20,'x');
  assertStringsEqual(buffer, "                   x");
}


void test_width_minus_20() {
  char buffer[100];

  sprintf(buffer, "%-20s", "Hello");
  assertStringsEqual(buffer, "Hello               ");

  sprintf(buffer, "%-20d", 1024);
  assertStringsEqual(buffer, "1024                ");

  sprintf(buffer, "%-20d", -1024);
  assertStringsEqual(buffer, "-1024               ");

  sprintf(buffer, "%-20i", 1024);
  assertStringsEqual(buffer, "1024                ");

  sprintf(buffer, "%-20i", -1024);
  assertStringsEqual(buffer, "-1024               ");

  sprintf(buffer, "%-20u", 1024);
  assertStringsEqual(buffer, "1024                ");

  sprintf(buffer, "%-20.4f", 1024.1234);
  assertStringsEqual(buffer, "1024.1234           ");

  sprintf(buffer, "%-20u", 4294966272U);
  assertStringsEqual(buffer, "4294966272          ");

  sprintf(buffer, "%-20o", 511);
  assertStringsEqual(buffer, "777                 ");

  sprintf(buffer, "%-20o", 4294966785U);
  assertStringsEqual(buffer, "37777777001         ");

  sprintf(buffer, "%-20x", 305441741);
  assertStringsEqual(buffer, "1234abcd            ");

  sprintf(buffer, "%-20x", 3989525555U);
  assertStringsEqual(buffer, "edcb5433            ");

  sprintf(buffer, "%-20X", 305441741);
  assertStringsEqual(buffer, "1234ABCD            ");

  sprintf(buffer, "%-20X", 3989525555U);
  assertStringsEqual(buffer, "EDCB5433            ");

  sprintf(buffer, "%-20c", 'x');
  assertStringsEqual(buffer, "x                   ");
}


void test_width_0_minus_20() {
  char buffer[100];

  sprintf(buffer, "%0-20s", "Hello");
  assertStringsEqual(buffer, "Hello               ");

  sprintf(buffer, "%0-20d", 1024);
  assertStringsEqual(buffer, "1024                ");

  sprintf(buffer, "%0-20d", -1024);
  assertStringsEqual(buffer, "-1024               ");

  sprintf(buffer, "%0-20i", 1024);
  assertStringsEqual(buffer, "1024                ");

  sprintf(buffer, "%0-20i", -1024);
  assertStringsEqual(buffer, "-1024               ");

  sprintf(buffer, "%0-20u", 1024);
  assertStringsEqual(buffer, "1024                ");

  sprintf(buffer, "%0-20u", 4294966272U);
  assertStringsEqual(buffer, "4294966272          ");

  sprintf(buffer, "%0-20o", 511);
  assertStringsEqual(buffer, "777                 ");

  sprintf(buffer, "%0-20o", 4294966785U);
  assertStringsEqual(buffer, "37777777001         ");

  sprintf(buffer, "%0-20x", 305441741);
  assertStringsEqual(buffer, "1234abcd            ");

  sprintf(buffer, "%0-20x", 3989525555U);
  assertStringsEqual(buffer, "edcb5433            ");

  sprintf(buffer, "%0-20X", 305441741);
  assertStringsEqual(buffer, "1234ABCD            ");

  sprintf(buffer, "%0-20X", 3989525555U);
  assertStringsEqual(buffer, "EDCB5433            ");

  sprintf(buffer, "%0-20c", 'x');
  assertStringsEqual(buffer, "x                   ");
}


void test_padding_20() {
  char buffer[100];

  sprintf(buffer, "%020d", 1024);
  assertStringsEqual(buffer, "00000000000000001024");

  sprintf(buffer, "%020d", -1024);
  assertStringsEqual(buffer, "-0000000000000001024");

  sprintf(buffer, "%020i", 1024);
  assertStringsEqual(buffer, "00000000000000001024");

  sprintf(buffer, "%020i", -1024);
  assertStringsEqual(buffer, "-0000000000000001024");

  sprintf(buffer, "%020u", 1024);
  assertStringsEqual(buffer, "00000000000000001024");

  sprintf(buffer, "%020u", 4294966272U);
  assertStringsEqual(buffer, "00000000004294966272");

  sprintf(buffer, "%020o", 511);
  assertStringsEqual(buffer, "00000000000000000777");

  sprintf(buffer, "%020o", 4294966785U);
  assertStringsEqual(buffer, "00000000037777777001");

  sprintf(buffer, "%020x", 305441741);
  assertStringsEqual(buffer, "0000000000001234abcd");

  sprintf(buffer, "%020x", 3989525555U);
  assertStringsEqual(buffer, "000000000000edcb5433");

  sprintf(buffer, "%020X", 305441741);
  assertStringsEqual(buffer, "0000000000001234ABCD");

  sprintf(buffer, "%020X", 3989525555U);
  assertStringsEqual(buffer, "000000000000EDCB5433");
}


void test_padding_point_20() {
  char buffer[100];

  sprintf(buffer, "%.20d", 1024);
  assertStringsEqual(buffer, "00000000000000001024");

  sprintf(buffer, "%.20d", -1024);
  assertStringsEqual(buffer, "-00000000000000001024");

  sprintf(buffer, "%.20i", 1024);
  assertStringsEqual(buffer, "00000000000000001024");

  sprintf(buffer, "%.20i", -1024);
  assertStringsEqual(buffer, "-00000000000000001024");

  sprintf(buffer, "%.20u", 1024);
  assertStringsEqual(buffer, "00000000000000001024");

  sprintf(buffer, "%.20u", 4294966272U);
  assertStringsEqual(buffer, "00000000004294966272");

  sprintf(buffer, "%.20o", 511);
  assertStringsEqual(buffer, "00000000000000000777");

  sprintf(buffer, "%.20o", 4294966785U);
  assertStringsEqual(buffer, "00000000037777777001");

  sprintf(buffer, "%.20x", 305441741);
  assertStringsEqual(buffer, "0000000000001234abcd");

  sprintf(buffer, "%.20x", 3989525555U);
  assertStringsEqual(buffer, "000000000000edcb5433");

  sprintf(buffer, "%.20X", 305441741);
  assertStringsEqual(buffer, "0000000000001234ABCD");

  sprintf(buffer, "%.20X", 3989525555U);
  assertStringsEqual(buffer, "000000000000EDCB5433");
}


void test_padding_alt_020() {
  char buffer[100];

  sprintf(buffer, "%#020d", 1024);
  assertStringsEqual(buffer, "00000000000000001024");

  sprintf(buffer, "%#020d", -1024);
  assertStringsEqual(buffer, "-0000000000000001024");

  sprintf(buffer, "%#020i", 1024);
  assertStringsEqual(buffer, "00000000000000001024");

  sprintf(buffer, "%#020i", -1024);
  assertStringsEqual(buffer, "-0000000000000001024");

  sprintf(buffer, "%#020u", 1024);
  assertStringsEqual(buffer, "00000000000000001024");

  sprintf(buffer, "%#020u", 4294966272U);
  assertStringsEqual(buffer, "00000000004294966272");

  sprintf(buffer, "%#020o", 511);
  assertStringsEqual(buffer, "00000000000000000777");

  sprintf(buffer, "%#020o", 4294966785U);
  assertStringsEqual(buffer, "00000000037777777001");

  sprintf(buffer, "%#020x", 305441741);
  assertStringsEqual(buffer, "0x00000000001234abcd");

  sprintf(buffer, "%#020x", 3989525555U);
  assertStringsEqual(buffer, "0x0000000000edcb5433");

  sprintf(buffer, "%#020X", 305441741);
  assertStringsEqual(buffer, "0X00000000001234ABCD");

  sprintf(buffer, "%#020X", 3989525555U);
  assertStringsEqual(buffer, "0X0000000000EDCB5433");
}


void test_padding_alt_20() {
  char buffer[100];

  sprintf(buffer, "%#20d", 1024);
  assertStringsEqual(buffer, "                1024");

  sprintf(buffer, "%#20d", -1024);
  assertStringsEqual(buffer, "               -1024");

  sprintf(buffer, "%#20i", 1024);
  assertStringsEqual(buffer, "                1024");

  sprintf(buffer, "%#20i", -1024);
  assertStringsEqual(buffer, "               -1024");

  sprintf(buffer, "%#20u", 1024);
  assertStringsEqual(buffer, "                1024");

  sprintf(buffer, "%#20u", 4294966272U);
  assertStringsEqual(buffer, "          4294966272");

  sprintf(buffer, "%#20o", 511);
  assertStringsEqual(buffer, "                0777");

  sprintf(buffer, "%#20o", 4294966785U);
  assertStringsEqual(buffer, "        037777777001");

  sprintf(buffer, "%#20x", 305441741);
  assertStringsEqual(buffer, "          0x1234abcd");

  sprintf(buffer, "%#20x", 3989525555U);
  assertStringsEqual(buffer, "          0xedcb5433");

  sprintf(buffer, "%#20X", 305441741);
  assertStringsEqual(buffer, "          0X1234ABCD");

  sprintf(buffer, "%#20X", 3989525555U);
  assertStringsEqual(buffer, "          0XEDCB5433");
}


void test_padding_20_point_5() {
  char buffer[100];

  sprintf(buffer, "%20.5d", 1024);
  assertStringsEqual(buffer, "               01024");

  sprintf(buffer, "%20.5d", -1024);
  assertStringsEqual(buffer, "              -01024");

  sprintf(buffer, "%20.5i", 1024);
  assertStringsEqual(buffer, "               01024");

  sprintf(buffer, "%20.5i", -1024);
  assertStringsEqual(buffer, "              -01024");

  sprintf(buffer, "%20.5u", 1024);
  assertStringsEqual(buffer, "               01024");

  sprintf(buffer, "%20.5u", 4294966272U);
  assertStringsEqual(buffer, "          4294966272");

  sprintf(buffer, "%20.5o", 511);
  assertStringsEqual(buffer, "               00777");

  sprintf(buffer, "%20.5o", 4294966785U);
  assertStringsEqual(buffer, "         37777777001");

  sprintf(buffer, "%20.5x", 305441741);
  assertStringsEqual(buffer, "            1234abcd");

  sprintf(buffer, "%20.10x", 3989525555U);
  assertStringsEqual(buffer, "          00edcb5433");

  sprintf(buffer, "%20.5X", 305441741);
  assertStringsEqual(buffer, "            1234ABCD");

  sprintf(buffer, "%20.10X", 3989525555U);
  assertStringsEqual(buffer, "          00EDCB5433");
}


void test_length() {
  char buffer[100];

  sprintf(buffer, "%.0s", "Hello testing");
  assertStringsEqual(buffer, "");

  sprintf(buffer, "%20.0s", "Hello testing");
  assertStringsEqual(buffer, "                    ");

  sprintf(buffer, "%.s", "Hello testing");
  assertStringsEqual(buffer, "");

  sprintf(buffer, "%20.s", "Hello testing");
  assertStringsEqual(buffer, "                    ");

  sprintf(buffer, "%20.0d", 1024);
  assertStringsEqual(buffer, "                1024");

  sprintf(buffer, "%20.0d", -1024);
  assertStringsEqual(buffer, "               -1024");

  sprintf(buffer, "%20.d", 0);
  assertStringsEqual(buffer, "                    ");

  sprintf(buffer, "%20.0i", 1024);
  assertStringsEqual(buffer, "                1024");

  sprintf(buffer, "%20.i", -1024);
  assertStringsEqual(buffer, "               -1024");

  sprintf(buffer, "%20.i", 0);
  assertStringsEqual(buffer, "                    ");

  sprintf(buffer, "%20.u", 1024);
  assertStringsEqual(buffer, "                1024");

  sprintf(buffer, "%20.0u", 4294966272U);
  assertStringsEqual(buffer, "          4294966272");

  sprintf(buffer, "%20.u", 0U);
  assertStringsEqual(buffer, "                    ");

  sprintf(buffer, "%20.o", 511);
  assertStringsEqual(buffer, "                 777");

  sprintf(buffer, "%20.0o", 4294966785U);
  assertStringsEqual(buffer, "         37777777001");

  sprintf(buffer, "%20.o", 0U);
  assertStringsEqual(buffer, "                    ");

  sprintf(buffer, "%20.x", 305441741);
  assertStringsEqual(buffer, "            1234abcd");

  sprintf(buffer, "%50.x", 305441741);
  assertStringsEqual(buffer, "                                          1234abcd");

  sprintf(buffer, "%50.x%10.u", 305441741, 12345);
  assertStringsEqual(buffer, "                                          1234abcd     12345");

  sprintf(buffer, "%20.0x", 3989525555U);
  assertStringsEqual(buffer, "            edcb5433");

  sprintf(buffer, "%20.x", 0U);
  assertStringsEqual(buffer, "                    ");

  sprintf(buffer, "%20.X", 305441741);
  assertStringsEqual(buffer, "            1234ABCD");

  sprintf(buffer, "%20.0X", 3989525555U);
  assertStringsEqual(buffer, "            EDCB5433");

  sprintf(buffer, "%20.X", 0U);
  assertStringsEqual(buffer, "                    ");
}


void test_float() {
  char buffer[100];

  sprintf(buffer, "%.4f", 3.1415354);
  assertStringsEqual(buffer, "3.1415");

  sprintf(buffer, "%.3f", 30343.1415354);
  assertStringsEqual(buffer, "30343.142");

  sprintf(buffer, "%.0f", 34.1415354);
  assertStringsEqual(buffer, "34");

  sprintf(buffer, "%.2f", 42.8952);
  assertStringsEqual(buffer, "42.90");

  sprintf(buffer, "%.9f", 42.8952);
  assertStringsEqual(buffer, "42.895200000");

  sprintf(buffer, "%.10f", 42.895223);
  assertStringsEqual(buffer, "42.8952230000");

  sprintf(buffer, "%6.2f", 42.8952);
  assertStringsEqual(buffer, " 42.90");

  sprintf(buffer, "%+6.2f", 42.8952);
  assertStringsEqual(buffer, "+42.90");

  sprintf(buffer, "%+5.1f", 42.9252);
  assertStringsEqual(buffer, "+42.9");

  sprintf(buffer, "%f", 42.5);
  assertStringsEqual(buffer, "42.500000");

  sprintf(buffer, "%.1f", 42.5);
  assertStringsEqual(buffer, "42.5");

  sprintf(buffer, "%f", 42167.0);
  assertStringsEqual(buffer, "42167.000000");

  sprintf(buffer, "%.9f", -12345.987654321);
  assertStringsEqual(buffer, "-12345.987654321");

  sprintf(buffer, "%.1f", 3.999);
  assertStringsEqual(buffer, "4.0");

  sprintf(buffer, "%.0f", 3.5);
  assertStringsEqual(buffer, "4");

  sprintf(buffer, "%.0f", 3.49);
  assertStringsEqual(buffer, "3");

  sprintf(buffer, "%.1f", 3.49);
  assertStringsEqual(buffer, "3.5");

  sprintf(buffer, "%.1f", 1E20);
  assertStringsEqual(buffer, "100000000000000000000.0");
}


void test_types() {
  char buffer[100];

  sprintf(buffer, "%i", 0);
  assertStringsEqual(buffer, "0");

  sprintf(buffer, "%i", 1234);
  assertStringsEqual(buffer, "1234");

  sprintf(buffer, "%i", 32767);
  assertStringsEqual(buffer, "32767");

  sprintf(buffer, "%i", -32767);
  assertStringsEqual(buffer, "-32767");

  sprintf(buffer, "%li", 30L);
  assertStringsEqual(buffer, "30");

  sprintf(buffer, "%li", -2147483647L);
  assertStringsEqual(buffer, "-2147483647");

  sprintf(buffer, "%li", 2147483647L);
  assertStringsEqual(buffer, "2147483647");

  sprintf(buffer, "%lli", 30LL);
  assertStringsEqual(buffer, "30");

  sprintf(buffer, "%lli", -9223372036854775807LL);
  assertStringsEqual(buffer, "-9223372036854775807");

  sprintf(buffer, "%lli", 9223372036854775807LL);
  assertStringsEqual(buffer, "9223372036854775807");

  sprintf(buffer, "%lu", 100000L);
  assertStringsEqual(buffer, "100000");

  sprintf(buffer, "%lu", 0xFFFFFFFFL);
  assertStringsEqual(buffer, "4294967295");

  sprintf(buffer, "%llu", 281474976710656LLU);
  assertStringsEqual(buffer, "281474976710656");

  sprintf(buffer, "%llu", 18446744073709551615LLU);
  assertStringsEqual(buffer, "18446744073709551615");

  sprintf(buffer, "%zu", 2147483647UL);
  assertStringsEqual(buffer, "2147483647");

  sprintf(buffer, "%zd", 2147483647UL);
  assertStringsEqual(buffer, "2147483647");

  if (sizeof(size_t) == sizeof(long)) {
    sprintf(buffer, "%zi", -2147483647L);
    assertStringsEqual(buffer, "-2147483647");
  }
  else {
    sprintf(buffer, "%zi", -2147483647LL);
    assertStringsEqual(buffer, "-2147483647");
  }

  sprintf(buffer, "%b", 60000);
  assertStringsEqual(buffer, "1110101001100000");

  sprintf(buffer, "%lb", 12345678L);
  assertStringsEqual(buffer, "101111000110000101001110");

  sprintf(buffer, "%o", 60000);
  assertStringsEqual(buffer, "165140");

  sprintf(buffer, "%lo", 12345678L);
  assertStringsEqual(buffer, "57060516");

  sprintf(buffer, "%lx", 0x12345678L);
  assertStringsEqual(buffer, "12345678");

  sprintf(buffer, "%llx", 0x1234567891234567LLU);
  assertStringsEqual(buffer, "1234567891234567");

  sprintf(buffer, "%lx", 0xabcdefabL);
  assertStringsEqual(buffer, "abcdefab");

  sprintf(buffer, "%lX", 0xabcdefabL);
  assertStringsEqual(buffer, "ABCDEFAB");

  sprintf(buffer, "%c", 'v');
  assertStringsEqual(buffer, "v");

  sprintf(buffer, "%cv", 'w');
  assertStringsEqual(buffer, "wv");

  sprintf(buffer, "%s", "A Test");
  assertStringsEqual(buffer, "A Test");
// TODO:
//
//  sprintf(buffer, "%hhu", 0xFFFFUL);
//  assertStringsEqual(buffer, "255");
//
//  sprintf(buffer, "%hu", 0x123456UL);
//  assertStringsEqual(buffer, "13398");
//
//  sprintf(buffer, "%s%hhi %hu", "Test", 10000, 0xFFFFFFFF);
//  assertStringsEqual(buffer, "Test16 65535");
//
//  sprintf(buffer, "%tx", &buffer[10] - &buffer[0]);
//  assertStringsEqual(buffer, "a");
//
//// TBD
//  if (sizeof(intmax_t) == sizeof(long)) {
//    sprintf(buffer, "%ji", -2147483647L);
//    assertStringsEqual(buffer, "-2147483647");
//  }
//  else {
//    sprintf(buffer, "%ji", -2147483647LL);
//    assertStringsEqual(buffer, "-2147483647");
//  }

}


void test_pointer() {
  char buffer[100];

  sprintf(buffer, "%p", (void*)0x1234U);
  if (sizeof(void*) == 4U) {
    assertStringsEqual(buffer, "00001234");
  }
  else {
    assertStringsEqual(buffer, "0000000000001234");
  }

  sprintf(buffer, "%p", (void*)0x12345678U);
  if (sizeof(void*) == 4U) {
    assertStringsEqual(buffer, "12345678");
  }
  else {
    assertStringsEqual(buffer, "0000000012345678");
  }

  sprintf(buffer, "%p-%p", (void*)0x12345678U, (void*)0x7EDCBA98U);
  if (sizeof(void*) == 4U) {
    assertStringsEqual(buffer, "12345678-7EDCBA98");
  }
  else {
    assertStringsEqual(buffer, "0000000012345678-000000007EDCBA98");
  }

  if (sizeof(uintptr_t) == sizeof(uint64_t)) {
    sprintf(buffer, "%p", (void*)(uintptr_t)0xFFFFFFFFU);
    assertStringsEqual(buffer, "00000000FFFFFFFF");
  }
  else {
    sprintf(buffer, "%p", (void*)(uintptr_t)0xFFFFFFFFU);
    assertStringsEqual(buffer, "FFFFFFFF");
  }
}

//
//void test_unknown_flag() {
//  char buffer[100];
//
//  sprintf(buffer, "%kmarco", 42, 37);
//  assertStringsEqual(buffer, "%kmarco");
//}


void test_buffer_length() {
  char buffer[100];
  int ret;

  ret = snprintf(buffer, 10, "%s", "Test");
  ASSERT(ret == 4);
  ret = snprintf(buffer, 0, "%s", "Test");
  ASSERT(ret == 4);

  buffer[0] = (char)0xA5;
  ret = snprintf(buffer, 0, "%s", "Test");
  ASSERT(buffer[0] == (char)0xA5);
  ASSERT(ret == 4);

  buffer[0] = (char)0xCC;
  snprintf(buffer, 1, "%s", "Test");
  ASSERT(buffer[0] == '\0');

  snprintf(buffer, 2, "%s", "Hello");
  assertStringsEqual(buffer, "H");
}


void test_ret_value() {
  char buffer[100] ;
  int ret;

  ret = snprintf(buffer, 6, "0%s", "1234");
  assertStringsEqual(buffer, "01234");
  ASSERT(ret == 5);

  ret = snprintf(buffer, 6, "0%s", "12345");
  assertStringsEqual(buffer, "01234");
  ASSERT(ret == 6);  // '5' is truncated

  ret = snprintf(buffer, 6, "0%s", "1234567");
  assertStringsEqual(buffer, "01234");
  ASSERT(ret == 8);  // '567' are truncated

  ret = snprintf(buffer, 10, "hello, world");
  ASSERT(ret == 12);

  ret = snprintf(buffer, 3, "%d", 10000);
  ASSERT(ret == 5);
  ASSERT(strlen(buffer) == 2U);
  ASSERT(buffer[0] == '1');
  ASSERT(buffer[1] == '0');
  ASSERT(buffer[2] == '\0');
}


void test_misc() {
  char buffer[100];

  sprintf(buffer, "%u%u%ctest%d %s", 5, 3000, 'a', -20, "bit");
  assertStringsEqual(buffer, "53000atest-20 bit");

  sprintf(buffer, "%.*f", 2, 0.33333333);
  assertStringsEqual(buffer, "0.33");

  sprintf(buffer, "%.3s", "foobar");
  assertStringsEqual(buffer, "foo");

  sprintf(buffer, "%10.5d", 4);
  assertStringsEqual(buffer, "     00004");

  sprintf(buffer, "%*sx", -3, "hi");
  assertStringsEqual(buffer, "hi x");
}