package model.components;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import model.instr.*;

import util.ansi_codes.Color;
import util.logs.ExecutionLog;
import util.validation.InstrSpec;

import java.util.Arrays;
import java.util.Map;

public class Component {
	
	public static Map<Integer,String> ALU_codes =Map.of(-1,"NOP",
														0,"ADD", 1,"SLL", 2, "SUB",
														4, "AND",5, "OR",
														6, "XOR",
														8,"SLT", 9, "SLT|E");
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
	   <li>010:[0] - ADD - Addition</li>
	   <li>000:[1] - SLL - Shift Left Logical</li>
	   <li>110:[2] - SUB - Subtraction (Binvert Addition)</li>
	 		<li>110:[4] - AND</li>
	 		<li>110:[5] - OR</li>
	 		<li>110:[6] - XOR - Exclusive OR</li>
	   <li>111:[8] - SLT - Set On Less Than</li>
	   <li>111:[9] - SLE - Set On Less Than Equal</li>
	 </ui>*/
	public static Integer ALU(Integer input0, Integer input1, Integer ALUCtrl, @NotNull ExecutionLog log){
		int output;
		String bitwise=null;
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
				
			case "AND": // Bitwise AND
				bitwise = " and ";
				output=input0 & input1; sign = " & ";
				break;
			case "OR": // Bitwise OR
				bitwise = " or ";
				output=input0 | input1; sign = " | ";
				break;
			case "XOR": // Bitwise XOR
				bitwise = " xor ";
				output=input0 ^ input1; sign = " ^ ";
				break;
				
			case "SLT": //Set Less Than
				output=input0 < input1?1:0; sign = " set-on < ";
				break;
			case "SLT|E": //Set Less Than
				output=input0 <= input1?1:0; sign = " set-on <= ";
				break;
			default:
				throw new IllegalStateException("ALU_OP ["+ALU_OP+"] Not Implemented!");
		}
		
		if ( bitwise!=null ){
			log.append( "\t (binary) '" + Integer.toBinaryString(input0) +"'"+ bitwise +"'"
						+  Integer.toBinaryString(input1) + "' ==> '" + Color.fmtUnder( Integer.toBinaryString(output)) +"'");
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
	 <li>[4] MemOp: 0-Read, 1-Write</li>
	 <li>[5] MemToReg: 0-AOR, 1-LMDR</li>
	 <li>[6] PCWrite: 0-No, 1-Yes, 2-Based on Cond</li>
	 <li>[7] BranchCond: 0-Eq Zero, 1-NotEq Zero</li>
	 </ul>*/
	public static Integer[] DECODER(Instruction ins, ExecutionLog log){
		String opcode = ins.getOpcode();
		if ( opcode==null )
			opcode="nop";
		
		InstrSpec spec = InstrSpec.findSpec( opcode );
		
		log.append( "\n" );
		log.append( DECODE + "\t----\t" + ins.getType() + " Instruction :: " + (opcode.toUpperCase())+" :: "+spec.getNAME() );
//		if ( ins instanceof R_Type )
//			log.append( "\n\t\t "+opcode+"\tRS["+ins.getRS()+"], RT["+ins.getRT()+"], RD["+ins.getRD()+"], [shamt], [funct]" );
//		else if ( ins instanceof I_Type )
//			log.append( "\n\t\t "+opcode+"\tRS["+ins.getRS()+"], RT["+ins.getRT()+"], 16_IMM["+ins.getImmediate()+"]" );
//		else if ( ins instanceof J_Type )
//			log.append( "\n\t\t "+opcode+"\t26_IMM["+ins.getImmediate()+"]" );
		
		Integer[] ctrl =spec.getCTRL();
		String[] name = new String[8];
		Arrays.fill( name, "-" );
		
		//Dest
		if ( ctrl[0]!=null ) {
			if ( ctrl[ 0 ]==0 )
				name[ 0 ]="RT";
			else if ( ctrl[ 0 ]==1 )
				name[ 0 ]="RD";
			else if ( ctrl[ 0 ]==2 )
				name[ 0 ]="$ReturnAddress:31";
		}//ALUSrc1
		
		if ( ctrl[1]!=null ) {
			if ( ctrl[ 1 ]==0 )
				name[ 1 ]="AIR1";
			else if ( ctrl[ 1 ]==1 )
				name[ 1 ]="NPC";
		}//ALUSrc2
		if ( ctrl[2]!=null ) {
			if ( ctrl[ 2 ]==0 )
				name[ 2 ]="AIR2";
			else if ( ctrl[ 2 ]==1 )
				name[ 2 ]="IMM";
		}//ALUOp
		if ( ctrl[3]!=null ) {
			name[ 3 ]=ALU_codes.get( ctrl[ 3 ] );
		}//MemOp
		if ( ctrl[4]!=null ) {
			if ( ctrl[ 4 ]==0 )
				name[ 4 ]="READ->LMDR";
			else if ( ctrl[ 4 ]==1 )
				name[ 4 ]="WRITE<-SVR";
		}//MemToReg
		if ( ctrl[5]!=null ) {
			if ( ctrl[ 5 ]==0 )
				name[ 5 ]="No:AOR";
			else if ( ctrl[ 5 ]==1 )
				name[ 5 ]="Yes:LMDR";
		}//PCWrite
		if ( ctrl[6]!=null ) {
			if ( ctrl[ 6 ]==0 )
				name[ 6 ]="NPC";
			else if ( ctrl[ 6 ]==1 )
				name[ 6 ]="IMM";
			else if ( ctrl[ 6 ]==2 )
				name[ 6 ]="COND";
		}//BranchCond
		if ( ctrl[7]!=null ) {
			if ( ctrl[ 7 ]==0 )
				name[ 7 ]="Zero";
			else if ( ctrl[ 7 ]==1 )
				name[ 7 ]="Not~Zero";
		}
		log.append( "\tALUSrc1["+name[1]+"], ALUSrc2["+name[2]+"], ALUOp["+name[3]+"],\tRegDest["+name[0]+"]" );
		log.append( "\tMemOp["+name[4]+"], MemToReg["+name[5]+"],\tPCWrite["+name[6]+"], BranchCond["+name[7]+"]" );
		
		return ctrl;
	}
}
