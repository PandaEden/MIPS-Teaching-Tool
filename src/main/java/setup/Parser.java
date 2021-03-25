package setup;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import model.MemoryBuilder;
import model.components.DataMemory;
import model.components.InstrMemory;
import model.instr.Operands;

import util.Convert;
import util.validation.OperandsValidation;
import util.validation.Validate;
import util.logs.ErrorLog;
import util.logs.ExecutionLog;
import util.logs.WarningsLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 Responsible for parsing files containing MIPS instructions and building a model for the emulator to run using.
 <p>
 The setup.Parser has 3 main sections: File Checks: Instruction Format: Label assembly:
 <p>
 View the specification on the GitHub for more information of valid MIPS Syntax instructions.
 <p>
 The setup.Parser also logs mistakes with file syntax in an Error & Warnings log. If any Errors are present, these will
 need to
 be corrected before the emulator will run. Warnings will not prevent execution, and in some cases (e.g. 'nop') might be
 intended by the user.
 <p>
 The setup.Parser will attempt to collect as many errors as possible before terminating to allow the user to correct
 multiple
 mistakes before re-parsing.
 */
public class Parser {
	public static final String DEFAULT_FILENAME="FileInput.s";
	public static final int MAX_LINES=512; //2^8
	
	private final ErrorLog errorLog;
	private final WarningsLog warningsLog;
	private final MemoryBuilder mb;
	private final Validate val;
	private final OperandsValidation opsVal;
	
	private String errorFn; // This should not be returned!, Error Messages should be meaningful!
	private boolean dataLimit, instrLimit;
	
	/**
	 Same as {@link #Parser(MemoryBuilder, ErrorLog, WarningsLog)}, But, it will automatically run {@link
	#loadParseFile(String)} on the parameter.
	 <p>
	 Run {@link #assemble()} to retrieve the models built. if Null, then there were errors!
	 <p>(if parser & assembler find no errors!)</p>
	 
	 @param filepath address of file to load & parse.
	 
	 @see #loadParseFile(String)
	 @see #assemble()
	 @see #Parser(MemoryBuilder, ErrorLog, WarningsLog)
	 */
	public Parser(@NotNull String filepath, @NotNull MemoryBuilder memoryBuilder,
				  @NotNull ErrorLog errorLog, @NotNull WarningsLog warningsLog) {
		this( memoryBuilder, errorLog, warningsLog );
		loadParseFile( filepath );
	}
	
	public static boolean isNullOrBlank (@Nullable String s){ return (s==null || s.isBlank()); }
	
	/**
	 Initializes the setup.Parser with an empty model. Either Load a file {@link #loadFile(String)}, and parse it {@link
	#loadParseFile(String)} Which will also automatically assemble the models ready for execution.
	 <p>
	 Or, single line manually {@link #parseLine(String, int)}(for testing purposes)
	 <p>
	 After you have parsed content. And there are no errors, run {@link #assemble()}
	 
	 @see #assemble()
	 @see #loadParseFile(String)
	 @see #parseLine(String, int)
	 */
	public Parser(@NotNull MemoryBuilder memoryBuilder, @NotNull ErrorLog errorLog, @NotNull WarningsLog warningsLog) {
		this.mb=memoryBuilder;
		this.errorLog=errorLog;
		this.warningsLog=warningsLog;
		this.val=new Validate( errorLog );
		this.opsVal=new OperandsValidation( errorLog,warningsLog );
	}
	
	/** Resets Internal State, Use before re-running modified file */
	@VisibleForTesting
	void clear(){
		this.errorLog.clear();
		this.warningsLog.clear();
		this.mb.clear();
		this.dataLimit=false;
		this.instrLimit=false;
		this.errorFn=" NOT SET !";
	}
	
	/**
	 Automatically runs {@link #loadFile(String)} on parameter.
	 <p>Then, parses the file for correct MIPS Syntax,
	 and builds the emulator's models based on it</p>
	 <p>After, you should run {@link #assemble()}</p>
	 
	 @param filename address of file to load & parse.
	 
	 @return success of parsing file.
	 
	 @see #assemble()
	 */
	public boolean loadParseFile(String filename) {
		return parseFile( loadFile( filename ) );
	}
	
