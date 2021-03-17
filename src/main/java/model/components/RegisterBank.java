package model.components;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import util.Convert;
import util.logs.ExecutionLog;
import util.logs.Logger;

/**
 Wrapper for int[32] registers. Must be size 32
 Null inputs are ignored.
 <p>
 Out of Range inputs throw {@link IndexOutOfBoundsException}
 */
public class RegisterBank {
	public static RegFormat regFormat=RegFormat.R;
	private final int[] registers;
	private final ExecutionLog executionLog;
	private final String NAME="RegisterBank";
	private Integer LAST_READ0=null;
	private Integer LAST_READ1=null;
	private Integer LAST_WRITTEN=null;
	
	//TODO Add Named versions of Read/Write, so the Opperand Name [RD/RS/RT] can be printed to the log with the action.
	
	public RegisterBank(int[] registers, ExecutionLog executionLog) {
		if ( registers.length!=32 )
			throw new IllegalArgumentException( "Register Bank Must be 32 indexes!" );
		if ( registers[ 0 ]!=0 )
			throw new IllegalArgumentException( "Register Bank Index 0, MUST equal 0!\tActual: "
												+ registers[ 0 ] );
		
		this.registers=registers;
		this.executionLog=executionLog;
	}
	
	/**
	 Reads the data of the register at the given index, Returns 0 for null input.
	 
	 @throws IndexOutOfBoundsException if register index out of bounds.
	 */
	public int read(@Nullable Integer index) {
		LAST_READ1=null;
		int val=0;
		
		if ( index==null ) {
			LAST_READ0=null;
			noAction( );
		} else if ( inRange( index ) ) {
			LAST_READ0=index;
			val=this.registers[ index ];
			
			this.executionLog.append( NAME + ":\t" + "Reading Value[" + val + "]\tFrom Register Index["
									  + fmtReg( index ) + "]!" );
		}
		
		return val;
	}
	
	/**
	 Reads the data of the registers at both given index, Returns 0 for null input.
	 
	 @return int[] size 2, value for index1 at pos0, value for index2 at pos1
	 
	 @throws IndexOutOfBoundsException if register index out of bounds.
	 @see Convert#named2R(String)
	 @see Convert#r2Index(String)
	 @see Convert#index2R(Integer)
	 @see Convert#r2Named(String)
	 @see Convert#namedRegisters
	 */
	public int[] read(@Nullable Integer index0, @Nullable Integer index1) {
		int data0=0, data1=0;
		
		if ( index0==null && index1==null ) {
			LAST_READ0=null;
			LAST_READ1=null;
			noAction( );
			
		} else if ( index0!=null && index1==null ) {
			data0=read( index0 );
			
		} else if ( index0==null ) { // index0 == null && index1 != null
			LAST_READ0=null;
			LAST_READ1=index1;
			if ( inRange( index1 ) ) {
				data1=this.registers[ index1 ];
				this.executionLog.append( NAME + ":\t" + "Reading Value[" + data1 + "]\tFrom Register Index["
										  + fmtReg( index1 ) + "]!" );
			}
		} else { // index0 != null && index1 != null
			LAST_READ0=index0;
			LAST_READ1=index1;
			if ( inRange( index0 ) && inRange( index1 ) ) {
				data0=this.registers[ index0 ];
				data1=this.registers[ index1 ];
				this.executionLog.append( NAME + ":\t" + "Reading Values[" + data0 + ", " + data1
										  + "]\tFrom Register Indexes[" + fmtReg( index0 ) + ", "
										  + fmtReg( index1 ) + "]!" );
			}
		}
		return new int[] { data0, data1 };
	}
	
	
	/**
	 Sets the data of the register at the given index.
	 <p>
	 Performs no action, if data is null. or index is null or 0.
	 
	 @throws IndexOutOfBoundsException if register index out of bounds.
	 @see Convert#named2R(String)
	 @see Convert#r2Index(String)
	 @see Convert#index2R(Integer)
	 @see Convert#r2Named(String)
	 @see Convert#namedRegisters
	 */
	@SuppressWarnings ("UnusedReturnValue")
	public boolean write(@Nullable Integer index, @Nullable Integer data) {
		if ( index==null || data==null || index==0 ) {
			LAST_WRITTEN=null;
			noAction( );
			return false;
		} else if ( inRange( index ) ) {
			LAST_WRITTEN=index;
			this.registers[ index ]=data;
			this.executionLog.append( NAME + ":\t" + "Writing Value[" + data + "]\tTo Register Index["
									  + fmtReg( index ) + "]!" );
		}
		return true;
	}
	
