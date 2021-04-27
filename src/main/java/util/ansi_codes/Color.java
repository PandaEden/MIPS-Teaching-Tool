package util.ansi_codes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.List;

public class Color {    // Static Class Constructor preventing 100% Coverage
	private static final String CSI="\033["; // Control Sequence Introducer "ESC ["
	//ESC{literal:\e octal:\033 hex:\x1b unicode:\u001b}
	private static String csi(String code){ return CSI + code + "m"; }
	public static String csi(int code){ return CSI + code + "m"; }
	// BLACK -> RED -> GREEN -> YELLOW
	public static final int BLACK=30;
	public static final int RED=31;    //Reserve for Error Log
	public static final int GREEN=32;    // - Read
	public static final int YELLOW=33;    //Reserve for Warnings Log
	public static final int BLUE=34;    // - Write
	// BLUE -> MAGENTA -> CYAN -> WHITE
	public static final int MAGENTA=35;
	public static final int CYAN=36;
	public static final int WHITE=37;
	public static final String RESET=csi(0);
	
	public static final String ERR_LOG=csi("97;41;4");
	public static final String WARN_LOG=bold(underline(csi(YELLOW)));
	public static final String READ=csi(bright(GREEN));
	public static final String WRITE=csi(bright(CYAN));
	
	public static boolean colorSupport=true;
	private static int nextColCounter=0;
	
	/**Only used for testing*/
	@VisibleForTesting
	public static String next ( ) {    // Excluding Purple & Black
		List<Integer> temp=List.of( RED, BLUE, GREEN, YELLOW, CYAN, WHITE, bright( BLACK),
								   bright( RED), bright( BLUE), bright( GREEN), bright( YELLOW),
								   bright( CYAN), bright( WHITE));
		return csi(temp.get( nextColCounter++%temp.size( ) ));
	}
	
	@NotNull
	public static String fmt (@NotNull String ansi, @NotNull String string) {
		return string.isBlank( ) ? string :
			   ((Color.colorSupport) ? (ansi + string + RESET) : string);
	}
	public static String fmt (int color, @NotNull String string) {
		return string.isBlank( ) ? string :
			   ((colorSupport) ? (csi( color ) + string + RESET) : string);
	}
	public static String fmtTitle(int color, String txt){
		return fmt(doubleUnderline( Color.bold(Color.csi(color))), txt );
	}
	public static String fmtSubTitle(int color, String txt){
		return fmt(underline(csi(color+";"+(bkg(BLACK)))), txt );
	}
	public static String fmtCmd(String txt){
		return fmt( csi("107;40;4"), txt );
	}
	public static String fmtUnder(String txt){
		return Color.fmt(Color.underline( Color.RESET ), txt );
	}
	
	// Bright & Bold are often displayed the same
	
	/**ANSI Color Code Modifier: BackGround*/
	public static int bkg(int color){
		return color+10;
	}
	/**ANSI Color Code Modifier: Bright*/
	public static int bright(int color){
		return color+60;
	}
	/**ANSI Color Code Modifier: Bold, Same as Bright on some Terminals, Expects Input: Esc[3#m*/
	public static String bold(String color){
		return color.substring( 0, color.length()-1 )+";1m";
	}
	/**ANSI Color Code Modifier: Underline, Expects Input: Esc[3#m*/
	public static String underline(String color){
		return color.substring( 0, color.length()-1 )+";4m";
	}
	/**ANSI Color Code Modifier: Double-Underline, Expects Input: Esc[3#m*/
	public static String doubleUnderline(String color){
		return color.substring( 0, color.length()-1 )+";21m";
	}
	/**ANSI Color Code Modifier: Reverse, Expects Input: Esc[3#m*/
	public static String reverse(String color){
		return color.substring( 0, color.length()-1 )+";7m";
	}
	
	// Test Print Ascii codes
	public static void main (String[] args) {
		for ( int i=0; i<8; i++ ) {
			for ( int j=0; j<2; j++ ) {
				int num=30 + i;
				String bkg_code=csi( bkg( num ) );
				if ( j==1 )
					num=bright( num );
				
				String code=csi( num );
				System.out.print( num + "\t" );
				System.out.print( fmt( code, " A" ) );
				System.out.print( fmt( bold( code ), " A" ) + "  " );
				System.out.print( fmt( underline( code ), " A" ) + " " );
				System.out.print( fmt( doubleUnderline( code ), " A" ) + " " );
				
				System.out.print( fmt( reverse( code ), " A" ) + " " );
				System.out.print( fmt( reverse( bold( doubleUnderline( code ) ) ), " A" ) + " " );
				
				System.out.print( bkg( num ) + "\t" );
				System.out.print( fmt( bold( doubleUnderline( bkg_code ) ), " A" ) + " " );
				System.out.print( fmt( reverse( bold( doubleUnderline( bkg_code ) ) ), " A" ) + "\n" );
			}
		}
		System.out.print( fmt( ERR_LOG, " ERR" ) + fmt( WARN_LOG, " WARN" )+"\n");
		System.out.print( fmtTitle( RED,"Title" )+fmtUnder( "Under" )+"\n");
		System.out.print( fmtSubTitle( RED,"SubTitle" ));
	}
}
