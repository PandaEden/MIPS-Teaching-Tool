package model.instr;

import org.jetbrains.annotations.NotNull;

import util.validation.InstructionValidation;

public class MemAccess extends I_Type {
	/**{@link InstructionValidation#I_MEM_RT_IMM}, Label needs to be assembled into IMM value*/
	public MemAccess (@NotNull String opcode, int RT, @NotNull String label) throws IllegalArgumentException{
		super( InstructionValidation.I_MEM_RT_IMM, opcode, 0, RT, label );
		assert RS==0 : "RS:["+RS+"], must be 0, when IMM is null";
		
	}
	
	/**{@link InstructionValidation#I_MEM_RT_IMM}*/
	public MemAccess (@NotNull String opcode, int RS, int RT, int IMM) throws IllegalArgumentException{
		super( InstructionValidation.I_MEM_RT_IMM, opcode, RS, RT, IMM );
	}
}
