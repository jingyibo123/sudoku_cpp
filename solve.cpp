
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
		if(!puzzle.solved){
			cout << "puzzle unsolvable";
			// puzzle.disp();
		} else {
			for(int i : puzzle.getResolvedDigits()){
				if (i == 0){
					cout << "not completely solved" << "\n";
					break;
				}
			}
			i++;
		}
	}
	cout << to_string(i) << " puzzles solved \n";

    return 0;
}
