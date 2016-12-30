
#include "assert.h"

class Solver {
public:
	Solver() {};
	virtual ~Solver() {};

    void Solve(int l, int shrinking);

    int solution;
};

void Solver::Solve(int l, int shrinking) {
    if(shrinking) {
        this->solution = 42;
    } else {
        this->solution = 43;
    }
}

extern "C" void test_empty_dtor() {
    Solver solver;
    solver.Solve(3, 0);

    ASSERT(solver.solution == 43);
}

//extern "C" void test_empty_ctor() {
//
//    Solver *solver = new SpecialSolver();
//
//    ASSERT(solver->solution == -1);
//
//    solver->Solve(3, 1);
//
//    ASSERT(solver->solution == 42);
//}
