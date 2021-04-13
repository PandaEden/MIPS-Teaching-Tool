package util.ansi_codes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.List;

public class Color {    // Static Class Constructor preventing 100% Coverage
	// BLACK -> RED -> GREEN -> YELLOW
	public static final String BLACK="\u001B[30m";
	public static final String RED="\u001B[31m";    //Reserve for Error Log
	public static final String GREEN="\u001B[32m";    // - Read
	public static final String YELLOW="\u001B[33m";    //Reserve for Warnings Log
	public static final String BLUE="\u001B[34m";    // - Write
	// BLUE -> MAGENTA -> CYAN -> WHITE
	public static final String MAGENTA="\u001B[35m";    // DataManager
	public static final String CYAN="\u001B[36m";    // RegisterBank
	public static final String WHITE="\u001B[37m";
	public static final String RESET="\u001B[0m";
	
	public static final String ERR_LOG=bold( RED);
	public static final String WARN_LOG=bold( YELLOW);
	public static final String RB_READ=GREEN;
	public static final String RB_WRITE=BLUE;
	public static final String DATA_READ=GREEN;
	public static final String DATA_WRITE=BLUE;
	public static final String DM=underline(reverse(BLUE));
	public static final String RB=underline(reverse(GREEN));
	
	public static boolean colorSupport=true;
	private static int nextColCounter=0;
	
	/**Only used for testing*/
	@VisibleForTesting
	public static String next ( ) {    // Excluding Purple & Black
		List<String> temp=List.of( RED, BLUE, GREEN, YELLOW, CYAN, WHITE, bold( BLACK),
								   bold( RED), bold( BLUE), bold( GREEN), bold( YELLOW),
								   bold( CYAN), bold( WHITE), RESET );
		return temp.get( nextColCounter++%temp.size( ) );
	}
	
	@NotNull
	public static String fmt (@NotNull String ansi, @NotNull String string) {
		return string.isBlank( ) ? string :
			   ((Color.colorSupport) ? (ansi + string + RESET) : string);
	}
	
	/**ANSI Color Code Modifier, BackGround*/
	public static String bkg(String color){
		return color.substring(0,2)+"4"+color.substring(3);//Expects: Esc,[,3,#,m
	}
	/**ANSI Color Code Modifier, BackGround*/
	public static String alt(String color){
		return color.substring(0,2)+"9"+color.substring(3);//Expects: Esc,[,3,#,m
	}
	/**ANSI Color Code Modifier, BackGround*/
	public static String alt_bkg(String color){
		return color.substring(0,2)+"10"+color.substring(3);//Expects: Esc,[,3,#,m
	}
	/**ANSI Color Code Modifier, Bright/Bold*/
	public static String bold(String color){
		return color.substring( 0, color.length()-1 )+";1m";
	}
	/**ANSI Color Code Modifier, Underline*/
	public static String underline(String color){
		return color.substring( 0, color.length()-1 )+";4m";
	}
	/**ANSI Color Code Modifier, Reverses*/
	public static String reverse(String color){
		return color.substring( 0, color.length()-1 )+";7m";
	}
	
	// Test Print Ascii codes
	public static void main (String[] args) {
		
		for ( int i=0; i<8; i++ ) {
			System.out.print( " \u001b[3"+i+"m A"+RESET );
			System.out.print( " \u001b[4"+i+"m A"+RESET );
			System.out.print( " \u001b[9"+i+"m A"+RESET );
			System.out.print( " \u001b[10"+i+"m A"+RESET );
			System.out.println( " " );
		}
		System.out.println( " " );
		
		for ( int j=0; j<4; j++ ){
			for ( int i=0; i<8; i++ ) {
				System.out.print( " \u001b[3"+i+"m A"+RESET );
				System.out.print( " \u001b[4"+i+"m A"+RESET );
				System.out.print( " \u001b[9"+i+"m A"+RESET );
				System.out.print( " \u001b[10"+i+"m A"+RESET );
				System.out.println( " " );
			}
		}
	}
}
