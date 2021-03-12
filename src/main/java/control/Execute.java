package control;

import model.Instruction;
import model.components.DataMemory;
import model.components.InstrMemory;
import model.components.RegisterBank;

import util.logs.ExecutionLog;

import java.util.ArrayList;

public class Execute {
	ExecutionLog exLog;
	DataMemory dataMem;
	RegisterBank regBank;
	InstrMemory instrMemory;
	
	public Execute(ExecutionLog exLog, DataMemory dataMem, RegisterBank regBank, InstrMemory instrMemory) {
		this.exLog=exLog;
		this.dataMem=dataMem;
		this.regBank=regBank;
		this.instrMemory=instrMemory;
	}
	
	private void execute(ArrayList<Instruction> instructions) {
		execute( dataMem, regBank, instrMemory, exLog );
		exLog.println( );
	}
	
	public static void execute(DataMemory dataMem, RegisterBank regBank, InstrMemory instrMemory, ExecutionLog exLog) {
		//
		Instruction ins;
		for ( Integer PC=InstrMemory.BASE_INSTR_ADDRESS;
			  PC!=null;
			  PC=ins.execute( PC, dataMem, regBank, exLog ) ) {
			exLog.println( );
			exLog.clear( ); // print ExecutionLog
			System.out.print( regBank.format( ) ); // print RegisterBank status
			ins=instrMemory.InstructionFetch( PC );
		}
	}
	
}