package boggle;

import java.lang.reflect.Array;
import java.util.*;

/**
 * The BoggleGame class for the first Assignment in CSC207, Fall 2022
 */
public class BoggleGame {

    /**
     * scanner used to interact with the user via console
     */
    public Scanner scanner;
    /**
     * stores game statistics
     */
    private BoggleStats gameStats;

    /**
     * dice used to randomize letter assignments for a small grid
     */
    private final String[] dice_small_grid= //dice specifications, for small and large grids
            {"AAEEGN", "ABBJOO", "ACHOPS", "AFFKPS", "AOOTTW", "CIMOTU", "DEILRX", "DELRVY",
                    "DISTTY", "EEGHNW", "EEINSU", "EHRTVW", "EIOSST", "ELRTTY", "HIMNQU", "HLNNRZ"};
    /**
     * dice used to randomize letter assignments for a big grid
     */
    private final String[] dice_big_grid =
            {"AAAFRS", "AAEEEE", "AAFIRS", "ADENNN", "AEEEEM", "AEEGMU", "AEGMNN", "AFIRSY",
                    "BJKQXZ", "CCNSTW", "CEIILT", "CEILPT", "CEIPST", "DDLNOR", "DDHNOT", "DHHLOR",
                    "DHLNOR", "EIIITT", "EMOTTT", "ENSSSU", "FIPRSY", "GORRVW", "HIPRRY", "NOOTUW", "OOOTTU"};

    /*
     * BoggleGame constructor
     */
    public BoggleGame() {
        this.scanner = new Scanner(System.in);
        this.gameStats = new BoggleStats();
    }

    /*
     * Provide instructions to the user, so they know how to play the game.
     */
    public void giveInstructions()
    {
        System.out.println("The Boggle board contains a grid of letters that are randomly placed.");
        System.out.println("We're both going to try to find words in this grid by joining the letters.");
        System.out.println("You can form a word by connecting adjoining letters on the grid.");
        System.out.println("Two letters adjoin if they are next to each other horizontally, ");
        System.out.println("vertically, or diagonally. The words you find must be at least 4 letters long, ");
        System.out.println("and you can't use a letter twice in any single word. Your points ");
        System.out.println("will be based on word length: a 4-letter word is worth 1 point, 5-letter");
        System.out.println("words earn 2 points, and so on. After you find as many words as you can,");
        System.out.println("I will find all the remaining words.");
        System.out.println("\nHit return when you're ready...");
    }


    /*
     * Gets information from the user to initialize a new Boggle game.
     * It will loop until the user indicates they are done playing.
     */
    public void playGame(){
        int boardSize;
        while(true){
            System.out.println("Enter 1 to play on a big (5x5) grid; 2 to play on a small (4x4) one:");
            String choiceGrid = scanner.nextLine();

            //get grid size preference
            if(choiceGrid == "") break; //end game if user inputs nothing
            while(!choiceGrid.equals("1") && !choiceGrid.equals("2")){
                System.out.println("Please try again.");
                System.out.println("Enter 1 to play on a big (5x5) grid; 2 to play on a small (4x4) one:");
                choiceGrid = scanner.nextLine();
            }

            if(choiceGrid.equals("1")) boardSize = 5;
            else boardSize = 4;

            //get letter choice preference
            System.out.println("Enter 1 to randomly assign letters to the grid; 2 to provide your own.");
            String choiceLetters = scanner.nextLine();

            if(choiceLetters == "") break; //end game if user inputs nothing
            while(!choiceLetters.equals("1") && !choiceLetters.equals("2")){
                System.out.println("Please try again.");
                System.out.println("Enter 1 to randomly assign letters to the grid; 2 to provide your own.");
                choiceLetters = scanner.nextLine();
            }

            if(choiceLetters.equals("1")){
                playRound(boardSize,randomizeLetters(boardSize));
            } else {
                System.out.println("Input a list of " + boardSize*boardSize + " letters:");
                choiceLetters = scanner.nextLine();
                while(!(choiceLetters.length() == boardSize*boardSize)){
                    System.out.println("Sorry, bad input. Please try again.");
                    System.out.println("Input a list of " + boardSize*boardSize + " letters:");
                    choiceLetters = scanner.nextLine();
                }
                playRound(boardSize,choiceLetters.toUpperCase());
            }

            //round is over! So, store the statistics, and end the round.
            this.gameStats.summarizeRound();
            this.gameStats.endRound();

            //Shall we repeat?
            System.out.println("Play again? Type 'Y' or 'N'");
            String choiceRepeat = scanner.nextLine().toUpperCase();

            if(choiceRepeat == "") break; //end game if user inputs nothing
            while(!choiceRepeat.equals("Y") && !choiceRepeat.equals("N")){
                System.out.println("Please try again.");
                System.out.println("Play again? Type 'Y' or 'N'");
                choiceRepeat = scanner.nextLine().toUpperCase();
            }

            if(choiceRepeat == "" || choiceRepeat.equals("N")) break; //end game if user inputs nothing

        }

        //we are done with the game! So, summarize all the play that has transpired and exit.
        this.gameStats.summarizeGame();
        System.out.println("Thanks for playing!");
    }

