package model;

import org.jetbrains.annotations.NotNull;

import model.components.Component;
import model.components.DataMemory;
import model.components.RegisterBank;
import model.instr.Operands;

import util.logs.ExecutionLog;

public class R_Type extends Instruction {
	
	R_Type(String ins, Operands operands) {
		super( ins, operands );
	}
	
	@Override
	protected void action(@NotNull DataMemory dataMem, @NotNull RegisterBank regBank,
						  @NotNull ExecutionLog executionLog) {
		int[] values = regBank.read( RS, RT );
		int rsVal=values[0];
		int rtVal=values[1];
		
		executionLog.append( "\tCalculating Result:" );
		int rdVal;
		rdVal=Component.ALU(rsVal,rtVal,ins,executionLog);
		dataMem.noAction( );
		regBank.write( RD, rdVal );
	}
	
}