	/** Explicit instruction to do nothing. */
	public void noAction() {
		this.executionLog.append( NAME + ":\t" + "No Action!" );
	}
	
	public boolean inRange(int index) {
		int MIN_INDEX=0;
		int MAX_INDEX=31;
		if ( index>=MIN_INDEX && index<=MAX_INDEX )
			return true;
		else
			throw new IndexOutOfBoundsException( "Index must be >=" + MIN_INDEX + " and <=" + MAX_INDEX + "!" );
	}
	
	/**
	 Formats the register depending on {@link RegFormat}, and combines with the value at that register
	 */
	private String fmtReg(int index) {
		return colorReg( index, regName( index ) );
	}
	
	/** Depending on the status colorize the output, and add an asterisk if {@link #LAST_WRITTEN} */
	private String colorReg(int index, String reg) {
		final String READ_COL=Logger.Color.RB_READ;
		final String WRITE_COL=Logger.Color.RB_WRITE;
		
		if ( LAST_READ0!=null )
			if ( index==LAST_READ0 )
				reg=Logger.Color.fmtColored( READ_COL, reg );
		
		if ( LAST_READ1!=null )
			if ( index==LAST_READ1 )
				reg=Logger.Color.fmtColored( READ_COL, reg );
		
		if ( LAST_WRITTEN!=null )
			if ( index==LAST_WRITTEN )
				reg=Logger.Color.fmtColored( WRITE_COL, "*" + reg );
		
		return reg;
	}
	
	/** Formats the register index for output based on {@link RegFormat} */
	private String regName(int index) {
		if ( regFormat==RegFormat.Index )
			return "$" + index;
		
		String rtn;
		switch ( regFormat ) {
			case $R:
			case $Named:
				rtn="$";
				break;
			default:
				rtn="";
				break;
		}
		String reg=Convert.index2R( index );
		switch ( regFormat ) {
			case Named:
			case $Named:
				reg=Convert.r2Named( reg );
			default:
				break;
		}
		return rtn + reg.toUpperCase( );
	}
	
	/**
	 Returns a formatted string, that when printed, displays the current state of the register bank.
	 <p><b>Use with print(</b><i> registerBank.format() </i><b>)</b>
	 */
	@NotNull
	public String format() {
		StringBuilder rtn=new StringBuilder( "-----------REGISTER-BANK-------------\n" );
		int I1=0, I2=8, I3=16, I4=24;
		
		for ( int i=0; i<8; i++ ) {
			rtn.append( "|" ).append( fmtRegWithData( I1++ ) )
			   .append( "\t\t" ).append( fmtRegWithData( I2++ ) )
			   .append( "\t" ).append( fmtRegWithData( I3++ ) )
			   .append( "\t" ).append( fmtRegWithData( I4++ ) )
			   .append( "|\n" );
		}
		rtn.append( "-------------------------------------\n" );
		return rtn.toString( );
	}
	
	/** Formats the register depending on {@link RegFormat}, and combines with the value at that register */
	private String fmtRegWithData(int index) {
		return colorReg( index, regName( index ) + ": " + registers[ index ] );
	}
	
	enum RegFormat {
		Index,  // $0 .. $31
		R, // R0..R31
		Named, //ZERO.S0.T0.RA
		$R, // $R0 ..$R31
		$Named //$ZERO.$S0.$T0.$RA
	}
	
}
