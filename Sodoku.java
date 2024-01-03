import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import java.util.ArrayList;

public class Sodoku extends JFrame implements Runnable, MouseListener, MouseMotionListener, KeyListener
{
    private static final long serialVersionUID = 1L;
    private int width; //width of screen
    private int height; //height of screen
    private int frame; //current frame of program
    private Thread thread; //thread of execution for the program
    private boolean running; //true while program is running
    private BufferedImage image; //allows array of pixels to be drawn to the screen
    private int[] pixels; //array of pixels on screen (pixel (x, y) = pixels[y * width + x])

    private int mouseX; //x coordinate of mouse
    private int mouseY; //y coordinate of mouse
    private boolean mousePressed; //whether or not mouse is being pressed (still or moving)
    private boolean keyPressed; //whether or not any key is being pressed
    private boolean keyReleased; //true immediately after key is released
    private boolean keyTyped; //true if key is pressed and a valid unicode character is generated
    private KeyEvent key; //key currently pressed (or last one pressed if none are pressed)

    private int[][] board;
    private int[][] initial;
    private boolean[][][] candidates;
    private boolean[][][] sameRowCol;
    private int xSelected, ySelected;
    private boolean win;

    private boolean showCandidates;
    private boolean highlightCandidates;
    private int iterationsPerUpdate;

    public Sodoku()
    {
        //set size of screen
        width = 550;
        height = 550;

        //set initial frame
        frame = 0;

        //what will be displayed to the user
        thread = new Thread(this);
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();

        //keyboard input
        keyPressed = false;
        keyReleased = false;
        keyTyped = false;
        key = new KeyEvent(new JFrame(), 0, 0, 0, 0, KeyEvent.CHAR_UNDEFINED);
        addKeyListener(this);

        //mouse input
        mouseX = 0;
        mouseY = 0;
        mousePressed = false;
        addMouseListener(this);
        addMouseMotionListener(this);

        //setting up the window
        setSize(width + 16, height + 39);
        setResizable(false);
        setTitle("Sodoku");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);

        // hard
        /*initial = new int[][] {
                {0, 0, 0, 0, 7, 0, 0, 5, 0},
                {0, 0, 8, 2, 0, 6, 0, 1, 0},
                {0, 1, 0, 0, 0, 0, 4, 0, 0},
                {0, 8, 3, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 9, 0, 0},
                {0, 0, 7, 0, 2, 8, 0, 0, 3},
                {8, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 5, 2, 6, 0, 0, 1, 9, 0},
                {0, 0, 0, 4, 9, 0, 0, 0, 6}};*/

        // hard
        initial = new int[][] {
                {0, 0, 0, 0, 0, 5, 1, 0, 0},
                {0, 7, 4, 0, 0, 0, 0, 2, 0},
                {0, 0, 9, 0, 0, 0, 0, 0, 3},
                {0, 0, 3, 0, 0, 0, 0, 0, 0},
                {7, 6, 0, 0, 8, 0, 0, 0, 0},
                {4, 0, 0, 6, 0, 1, 0, 0, 0},
                {0, 0, 6, 2, 4, 0, 3, 0, 9},
                {0, 0, 0, 7, 6, 3, 0, 0, 2},
                {0, 0, 0, 0, 0, 0, 0, 4, 0}};

        // worlds hardest
        /*initial = new int[][] {
                {8, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 3, 6, 0, 0, 0, 0, 0},
                {0, 7, 0, 0, 9, 0, 2, 0, 0},
                {0, 5, 0, 0, 0, 7, 0, 0, 0},
                {0, 0, 0, 0, 4, 5, 7, 0, 0},
                {0, 0, 0, 1, 0, 0, 0, 3, 0},
                {0, 0, 1, 0, 0, 0, 0, 6, 8},
                {0, 0, 8, 5, 0, 0, 0, 1, 0},
                {0, 9, 0, 0, 0, 0, 4, 0, 0}};*/

