package com.chandrawansha.shavin.battleship;

import java.util.Scanner;

public class BattleShip {

    public static void main(String[] args) {

        // create scanner
        Scanner scanner = new Scanner(System.in);

        // create two players
        BattleShipGrid player1 = new BattleShipGrid("Player 1");
        BattleShipGrid player2 = new BattleShipGrid("Player 2");

        for (BattleShipGrid grid : new BattleShipGrid[] {player1, player2}) {
            System.out.printf("%s, place your ships on the game field%n%n", grid.getPlayerName());

            for (int i = 0; i < BattleShipGrid.shipLengths.length; i++) {

                grid.displayGrid();
                System.out.printf("%nEnter the coordinates of the %s (%d cells):%n%n",
                        BattleShipGrid.shipNames[i], BattleShipGrid.shipLengths[i]);

                String input = scanner.nextLine();

                while (true) {
                    try {
                        if (grid.isCompatible(input,i)) {
                            break;
                        }
                    } catch (IllegalArgumentException exp) {
                        System.out.printf("%n%s Try again:%n%n", exp.getMessage());
                        input = scanner.nextLine();
                    }
                }
                grid.placeShip(new Range(input)); // set the ship in the grid
                grid.addShipLocation(new Range(input),i);

            }

            System.out.println();
            grid.displayGrid();

            System.out.printf("%nPress Enter and pass the move to another player");
            scanner.nextLine();
            System.out.println("...");

        }

        GamePlayer gamePlayer = new GamePlayer(player1, player2, scanner);
        gamePlayer.play();

    }
}

class GamePlayer {

    private BattleShipGrid player1;
    private BattleShipGrid player2;
    private Scanner scanner;

    public GamePlayer(BattleShipGrid player1, BattleShipGrid player2, Scanner scanner) {
        this.player1 = player1;
        this.player2 = player2;
        this.scanner = scanner;
    }

    public void play() {

        BattleShipGrid attacker = player1;
        BattleShipGrid hider = player2;

        while (!player1.isFinished() && !player2.isFinished()) {

            display(hider);
            System.out.printf("%n%s, it's your turn:%n%n", attacker.getPlayerName());
            // get the input
            String input = scanner.nextLine();
            Point point = null;

            while (true) {
                try {
                    point = new Point(input);
                    break;
                } catch (IllegalArgumentException exp) {
                    System.out.printf("%n%s%n%n", exp.getMessage());
                }
                input = scanner.nextLine();
            }

            String status = hider.shot(point);

            if (hider.isFinished()) {
                System.out.printf("%n%s", status);
                break;
            } else {
                if (!status.equals("you hit a ship!") && !status.equals("You sank a ship!")) {
                    // swap the players
                    BattleShipGrid temp = attacker;
                    attacker = hider;
                    hider = temp;

                    System.out.printf("%n%s%n", status);
                    System.out.println("Press Enter and pass the move to another player");
                    scanner.nextLine();
                    System.out.println("...");
                } else {
                    System.out.printf("%n%s%n%n", status);
                }


            }


        }

        System.out.println("\nYou sank the last ship. You won. Congratulations!");

    }

    public void display(BattleShipGrid hidePlayer) {

        System.out.println();
        hidePlayer.displayGridWithFog();

        System.out.println("----------------------");
        if (hidePlayer == player1) {
            player2.displayGrid();
        } else {
            player1.displayGrid();
        }
    }

}

class BattleShipGrid {

    public final static int SIZE = 10;
    public final static int[] shipLengths = new int[] {
            5, 4, 3, 3, 2
    };

    public final static String[] shipNames = new String[] {
            "Aircraft Carrier",
            "Battleship",
            "Submarine",
            "Cruiser",
            "Destroyer"
    };

    private String playerName;
    private final char[][] grid = new char[SIZE][SIZE];
    private Range[] shipLocations;

