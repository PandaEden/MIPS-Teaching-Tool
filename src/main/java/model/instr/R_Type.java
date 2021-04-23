package model.instr;

import org.jetbrains.annotations.NotNull;

import model.components.Component;
import model.components.DataMemory;
import model.components.RegisterBank;

import util.logs.ErrorLog;
import util.logs.ExecutionLog;
import util.validation.InstructionValidation;

import java.util.HashMap;

public class R_Type extends Instruction {
	
	/**{@link InstructionValidation#R_TYPE}*/
	public R_Type (@NotNull String opcode, int RS, int RT, int RD) {
		super( Type.REGISTER, InstructionValidation.R_RD_RS_RT, opcode, RS, RT, RD, null, null );
		
		super.regNotInRange_Register( RD );
		super.regNotInRange_Register( RS );
		super.regNotInRange_Register( RT );
	}	// TODO - Make a different constructor for Shift instructions
	
	@Override
	protected void action(@NotNull DataMemory dataMem, @NotNull RegisterBank regBank,
						  @NotNull ExecutionLog executionLog) {
		int[] values = regBank.read( RS, RT );
		int rsVal=values[0];
		int rtVal=values[1];
		
		executionLog.append( "\tCalculating Result:" );
		int rdVal;
		rdVal=Component.ALU( rsVal, rtVal, Component.searchALUCode( opcode ), executionLog);
		dataMem.noAction( );
		regBank.write( RD, rdVal );
	}
	@Override public boolean assemble (@NotNull ErrorLog log, @NotNull HashMap<String, Integer> labelMap) throws IllegalArgumentException {
		return true;
	}
	
}
