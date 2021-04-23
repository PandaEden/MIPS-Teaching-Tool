package model.instr;

import org.jetbrains.annotations.NotNull;

import util.validation.InstructionValidation;

public class R_Type extends Instruction {
	
	/**{@link InstructionValidation#R_TYPE}*/
	public R_Type (@NotNull String opcode, int RS, int RT, int RD) {
		super( Type.REGISTER, InstructionValidation.R_RD_RS_RT, opcode, RS, RT, RD, null, null );
		
		super.regNotInRange_Register( RD );
		super.regNotInRange_Register( RS );
		super.regNotInRange_Register( RT );
	}	// TODO - Make a different constructor for Shift instructions
	
}
