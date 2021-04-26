package util.validation;

import model.instr.Instruction;

import util.logs.ExecutionLog;

public class InstrSpec {
	private final String OPCODE;
	private final String NAME;
	private final int OPS_SPLIT;
	private final FMT FORMAT_TYPE;
	/** {@link model.components.Component#DECODER(Instruction, ExecutionLog)} */
	private final Integer[] CTRL;
	
	InstrSpec (String OPCODE, String NAME, int OPS_SPLIT, FMT FORMAT_TYPE, Integer[] CTRL) {
		this.OPCODE=OPCODE;
		this.NAME=NAME;
		this.OPS_SPLIT=OPS_SPLIT;
		this.FORMAT_TYPE=FORMAT_TYPE;
		this.CTRL=CTRL;
	}
	
	public String getOPCODE ( ) {
		return OPCODE;
	}
	public String getNAME ( ) {
		return NAME;
	}
	public int getOPS_SPLIT ( ) {
		return OPS_SPLIT;
	}
	public FMT getFORMAT_TYPE ( ) {
		return FORMAT_TYPE;
	}
	public Integer[] getCTRL ( ) {
		return CTRL;
	}
	
	public enum FMT {
		RD_RS_RT,
		RT_RS_IMM,
		RT_RS_INSTR,
		RT_MEM,
		JUMP_ADDR,
		NO_OPS
	}
	
	public static InstrSpec findSpec(String opcode){
		return InstructionValidation.SPEC.stream( ).filter( s -> s.getOPCODE().equals(opcode)).findFirst().orElseThrow();
	}
}