        // easy
        /*initial = new int[][] {
                {0, 6, 2, 0, 0, 0, 3, 0, 9},
                {0, 8, 0, 6, 0, 0, 4, 7, 0},
                {9, 0, 0, 5, 0, 2, 0, 1, 0},
                {0, 0, 3, 0, 5, 0, 2, 0, 0},
                {0, 0, 0, 7, 2, 3, 9, 4, 0},
                {2, 0, 9, 4, 0, 8, 6, 0, 7},
                {6, 2, 4, 0, 7, 0, 0, 0, 0},
                {1, 0, 8, 0, 0, 0, 7, 2, 4},
                {0, 0, 0, 0, 9, 4, 0, 0, 8}};*/

        // medium
        /*initial = new int[][] {
                {0, 9, 0, 0, 0, 0, 0, 1, 3},
                {0, 0, 5, 0, 0, 0, 7, 0, 0},
                {0, 0, 0, 0, 8, 1, 0, 5, 0},
                {8, 0, 9, 2, 0, 0, 0, 0, 0},
                {0, 0, 0, 3, 0, 0, 8, 0, 0},
                {4, 0, 6, 5, 0, 0, 9, 0, 0},
                {0, 0, 0, 0, 3, 5, 1, 6, 0},
                {0, 0, 0, 0, 0, 0, 0, 3, 2},
                {0, 4, 0, 0, 0, 0, 0, 0, 0}};*/

        // hard
        /*initial = new int[][] {
                {0, 6, 0, 1, 2, 4, 0, 0, 9},
                {9, 0, 0, 0, 0, 5, 0, 7, 0},
                {0, 1, 5, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 7, 0, 4},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {6, 3, 0, 0, 0, 0, 0, 8, 0},
                {0, 0, 0, 0, 0, 7, 9, 0, 1},
                {0, 0, 0, 6, 0, 0, 0, 0, 0},
                {0, 0, 4, 2, 8, 0, 6, 0, 0}};*/

        /*initial = new int[9][9];
        int numRandomSquares = 30;
        int count = 0;
        while(count < numRandomSquares) {
            int x = (int) (Math.random() * 9);
            int y = (int) (Math.random() * 9);
            int value = (int) (Math.random() * 9 + 1);
            if(initial[y][x] != 0)
                continue;
            initial[y][x] = value;
            if(!valid(initial))
                initial[y][x] = 0;
            else
                count++;
        }
        solution = new int[9][9];*/

        board = new int[initial.length][initial[0].length];
        for(int y = 0; y < initial.length; y++)
            for(int x = 0; x < initial[y].length; x++)
                board[y][x] = initial[y][x];

        candidates = new boolean[board.length][board[0].length][board.length];
        for(int y = 0; y < candidates.length; y++) {
            for (int x = 0; x < candidates[y].length; x++) {
                for (int i = 0; i < candidates[y][x].length; i++) {
                    candidates[y][x][i] = !(sameInRow(x, y, i + 1) || sameInCol(x, y, i + 1) || sameInSquare(x, y, i + 1));
                }
            }
        }

        sameRowCol = new boolean[board.length][board[0].length][board.length];

        xSelected = -1;
        ySelected = -1;

        win = false;

        showCandidates = false;
        highlightCandidates = false;
        iterationsPerUpdate = 1;

