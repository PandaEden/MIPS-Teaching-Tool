package model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import model.components.DataMemory;
import model.components.InstrMemory;
import model.instr.Operands;

import setup.Parser;

import util.Convert;
import util.validation.Validate;
import util.logs.ErrorLog;
import util.logs.ExecutionLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

/**
 <pre>
 <b>int (signed 32bit)</b>
 <i>supports values 2,147,483,647 to - 2,147,483,648</i>
 <i>Hex: 0x7FFF,FFFF to -0x800,0000</i>
 
 <b>long (signed 64bit)</b>
 <i>supports values 9,223,372,036,854,775,807 to -9,223,372,036,854,775,808</i>
 <i>Hex:0x7FFF,FFFF,FFFF,FFFF to -0x800,0000,0000,0000</i>
 
 Code range (.text) 0x00400000 to 0x004FFFFF (4194304 to 5242879)
 Global Data (.data) 0x10010000 to 0x1003FFFF (268500992 to 268697599)
 Heap data 0x10040000 to 0x1FFFFFFF (268697600 to 536870911)
 Stack data 0x70000000 to 0x7FFFFFFF (1879048192 to 2147483647)
 In powers:
 0x00400000:(2^22)       >= Code     <0x00500000:(2^22 +2^20)
 0x10010000:(2^28 +2^16) >= Global   <0x10040000:(2^28 +2^18)
 0x10040000:(2^28 +2^18) >= Heap     <0x20000000:(2^29)
 0x70000000:(2^31-2^28)  >= Stack    <0x80000000:(2^31)
 </pre>
 
 <pre> Invalid Values Examples
 INVALID_DOUBLE = "-2222222222222222222222222222222222222222.22222222222222222222222222222222222222222222222222222222";
 INVALID_LONG = "9223372036854775808"; //Long.MAX_VALUE +1
 INVALID_LONG_UNDER = "-9223372036854775809"; //Long.MIN_VALUE -1
 INVALID_INT = "2147483648"; //Integer.MAX_VALUE +1
 INVALID_INT_UNDER = "-2147483649"; //Integer.MIN_VALUE -1
 
 Data Type Ranges - https://www.w3schools.com/java/java_data_types.asp
 32bit Int - Signed [-2,147,483,648 -> 2,147,483,647]
 - Unsigned [0 -> 4,294,967,296]
 64Bit Long - Unsigned [-9,223,372,036,854,775,808 -> 9,223,372,036,854,775,807]
 - Signed [0 -> 18,446,744,073,709,551,616]
 <p>
 32bit Float - Sufficient for storing 6 to 7 decimal digits
 64
 </pre>
 */
public class MemoryBuilder {
	private static final int INS_ADDR_BASE=InstrMemory.BASE_INSTR_ADDRESS; //0x 0400 0000
	private static final int DATA_ADDR_BASE=DataMemory.BASE_DATA_ADDRESS; //0x 1001 0000
	private static final int LIMIT=InstrMemory.MAX_INSTR_COUNT;
	private static final int DATA_LIMIT=DataMemory.MAX_DATA_ITEMS;
	private static final int ADDR_SIZE=InstrMemory.ADDR_SIZE;
	private static final int DATA_SIZE=DataMemory.DATA_ALIGN;
	
	private final HashMap<Integer, Double> dataArr=new HashMap<>( ); //index=(offset from BASE)/DataMemory.DATA_ALIGN;
	private final DataMemory dataMem=new DataMemory( dataArr, new ExecutionLog( new ArrayList<>( ) ) );
	private final HashMap<String, Integer> labelMap=new HashMap<>( );
	private final LinkedList<String> labels=new LinkedList<>( );
	
	private final ArrayList<Instruction> instructions=new ArrayList<>( );
	private int ProgramCounter=INS_ADDR_BASE;
	private int MEM_PTR=DATA_ADDR_BASE; //decimal representation of address
	
	// TODO - Move addData / addCSVArray / addRange .  to setup.Parser
	
	/**
	 Given valid input it will add the information to the {@link #dataArr},
	 It will automatically collect any pushed {@link #labels} and attach to the
	 address of the first (if a range or array) value added, in {@link #labelMap}
	 
	 <p><b>If DataType is Null, performs no action ,But -> Still returns True</b></p>
	 
	 <p>Supported Data Format: (.word)</p>
	 <ul>
	 <li>.word [±int_Val]:[+int_N]</li>
	 <li>.word [±int], [±int]... <i>(whitespace ignored)</i></li>
	 <li>.word [±int]</li>
	 </ul>
	 
	 @return boolean - success of adding all of the data.
	 
	 @throws IllegalStateException dataType not supported!/ Has not been Validated
	 @see Validate#isValidDirective(int, String)
	 @see Validate#isDataType(String)
	 */
	public boolean addData(@Nullable String dataType, @Nullable String data, @NotNull ErrorLog errorLog) {
		final String SIGNED_INT="-?\\d*";    // matches optional '-' sign, then any length int
		if ( dataType==null )
			return true;
		
		// Validate.isValidDataType // Directive
		if ( Parser.isNullOrBlank( data ) ) {
			errorLog.append( "No Data Given! For DataType: \"" + dataType + "\"!" );    //TODO Line Numbers ?
		} else if ( dataType.equals( ".word" ) ) {
			if ( MEM_PTR<DATA_ADDR_BASE + DATA_LIMIT*DATA_SIZE ) {
				if ( data.contains( ":" ) )
					return storeRange( data, errorLog );    // Val:N
				else if ( data.contains( "," ) )
					return storeCsvArray( data, errorLog );    // Int, Int, Int ...
				else if ( data.matches( SIGNED_INT ) )
					return storeWord( tryParseInt( data, null, errorLog ) );    // INT
				else
					errorLog.append( "Data: [" + data + "], Not Valid For DataType: \"" + dataType + "\"!" );
			}
		} else {
			throw new IllegalStateException( "DataType should be validated before Data is, DataType: \"" + dataType
											 + "\" Is Not Supported!" );
		}
		return false;
	}
	