    public BattleShipGrid(String playerName) {
        this.playerName = playerName;

        // fill the grid
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                grid[i][j] = '~';
            }
        }

        shipLocations = new Range[shipNames.length];
    }

    public void placeShip(Range range) {

        if (range.isHorizontal()) {
            for (int i = range.getMin(); i <= range.getMax(); i++) {
                grid[range.level()][i] = 'O';
            }
        } else {
            for (int i = range.getMin(); i <= range.getMax(); i++) {
                grid[i][range.level()] = 'O';
            }
        }

    }

    public void addShipLocation(Range range, int index) {
        shipLocations[index] = range;
    }

    public boolean isCompatible(String rangeStr, int shipIndex) {

        Range range = new Range(rangeStr);

        if (range.length() != shipLengths[shipIndex]) {
            throw new IllegalArgumentException(
                    String.format("Error! Wrong length of the %s!", shipNames[shipIndex]));
        }

        if (checkForTouches(range)) {
            throw  new IllegalArgumentException("Error! You placed it too close to another one.");
        }

        return true;

    }

    public boolean checkForTouches(Range range) {

        if (range.isHorizontal()) {
            for (int i = range.getMin(); i <= range.getMax(); i++) {
                if (isTouch(new Point(i, range.level()))) {
                    return true;
                }
            }
        } else {
            for (int i = range.getMin(); i <= range.getMax(); i++) {
                if (isTouch(new Point(range.level(), i))) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isTouch(Point point) {

        int[][] points = new int[][] {
                {point.x() - 1, point.y() - 1},
                {point.x() - 1, point.y()},
                {point.x(), point.y() - 1},
                {point.x() + 1, point.y() + 1},
                {point.x() + 1, point.y()},
                {point.x(), point.y() + 1}
        };

        for (int[] p : points) {
            if (p[0] < Point.MIN || p[1] < Point.MIN || p[0] > Point.MAX || p[1] > Point.MAX) {
                continue;
            }
            if (grid[p[1]][p[0]] == 'O') {
                return true;
            }
        }

        return false;

    }

    public String shot(Point point) {

        if (grid[point.y()][point.x()] == 'O' || grid[point.y()][point.x()] == 'X') {
            grid[point.y()][point.x()] = 'X';

            if (isFinished()) {
                return "You sank the last ship. You won. Congratulations!";
            }
            else if (isShipSank()) {
                return "You sank a ship!";
            }
            return "You hit a ship!";
        } else {
            grid[point.y()][point.x()] = 'M';
            return "You missed!";
        }
    }

    public boolean isShipSank() {

        for (int i = 0; i < shipLocations.length; i++) {

            if (shipLocations[i] != null) {

                Range range = shipLocations[i];
                int count = 0;

                for (int j = range.getMin(); j <= range.getMax(); j++) {
                    if (range.isHorizontal()) {
                        if (grid[range.level()][j] == 'X') {
                            count++;
                        }
                    } else {
                        if (grid[j][range.level()] == 'X') {
                            count++;
                        }
                    }
                }

                if (range.length() == count) {
                    shipLocations[i] = null;
                    return true;
                }

            }
        }
        return false;

    }

    public boolean isFinished() {

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (grid[i][j] == 'O') {
                    return false;
                }
            }
        }
        return true;

    }

    public void displayGrid() {

        System.out.print("  ");
        for (int i = 1; i <= SIZE; i++) {
            System.out.print(i + " ");
        }
        System.out.println();

        char c = 'A';
        for (int i = 0; i < SIZE; i++) {
            System.out.print(c++ + " ");
            for (int j = 0; j < SIZE; j++) {
                System.out.print(grid[i][j] + " ");
            }
            System.out.println();
        }

    }

    public void displayGridWithFog() {

        System.out.print("  ");
        for (int i = 1; i <= SIZE; i++) {
            System.out.print(i + " ");
        }
        System.out.println();

        char c = 'A';
        for (int i = 0; i < SIZE; i++) {
            System.out.print(c++ + " ");
            for (int j = 0; j < SIZE; j++) {
               if (grid[i][j] == 'O') {
                   System.out.print("~ ");
               } else {
                   System.out.print(grid[i][j] + " ");
               }
            }
            System.out.println();
        }
    }

    public String getPlayerName() {
        return playerName;
    }

}

class Range {

    private Point start;
    private Point end;

    public Range(Point p1, Point p2) throws IllegalArgumentException {
        if (p1.x() != p2.x() && p1.y() != p2.y()) {
            throw new IllegalArgumentException("Error! Wrong ship location!");
        }

        this.start = p1;
        this.end = p2;
    }

    public Range(String rangeStr) {;
        String[] splitPoints = rangeStr.split(" ");

        Point p1 = new Point(splitPoints[0]);
        Point p2 = new Point(splitPoints[1]);

        if (p1.x() != p2.x() && p1.y() != p2.y()) {
            throw new IllegalArgumentException("Error! Wrong ship location!");
        }

        this.start = p1;
        this.end = p2;
    }

    public boolean isHorizontal() {
        return start.y() == end.y();
    }

    public boolean isVertical() {
        return start.x() == end.x();
    }

    public int getMin() {
        if (isHorizontal()) {
            return Math.min(start.x(), end.x());
        }
        return Math.min(start.y(), end.y());
    }

    public int getMax() {
        if (isHorizontal()) {
            return Math.max(start.x(), end.x());
        }
        return Math.max(start.y(), end.y());
    }

    public int length() {
        if (isHorizontal()) {
            return Math.abs(start.x() - end.x()) + 1;
        }
        return Math.abs(start.y() - end.y()) + 1;
    }

    public int level() {
        if (isHorizontal()) {
            return start.y();
        } return start.x();
    }

}

class Point {

    private int x;
    private int y;

    public final static int MAX = 9;
    public final static int MIN = 0;

    public Point(int x, int y) {
        if (x > MAX || x < MIN || y > MAX || y < MIN) {
            throw new IllegalArgumentException("Error! You entered the wrong coordinates! Try again:");
        }

        this.x = x;
        this.y = y;
    }

    public Point(String pointStr) {
        try {
            int x = Integer.parseInt(pointStr.substring(1)) - 1;
            int y = pointStr.charAt(0) - 'A';

            if (x > MAX || x < MIN || y > MAX || y < MIN) {
                throw new IllegalArgumentException("Error! You entered the wrong coordinates! Try again:");
            }

            this.x = x;
            this.y = y;

        } catch (Exception exp) {
            throw new IllegalArgumentException("Error! You entered the wrong coordinates! Try again:");
        }

    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

}