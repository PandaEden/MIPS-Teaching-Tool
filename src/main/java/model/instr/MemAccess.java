package model.instr;

import org.jetbrains.annotations.NotNull;

import util.validation.InstructionValidation;

public class MemAccess extends I_Type {
	/**{@link InstructionValidation#I_TYPE_MEM_ACCESS}, Label needs to be assembled into IMM value*/
	public MemAccess (@NotNull String opcode, int RT, @NotNull String label) throws IllegalArgumentException{
		super( InstructionValidation.I_TYPE_MEM_ACCESS, opcode, 0, RT, label );
	}
	
	/**{@link InstructionValidation#I_TYPE_RT_IMM_RS}*/
	public MemAccess (@NotNull String opcode, int RS, int RT, int IMM) throws IllegalArgumentException{
		super( InstructionValidation.I_TYPE_MEM_ACCESS, opcode, RS, RT, IMM );
	}
}
