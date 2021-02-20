package Util.Logs;

import java.util.ArrayList;

/**
 Provides logging capacity for the application.
 */
public class Logger{
	private String name;
	private ArrayList<String> logs;
	
	public Logger(String name, ArrayList<String> logs){}
	
	/**
	 Adds the message to the log, null will be ignored
	 */
	public void append(String msg){
	
	}
	
	/**
	 Use with <b>System.out.print()</b>
	 @return a String with the {@link #name}:, then tab indented contents of {@link #logs} and a newline.
	 or an empty string "" if logs is empty
	 */
	@Override
	public String toString(){
		return null;
	}
	
	/**
	 Clears the {@link #logs}.
	 */
	public void clear(){
	
	}
	
	/**
	 @return if {@link #logs} is not empty.
	 */
	public boolean hasEntries(){
		return false;
	}
	
	public static class Color {
		public static boolean colorSupport=false;
		private static String ifSupported(String ansi_color){ return colorSupport?ansi_color:""; }
		public static String formatColored(String ansi, String string){ return ansi+string+ANSI_RESET; }
		
		public static final String ANSI_RESET = "\u001B[0m";
		public static final String BLACK_ANSI = "\u001B[30m";
		public static final String RED_ANSI = "\u001B[31m";		//Reserve for Error Log
		public static final String GREEN_ANSI = "\u001B[32m";
		public static final String YELLOW_ANSI = "\u001B[33m";	//Reserve for Warnings Log
		public static final String BLUE_ANSI = "\u001B[34m";
		public static final String PURPLE_ANSI = "\u001B[35m";
		public static final String CYAN_ANSI = "\u001B[36m";
		public static final String WHITE_ANSI = "\u001B[37m";
		
		public static void reset(){System.out.print(ifSupported(ANSI_RESET));}
		public static void setAnsiBlack(){System.out.print(ifSupported(BLACK_ANSI));}
		public static void setAnsiRed(){System.out.print(ifSupported(RED_ANSI));}
		public static void setAnsiGreen(){System.out.print(ifSupported(GREEN_ANSI));}
		public static void setAnsiYellow(){System.out.print(ifSupported(YELLOW_ANSI));}
		public static void setAnsiBlue(){System.out.print(ifSupported(BLUE_ANSI));}
		public static void setAnsiPurple(){System.out.print(ifSupported(PURPLE_ANSI));}
		public static void setAnsiCyan(){System.out.print(ifSupported(CYAN_ANSI));}
		public static void setAnsiWhite(){System.out.print(ifSupported(WHITE_ANSI));}
	}
}