    /*
     * Play a round of Boggle.
     * This initializes the main objects: the board, the dictionary, the map of all
     * words on the board, and the set of words found by the user. These objects are
     * passed by reference from here to many other functions.
     */
    public void playRound(int size, String letters){
        //step 1. initialize the grid
        BoggleGrid grid = new BoggleGrid(size);
        grid.initalizeBoard(letters);
        //step 2. initialize the dictionary of legal words
        Dictionary boggleDict = new Dictionary("wordlist.txt"); //you may have to change the path to the wordlist, depending on where you place it.
        //step 3. find all legal words on the board, given the dictionary and grid arrangement.
        Map<String, ArrayList<Position>> allWords = new HashMap<String, ArrayList<Position>>();
        findAllWords(allWords, boggleDict, grid);
        //step 4. allow the user to try to find some words on the grid
        humanMove(grid, allWords);
        //step 5. allow the computer to identify remaining words
        computerMove(allWords);
    }

    /*
     * This method should return a String of letters (length 16 or 25 depending on the size of the grid).
     * There will be one letter per grid position, and they will be organized left to right,
     * top to bottom. A strategy to make this string of letters is as follows:
     * -- Assign a one of the dice to each grid position (i.e. dice_big_grid or dice_small_grid)
     * -- "Shuffle" the positions of the dice to randomize the grid positions they are assigned to
     * -- Randomly select one of the letters on the given die at each grid position to determine
     *    the letter at the given position
     *
     * @return String a String of random letters (length 16 or 25 depending on the size of the grid)
     */
    // CHANGE TO PRIVATE
    private String randomizeLetters(int size){
        String word = "";
        for(int i = 0; i < size*size; i++){
            randomDice(size);
        }

        for(int row = 0; row < size*size; row++){
            Random randoms = new Random();
            int dice = randoms.nextInt(6);
            if(size == 4){
                //printing(dice_small_grid);
                char newChar = dice_small_grid[row].charAt(dice);
                word += newChar;
            }else{
                char newChar = dice_big_grid[row].charAt(dice);
                word += newChar;
            }
        }
        return word;
    }


    /*
     * This should be a recursive function that finds all valid words on the boggle board.
     * Every word should be valid (i.e. in the boggleDict) and of length 4 or more.
     * Words that are found should be entered into the allWords HashMap.  This HashMap
     * will be consulted as we play the game.
     *
     * Note that this function will be a recursive function.  You may want to write
     * a wrapper for your recursion. Note that every legal word on the Boggle grid will correspond to
     * a list of grid positions on the board, and that the Position class can be used to represent these
     * positions. The strategy you will likely want to use when you write your recursion is as follows:
     * -- At every Position on the grid:
     * ---- add the Position of that point to a list of stored positions
     * ---- if your list of stored positions is >= 4, add the corresponding word to the allWords Map
     * ---- recursively search for valid, adjacent grid Positions to add to your list of stored positions.
     * ---- Note that a valid Position to add to your list will be one that is either horizontal, diagonal, or
     *      vertically touching the current Position
     * ---- Note also that a valid Position to add to your list will be one that, in conjunction with those
     *      Positions that precede it, form a legal PREFIX to a word in the Dictionary (this is important!)
     * ---- Use the "isPrefix" method in the Dictionary class to help you out here!!
     * ---- Positions that already exist in your list of stored positions will also be invalid.
     * ---- You'll be finished when you have checked EVERY possible list of Positions on the board, to see
     *      if they can be used to form a valid word in the dictionary.
     * ---- Food for thought: If there are N Positions on the grid, how many possible lists of positions
     *      might we need to evaluate?
     *
     * @param allWords A mutable list of all legal words that can be found, given the boggleGrid grid letters
     * @param boggleDict A dictionary of legal words
     * @param boggleGrid A boggle grid, with a letter at each position on the grid
     */
    private void findAllWords(Map<String,ArrayList<Position>> allWords, Dictionary boggleDict, BoggleGrid boggleGrid) {
        for(int row = 0; row < boggleGrid.numRows(); row++){
            for(int col = 0; col < boggleGrid.numCols(); col++){
                ArrayList<Position> tempArray = new ArrayList<>();
                helperDistance(allWords, "", row, col, boggleGrid, boggleDict, tempArray, allWords.size());
            }
        }
        for(String word: allWords.keySet()){
            ArrayList<Position> newArray = fixTempArray(allWords.get(word), word, boggleGrid);
            allWords.put(word, newArray);
        }
    }

