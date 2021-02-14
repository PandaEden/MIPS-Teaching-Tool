package model;

public class Color {
	
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";
	
	public static void reset(){System.out.print(ANSI_RESET);}
	public static void setAnsiBlack(){System.out.print(ANSI_BLACK);}
	public static void setAnsiRed(){System.out.print(ANSI_RED);}
	public static void setAnsiGreen(){System.out.print(ANSI_GREEN);}
	public static void setAnsiYellow(){System.out.print(ANSI_YELLOW);}
	public static void setAnsiBlue(){System.out.print(ANSI_BLUE);}
	public static void setAnsiPurple(){System.out.print(ANSI_PURPLE);}
	public static void setAnsiCyan(){System.out.print(ANSI_CYAN);}
	public static void setAnsiWhite(){System.out.print(ANSI_WHITE);}
}
