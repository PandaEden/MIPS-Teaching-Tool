package model.components;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import model.instr.I_Type;
import model.instr.Instruction;
import model.instr.J_Type;
import model.instr.R_Type;

import util.ansi_codes.Color;
import util.logs.ExecutionLog;

import java.util.Arrays;
import java.util.Map;

public class Component {
	
	public static Map<Integer,String> ALU_codes =Map.of(-1,"NOP", 0,"SLL",2,"ADD",6, "SUB",7, "SLT");
	public static Integer searchALUCode(@Nullable String Op){
		if ( Op==null )
			Op="NOP";
		for ( Map.Entry<Integer,String> e: ALU_codes.entrySet() )
			if ( e.getValue().equals( Op.toUpperCase() ) )
				return e.getKey();
		throw new IllegalArgumentException("ALU Code for Op["+Op+"], not found!");
	}
	
	/** Integer Multiplexer::Forwards the index matching the control signal, Or null*/
	@Nullable
	public static Integer MUX(@Nullable Integer sig, String name ,Integer... input){
		if ( sig==null ) {
			return null;
		}else if ( sig < input.length) {
			return input[ sig ];
		}else
			throw new IndexOutOfBoundsException("Control Signal["+sig+"] Invalid for Mux["+Arrays.toString(input)+"]");
	}
	
	/** Logs the message {@link util.logs.Logger#appendEx(String)} and returns result of adding the two inputs.
	 <p>If either input is null, addition is not attempted, null is forwarded
	 <p>And the message is not logged. */
	@Nullable
	public static Integer ADDER(Integer input0, Integer input1, @NotNull String message, @NotNull ExecutionLog log){
		if ( input0==null || input1==null )
			return null;
		
		log.append(message);
		return input0+input1;
	}
	
	/** Performs ALU operation based on ALU_OP,
	 <ui>
	   <li>null:[-1] - NOP - no action/output</li>
	   <li>000:[0] - SLL - Shift Left Logical</li>
	   <li>010:[2] - ADD - Addition</li>
	   <li>110:[3] - SUB - Subtraction (Binvert Addition)</li>
	   <li>111:[4] - SLT - Set On Less Than</li>
	 </ui>*/
	public static Integer ALU(Integer input0, Integer input1, Integer ALUCtrl, @NotNull ExecutionLog log){
		int output;
		
		// BInvert is determined by bit[0], SUB/SLT
		String ALU_OP = ALU_codes.get( ALUCtrl==null?-1:ALUCtrl );
		String sign;
		
		if ( !ALU_OP.equals( "NOP" ) && ( input0==null || input1 ==null ))
			throw new IllegalArgumentException("ALU Inputs["+input0+","+input1+"] are null when ALUOp is not NOP");
		
		switch (ALU_OP.toUpperCase()){
			case "NOP": // -- do nothing, for Nop & Exit
				if ( input0!=null )
					log.append( "\tALU Result = " + input0 + " ==> " + Color.fmtUnder( ""+input0 ) );
				return input0;
			case "ADD": // ADD
				output=input0 + input1; sign = " + ";
				break;
			case "SUB": // SUB
				output=input0 - input1; sign = " - ";
				break;
			case "SLL": //Shift Left Logical
				String biteStream = Integer.toBinaryString(input1);
				if (biteStream.length()>5)
					input1 = Integer.parseInt(biteStream.substring(biteStream.length()-5), 2);
				output=input0 << input1; sign = " << ";
				break;
			case "SLT": //Set Less Than
				output=input0 < input1?1:0; sign = " set-on < ";
				break;
			default:
				throw new IllegalStateException("ALU_OP ["+ALU_OP+"] Not Implemented!");
		}
		
		log.append( "\tALU Result = " + input0 + sign + input1 + " ==> " + Color.fmtUnder( ""+output ) );
		return output;
	}
	
	private static final String DECODE =Color.fmtTitle( Color.YELLOW, "Decoding" ) + ":";
	
	/**
	 <ul><li>[0] Destination: 0-RT, 1-RD, 2-$RA</li>
	 <li>[1] ALUSrc1: 0-AIR1, 1-NPC</li>
	 <li>[2] ALUSrc2: 0-AIR2, 1-IMM</li>
	 <li>[3] ALUOp: {@link #ALU_codes}, {@link #searchALUCode(String)}</li>
	 <li>[4] MemAction: 0-Read, 1-Write</li>
	 <li>[5] MemToReg: 0-AOR, 1-LMDR</li>
	 <li>[6] PCWrite: 0-No, 1-Yes</li>
	 </ul>*/
	public static Integer[] DECODER(Instruction ins, ExecutionLog log){
		log.append( DECODE + " ---- " + ins.getType() + " Instruction :: " + ins.getOpcode() );
		log.append( "" );
		Integer[] ctrl = new Integer[7];
		String[] name = new String[7];
		Arrays.fill( name, "-" );
		if ( ins.getOpcode()!=null ) {
			if ( ins instanceof R_Type || ins instanceof I_Type ) {
				ctrl=new Integer[] { 1, 0, 0, 2, null, 0, 0 };
				name=new String[] { "RD", "AIR1", "AIR2", "-", "-", "No:AOR", "NPC" };
				if ( ins instanceof R_Type ) {
					ctrl[ 3 ]=searchALUCode(ins.getOpcode());
				} else {//ins instanceof I_Type
					ctrl[ 0 ]=0;
					name[ 0 ]="RT";
					ctrl[ 2 ]=1;
					name[ 2 ]="IMM";
					ctrl[ 3 ]=2;
					if ( ins.getOpcode( ).equals( "lw" ) ) {
						ctrl[ 4 ]=0;
						name[ 4 ]="READ->LMDR";
						ctrl[ 5 ]=1;
						name[ 5 ]="Yes:LMDR";
					} else if ( ins.getOpcode( ).equals( "sw" ) ) {
						ctrl[ 0 ]=null;
						name[ 0 ]="-";
						ctrl[ 4 ]=1;
						name[ 4 ]="WRITE<-SVR";
						ctrl[ 5 ]=null;
						name[ 5 ]="-";
					}
				}
			} else if ( ins instanceof J_Type ) {
				ctrl[ 6 ]=1;
				name[ 6 ]="IMM";
				
				if ( ins.getOpcode( ).equals( "jal" ) ) {
					ctrl[ 0 ]=2;
					name[ 0 ]="$31:ReturnAddress";
					ctrl[ 1 ]=1;
					name[ 1 ]="NPC";
					ctrl[ 3 ]=-1;
					ctrl[ 5 ]=0;
					name[ 5 ]="No:AOR";
				}
			}
		}
		if ( ctrl[3]!=null )
			name[ 3 ]=ALU_codes.get( ctrl[3] );
		
		log.append( "\tALUSrc1["+name[1]+"], ALUSrc2["+name[2]+"], ALUOp["+name[3]+"]" );
		log.append( "\tMemOp["+name[4]+"], MemToReg["+name[5]+"]" );
		log.append( "\tRegDest["+name[0]+"], PCWrite["+name[6]+"]" );
		
		return ctrl;
	}
}
