package model.instr;

public interface Instruction{
	
	/**
	 @return -1 if auto inserted Exit
	 */
	int getLineNo();
	
	String getOpCode();

	
	/**
	 <li>R_Type - RS - source register</li>
	 <li>I_Type - RS - source register or -1 (no operand)</li>
	 <li>J_Type - (-1) (no operand)</li>
	 @return 	registerNo, or -1
	 */
	int getRead1();
	
	/**
	 <li>R_Type - RS - source register</li>
	 <li>I_Type - RS - source register or -1 (no operand)</li>
	 <li>J_Type - (-1) (no operand)</li>
	 @return 	registerNo, or -1
	 */
	int getRead2();
	
	/**
	 <li>R_Type - RS - source register</li>
	 <li>I_Type - RT - Target/Third register or -1 (no operand)</li>
	 <li>J_Type - (-1) (no operand)</li>
	 @return 	registerNo, or -1
	 */
	int getWrite();
	
	
	/**
	 <li>R_Type - RS - source register</li>
	 <li>I_Type - RS - source register (may be null)</li>
	 <li>J_Type - (-1) (no operand)</li>
	 */
	int getImmediate();
	
	/**
	 Runs the Instruction though all stages of execution.
	 */
	void execute();
	
	//Load / Store will the immediate will hold the address despite normally being 16bits
	// until 'la' is implemented (or lui & ori),
	// lw / sw use a label to reference the memory.
}