    private Map<String,ArrayList <Position>> helperDistance(Map<String, ArrayList<Position>> allWords,
                                                            String word, int row, int col,
                                                            BoggleGrid boggleGrid, Dictionary boggleDict,
                                                            ArrayList<Position> tempArray, int size){
        //Add the elements into array
        char currentChar = boggleGrid.getCharAt(row, col);
        word += currentChar;
        Position position = new Position(row, col);
        tempArray.add(position);

        //Search 1 around the current letter
        for(int i = row - 1; i < boggleGrid.numRows() && i < row + 2; i++) {
            for (int j = col - 1; j < boggleGrid.numCols() && j < col + 2; j++) {
                //Check if visited
                if (!(i < 0) && !(j < 0) && checkPositions(tempArray, new Position(i, j))) {
                    if (boggleDict.isPrefix(word)) {
                        //add word if legal
                        if (boggleDict.containsWord(word)) {
                            if (word.length() >= 4) {
                                allWords.put(word, tempArray);
                                return allWords;
                            }
                        }
                        //If prefix is legal then call the function again and keep going deeper into the word
                        allWords.putAll(helperDistance(allWords, word, i, j, boggleGrid, boggleDict, tempArray, size));
                    }else{
                        tempArray.remove(position);
                    }
                }
            }
        }

        return allWords;
    }

    private boolean checkPositions(ArrayList<Position> array, Position currentPosition){
        //Fixing the position from recurrsive function
        for(Position position: array){
            if(currentPosition.getCol() == position.getCol() && currentPosition.getRow() == position.getRow()){
                return false;
            }
        }
        return true;
    }

    private ArrayList<Position> fixTempArray(ArrayList<Position> array, String word, BoggleGrid boggleGrid){
        //Fix the positions, It just does stuff somehow someway!
        ArrayList<Position> newArray = new ArrayList<Position>();
        char[] wordList = new char[word.length()];
        for(int i = 0; i < word.length(); i++){
            wordList[i] = Character.toLowerCase(word.charAt(i));
        }

        boolean firstCheck = true;
        Position prevPosition = new Position(99999, 99999);
        int colDiff;
        int rowDiff;
        char prevChar = word.charAt(0);
        int wordCounter = 0;
        ArrayList<Position> oldPosition = new ArrayList<Position>();

        for(int counter = 0; counter < array.size(); counter++){
            Position position1 = array.get(counter);
            int row = position1.getRow();
            int col = position1.getCol();

            if(col >= prevPosition.getCol()){
                colDiff = col - prevPosition.getCol();
            }else{
                colDiff = prevPosition.getCol() - col;
            }
            if(row >= prevPosition.getRow()){
                rowDiff = row - prevPosition.getRow();
            }else{
                rowDiff = prevPosition.getRow() - row;
            }

            if(Character.toLowerCase(boggleGrid.getCharAt(row, col)) == wordList[wordCounter] &&
                    newArray.size() < word.length()){
                if(firstCheck){
                    newArray.add(new Position(row, col));
                    firstCheck = false;
                    wordCounter += 1;
                    prevPosition = position1;
                    prevChar = Character.toLowerCase(boggleGrid.getCharAt(row, col));
                }else if(colDiff > 1 || rowDiff > 1){
                    for(Position checkPosition: oldPosition){
                        if(col >= checkPosition.getCol()){
                            colDiff = col - checkPosition.getCol();
                        }else{
                            colDiff = checkPosition.getCol() - col;
                        }
                        if(row >= checkPosition.getRow()){
                            rowDiff = row - checkPosition.getRow();
                        }else{
                            rowDiff = checkPosition.getRow() - row;
                        }
                    }
                    if(!(colDiff > 1 || rowDiff > 1)){
                        newArray.add(new Position(row, col));
                        if(wordCounter < wordList.length-1) {
                            wordCounter += 1;
                        }
                        prevPosition = position1;
                        prevChar = Character.toLowerCase(boggleGrid.getCharAt(row, col));
                    }
                }else if(newArray.size() < word.length()){
                    newArray.add(new Position(row, col));
                    if(wordCounter < wordList.length-1) {
                        wordCounter += 1;
                    }
                    prevPosition = position1;
                    prevChar = Character.toLowerCase(boggleGrid.getCharAt(row, col));
                }
            }else if(newArray.size() < word.length() && !(colDiff > 1 || rowDiff > 1)){
                if(prevChar == Character.toLowerCase(boggleGrid.getCharAt(row, col))){
                    newArray.set(newArray.size() - 1, new Position(row, col));
                    oldPosition.add(prevPosition);
                    prevPosition = position1;

                }
            }
        }
        return newArray;
    }

