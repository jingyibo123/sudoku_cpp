
#include <iostream>
#include <fstream>
#include <stdio.h>
#include "Puzzle.h"

using namespace std;

int main(int, char**)
{

	ifstream file("puzzles.txt");
	string line;
	int i = 0;
	while (getline(file, line)) {
		if(line.size() < 10)
			break;
		Puzzle puzzle(line.data());
		int re = puzzle.solve();
		if(re == PUZZLE_SOLVED){
			i++;
			// puzzle.disp();
		} else if (re == PUZZLE_NOT_ENOUGH_DIGITS){
			cout << "not enough digits" << "\n";
		} else if (re == PUZZLE_NO_SOLUTION){
			cout << "puzzle not solveable" << "\n";
		}
	}
	cout << to_string(i) << " puzzles solved \n";
    return 0;
}
