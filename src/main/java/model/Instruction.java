package model;

import java.util.Arrays;



public  class Instruction {
	private enum Type {REGISTER, IMMEDIATE, JUMP, NO_INS}
	private Type type;
	private String ins;
	private String[] operands;
	private String comment;
	private String tag;
	
	/**
	 *
	 * @param ins - NotNull use code 'no_ins' if not an instruction
	 * @param operands - Nullable if opcode is 'no_ins'
	 * @param comment - Nullable, takes the form '# comment...' at the end of a line
	 * @param tag - Nullable takes the form 'word:' at the beginning of a line
	 */
	public Instruction( String ins, String[] operands, String comment, String tag ){
		this.ins=ins;
		this.operands=operands;
		this.comment=comment;
		this.tag=tag;
		insTypeLookup();
	}
	
	public String printIns( ){
		return ins;
	}
	
	public String[] printOperands( ){
		
		Arrays.stream( operands ).forEach(System.out::println);
		return operands;
	}
	
	public String printComment( ){
		return comment;
	}
	
	private String insTypeLookup(){
		String ins_type;
		switch (ins){
			case "add":
			case "sub":
			case "mul":
			case "div":
				ins_type = "R-Type";
				type = Type.REGISTER;
				break;
			case "addi":
			case "lw":
			case "sw":
				ins_type = "I-Type";
				type = Type.IMMEDIATE;
				break;
			case "j":
			case "jal":
				ins_type = "J-Type";
				type = Type.JUMP;
				break;
			default:
				ins_type = "no_ins";
				type = Type.NO_INS;
		}
		return ins_type;
	}
	
	@Override
	public String toString( ){
		return "Instruction{"+type+"\t"
		       +(tag.isEmpty()?"":tag+" ")
		       +(type.equals(Type.NO_INS)?"":ins)
		         +"} "+comment;
	}
}
