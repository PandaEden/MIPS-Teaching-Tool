package model;

import java.util.Arrays;

public class Instruction {
	private enum Type {REGISTER, IMMEDIATE, JUMP, NO_INS}
	private final Type type;
	 String ins;
	private final String[] operands;
	private final String comment;
	
	/**
	 *
	 * @param ins - NotNull use code 'no_ins' if not an instruction
	 * @param operands - Nullable if opcode is 'no_ins'
	 * @param comment - Nullable, takes the form '# comment...' at the end of a line
	 */
	 Instruction( String ins, String[] operands, String comment){
		this.ins=ins;
		this.operands=operands;
		this.comment=(comment);
		this.type=ins2Type(ins);
	}
	
	public static Instruction buildInstruction( String ins, String[] operands, String comment){
		Type type = ins2Type(ins);
		if (type==Type.REGISTER)
			return new R_Type(ins, operands, comment);
		
		if (type==Type.IMMEDIATE) //TODO: Branch instructions check
			return new I_Type(ins, operands, comment, false);
		else return new Instruction(ins, operands, comment);
	}
	
	public void execute(){
		System.out.println( "\n\t"+type2String(type)+" - "+ins+"");
		//if (type == Type.REGISTER | type == Type.IMMEDIATE) {
			Register_Bank.printIDs();
			Register_Bank.printT();
			Register_Bank.printS();
		//}
	}
	
	int calculateAddress(){return -1;}
	
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
				return "Err: NO_INS";
		}
	}
	
	@Override
	public String toString( ){
		return "Instruction{"+type+"\t"+ins+"} ";
		       //+comment;
	}
}
