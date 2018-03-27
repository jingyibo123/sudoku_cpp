/*
 * Created by Yibo on 08/09/2017
 */
package com.yibo.sudoku;

public class Puzzle {

    /* ----- static fields----- */

    private static final int SIZE = 9;

    // A cell always has 20 peers
    private static final int NB_PEERS = 3 * (SIZE - 1) - 4;

    // Array of the position in 0-80 form of all peers of every cell
    // For example, peers of last cell will be peers[8][8] = {8, 17... 72, 73, ...60, 61, 69, 70}
    private static final int[][][] peers = new int[SIZE][SIZE][NB_PEERS];

    // Initialize peers
    static {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                int k = 0;
                // column
                for (int r = 0; r < SIZE; r++) {
                    if (r == i)
                        continue;
                    peers[i][j][k++] = r * SIZE + j;
                }
                // row
                for (int c = 0; c < SIZE; c++) {
                    if (c == j)
                        continue;
                    peers[i][j][k++] = i * SIZE + c;
                }
                // block
                for (int n = 0; n < SIZE * SIZE; n++) {
                    if (n / SIZE == i || n % SIZE == j || n / SIZE == i && n % SIZE == j)
                        continue;
                    if (i / 3 == n / 27 && j / 3 == n % 9 / 3)
                        peers[i][j][k++] = n;
                }
            }
        }
    }

    // Array of the position in 0-80 form of all row peers of every cell
    // For example, row peers of last cell will be peers[8][8] = {72, 73, 74, 75, 76, 77, 78, 79}
    private static final int[][][] rowPeers = new int[SIZE][SIZE][SIZE - 1];

    // Initialize rowPeers
    static {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                int k = 0;
                // row
                for (int c = 0; c < SIZE; c++) {
                    if (c == j)
                        continue;
                    rowPeers[i][j][k++] = i * SIZE + c;
                }
            }
        }
    }

    // Array of the position in 0-80 form of all column peers of every cell
    // For example, column peers of last cell will be peers[8][8] = {8, 17, 26, 35, 44, 53, 62, 71}
    private static final int[][][] colPeers = new int[SIZE][SIZE][SIZE - 1];

    // Initialize colPeers
    static {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                int k = 0;
                // column
                for (int r = 0; r < SIZE; r++) {
                    if (r == i)
                        continue;
                    colPeers[i][j][k++] = r * SIZE + j;
                }
            }
        }
    }

    // Array of the position in 0-80 form of all block peers of every cell
    // For example, block peers of last cell will be peers[8][8] = {60, 61, 62, 69, 70, 71, 78, 79}
    private static final int[][][] blockPeers = new int[SIZE][SIZE][SIZE - 1];

    // Initialize blockPeers
    static {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                int k = 0;
                // block
                for (int n = 0; n < SIZE * SIZE; n++) {
                    if (i / 3 == n / 27 && j / 3 == n % 9 / 3 && n != i * 9 + j)
                        blockPeers[i][j][k++] = n;
                }
            }
        }
    }

    // The array of all three types of peer group for iteration
    private static final int[][][][] allPeersGroup = new int[][][][] { rowPeers, colPeers, blockPeers };

    /* ----- class fields ----- */

    // The matrix storing: all known values from the input string in case of the root branch, or the asserted value in case of a search branch
    private final int[] initValues = new int[SIZE * SIZE];

    // The white board storing all values resolved
    private final int[][] values = new int[SIZE][SIZE];

    // Count of values already assigned into matrix values
    private int nbAssignedValues;

    // Matrix of all possible options for each cell, options[row][col][val-1]
    // For example, options[2][4][6] == false --> 7 is eliminated for cell C5
    private final boolean[][][] options = new boolean[SIZE][SIZE][SIZE];

    /**
     * The common constructor to parse a string.
     * 
     * @param the input string
     * @throws IllegamArgumentException if the string is not of length 81
     */
    public Puzzle(String s) {
        if (s.length() != SIZE * SIZE)
            throw new IllegalArgumentException("The string as input must be of length 81");

        // Initialize options
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                for (int k = 0; k < SIZE; k++) {
                    options[i][j][k] = true;
                }
            }
        }

        int i = 0;
        for (char c : s.toCharArray()) {
            initValues[i++] = Character.getNumericValue(c);
        }
    }

    /**
     * Use this copy constructor to copy values and options to create search branches
     */
    private Puzzle(Puzzle source) {
        this.nbAssignedValues = source.nbAssignedValues;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                for (int k = 0; k < SIZE; k++) {
                    if (source.options[i][j][k])
                        this.options[i][j][k] = true;
                }
                if (source.values[i][j] != 0)
                    this.values[i][j] = source.values[i][j];
            }
        }

    }

    /**
     * Assign all values in initValues which is: values in initial input for the root branch, or the asserted value for a search branch.
     * 
     * @return false when contradiction occurred
     */
    private boolean assignAll() {
        for (int i = 0; i < SIZE * SIZE; i++) {
            if (initValues[i] > 0)
                if (!assignOne(i / SIZE, i % SIZE, initValues[i]))
                    return false;

        }
        return true;
    }

    /**
     * Assign a value to an unknown cell and propagate the elimination.
     * 
     * @param row the row id 0-8
     * @param col the column id 0-8
     * @param val the new value 1-9
     * @return false when contradiction occurred
     */
    private boolean assignOne(int row, int col, int val) {

        // get all other possible options other than val
        int nbOps = 0;
        int[] ops = new int[SIZE];
        for (int i = 0; i < SIZE; i++) {
            if (options[row][col][i] && i != val - 1)
                ops[nbOps++] = i + 1;
        }

        // eliminate all the other values
        for (int i = 0; i < nbOps; i++) {
            if (!eliminate(row, col, ops[i]))
                return false;
        }
        return true;
    }

    /**
     * Eliminate an option from a cell and analyze the impact.
     * 
     * @param row the row id 0-8
     * @param col the column id 0-8
     * @param val the new value 1-9
     * @return false when contradiction occurred
     */
    private boolean eliminate(int row, int col, int val) {
        if (!options[row][col][val - 1]) {
            return true;
        }
        // eliminate that option
        options[row][col][val - 1] = false;
        // get all the options left
        int nbOps = 0;
        int[] ops = new int[SIZE];
        for (int i = 0; i < SIZE; i++) {
            if (options[row][col][i]) {
                ops[nbOps++] = i + 1;
            }
        }

        // if none left after elimination, return error
        if (nbOps == 0)
            return false;
        // if only one left and that cell is not known, assign it with that value
        if (nbOps == 1) {
            int op = ops[0];
            values[row][col] = op;
            nbAssignedValues++;
            // eliminate val on all its peers
            for (int peer : peers[row][col]) {
                if (!eliminate(peer / SIZE, peer % SIZE, op))
                    return false;
            }
        }
        // See if after eliminating val, its peers(row, column or block) contain only one place for val
        for (int[][][] somePeers : allPeersGroup) {

            int nbPos = 0;
            int[] pos = new int[SIZE];
            // Get all peer cells where val is possible
            for (int peerPos : somePeers[row][col]) {
                if (options[peerPos / SIZE][peerPos % SIZE][val - 1]) {
                    pos[nbPos++] = peerPos;
                }
            }

            if (nbPos == 0)
                return false;

            if (nbPos == 1) {
                int p = pos[0];
                return assignOne(p / SIZE, p % SIZE, val);
            }
        }

        return true;
    }

    /**
     * Attempt to solve the puzzle with conventional algorithm (assign() and eliminate()) and start a recursive search if still unsolved
     * 
     * @return false when contradiction occurred
     */
    public boolean search() {
        if (!assignAll())
            return false;
        if (nbAssignedValues == SIZE * SIZE)
            return validate();

        // if there's still uncertain cell

        // find number of options for all cells
        int[][] nbOptions = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                for (int k = 0; k < SIZE; k++) {
                    nbOptions[i][j] += options[i][j][k] ? 1 : 0;
                }
            }
        }
        // find the position with the fewest options
        int nbOps = SIZE;
        int pos = 0; // 0-80
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (nbOptions[i][j] > 1 && nbOptions[i][j] < nbOps) {
                    nbOps = nbOptions[i][j];
                    pos = i * SIZE + j;
                }
                if (nbOps == 2)
                    break;
            }
        }
        // get all options of that position
        int nb = 0;
        int[] ops = new int[SIZE];// 1 - 9
        for (int i = 0; i < SIZE; i++) {
            if (options[pos / SIZE][pos % SIZE][i])
                ops[nb++] = i + 1;
        }
        // try each option with a new object
        for (int l = 0; l < nb; l++) {
            Puzzle branch = new Puzzle(this);
            branch.initValues[pos] = ops[l];

            // if solved
            if (branch.search()) {
                // copy back all values
                this.nbAssignedValues = branch.nbAssignedValues;
                for (int i = 0; i < SIZE; i++) {
                    for (int j = 0; j < SIZE; j++) {
                        for (int k = 0; k < SIZE; k++) {
                            this.options[i][j][k] = branch.options[i][j][k];
                        }
                        this.values[i][j] = branch.values[i][j];
                    }
                }
                return true;
            }
        }
        // still no solution after searching
        return false;
    }

    /**
     * Test result by summing all peers
     * 
     * @return false when contradiction occurred
     */
    private boolean validate() {
        if (nbAssignedValues < SIZE * SIZE)
            return false;
        // First make sure we have correct number of all digits
        int[] count = new int[SIZE];
        for (int i = 0; i < SIZE * SIZE; i++) {
            count[values[i / SIZE][i % SIZE] - 1]++;
        }
        for (int c : count) {
            if (c != SIZE)
                return false;
        }

        for (int i = 0; i < SIZE; i++) {
            // lines
            int sum = 0;
            for (int j = 0; j < SIZE; j++) {
                sum += values[i][j];
            }
            if (sum != 45)
                return false;
            // columns
            sum = 0;
            for (int j = 0; j < SIZE; j++) {
                sum += values[j][i];
            }
            if (sum != 45)
                return false;
        }
        // blocks
        int[] blocks = { 0, 3, 6, 27, 30, 33, 54, 57, 60 };
        for (int i : blocks) {
            int sum = values[i / SIZE][i % SIZE];
            for (int j : blockPeers[i / SIZE][i % SIZE]) {
                sum += values[j / SIZE][j % SIZE];
            }
            if (sum != 45)
                return false;

        }
        return true;
    }

    /**
     * Display the values.
     */
    public void display() {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                sb.append(values[i][j]).append(" ");
                if ((j + 1) % 3 == 0 && j != 8)
                    sb.append("|");
            }
            sb.append("\n");
            if ((i + 1) % 3 == 0 && i != 8)
                sb.append("------+------+------\n");
        }

        System.out.println(sb.toString());
    }

    /**
     * Get the values when puzzle solved (solve() returns true).
     * 
     * @return the values in a 2D array if solved, null otherwise
     */
    public int[][] getValues() {
        if (nbAssignedValues == SIZE * SIZE)
            return values;
        return null;
    }
}
