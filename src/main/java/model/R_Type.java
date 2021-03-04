package model;

import model.components.DataMemory;
import model.components.RegisterBank;
import model.instr.Operands;
import org.jetbrains.annotations.NotNull;
import util.logs.ExecutionLog;

import java.security.InvalidParameterException;

public class R_Type extends Instruction{
	
	R_Type(String ins, Operands operands){
		super(ins, operands);
	}
	
	@Override
	protected void action(@NotNull DataMemory dataMem, @NotNull RegisterBank regBank,
						  @NotNull ExecutionLog executionLog){
		int rsVal = regBank.read(RS);
		int rtVal = regBank.read(RT);
		
		executionLog.append("Calculating Result:");
		int rdVal;
		switch (ins) {
			case "add":
				rdVal = rsVal+rtVal;
				executionLog.append("RD = RS+RT = "+rsVal+"+"+rtVal+" = "+rdVal);
				break;
			case "sub":
				rdVal = rsVal-rtVal;
				executionLog.append("RD = RS-RT = "+rsVal+"-"+rtVal+" = "+rdVal);
				break;
			default:
				throw new InvalidParameterException("Instruction:{"+ins+"} not recognised or Implemented");
		}
		dataMem.noAction();
		regBank.write(RD, rdVal);
	}
}
