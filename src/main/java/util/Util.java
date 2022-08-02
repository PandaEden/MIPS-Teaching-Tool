package util;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Scanner;

public class Util {
	public static boolean wait=true;
	
	/** Min and Max Inclusive */
	public static boolean notNullAndInRange (Integer val, int min, int max) {
		if ( max<min )
			throw new IllegalArgumentException( "Max " + max + " is larger than Min " + min + " What?" );
		
		return (val!=null && val>=min && val<=max);
	}
	
	public static boolean isNullOrBlank (@Nullable String s){ return (s==null || s.isBlank()); }
	
	public static String input(@Nullable String msg){
		if (msg==null){
			msg = "Press ENTER to continue . . .";
		}
		return Input.getInstance().getInput( msg );
	}
	public static String input(){
		return input( null );
	}
	
	@VisibleForTesting
	public static void overrideScanner( String input_text ){
		Input.getInstance().overrideScannerInstance( input_text );
	}
	
}
class Input {
	private static Scanner scanner;
	private static Input input=null;
	private Input (Scanner scanner) {
		Input.scanner=scanner;
	}
	
	public static Input getInstance ( ) {
		if ( input==null )
			input=new Input( new Scanner( System.in ) );
		
		return input;
	}
	
	@VisibleForTesting
	public void overrideScannerInstance (String input_text) {
		input = new Input( new Scanner( input_text ) );
	}
	
	public String getInput(String prompt){
		System.out.println( prompt );
		while ( !scanner.hasNextLine() ){
			scanner.nextLine();
		}
		return scanner.nextLine();
	}
}
