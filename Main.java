import java.util.Scanner;

public class Main {

	static int size = 9;
	static int boxSize = 3;

	static int sudokuGrid[][] = new int[size][size];
	static boolean cellIsNotList[][][] = new boolean[size][size][size];

	public static void main(String[] args) {
		
		getUserPuzzle();
		//initEasyPuzzle();
		//initEvilPuzzle();
		//init4by4Puzzle();
		
		attemptSolve();
		
		if (isComplete())
			System.out.println("\nThe Solution!");
		printGrid(sudokuGrid);
	}
	
	//The main loop.
	//Can be indirectly recursively called through guessRecurse();
	public static void attemptSolve() {
	
		int itterationCount = 0;
		//Each processGrid and processValues should solve for at least 1 block.
		// Usually it's more, but as they don't currently return how many they add,
		// run the system size^2 times. (Absolute maximum)
		while (!isComplete() && itterationCount < size * size) {
			processGrid();
			processValues();
			itterationCount++;
		}
		
		//If good, but not complete, then guess a value, attempt again.
		if (isGood() && !isComplete())
			guessRecurse();		
	}
	
	
	//For use by the guessRecurse. Copies a grid and the notIs lists. Used for backups.
	public static void copyGrid(int sourceGrid[][], int targetGrid[][], boolean sourceIsList[][][], boolean targetIsList[][][]) {
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++) {
				targetGrid[i][j] = sourceGrid[i][j];
				for (int k = 0; k < size; k++)
					targetIsList[i][j][k] = sourceIsList[i][j][k];
			}
		
	}
	
	
	
	//Guesses a value for a block.
	//Will only return when the grid is done.
	public static void guessRecurse() {		
		//Backups, in case of incorrect guess.
		int backupGrid[][] = new int[size][size];
		boolean backupCellIsNotList[][][] = new boolean[size][size][size];
		copyGrid(sudokuGrid,backupGrid,cellIsNotList,backupCellIsNotList);
		
		//For each block, if empty:
		for (int x = 0; x < size; x++)
			for (int y = 0; y < size; y++) {
				if (sudokuGrid[x][y] == 0)
					//Guess a value that it could be.
					for (int i = 0; i < size; i++)
						if (cellIsNotList[x][y][i] == false) {
							sudokuGrid[x][y] = i+1;
							//Attempt the solution
							attemptSolve();
							if (!isGood() || !isComplete()) //If the guess doesn't precipitate the solute (Chemistry joke!)
								copyGrid(backupGrid,sudokuGrid,backupCellIsNotList,cellIsNotList); //Reset
						} //Attempt the next possible value.	
			} //And if that doesn't work, run through the tests on the next empty block.	
	}
	
	//Tests if the system is complete.
	// return false if any zeros, OR if the system fails an integrity check.
	public static boolean isComplete() {
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++)
				if (sudokuGrid[i][j] == 0)
					return false;
		return isGood(); //To triple check.
	}	
	
	//Returns true if grid does not have any conflicting blocks.
	public static boolean isGood() {
		//Look for each value in each row, column, box. If duplicates, return false.
		for (int searchVal = 1; searchVal < size+1; searchVal++)
		{
			int rowCount = 0;
			int columnCount = 0;
			int boxCount = 0;
			//Check columns (X statis)
			for (int x = 0; x < size; x++) { 
				columnCount = 0;
				for (int y = 0; y < size; y++)
					if (sudokuGrid[x][y] == searchVal)
						columnCount++;
				if (columnCount > 1)
					return false;
			}
			//Check Rows (Y statis)
			for (int y = 0; y < size; y++) { 
				rowCount = 0;
				for (int x = 0; x < size; x++)
					if (sudokuGrid[x][y] == searchVal)
						rowCount++;
				if (rowCount > 1)
					return false;
			}		
			//Check boxes (3x3)
			for (int x = 0; x < size; x++)
				for (int y = 0; y < size; y++) {
					boxCount = 0;
					
					int boxX = x / boxSize;
					int boxY = y / boxSize;
					
					//For each element in the box ==searchVal, increment.
					for (int checkBoxX = boxX * boxSize; checkBoxX < boxSize+boxX*boxSize; checkBoxX++)
						for (int checkBoxY = boxY * boxSize; checkBoxY < boxSize+boxY*boxSize; checkBoxY++) {
							if (sudokuGrid[checkBoxX][checkBoxY] == searchVal)
								boxCount++;
						}
					if (boxCount > 1)
						return false;
				}
		}		
		return true; //If passes all tests, return true!
	}
	
	

	//Processes the negative spaces in the sudoku puzzle.
	// Ex: Instead of for each box, look for the only value that will fit in it, 
	//  for each value, look for a box that will only fit it.
	// Eg: If you have 8 of one value in a puzzle, you will KNOW where the last one fits in.
	//  This function does that.
	public static void processValues() {

		// For each value:
		for (int value = 1; value < size + 1; value++) {
			//Initialize a test grid.
			int testGrid[][] = new int[size][size];

			int sentinel; //Sentinel value, for more easily read debug output.
			if (value == 1)
				sentinel = 2;
			else
				sentinel = 1;

			//Fill the test grid. 0 if empty, the value if equal, the sentinel if not.
			for (int x = 0; x < size; x++)
				for (int y = 0; y < size; y++) {
					if (sudokuGrid[x][y] == 0)
						testGrid[x][y] = 0;
					else if (sudokuGrid[x][y] == value)
						testGrid[x][y] = value;
					else
						testGrid[x][y] = sentinel;
				}
			
			// For each known block, fill the block's column, row, and box with the sentinel.
			for (int x = 0; x < size; x++)
				for (int y = 0; y < size; y++)
					if (testGrid[x][y] == value) {
						// Fill column, row
						for (int i = 0; i < size; i++) {
							testGrid[i][y] = sentinel;
							testGrid[x][i] = sentinel;
						}
						testGrid[x][y] = value; //As overwritten.

						// Fill Box
						int boxX = x / boxSize;
						int boxY = y / boxSize;

						for (int checkBoxX = boxX * boxSize; checkBoxX < boxSize+boxX*boxSize; checkBoxX++)
							for (int checkBoxY = boxY * boxSize; checkBoxY < boxSize+boxY*boxSize; checkBoxY++) {
								if (testGrid[checkBoxX][checkBoxY] == 0)
									testGrid[checkBoxX][checkBoxY] = sentinel;
							}
					}

			
			//Look through the grid, searching for a single 0 in a row, column or block.
			// This is the spot for the searched value to go into.
			for (int x = 0; x < size; x++)
				for (int y = 0; y < size; y++)
					if (testGrid[x][y] == 0) {
						int columnCount = 0;
						int rowCount = 0;
						int boxCount = 0;
						boolean failTest = false;

						for (int i = 0; i < size; i++) {
							if (testGrid[x][i] == 0)
								columnCount++;
							if (testGrid[i][y] == 0)
								rowCount++;
							if (testGrid[x][i] == value	|| testGrid[i][y] == value) { // In case of not filled row/col/box with the sentinel.
								failTest = true; //fail.
								break;
							}
						}

						int boxX = x / boxSize;
						int boxY = y / boxSize;
						int startIndex[] = { boxX * boxSize, boxY * boxSize };
						for (int checkBoxX = startIndex[0]; checkBoxX < startIndex[0]
								+ boxSize; checkBoxX++)
							for (int checkBoxY = startIndex[1]; checkBoxY < startIndex[1]
									+ boxSize; checkBoxY++) {
								if (testGrid[checkBoxX][checkBoxY] == 0)
									boxCount++;
								else if (testGrid[checkBoxX][checkBoxY] == value) {
									failTest = true;
								}
							}
						//If not failed, and is the only 0 in the box/row/col, must contain the value.
						if (failTest == false && (columnCount == 1 || rowCount == 1 || boxCount == 1))
							testGrid[x][y] = value;
					}

			// Finished. Copy array back to grid.
			for (int x = 0; x < size; x++)
				for (int y = 0; y < size; y++) {
					if (testGrid[x][y] == value)
						sudokuGrid[x][y] = value;
					else if (testGrid[x][y] == sentinel)
						cellIsNotList[x][y][value-1] = true; //As it is known that this can't be the value.
				}
			}
	}
	
	

	//Child to processBlock.
	//Separated for testing purposes.
	public static void processBox(int x, int y) {
		int boxX = x / boxSize;
		int boxY = y / boxSize;

		for (int checkBoxX = boxX * boxSize; checkBoxX < boxSize+boxX*boxSize; checkBoxX++)
			for (int checkBoxY = boxY * boxSize; checkBoxY < boxSize+boxY*boxSize; checkBoxY++)
				if (sudokuGrid[checkBoxX][checkBoxY] != 0)
					cellIsNotList[x][y][sudokuGrid[checkBoxX][checkBoxY] - 1] = true;		
	}
	
	//Processes the positive spaces in the full puzzle.
	//For each block, look through it's col/row/box and insert the value if it's the only possible one.
	// Returns false on an error, when a block CANNOT be ANY value.
	public static boolean processBlock(int x, int y) {
		if (sudokuGrid[x][y] != 0)
			return true;

		// Process Horizontal + Vertical
		for (int i = 0; i < size; i++) {
			if (sudokuGrid[x][i] != 0)
				cellIsNotList[x][y][sudokuGrid[x][i]-1] = true;
			if (sudokuGrid[i][y] != 0)
				cellIsNotList[x][y][sudokuGrid[i][y]-1] = true;
		}
		//Process the box.
		processBox(x, y);

		//Step through the isNot list, and fill block if there's only one value.
		int notCount = 0;
		for (int i = 0; i < size; i++)
			if (cellIsNotList[x][y][i] == true)
				notCount++;
		
		
		if (notCount == size) //Edge case. Only happens on guessing portion. Return false.
			return false;
		if (notCount == size - 1)
			for (int i = 0; i < size; i++)
				if (cellIsNotList[x][y][i] == false) {
					sudokuGrid[x][y] = i + 1;
				}
		return true;
	}

	//Loop Call for processBlock.
	//Returns early/breaks if there is an error with the grid.
	public static void processGrid() {
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++)
				if (processBlock(i, j) == false)
					return;
	}
	
	//Prints a supplied grid.
	//For debugging and final output purposes.
	public static void printGrid(int Grid[][]) {
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++)
				System.out.print(Grid[i][j] + " ");
			System.out.println("");
		}
		System.out.println();
	}
	
	//Gets user input. 
	public static void getUserPuzzle() {
		Scanner in = new Scanner(System.in);

		String inString; 
		boolean gotFullGrid = false;
		while (!gotFullGrid) {
			try {
				System.out.println("Please Enter Your Sudoku. Use a space or a zero for empty boxes. Hit enter for the next line.");
				for (int i = 0; i < 9; i++) { 
					inString = in.nextLine(); 
										
					for (int j = 0; j < size; j++) {
						if ( inString.charAt(j) == ' ' || inString.charAt(j) == '0') 
							sudokuGrid[i][j] = 0; 
						else if (inString.charAt(j) <= '9' && inString.charAt(j) >= '0')
							sudokuGrid[i][j] = Character.getNumericValue(inString.charAt(j)); 
						else
							System.out.println("Unknown char: " + (int) inString.charAt(j) + " " + inString.charAt(j));
					}
				}
				gotFullGrid = isGood();
				if (!gotFullGrid)
					System.out.println("Is there an error in your input? This grid doesn't seem right.");
			}
			catch (Exception e) {
				System.out.println("\nThere was an error in the input. Please try again.");
			}
		}
		in.close();// To get rid of annoying dubug messaging.
	}
	
	//An example 4x4 sudoku puzzle.
	public static void init4by4Puzzle() {
		size = 4;
		boxSize = 2;
		int row0[] = { 2, 0, 4, 0 };
		int row1[] = { 0, 4, 0, 0 };
		int row2[] = { 0, 0, 3, 0 };
		int row3[] = { 0, 0, 0, 1 };
		for (int i = 0; i < 4; i++) {
			sudokuGrid[0][i] = row0[i];
			sudokuGrid[1][i] = row1[i];
			sudokuGrid[2][i] = row2[i];
			sudokuGrid[3][i] = row3[i];
		}
	}
	public static void initEasyPuzzle() {
		// As reading double-nested initializers was giving me a headache.
		int row0[] = {8,0,1,7,4,0,0,0,0};
		int row1[] = {0,9,0,1,0,3,0,0,0};
		int row2[] = {6,2,0,8,0,9,0,0,4};
		int row3[] = {1,4,5,0,0,0,0,3,9};
		int row4[] = {0,0,6,0,0,0,5,0,0};
		int row5[] = {9,8,0,0,0,0,1,4,2};
		int row6[] = {3,0,0,5,0,2,0,7,1};
		int row7[] = {0,0,0,6,0,1,0,5,0};
		int row8[] = {0,0,0,0,9,7,3,0,6};

		for (int i = 0; i < 9; i++) {
			sudokuGrid[0][i] = row0[i];
			sudokuGrid[1][i] = row1[i];
			sudokuGrid[2][i] = row2[i];
			sudokuGrid[3][i] = row3[i];
			sudokuGrid[4][i] = row4[i];
			sudokuGrid[5][i] = row5[i];
			sudokuGrid[6][i] = row6[i];
			sudokuGrid[7][i] = row7[i];
			sudokuGrid[8][i] = row8[i];
		}
	}

	public static void initEvilPuzzle() {
		// As reading double-nested initializers was giving me a headache.
		int row0[] = {0,5,0,8,7,0,1,0,0};
		int row1[] = {0,0,0,0,0,4,0,0,8};
		int row2[] = {1,0,0,0,6,0,0,0,9};
		int row3[] = {0,0,6,0,0,7,8,0,2};
		int row4[] = {0,0,0,0,0,0,0,0,0};
		int row5[] = {3,0,8,2,0,0,9,0,0};
		int row6[] = {4,0,0,0,1,0,0,0,7};
		int row7[] = {6,0,0,7,0,0,0,0,0};
		int row8[] = {0,0,1,0,2,5,0,3,0};

		for (int i = 0; i < 9; i++) {
			sudokuGrid[0][i] = row0[i];
			sudokuGrid[1][i] = row1[i];
			sudokuGrid[2][i] = row2[i];
			sudokuGrid[3][i] = row3[i];
			sudokuGrid[4][i] = row4[i];
			sudokuGrid[5][i] = row5[i];
			sudokuGrid[6][i] = row6[i];
			sudokuGrid[7][i] = row7[i];
			sudokuGrid[8][i] = row8[i];
		}
	}
}
