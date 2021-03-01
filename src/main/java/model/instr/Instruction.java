package model.instr;

import util.logs.ExecutionLog;

public interface Instruction{
	
	/**
	 Runs the Instruction though all stages of execution.
	 TODO - Refactor, to Execute Interface
	 */
	void execute(ExecutionLog executionLog);
	
	//Load / Store will the immediate will hold the address despite normally being 16bits
	// until 'la' is implemented (or lui & ori),
	// lw / sw use a label to reference the memory.
}
