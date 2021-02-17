package model.instr;

import Util.Log;
import Util.Validate;

/**
 Wrapper for Instruction[] instructions. Provides protection for runtime errors where an address calculated during
 execution (based on register value), is out of bounds.
 
 Index's are word-aligned (multiple of 4).
 
 use {@link Util.Validate#isValidInstrAddr(int)} and {@link Util.Validate#addr2index(int)}
 @see Util.Validate#isValidInstrAddr(int)
 @see Util.Validate#addr2index(int)
 @see Util.Validate#BASE_INSTR_ADDRESS
 @see Util.Validate#MAX_INSTR_ADDRESS
 */
public class InstrMemory{
	// contain a reference autoExit instruction with lineNo (-1)
	
	public InstrMemory(Instruction[] instructions, Validate validator, Log log){
	}
	
	/**
	 Given a valid Instruction Address, Returns the Instruction object for that address.
	 <p>
	 Fetching from address without instruction:
	 <li>below base address - Error</li>
	 <li>above max address - Error</li>
	 <li>after last instruction - automatically Appends 'halt' instruction, and pushes a
	 warning</li>
	 */
	public Instruction InstructionFetch(int PC_address){
		return null;
		//TODO might not actually need access to log, if using Validate
	}
	
	/**
	 Factory Method, returns an Instruction of the correct type with the operands parsed and individually set.
	 <p>
	 if the Instruction uses a label it will return a {@link PlaceholderInstruction}, That the assembler should replace
	 with an actual instruction before execution.
	 */
	public Instruction buildInstruction(int lineNo, String opcode, String[] operands){
		//R_Type - $rd, $rs, $rt
		//I_Type - $rt, $rs, immediate
		//		 - $rt, offset($rs)
		//		 - $rt, immediate
		//J_Type - $rd, address
		return null;
	}
	
	/**
	 Replaces the model {@link InstrMemory} encapsulates
	 @throws NullPointerException
	 */
	public boolean loadModel(Instruction[] instructions){
		return false;
	}
	
	/**
	 Returns the data model {@link InstrMemory} encapsulates.
	 */
	public float[] getModel(){
		return null;
	}
}
