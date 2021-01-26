package model;

import java.security.InvalidParameterException;
import java.util.Arrays;

public class Instruction {
	private enum Type {REGISTER, IMMEDIATE, JUMP, EXIT}
	private final Type type;
	 String ins;
	
	/**
	 *
	 * @param ins - NotNull use code 'no_ins' if not an instruction
	 */
	 Instruction( String ins){
		this.ins=ins;
		this.type=ins2Type(ins);
	}
	
	public static Instruction buildInstruction( String ins, String[] operands ){
		Type type = ins2Type(ins);
		if (type==Type.REGISTER)
			return new R_Type(ins, operands);
		
		if (type==Type.IMMEDIATE) //TODO: Branch instructions check
			return new I_Type(ins, operands, false);
		else return new Instruction(ins);
	}
	
	public void execute(){
		System.out.println( "\n\t"+type2String(type)+" - "+ins+"");
		if (type == Type.REGISTER | type == Type.IMMEDIATE) {
			Register_Bank.printIDs();
			Register_Bank.printT();
			Register_Bank.printS();
		}
	}
	
	public boolean isEXIT(){
	 	return type==Type.EXIT;
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
				return  Type.EXIT;
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
			case EXIT:
				return "EXIT";
			default:
				throw new InvalidParameterException();
		}
	}
	
	@Override
	public String toString( ){
		return "Instruction{"+type+"\t"+ins+"} ";
	}
}
