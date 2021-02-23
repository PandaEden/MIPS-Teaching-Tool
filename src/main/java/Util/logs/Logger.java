package util.logs;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 Provides logging capacity for the application.
 Use with <b>System.out.print()</b>
 */
public class Logger{
	private final String name;
	private final ArrayList<String> logs;
	
	public Logger(@NotNull String name, @NotNull ArrayList<String> logs){
		this.name = name;
		this.logs = logs;
	}
	
	/**
	 Adds the message to the log, null/empty will be ignored
	 */
	public void append(@Nullable String msg){
		if (msg!=null && !msg.isBlank())
			this.logs.add(msg);
	}
	
	/*   IDEAS
	 Possible Append to last time method? , for adding things in steps,
	 (if not blank) strips the \n of the previous line, and then appends a new one
	
	 Indent Log - for using one log within another, It adds and extra Tab to EVERY LINE,
	 	 int indentionLevel - # of tabs to auto insert on each line
	 prefix()- adding something to the start of every line
	 suffix()- adding something to the end of every line, like a comma, period, or exclamation-point
	 final line suffix() -- replace the ending of the final line.  useful for commas on all lines except last.
	*/
	
	/**
	 Use with <b>System.out.print()</b>
	 
	 @return a String with the {@link #name}:, then tab indented contents of {@link #logs} and a newline.
	 or an empty string "" if logs is empty
	 */
	@Override
	public String toString(){
		StringBuilder rtn = new StringBuilder();
		if (!this.logs.isEmpty()) {
			rtn.append(this.name).append(":\n");
			for (String line : this.logs)
				rtn.append("\t").append(line).append("\n");
		}
		return rtn.toString();
	}
	
	/**
	 Clears the {@link #logs}.
	 */
	public void clear(){
		this.logs.clear();
	}
	
	/**
	 @return if {@link #logs} is not empty.
	 */
	public boolean hasEntries(){
		return !this.logs.isEmpty();
	}
	
	public static class Color{
		public static final String ANSI_RESET = "\u001B[0m";
		public static final String BLACK_ANSI = "\u001B[30m";
		public static final String RED_ANSI = "\u001B[31m";        //Reserve for Error Log
		public static final String GREEN_ANSI = "\u001B[32m";
		public static final String YELLOW_ANSI = "\u001B[33m";    //Reserve for Warnings Log
		public static final String BLUE_ANSI = "\u001B[34m";
		public static final String PURPLE_ANSI = "\u001B[35m";
		public static final String CYAN_ANSI = "\u001B[36m";
		public static final String WHITE_ANSI = "\u001B[37m";
		public static boolean colorSupport = false;
		
		@NotNull
		public static String formatColored(@NotNull String ansi, @NotNull String string){
			return (Color.colorSupport) ? (ansi+string+ANSI_RESET) : string;
		}
	}
}
