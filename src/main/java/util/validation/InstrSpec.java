package util.validation;

import model.instr.Instruction;

import util.logs.ExecutionLog;

public class InstrSpec {
	private final String OPCODE;
	private final String NAME;
	private final FMT FORMAT_TYPE;
	/** {@link model.components.Component#DECODER(Instruction, ExecutionLog)} */
	private final Integer[] CTRL;
	
	InstrSpec (String OPCODE, String NAME, FMT FORMAT_TYPE, Integer[] CTRL) {
		this.OPCODE=OPCODE;
		this.NAME=NAME;
		this.FORMAT_TYPE=FORMAT_TYPE;
		this.CTRL=CTRL;
	}
	
	public String getOPCODE ( ) {
		return OPCODE;
	}
	public String getNAME ( ) {
		return NAME;
	}
	public int getNUM_OPERANDS ( ) {
		return getFORMAT_TYPE().NUM_OPERANDS;
	}
	public FMT getFORMAT_TYPE ( ) {
		return FORMAT_TYPE;
	}
	public Integer[] getCTRL ( ) {
		return CTRL;
	}
	
	public enum FMT {
		RD_RS_RT(3),
		RT_RS_IMM(3),
		RS_RT_OFFSET(3),
		RT_MEM(2),
		JUMP_ADDR(1),
		NO_OPS(0);
		public final int NUM_OPERANDS;
		private FMT(int numOps){ NUM_OPERANDS = numOps; }
	}
	
	public static InstrSpec findSpec(String opcode){
		return InstructionValidation.SPEC.stream( ).filter( s -> s.getOPCODE().equals(opcode)).findFirst().orElseThrow();
	}
}