	/**
	 Given a string it will parse it into the values <b>int_Val</b> representing a signed 32bit integer.
	 And <b>int_N</b> representing the number of values.
	 <p>
	 It then creates an array of <b>int_N</b> integers, and sets all the values to <b>int_Val</b>.
	 <p>
	 Then, Stores this array into {@link #dataArr} and increments the {@link #MEM_PTR} by {@link #DATA_SIZE}*<b>int_N</b>
	 <b><pre>String format: "[±int_Val]:[+int_N]"</pre></b>
	 
	 @return Success of adding all of the values.
	 
	 @see #storeCsvArray(String, ErrorLog)
	 @see #storeWord(Integer)
	 */
	private boolean storeRange(@NotNull String input, ErrorLog errorLog) {
		//TODO prevent wasted cycles if isMemoryFull() ?
		
		// Parse/Validate Input
		String[] arr=input.split( "\\s?:\\s?",2);
		Integer v=tryParseInt( arr[ 0 ], null, errorLog );
		Integer n=tryParseInt( arr[ 1 ], null, errorLog );
		if ( n!=null && n<0 ) { // if n negative -> invalid
			errorLog.append( "<Int_N>: [" + n + "], Must Be A Positive Integer!\tFormat: \"<Int_Val> : <Int_N>\"" );
			return false;
		} else if ( v==null || n==null )
			return false;
		
		// if range is >DATA_LIMIT, set it to LIMIT+1, so storeArray still return false;
		int[] intArr=new int[ (n>DATA_LIMIT) ? DATA_LIMIT + 1 : n ];
		
		Arrays.fill( intArr, v );
		for ( int i : intArr ) {
			if ( !storeWord( i ) )
				return false;
		}
		return true;
	}
	
	/**
	 input String needs to be in the format of a comma separated list of 32bit integers.
	 whitespace is ignored.
	 <pre></pre>
	 Creates an Array from the CSV list and stores it into the {@link #dataArr} and increments the {@link #MEM_PTR} by
	 {@link #DATA_SIZE}*size.ofArray.
	 <b><pre>String format: "[±int], [±int]..." <i>(whitespace ignored)</i></pre></b>
	 
	 @param csvIntArray String of comma separated values to parse.
	 
	 @return Success of adding all of the values.
	 
	 @see #storeRange(String, ErrorLog)
	 @see #storeWord(Integer)
	 */
	private boolean storeCsvArray(@NotNull String csvIntArray, ErrorLog errorLog) {
		//TODO prevent wasted cycles if isMemoryFull() ?
		
		// if contains more than DATA_LIMIT CSV's .  then split at DATA_LIMIT'st comma
		//TODO refactor to more space efficient algorithm.
		// Splitting at a comma, storing that value. Then splitting the next comma
		// Until no more commas .. or DATA_MAX
		int count=0;
		int index=0;
		for ( ; index<csvIntArray.length( ); index++ ) {
			final char c=csvIntArray.charAt( index );
			if ( c==',' ) {
				count++;
				if ( count>DATA_LIMIT ) {
					break; // escape loop
				}    // index will be at the index of the DATA_LIMIT+1'st comma
			}
		}
		if ( count>DATA_LIMIT ) {
			csvIntArray=csvIntArray.substring( 0, index );    // split before the 'th comma
			errorLog.appendEx( "CSV Too Large, stopped parsing at 256th Segment" );
		}
		
		String[] str_arr=Convert.splitCSV( csvIntArray );
		
		boolean rtn=true;
		for ( int i=0; i<str_arr.length; i++ ) {
			if ( !storeWord( tryParseInt( str_arr[ i ], i, errorLog ) ) )
				rtn=false;
		}
		return rtn;
	}
	/**
	 Stores given signed 32 bit integer into {@link #dataArr} and increments {@link #MEM_PTR} by {@link #DATA_SIZE}
	 
	 <p> If Input is Null, Returns False;
	 
	 @return Success of adding data.
	 
	 @see #storeRange(String, ErrorLog)
	 @see #storeCsvArray(String, ErrorLog)
	 */
	private boolean storeWord(Integer word) {
		if ( word!=null && !isMemoryFull( ) ) {
			dataMem.writeData( MEM_PTR, word );
			attachLabelsToAddress( MEM_PTR ); // Labels are only attached, if Data is successfully added.
			
			MEM_PTR+=DATA_SIZE;
			return true;
		} else {
			return false;
		}
	}
	
