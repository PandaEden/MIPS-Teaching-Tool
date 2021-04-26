package model.instr;

import org.jetbrains.annotations.NotNull;

import util.validation.InstructionValidation;

public class Branch extends I_Type{
	
	/**{@link InstructionValidation#I_RT_RS_IMM}*/
	public Branch (@NotNull String opcode, int RS, int RT, int IMM) throws IllegalArgumentException{
		super( InstructionValidation.I_RS_RT_IMM, opcode, RS, RT, IMM );
	}
	/**{@link InstructionValidation#I_RT_RS_IMM}*/
	public Branch(@NotNull String opcode, int RS, int RT, String label) throws IllegalArgumentException{
		super( InstructionValidation.I_RS_RT_IMM, opcode, RS, RT, label );
	}
}
