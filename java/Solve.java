/*
 * Creation : 7 août 2017
 */
package com.yibo.sudoku;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Solve {

    public static void main(String[] args) {

        try (BufferedReader br = new BufferedReader(new FileReader("puzzles.txt"))) {
            long startTime = System.nanoTime();
            String line;
            int nbSolved = 0;
            while ((line = br.readLine()) != null) {

                Puzzle puzzle = new Puzzle(line);

                if (puzzle.search()) {
                    nbSolved++;
                } else {
                    System.out.println("No valid solution");
                    puzzle.display();
                }
            }
            System.out.print(nbSolved + " puzzles solved, total time: " + (System.nanoTime() - startTime) / 1000000.0 + " ms\n");
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }

    }

}