	/**
	 Parses the file loaded into the parser. Used after running {@link #loadFile(String)}.
	 <p>After, you should run {@link #assemble()}</p>
	 
	 @return success of parsing file.
	 
	 @see #Parser(MemoryBuilder, ErrorLog, WarningsLog)
	 @see #loadFile(String)
	 @see #assemble()
	 */
	public boolean parseFile(@Nullable File file) {
		boolean rtn=true;
		
		BufferedReader reader=null;
		if ( file!=null ) {
			errorFn="File: \"" + file.getName() + "\", ";
			try {
				reader=new BufferedReader( new FileReader( file ) );
				int lineNo=1;
				
				for ( String line=reader.readLine( ); line!=null; line=reader.readLine( ) ) {
					if ( lineNo>=MAX_LINES ) {
						errorLog.appendEx( errorFn + "Has Too Many Lines!, Max Lines = [" + MAX_LINES + "]" );
						rtn=false;
						break;
					}
					rtn&=parseLine( line, lineNo++ );
				}
			} catch ( IOException e ) {
				errorLog.appendEx( errorFn + "Not Valid FileName - Parsed" );
				rtn=false;
			} finally {
				try {
					if ( reader!=null ) reader.close( ); // Close reader
				} catch ( IOException e ) { e.printStackTrace( );}
			}
		} else
			rtn=false;
		
		return rtn;
	}
	
	/**
	 Apples file checks on the address given.
	 <li>File Exists</li>
	 <li>File is Accessible</li>
	 <p><b>Also clears any internal state!
	 - {@link #parseLine(String, int)} does not do this!</b>
	 
	 @param filename address of file to load.
	 
	 @return File object, if the File is valid
	 */
	public File loadFile(String filename) {
		clear(); // Reset State
		// Check fileName
		if ( isNullOrBlank( filename ) ) {
			warningsLog.append( "Filename Not Provided, Using Default File: \"" + DEFAULT_FILENAME + "\"" );
			filename=DEFAULT_FILENAME;
		}
		
		errorFn="File: \"" + filename + "\", ";
		File rtn=null;
		try {
			File temp=new File( filename );
			
			String name=temp.getCanonicalPath( );
			File parent=temp.getParentFile( );
			
			if ( parent!=null )
				name=name.substring( parent.getCanonicalPath( ).length( ) + 1 );
			
			errorFn="File: \"" + name + "\", ";
			
			if ( !temp.exists( ) ) this.errorLog.appendEx( errorFn + "Does Not Exist" );
			else if ( !temp.isFile( ) ) this.errorLog.appendEx( errorFn + "Is Not a File" );
			else if ( !temp.canRead( ) ) this.errorLog.appendEx( errorFn + "Can Not Be Read" );
			else rtn=temp;
		} catch ( IOException e ) {
			errorLog.appendEx( errorFn + "Not Valid FileName" );
			// File is invalid,   rtn is null by default
		}
		return rtn;
	}
	
