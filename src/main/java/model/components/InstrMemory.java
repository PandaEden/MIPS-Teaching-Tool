package model.components;

import org.jetbrains.annotations.NotNull;

import model.instr.Instruction;
import model.instr.Nop;

import util.Convert;
import util.ansi_codes.Color;
import util.logs.ErrorLog;
import util.logs.ExecutionLog;
import util.validation.AddressValidation;

import java.util.ArrayList;
import java.util.HashMap;

/**
 Wrapper for Instruction[] instructions. Provides protection for runtime errors where an address calculated during
 execution (based on register value), is out of bounds.
 <p>
 Index's are word-aligned (multiple of 4).
 <p>
 use {@link AddressValidation#isSupportedInstrAddr(int, ErrorLog)}
 
 @see AddressValidation#isSupportedInstrAddr(int, util.logs.ErrorLog) */
public class InstrMemory {
	public static final int ADDR_SIZE=4;
	public static final int BASE_INSTR_ADDRESS=0x00400000;
	public static final int OVER_SUPPORTED_INSTR_ADDRESS=0x00500000+ADDR_SIZE;
	public static final int OVER_INSTR_ADDRESS=0x10000000;
	public static final int MAX_INSTR_COUNT=256;
	
	private final ArrayList<Instruction> instructions;
	private final ExecutionLog executionLog;
	// reference autoExit instruction TODO with lineNo (-1)
	private static final Instruction autoExit = new Nop( "exit" );
	
	public InstrMemory(@NotNull ArrayList<Instruction> instructions, @NotNull ExecutionLog executionLog) {
		this.instructions=instructions;
		this.executionLog=executionLog;
		autoExit.assemble(new ErrorLog( new ArrayList<>() ),new HashMap<>(), 0x00400000);// Pre-Assemble AutoExit
	}
	
	private static final String fetching=Color.fmtTitle(Color.GREEN,"Fetching")+":";
	/**
	 Given a valid Instruction Address, Returns the Instruction object for that address.
	 <p>
	 Fetching past last instruction, returns a Exit instruction.
	 
	 @throws IndexOutOfBoundsException for Not Supported Address
	 @throws IllegalArgumentException  for Non-Word Aligned Address
	 @see AddressValidation#isSupportedInstrAddr(int, ErrorLog)
	 */
	public Instruction InstructionFetch(int PC_Address) throws IndexOutOfBoundsException, IllegalArgumentException{
		String hex_addr=Convert.int2Hex( PC_Address );
		//Supported Instr Address
		if ( ( PC_Address<BASE_INSTR_ADDRESS || PC_Address>=OVER_SUPPORTED_INSTR_ADDRESS ) )
			throw new IndexOutOfBoundsException( "Instruction Address ["+Convert.int2Hex(PC_Address)
												 +", "+PC_Address+"]  " + hex_addr + " Not In Range!" );
		if ( PC_Address%ADDR_SIZE!=0 )
			throw new IllegalArgumentException( "Instruction Address ["+Convert.int2Hex(PC_Address)
												 +", "+PC_Address+"] " + hex_addr + " Not Word Aligned!" );
		
		int index=Convert.instrAddr2Index( PC_Address );
		
		executionLog.append( fetching+" Instruction At Address [" + Color.fmtUnder(hex_addr) + "]" );
		if ( index<instructions.size( ) ) {
			return instructions.get( index );
		} else { // index >256
			executionLog.appendEx(
					Color.fmt( Color.WARN_LOG,
							   "\tRun Over Provided Instructions -- Auto Exit"  )
			);
			return autoExit;
		}
	}
}
