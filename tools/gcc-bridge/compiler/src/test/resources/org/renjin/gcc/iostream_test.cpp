
#include <string>       // std::string
#include <iostream>     // std::cout
#include <sstream>      // std::stringstream

extern "C" void test_main() {

  std::stringstream ss;

  ss << 100 << ' ' << 200;

  int foo,bar;
  ss >> foo >> bar;

  std::cout << "foo: " << foo << '\n';
  std::cout << "bar: " << bar << '\n';

}