	/**
	 Parses an individual line for MIPS Syntax.
	 <p>Adds information to emulator's models</p>
	 
	 @param line MIPS instruction to be parsed.
	 
	 @return success of parsing instruction.
	 
	 @see #assemble()
	 */
	public boolean parseLine(@NotNull String line, int lineNo) {
		String[] split=splitLine( line );
		int errLength=errorLog.toString( ).length( );
		// parse mode -> ignored
		// validate label
		String label=this.val.isValidLabel( lineNo, split[ 0 ] );
		if ( label!=null )
			mb.pushLabel( label );
//
//		if ( errLength<errorLog.toString( ).length( ) )    // caused by Invalid Label
//			errorLog.append( "_" );
		
		String arg1=split[ 1 ];
		String arg2=split[ 2 ];
		if ( !isNullOrBlank( arg1 ) ) {
			// first character is a dot '.'
			if ( arg1.matches( "\\..*" ) ) {
				// validate directive
				if ( this.val.isValidDirective( lineNo, arg1 ) )
					if ( !dataLimit && Validate.isDataType( arg1 ) ) // is DataType
						if ( !mb.addData( arg1, arg2, errorLog ) ) { // mb.addData
							if ( mb.retrieveData( ).size( )>=DataMemory.MAX_DATA_ITEMS ) {
								warningsLog.appendEx( lineNo,"Reached MAX Data Size!, No More Data Will Be Parsed" );
								warningsLog.append( "\t\t\tData Limit == [" + DataMemory.MAX_DATA_ITEMS + "]" );
								dataLimit=true;
							}
						}
			} else if ( !instrLimit ) {
				// validate opcode
				// validate arg2 (operands)
				// mb.addInstr
				if ( this.opsVal.isValidOpCode( lineNo, arg1 ) ) {
					Operands operands=this.opsVal.splitValidOperands( lineNo, arg1, arg2 );
					if ( operands!=null && !mb.addInstruction( arg1, operands ) ) {
						warningsLog.appendEx( lineNo,"Reached MAX Instructions!, Further Instructions Will Not Be Parsed" );
						warningsLog.append( "\t\t\tInstruction Limit == [" + InstrMemory.MAX_INSTR_COUNT + "]" );
						instrLimit=true;
					}// TODO Change to report Error
				}
			}
		}
		return (errLength==errorLog.toString( ).length( ));
	}
	
	/**
	 Splits a line into it's components
	 <li>[0] - Labels</li>
	 <li>[1] - Directive/ DataType/ Opcode</li>
	 <li>[2] - Data/Operands</li>
	 <li>[3] - Comments</li>
	 */
	@Nullable
	String[] splitLine(@NotNull String line) {
		String comment, label, ARG1, ARG2;
		comment=label=ARG1=ARG2=null;
		String[] split;
		
		// Split Comments - reserve capitalization
		split=splitComment( Convert.removeExtraWhitespace( line ) );
		if ( split.length==2 )
			comment=split[ 1 ];
		//
		line=split[ 0 ].toLowerCase( ).strip( );    // Make the remainder lowercase
		
		boolean directive = line.matches( "^\\s?\\..*" ); // first non-whitespace is a period
		
		// Split Labels
		int colonIndex = line.indexOf( ":" );
		if ( !directive&&colonIndex!=-1 ) {
			int periodIndex = line.indexOf( "." );
			if ( periodIndex>colonIndex ) // if line contains '.' after colon ':'
				split=line.split( ":\\s?(?=.*\\.)", 2 );    // forward lookup : before .
			else
				split=line.split( ":\\s?", 2 );
			
			label=split[ 0 ];
			line=split[ 1 ];
		}
		
		if ( !line.isBlank( ) ) {    // skip if rest of line is blank
			// Split Arg1 (.directive/ .datatype/ opcode)
			split=line.split( "\\s", 2 ); // split around first white space
			ARG1=split[ 0 ];
			
			if ( split.length==2 && !split[ 1 ].isEmpty( ) )
				ARG2=split[ 1 ]; // if empty, remain null
		}
		
		return new String[] { label, ARG1, ARG2, comment };
	}
	
	/** Return: [0] contains line, if (length==2) [1] contains comment */
	private String[] splitComment(@NotNull String line) {
		// Split at Comment, # or ;
		String[] split;
		
		split=line.split( "#", 2 );
		String comments="";
		if ( split.length==2 )
			comments="#" + split[ 1 ];
		
		line=split[ 0 ];
		
		split=line.split( ";", 2 );
		if ( split.length==2 )
			comments=";" + split[ 1 ] + comments;
		
		line=split[ 0 ];
		
		if ( comments.isBlank( ) )
			return new String[] { line };
		else    // contains comments
			return new String[] { line, comments };
	}
	
	/**
	 Wrapper for {@link MemoryBuilder#assembleInstr(ErrorLog)}
	 
	 @return success of assembly.
	 */
	public ArrayList<model.Instruction> assemble() {
		return mb.assembleInstr( errorLog );
	}
	
	public DataMemory getMem(ExecutionLog log) {
		return new DataMemory( mb.retrieveData( ), log );
	}
	
}