        //start the program
        start();
    }

    private synchronized void start()
    {
        //starts game
        running = true;
        thread.start();
    }

    private synchronized void stop()
    {
        //stops game
        running = false;
        try
        {
            thread.join();
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    private void update()
    {
        // updates everything

        // update background pixels
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                if(win)
                    pixels[y * width + x] = RGB(0, 192 * (Math.abs(x - width/2) + Math.abs(y - height/2)) / width, 0);
                else
                    pixels[y * width + x] = RGB(192 * (Math.abs(x - width/2) + Math.abs(y - height/2)) / width, 0, 0);
            }
        }

        // check if you won
        win = valid(board);
        for(int y = 0; y < initial.length; y++)
            for(int x = 0; x < initial[y].length; x++)
                if(board[y][x] == 0)
                    win = false;


        if(xSelected >= 0 && xSelected < initial[0].length && ySelected >= 0 && ySelected < initial.length && initial[ySelected][xSelected] == 0) {
            if (keyPressed && key.getKeyCode() == KeyEvent.VK_1) {
                board[ySelected][xSelected] = 1;
                updateCandidates(xSelected, ySelected, 1);
            }
            else if (keyPressed && key.getKeyCode() == KeyEvent.VK_2) {
                board[ySelected][xSelected] = 2;
                updateCandidates(xSelected, ySelected, 2);
            }
            else if (keyPressed && key.getKeyCode() == KeyEvent.VK_3) {
                board[ySelected][xSelected] = 3;
                updateCandidates(xSelected, ySelected, 3);
            }
            else if (keyPressed && key.getKeyCode() == KeyEvent.VK_4) {
                board[ySelected][xSelected] = 4;
                updateCandidates(xSelected, ySelected, 4);
            }
            else if (keyPressed && key.getKeyCode() == KeyEvent.VK_5) {
                board[ySelected][xSelected] = 5;
                updateCandidates(xSelected, ySelected, 5);
            }
            else if (keyPressed && key.getKeyCode() == KeyEvent.VK_6) {
                board[ySelected][xSelected] = 6;
                updateCandidates(xSelected, ySelected, 6);
            }
            else if (keyPressed && key.getKeyCode() == KeyEvent.VK_7) {
                board[ySelected][xSelected] = 7;
                updateCandidates(xSelected, ySelected, 7);
            }
            else if (keyPressed && key.getKeyCode() == KeyEvent.VK_8) {
                board[ySelected][xSelected] = 8;
                updateCandidates(xSelected, ySelected, 8);
            }
            else if (keyPressed && key.getKeyCode() == KeyEvent.VK_9) {
                board[ySelected][xSelected] = 9;
                updateCandidates(xSelected, ySelected, 9);
            }
            else if (keyPressed && key.getKeyCode() == KeyEvent.VK_DELETE) {
                board[ySelected][xSelected] = 0;
                updateCandidates(xSelected, ySelected, 0);
            }
            else if (keyPressed && key.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                board[ySelected][xSelected] = 0;
                updateCandidates(xSelected, ySelected, 0);
            }
        }

        if(mousePressed) {
            xSelected = mouseX / 50 - 1;
            ySelected = mouseY / 50 - 1;
        }

        // have computer do one iteration of solving
        if(keyReleased && key.getKeyCode() == KeyEvent.VK_SPACE) {
            int iterations = 0;
            while(!win && iterations < iterationsPerUpdate) {
                // create new board as copy of board
                int[][] newBoard = new int[board.length][board[0].length];
                for (int y = 0; y < board.length; y++)
                    for (int x = 0; x < board[y].length; x++)
                        newBoard[y][x] = board[y][x];

                // if a number is the only one possible in a square, fill that square with that number
                for (int y = 0; y < board.length; y++) {
                    for (int x = 0; x < board[y].length; x++) {
                        if (board[y][x] == 0) {
                            ArrayList<Integer> possible = new ArrayList<Integer>();
                            for (int i = 0; i < candidates[y][x].length; i++) {
                                if (candidates[y][x][i])
                                    possible.add(i + 1);
                            }
                            if (possible.size() == 1)
                                newBoard[y][x] = possible.get(0);
                        }
                    }
                }

                // if a number can only go in one place within a row, column, or square, put the number there
                for (int y = 0; y < board.length; y++) {
                    for (int x = 0; x < board[y].length; x++) {
                        if (board[y][x] == 0) {
                            ArrayList<Integer> possible = new ArrayList<Integer>();
                            for (int i = 0; i < candidates[y][x].length; i++) {
                                if (candidates[y][x][i])
                                    possible.add(i + 1);
                            }
                            int digit = 0;

                            // if a number can only go in one place within a row, put the number there
                            for (int i = 0; i < possible.size(); i++) {
                                boolean rowHasSameCandidate = false;
                                for (int j = 0; j < board[y].length; j++) {
                                    //another one in same row has same candidate
                                    if (board[y][j] != 0)
                                        continue;
                                    if (j != x && candidates[y][j][possible.get(i) - 1])
                                        rowHasSameCandidate = true;
                                }
                                if (!rowHasSameCandidate) {
                                    digit = possible.get(i);
                                    break;
                                }
                            }

                            // if a number can only go in one place within a column, put the number there
                            for (int i = 0; i < possible.size(); i++) {
                                boolean colHasSameCandidate = false;
                                for (int j = 0; j < board.length; j++) {
                                    //another one in same col has same candidate
                                    if (board[j][x] != 0)
                                        continue;
                                    if (j != y && candidates[j][x][possible.get(i) - 1])
                                        colHasSameCandidate = true;
                                }
                                if (!colHasSameCandidate) {
                                    digit = possible.get(i);
                                    break;
                                }
                            }

                            // if a number can only go in one place within a square, put the number there
                            for (int i = 0; i < possible.size(); i++) {
                                boolean squareHasSameCandidate = false;
                                for (int j = 0; j < board.length; j++) {
                                    //another one in same square has same candidate
                                    int squareX = x / 3;
                                    int squareY = y / 3;
                                    if (board[3 * squareY + j / 3][3 * squareX + j % 3] != 0)
                                        continue;
                                    if (!(3 * squareY + j / 3 == y && 3 * squareX + j % 3 == x) && candidates[3 * squareY + j / 3][3 * squareX + j % 3][possible.get(i) - 1])
                                        squareHasSameCandidate = true;
                                }
                                if (!squareHasSameCandidate) {
                                    digit = possible.get(i);
                                    break;
                                }
                            }
                            if (digit != 0)
                                newBoard[y][x] = digit;
                        }
                    }
                }

                // if within a square, a number must be in a certain row or column, remove that candidate number from other boxes in the same row or column
                for (int squareY = 0; squareY < board.length; squareY += 3) {
                    for (int squareX = 0; squareX < board[squareY].length; squareX += 3) {
                        for (int i = 0; i < board.length; i++) {
                            ArrayList<Integer> rows = new ArrayList<Integer>();
                            ArrayList<Integer> cols = new ArrayList<Integer>();
                            for (int y = squareY; y < squareY + 3; y++) {
                                for (int x = squareX; x < squareX + 3; x++) {
                                    if (board[y][x] == 0 && candidates[y][x][i]) {
                                        rows.add(y);
                                        cols.add(x);
                                    }
                                }
                            }
                            // are all integers in rows the same
                            boolean sameRow = false;
                            if (rows.size() >= 2) {
                                sameRow = true;
                                for (int j = 1; j < rows.size(); j++) {
                                    if (rows.get(j) != rows.get(0))
                                        sameRow = false;
                                }
                            }
                            // are all integers in cols the same
                            boolean sameCol = false;
                            if (cols.size() >= 2) {
                                sameCol = true;
                                for (int j = 1; j < cols.size(); j++) {
                                    if (cols.get(j) != cols.get(0))
                                        sameCol = false;
                                }
                            }
                            //
                            if (sameRow) {
                                for (int c : cols)
                                    sameRowCol[rows.get(0)][c][i] = true;
                                for (int j = 0; j < board[0].length; j++) {
                                    if (!cols.contains(j))
                                        candidates[rows.get(0)][j][i] = false;
                                }
                            }
                            if (sameCol) {
                                for (int r : rows)
                                    sameRowCol[r][cols.get(0)][i] = true;
                                for (int j = 0; j < board.length; j++) {
                                    if (!rows.contains(j))
                                        candidates[j][cols.get(0)][i] = false;
                                }
                            }
                        }
                    }
                }

                // if within a row, a number must be in a certain square, remove that candidate number from other boxes in the same square
                for (int y = 0; y < board.length; y++) {
                    for (int i = 0; i < board.length; i++) {
                        ArrayList<Integer> squares = new ArrayList<Integer>();
                        ArrayList<Integer> cols = new ArrayList<Integer>();
                        for (int x = 0; x < board[y].length; x++) {
                            if (board[y][x] == 0 && candidates[y][x][i]) {
                                int squareX = x / 3;
                                int squareY = y / 3;
                                squares.add(3 * squareY + squareX);
                                cols.add(x);
                            }
                        }
                        // are all integers in squares the same
                        boolean sameSquare = false;
                        if (squares.size() >= 2) {
                            sameSquare = true;
                            for (int j = 1; j < squares.size(); j++) {
                                if (squares.get(j) != squares.get(0))
                                    sameSquare = false;
                            }
                        }
                        if (sameSquare) {
                            for (int c : cols)
                                sameRowCol[y][c][i] = true;
                            int squareX = 3 * (squares.get(0) % 3);
                            int squareY = 3 * (squares.get(0) / 3);
                            for (int j = 0; j < board.length; j++) {
                                if (y != squareY + j / 3)
                                    candidates[squareY + j / 3][squareX + j % 3][i] = false;
                            }
                        }
                    }
                }

                // if within a column, a number must be in a certain square, remove that candidate number from other boxes in the same square
                for (int x = 0; x < board[0].length; x++) {
                    for (int i = 0; i < board.length; i++) {
                        ArrayList<Integer> squares = new ArrayList<Integer>();
                        ArrayList<Integer> rows = new ArrayList<Integer>();
                        for (int y = 0; y < board.length; y++) {
                            if (board[y][x] == 0 && candidates[y][x][i]) {
                                int squareX = x / 3;
                                int squareY = y / 3;
                                squares.add(3 * squareY + squareX);
                                rows.add(y);
                            }
                        }
                        // are all integers in squares the same
                        boolean sameSquare = false;
                        if (squares.size() >= 2) {
                            sameSquare = true;
                            for (int j = 1; j < squares.size(); j++) {
                                if (squares.get(j) != squares.get(0))
                                    sameSquare = false;
                            }
                        }
                        if (sameSquare) {
                            for (int r : rows)
                                sameRowCol[r][x][i] = true;
                            int squareX = 3 * (squares.get(0) % 3);
                            int squareY = 3 * (squares.get(0) / 3);
                            for (int j = 0; j < board.length; j++) {
                                if (x != squareX + j % 3)
                                    candidates[squareY + j / 3][squareX + j % 3][i] = false;
                            }
                        }
                    }
                }

                // if within a square, there are 2 places that can only be 2 numbers, etc, eliminate all other candidates in those squares
                for (int squareY = 0; squareY < board.length; squareY += 3) {
                    for (int squareX = 0; squareX < board[squareY].length; squareX += 3) {
                        // possibilities[i][j] true if location j in the square has candidate i
                        boolean[][] possibilities = new boolean[board.length][board.length];
                        for (int i = 0; i < board.length; i++) {
                            for (int y = squareY; y < squareY + 3; y++) {
                                for (int x = squareX; x < squareX + 3; x++) {
                                    if (board[y][x] == 0 && candidates[y][x][i])
                                        possibilities[i][(y - squareY) * 3 + (x - squareX)] = true;
                                }
                            }
                        }
                        // i is the candidate value we are looking at
                        for (int i = 0; i < board.length; i++) {
                            //count the number of locations where this candidate value appears
                            int countLocations = 0;
                            for (int j = 0; j < board.length; j++) {
                                if (possibilities[i][j])
                                    countLocations++;
                            }
                            if (countLocations > 0) {
                                // count number of other candidates that have the same set of locations as this candidate
                                int countSame = 1; // 1 to include this current candidate in the count

                                ArrayList<Integer> numbers = new ArrayList<Integer>();
                                numbers.add(i);
                                ArrayList<Integer> sameLocations = new ArrayList<Integer>();
                                for (int j = 0; j < board.length; j++) {
                                    if (possibilities[i][j])
                                        sameLocations.add(j);
                                }

                                for (int j = 0; j < board.length; j++) {
                                    if (i == j)
                                        continue;
                                    // true if candidate i and candidate j have same set of locations
                                    boolean same = true;
                                    for (int k = 0; k < board.length; k++) {
                                        if (possibilities[j][k] != possibilities[i][k])
                                            same = false;
                                    }
                                    if (same) {
                                        numbers.add(j);
                                        countSame++;
                                    }
                                }
                                if (countSame == countLocations) {
                                    // eliminate all other candidates for these squares
                                    for (int j = 0; j < sameLocations.size(); j++) {
                                        int locX = sameLocations.get(j) % 3;
                                        int locY = sameLocations.get(j) / 3;
                                        for (int k = 0; k < board.length; k++) {
                                            // if candidate not in list of numbers, remove it from candidates
                                            if (!numbers.contains(k))
                                                candidates[squareY + locY][squareX + locX][k] = false;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // within a square, if there are 2 places that could only be the same 2 numbers, remove those numbers as candidates from the rest of the square
                for (int squareY = 0; squareY < board.length; squareY += 3) {
                    for (int squareX = 0; squareX < board[squareY].length; squareX += 3) {
                        ArrayList<ArrayList<Integer>> candidatesForSquare = new ArrayList<ArrayList<Integer>>();
                        for (int y = squareY; y < squareY + 3; y++) {
                            for (int x = squareX; x < squareX + 3; x++) {
                                ArrayList<Integer> candidatesForXY = new ArrayList<Integer>();
                                for (int i = 0; i < candidates[y][x].length; i++) {
                                    if (candidates[y][x][i])
                                        candidatesForXY.add(i);
                                }
                                if (board[y][x] == 0)
                                    candidatesForSquare.add(candidatesForXY);
                                else
                                    candidatesForSquare.add(null);
                            }
                        }
                        for (int i = 0; i < candidatesForSquare.size(); i++) {
                            if (candidatesForSquare.get(i) == null)
                                continue;
                            // i is index of box within square
                            // count number of boxes in square with same candidates as box i
                            int countSame = 1;
                            ArrayList<Integer> indices = new ArrayList<Integer>();
                            indices.add(i);
                            for (int j = 0; j < candidatesForSquare.size(); j++) {
                                if (candidatesForSquare.get(j) == null)
                                    continue;
                                if (i != j && candidatesForSquare.get(i).equals(candidatesForSquare.get(j))) {
                                    countSame++;
                                    indices.add(j);
                                }
                            }
                            // count how many candidates box i has
                            int countCandidates = candidatesForSquare.get(indices.get(0)).size();
                            if (countSame == countCandidates) {
                                // remove all of these candidates from all other boxes in this square
                                for (int y = squareY; y < squareY + 3; y++) {
                                    for (int x = squareX; x < squareX + 3; x++) {
                                        int index = 3 * (y - squareY) + (x - squareX);
                                        if (!indices.contains(index)) {
                                            for (int j = 0; j < candidatesForSquare.get(i).size(); j++) {
                                                candidates[y][x][candidatesForSquare.get(i).get(j)] = false;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                //same as last two but for rows and columns

                // apply all updates to board
                for (int y = 0; y < board.length; y++) {
                    for (int x = 0; x < board[y].length; x++) {
                        if (newBoard[y][x] != board[y][x]) {
                            board[y][x] = newBoard[y][x];
                            updateCandidates(x, y, board[y][x]);
                        }
                    }
                }
                iterations++;
            }
        }

        //reset key states
        if(keyReleased)
            keyReleased = false;
        if(keyTyped)
            keyTyped = false;
    }

    private void render()
    {
        //sets up graphics
        BufferStrategy bs = getBufferStrategy();
        if(bs == null)
        {
            createBufferStrategy(3);
            return;
        }
        Graphics g = bs.getDrawGraphics();
        g.translate(8, 31);
        g.setFont(g.getFont().deriveFont(Font.PLAIN).deriveFont(30f));
        FontMetrics fontmetrics = g.getFontMetrics(g.getFont());

        //draws pixel array to screen
        g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);

        //draw other objects on screen
        for(int y = 0; y < 9; y++) {
            for(int x = 0; x < 9; x++) {
                if(x == xSelected && y == ySelected)
                    g.setColor(new Color(160, 160, 0));
                /*else if(mouseX >= 50 + 50 * x && mouseX < 100 + 50 * x && mouseY >= 50 + 50 * y && mouseY < 100 + 50 * y)
                    g.setColor(new Color(96, 96, 96));*/
                else if(initial[y][x] != 0)
                    g.setColor(new Color(128, 128, 128));
                else
                    g.setColor(new Color(160, 160, 160));
                g.fillRect(50 + 50 * x, 50 + 50 * y, 50, 50);
                g.setColor(new Color(0, 0, 0));
                g.drawRect(50 + 50 * x, 50 + 50 * y, 50, 50);
                if(board[y][x] > 0) {
                    //if(board[y][x] == solution[y][x])
                        g.setColor(new Color(0, 0, 0));
                    //else
                        //g.setColor(new Color(255, 0, 0));
                    String text = "" + board[y][x];
                    int textX = 75 + 50 * x - fontmetrics.stringWidth(text)/2;
                    int textY = 75 + 50 * y - fontmetrics.getHeight()/2 + fontmetrics.getAscent();
                    g.drawString("" + board[y][x], textX,textY);
                }
                else if(showCandidates) {
                    for(int i = 0; i < candidates[y][x].length; i++) {
                        if(candidates[y][x][i]) {
                            String text = "" + (i + 1);
                            g.setFont(g.getFont().deriveFont(Font.PLAIN).deriveFont(10f));
                            fontmetrics = g.getFontMetrics(g.getFont());
                            int xoff = i % 3 * 17 + 9;
                            int yoff = i / 3 * 17 + 9;
                            int textX = 50 + xoff + 50 * x - fontmetrics.stringWidth(text) / 2;
                            int textY = 50 + yoff + 50 * y - fontmetrics.getHeight() / 2 + fontmetrics.getAscent();
                            if(highlightCandidates && sameRowCol[y][x][i]) {
                                g.setColor(new Color(160, 0, 0));
                                g.fillRect(43 + xoff + 50 * x, 43 + yoff + 50 * y, 14, 14);
                            }
                            g.setColor(new Color(96, 96, 96));
                            g.drawString("" + (i + 1), textX, textY);
                            g.setFont(g.getFont().deriveFont(Font.PLAIN).deriveFont(30f));
                            fontmetrics = g.getFontMetrics(g.getFont());
                        }
                    }
                }
            }
        }
        for(int i = 0; i < 4; i++) {
            g.fillRect(48 + 150 * i, 50, 4, 450);
            g.fillRect(50, 48 + 150 * i, 450, 4);
        }

        //display all the graphics
        bs.show();
    }

    public void run()
    {
        //main program loop
        long lastTime = System.nanoTime();
        final double ns = 1000000000.0 / 60.0; //60 times per second
        double delta = 0;
        requestFocus();
        while(running)
        {
            //updates time
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            frame++;
            while(delta >= 1) //Make sure update is only happening 60 times a second
            {
                //update
                update();
                delta--;
            }
            //display to the screen
            render();
        }
    }

    private int RGB(int r, int g, int b)
    {
        return r << 16 | g << 8 | b;
    }

    private int R(int color)
    {
        return color >> 16;
    }

    private int G(int color)
    {
        return color >> 8 & 255;
    }

    private int B(int color)
    {
        return color & 255;
    }

    public void mouseClicked(MouseEvent me)
    {

    }

    public void mouseEntered(MouseEvent me)
    {

    }

    public void mouseExited(MouseEvent me)
    {

    }

    public void mousePressed(MouseEvent me)
    {
        mousePressed = true;
    }

    public void mouseReleased(MouseEvent me)
    {
        mousePressed = false;
    }

    public void mouseDragged(MouseEvent me)
    {
        mousePressed = true;
        mouseX = me.getX() - 8;
        mouseY = me.getY() - 31;
    }

    public void mouseMoved(MouseEvent me)
    {
        mousePressed = false;
        mouseX = me.getX() - 8;
        mouseY = me.getY() - 31;
    }

    public void keyPressed(KeyEvent key)
    {
        keyPressed = !keyTyped;
        this.key = key;
    }

    public void keyReleased(KeyEvent key)
    {
        keyPressed = false;
        keyReleased = true;
        this.key = key;
    }

    public void keyTyped(KeyEvent key)
    {
        keyTyped = true;
    }

    private boolean sameInRow(int x, int y, int digit) {
        for(int i = 0; i < board[y].length; i++) {
            if(board[y][i] == digit)
                return true;
        }
        return false;
    }

    private boolean sameInCol(int x, int y, int digit) {
        for(int i = 0; i < board.length; i++) {
            if(board[i][x] == digit)
                return true;
        }
        return false;
    }

    private boolean sameInSquare(int x, int y, int digit) {
        int squareX = x / 3;
        int squareY = y / 3;
        for(int i = 0; i < board.length; i++) {
            if(board[3 * squareY + i / 3][3 * squareX + i % 3] == digit)
                return true;
        }
        return false;
    }

    private void updateCandidates(int x, int y, int value) {
        if(value > 0) {
            for (int i = 0; i < board[y].length; i++) {
                candidates[y][i][value - 1] = false;
            }
            for (int i = 0; i < board.length; i++) {
                candidates[i][x][value - 1] = false;
            }
            int squareX = x / 3;
            int squareY = y / 3;
            for (int i = 0; i < board.length; i++) {
                candidates[3 * squareY + i / 3][3 * squareX + i % 3][value - 1] = false;
            }
        }
        else {
            for (int c = 0; c < candidates[y].length; c++) {
                for (int i = 0; i < candidates[y][c].length; i++) {
                    candidates[y][c][i] = !(sameInRow(c, y, i + 1) || sameInCol(c, y, i + 1) || sameInSquare(c, y, i + 1));
                }
            }
            for(int r = 0; r < candidates.length; r++) {
                for (int i = 0; i < candidates[r][x].length; i++) {
                    candidates[r][x][i] = !(sameInRow(x, r, i + 1) || sameInCol(x, r, i + 1) || sameInSquare(x, r, i + 1));
                }
            }
            int squareX = x / 3;
            int squareY = y / 3;
            for(int i = 0; i < board.length; i++) {
                int r = 3 * squareY + i / 3;
                int c = 3 * squareX + i % 3;
                for (int j = 0; j < candidates[y][x].length; j++) {
                    candidates[r][c][j] = !(sameInRow(c, r, j + 1) || sameInCol(c, r, j + 1) || sameInSquare(c, r, j + 1));
                }
            }
        }
    }

    private boolean valid(int[][] board) {
        for(int y = 0; y < board.length; y++) {
            for (int x = 0; x < board[y].length; x++) {
                for (int x2 = 0; x2 < board[y].length; x2++) {
                    if (x != x2 && board[y][x] != 0 && board[y][x] == board[y][x2])
                        return false;
                }
            }
        }
        for(int x = 0; x < board[0].length; x++) {
            for (int y = 0; y < board.length; y++) {
                for (int y2 = 0; y2 < board.length; y2++) {
                    if (y != y2 && board[y][x] != 0 && board[y][x] == board[y2][x])
                        return false;
                }
            }
        }
        for (int squareY = 0; squareY < board.length; squareY += 3) {
            for (int squareX = 0; squareX < board[squareY].length; squareX += 3) {
                for (int y = squareY; y < squareY + 3; y++) {
                    for (int x = squareX; x < squareX + 3; x++) {
                        for (int y2 = squareY; y2 < squareY + 3; y2++) {
                            for (int x2 = squareX; x2 < squareX + 3; x2++) {
                                if(!(x == x2 && y == y2) && board[y][x] != 0 && board[y][x] == board[y2][x2])
                                    return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    public static void main(String [] args)
    {
        Sodoku s = new Sodoku();
    }
}