    /*
     * Gets words from the user.  As words are input, check to see that they are valid.
     * If yes, add the word to the player's word list (in boggleStats) and increment
     * the player's score (in boggleStats).
     * End the turn once the user hits return (with no word).
     *
     * @param board The boggle board
     * @param allWords A mutable list of all legal words that can be found, given the boggleGrid grid letters
     */
    private void humanMove(BoggleGrid board, Map<String,ArrayList<Position>> allWords){
        System.out.println("It's your turn to find some words!");
        while(true) {
            //You write code here!
            //step 1. Print the board for the user, so they can scan it for words
            //step 2. Get a input (a word) from the user via the console
            //step 3. Check to see if it is valid (note validity checks should be case-insensitive)
            //step 4. If it's valid, update the player's word list and score (stored in boggleStats)
            //step 5. Repeat step 1 - 4
            //step 6. End when the player hits return (with no word choice).
            //Print Board
            System.out.println(board);
            String playerInput = scanner.nextLine();
            String validPlayerInput = returnValid(playerInput);

            //Keep this if statement with playerInput or break
            if(playerInput == "") break;
            else{
                if(ignoreCaseKey(allWords, playerInput)){
                    this.gameStats.addWord(validPlayerInput, BoggleStats.Player.Human);
                }else{
                    System.out.println("Invalid word! ");
                    System.out.println("No points awarded \t");
                }

            }
        }
    }

    private boolean ignoreCaseKey(Map<String, ArrayList<Position>> map, String word){
        for(String key: map.keySet()){
            if(key.equalsIgnoreCase(word)){
                return true;
            }
        }
        return false;
    }

    private String returnValid(String str){
        String validString = str.toLowerCase();
        String newValidString = validString.strip();
        return newValidString;
    }
    /*
     * Gets words from the computer.  The computer should find words that are
     * both valid and not in the player's word list.  For each word that the computer
     * finds, update the computer's word list and increment the
     * computer's score (stored in boggleStats).
     *
     * @param allWords A mutable list of all legal words that can be found, given the boggleGrid grid letters
     */
    private void computerMove(Map<String,ArrayList<Position>> all_words){
        for(String word: all_words.keySet()){
            this.gameStats.addWord(word, BoggleStats.Player.Computer);
        }
    }

    private void randomDice(int size){
        if(size == 4) {
            for (int row = 0; row <= size; row++) {
                for (int col = 0; col <= size; col++) {
                    swap(dice_small_grid, randomNum(size), randomNum(size));
                }
            }
        }
        else{
            for (int row = 0; row <= size; row++) {
                for (int col = 0; col <= size; col++) {
                    swap(dice_big_grid, randomNum(size), randomNum(size));
                }
            }
        }
    }
    /*
    private void convertArray(Map<String, ArrayList<Position>> tempArray){
        for(int i = 0; i < tempArray.length; i++){
            System.out.println(tempArray[i]);
        }
    }

     */
    private void swap(String[] tempArray, int tempI, int tempJ){
        String temp = tempArray[tempI];
        tempArray[tempI] = tempArray[tempJ];
        tempArray[tempJ] = temp;
    }

    private int randomNum(int size){
        Random rand = new Random();
        int chance = rand.nextInt(size*size);
        return chance;
    }
}
