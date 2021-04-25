package model.instr;

import org.jetbrains.annotations.NotNull;

import util.validation.InstructionValidation;

public class MemAccess extends I_Type {
	/**{@link InstructionValidation#I_TYPE_RT_IMM_RS}, Label needs to be assembled into IMM value*/
	public MemAccess (@NotNull String opcode, int RT, @NotNull String label) throws IllegalArgumentException{
		super( InstructionValidation.I_TYPE_RT_IMM_RS, opcode, 0, RT, label );
		if ( RS!=0 )
			throw new IllegalArgumentException("RS:["+RS+"], must be 0, when IMM is null");
		
	}
	
	/**{@link InstructionValidation#I_TYPE_RT_IMM_RS}*/
	public MemAccess (@NotNull String opcode, int RS, int RT, int IMM) throws IllegalArgumentException{
		super( InstructionValidation.I_TYPE_RT_IMM_RS, opcode, RS, RT, IMM );
	}
}
