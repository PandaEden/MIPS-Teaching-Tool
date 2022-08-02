package util.logs;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import util.Util;

import java.util.ArrayList;

/**
 Provides logging capacity for the application.
 Use with <b>System.out.print()</b>
 */
public class Logger {
	protected String name;
	private final ArrayList<String> logs;
	private String prefix;
	
	public Logger (@NotNull String name, @NotNull ArrayList<String> logs) {
		this.name=name;
		this.logs=logs;
		clearPrefix( );
	}
	public String clearPrefix ( ) {
		return setPrefix( null );
	}
	/** Prefixes "prefix+"\t", returns existing prefix */
	public String setPrefix (String prefix) {
		String existing=this.prefix;
		this.prefix=prefix;
		return existing;
	}
	/** Adds the message to the log, null/empty will be ignored, Prefixes LineNo */
	public void append (int lineNo, @Nullable String msg) {
		setLineNoPrefix( lineNo );
		append( msg );
		clearPrefix( );
	}
	/** Prefixes "LineNo: " + lineNo + "\t", returns existing prefix */
	public String setLineNoPrefix (int lineNo) {
		return setPrefix( "LineNo: " + lineNo );
	}
	/** Adds the message to the log, null/empty will be ignored */
	public void append (@Nullable String msg) {
		if ( !Util.isNullOrBlank( msg ) )
			this.logs.add(
					((prefix!=null) ? prefix + "\t" : "") + msg );
	}
	
	public void appendNwLine(){
		logs.add( "" );
	}
	
	/** Adds the message to the log, null/empty will be ignored, Prefixes LineNo, Suffix '!' */
	public void appendEx (int lineNo, @Nullable String msg) {
		setLineNoPrefix( lineNo );
		appendEx( msg );
		clearPrefix( );
	}
	/** Adds the message to the log, null/empty will be ignored, Suffix '!' */
	public void appendEx (@Nullable String msg) {
		append( msg + "!" );
	}
	
	/* TODO   IDEAS
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
	public String toString ( ) {
		StringBuilder rtn=new StringBuilder( );
		if ( !this.logs.isEmpty( ) ) {
			rtn.append( this.name ).append( ":\n" );
			for ( String line : this.logs )
				rtn.append( "\t" ).append( line ).append( "\n" );
		}
		return rtn.toString( );
	}
	
	/** Clears the {@link #logs}. */
	public void clear ( ) {
		this.logs.clear( );
	}
	
	/** @return Whether the {@link #logs} are not empty. */
	public boolean hasEntries ( ) {
		return !this.logs.isEmpty( );
	}
	
	/** Shortcut for System.Out.Print(this).  Does <b>NOT</b> clear the log, use {@link #clear()} */
	public void println ( ) {
		System.out.print( this );
	}
	
	public String getName ( ) {
		return name;
	}
	
}
