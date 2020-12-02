package model;

import java.util.Arrays;

public class Instruction {
	private enum Type {REGISTER, IMMEDIATE, JUMP, NO_INS}
	private Type type;
	 String ins;
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
	 Instruction( String ins, String[] operands, String comment, String tag ){
		this.ins=ins;
		this.operands=operands;
		this.comment=(comment);
		this.tag=tag;
		this.type=ins2Type(ins);
	}
	
	public static Instruction buildInstruction( String ins, String[] operands, String comment,
	                                            String tag ){
		Type type = ins2Type(ins);
		if (type==Type.REGISTER){
			return new R_Type(ins, operands, comment, tag);
		}
		else return new Instruction(ins, operands, comment, tag);
	}
	
	public boolean execute(){
		Register_Bank.printT();
		Register_Bank.printS();
		System.out.println( "\t"+type2String(type)+" - "+ins+"\n");
		return true;
	}
	
	public String getIns( ){
		return ins;
	}
	
	public String[] getOperands( ){
		
		Arrays.stream( operands ).forEach(System.out::println);
		return operands;
	}
	
	public String getComment( ){
		return comment;
	}
	
	private static Type ins2Type(String ins){
		switch (ins){
			case "add":
			case "sub":
			case "mul":
			case "div":
				return Type.REGISTER;
			case "addi":
			case "lw":
			case "sw":
				return Type.IMMEDIATE;
			case "j":
			case "jal":
				return Type.JUMP;
			default:
				return  Type.NO_INS;
		}
	}
	
	private static String type2String(Type type){
		switch (type){
			case REGISTER:
				return "R-Type";
			case IMMEDIATE:
				return "I-Type";
			case JUMP:
				return "J-Type";
			default:
				return "no_ins";
		}
	}
	
	@Override
	public String toString( ){
		return "Instruction{"+type+"\t"
		       +(tag.isEmpty()?"":tag+" ")
		       +(type.equals(Type.NO_INS)?"":ins)
		         +"} "+comment;
	}
}