	private boolean isMemoryFull() {
		return !(MEM_PTR<DATA_ADDR_BASE + DATA_LIMIT*DATA_SIZE);
	}
	
	/** If successful, returns value, Else, returns null and prints to {@link ErrorLog} */
	private Integer tryParseInt(@NotNull String value, @Nullable Integer index, @NotNull ErrorLog errorLog) {
		try {
			return Integer.parseInt( value );
		} catch ( NumberFormatException e ) {
			if ( index!=null )
				errorLog.append( "Data Value: [" + value + "], Index: \"" + index + "\", Not Valid Signed Integer!" );
			else
				errorLog.append( "Data Value: [" + value + "], Not Valid Signed Integer!" );
			return null;
		}
	}
	
	/**
	 Pushes given string to list {@link #labels}
	 the list is later read, attached to an addresses and cleared by the method
	 {@link #attachLabelsToAddress(int)}
	 
	 <b>Input String is assumed to be valid, setup.Parser should use {@link Validate#isValidLabel(int, String)}
	 to check before running this method.
	 <p>Null Input is ignored. and no action is performed.
	 
	 @see #attachLabelsToAddress(int)
	 @see Validate#isValidLabel(int, String)
	 */
	public void pushLabel(@Nullable String label) {
		if ( label!=null ) labels.push( label );
	}
	
	/**
	 Given the index of an instruction (in the instructions list),
	 collect pushed labels in {@link MemoryBuilder#labels} and maps them to the calculated
	 instruction address, in {@link MemoryBuilder#labelMap}.
	 <p>
	 Returns False after reaching the instruction count limit.
	 <p>
	 <b>If Opcode is null, does nothing</b>
	 
	 @throws IllegalStateException If Operands And Opcode are null
	 @see MemoryBuilder#attachLabelsToAddress(int)
	 @see util.validation.OperandsValidation#isValidOpCode(int, String)
	 @see util.validation.OperandsValidation#splitValidOperands(int, String, String)
	 */
	public boolean addInstruction(@Nullable String opcode, Operands operands) {
		if ( ProgramCounter<(INS_ADDR_BASE + LIMIT*ADDR_SIZE) ) {
			if ( opcode!=null ) {
				if ( operands==null )
					throw new IllegalStateException( "Null Operands for Opcode: " + opcode );
				
				int index=Convert.instrAddr2Index( ProgramCounter );
				
				instructions.add( index, Instruction.buildInstruction( opcode, operands ) );
				attachLabelsToAddress( ProgramCounter );
				ProgramCounter+=ADDR_SIZE;
			}
			return true;
		}
		return false;
	}
	
	/**
	 Given an address (decimal index of address) it will collect all pushed
	 <b>labels</b> in {@link MemoryBuilder#labels} and maps them to the provided
	 <b>addr</b> in {@link MemoryBuilder#labelMap}
	 <pre>use {toDec(String)} to convert hex addresses to decimal form.</pre>
	 Memory#toDec(String)
	 
	 @param addr - address to attach labels to
	 
	 @throws IndexOutOfBoundsException if addr does not refer to an instruction or data address
	 */
	private void attachLabelsToAddress(int addr) {
		if ( addr<INS_ADDR_BASE || addr>DataMemory.OVER_DATA_ADDRESS )
			throw new IndexOutOfBoundsException( "Address: " + addr );
		
		for ( String label : labels ) {
			labelMap.put( label, addr );
		}
		labels.clear( ); // clear the list of labels once allocated
	}
	
	/** May Have 0 Entries, This does not mean it is invalid */
	public HashMap<Integer, Double> retrieveData() {
		return dataArr;
	}
	
	/**
	 @return null means error during assembly, and application should be terminated.
	 <b>Even if errors are from before assembly!</b>
	 */
	public ArrayList<Instruction> assembleInstr(ErrorLog errorLog) {
		if ( instructions.isEmpty( ) ) {
			errorLog.appendEx( "No Instructions Found" );
		} else { // if errorLog already has errors, then assembly should report as failed anyway.
			boolean assembled=!errorLog.hasEntries( );
			
			for ( Instruction instr : instructions ) {
				assembled&=instr.assemble( errorLog, labelMap );
			}
			
			if ( assembled )    // if no errors, new/existing
				return instructions;
			else
				errorLog.append( "Failed To Assemble Instructions!" );
		}
		return null;
	}
	
	@VisibleForTesting
	LinkedList<String> getLabels ( ) {
		return labels;
	}
	@VisibleForTesting
	public HashMap<String, Integer> getLabelMap ( ) {
		return labelMap;
	}
	
}
