package model.components;

import model.Instruction;
import model.instr.Operands;
import util.AddressValidation;
import util.Convert;
import util.logs.ErrorLog;
import util.logs.ExecutionLog;

import java.util.ArrayList;

/**
 Wrapper for Instruction[] instructions. Provides protection for runtime errors where an address calculated during
 execution (based on register value), is out of bounds.
 <p>
 Index's are word-aligned (multiple of 4).
 <p>
 use {@link AddressValidation#isSupportedInstrAddr(int, ErrorLog)} and
 {@link AddressValidation#addr2index(int, boolean, ErrorLog)}
 
 @see AddressValidation#isSupportedInstrAddr(int, util.logs.ErrorLog)
 @see AddressValidation#addr2index(int, boolean, ErrorLog) */
public class InstrMemory{
	public static final int BASE_INSTR_ADDRESS = 0x00400000;
	public static final int OVER_SUPPORTED_INSTR_ADDRESS = 0x00500000;
	public static final int OVER_INSTR_ADDRESS = 0x10000000;
	public static final int MAX_INSTR_COUNT = 256;
	public static final int ADDR_SIZE = 4;
	
	private final ArrayList<Instruction> instructions;
	private final ExecutionLog executionLog;
	
	// contain a reference autoExit instruction with lineNo (-1)
	
	public InstrMemory(ArrayList<Instruction> instructions, ExecutionLog executionLog){
		this.instructions = instructions;
		this.executionLog = executionLog;
	}
	
	/**
	 Given a valid Instruction Address, Returns the Instruction object for that address.
	 <p>
	 Fetching past last instruction, returns a Exit instruction.
	 
	 @throws IndexOutOfBoundsException for Not Supported Address
	 @throws IllegalArgumentException  for Non-Word Aligned Address
	 @see AddressValidation#isSupportedInstrAddr(int, ErrorLog)
	 */
	public Instruction InstructionFetch(int PC_Address){
		String hex_addr = Convert.uInt2Hex(PC_Address);
		//Supported Instr Address
		if ((PC_Address<BASE_INSTR_ADDRESS || PC_Address>=OVER_SUPPORTED_INSTR_ADDRESS))
			throw new IndexOutOfBoundsException("Address "+hex_addr+" Not In Range!");
		if (PC_Address%ADDR_SIZE!=0)
			throw new IllegalArgumentException("Address "+hex_addr+" Not Word Aligned!");
		
		int index = Convert.address2Index(PC_Address);
		
		if (index<instructions.size()) {
			executionLog.append("Fetching Instruction At Address ["+hex_addr+"]");
			return instructions.get(index);
		} else { // index >256
			return Instruction.buildInstruction("exit", Operands.getExit());
		}
	}

	//TODO move process output into here & test
}
