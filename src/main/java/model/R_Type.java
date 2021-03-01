package model;

import util.Convert;

import java.security.InvalidParameterException;

public class R_Type extends Instruction{
	private final int RD;
	private final int RS;
	private final int RT;
	
	 R_Type( String ins, String[] operands){
		super(ins);
		RD=Convert.r2Index(Convert.named2R(operands[0]));
		RS=Convert.r2Index(Convert.named2R(operands[1]));
		RT=Convert.r2Index(Convert.named2R(operands[2]));
	}
		
		@Override
	public void execute( ){
		super.execute( );
			ex( );
		}
		
	private boolean ex( ){
		System.out.print( "Reading register RS["+RS+": ");
		int rsVal = Register_Bank.read(RS);
		System.out.print( rsVal+"], Reading register RT["+RT+": ");
		int rtVal = Register_Bank.read(RT);
		System.out.println( rtVal+"]");
		System.out.print( "Calculating Result:\n\tRD = ");
		int rdVal;
		switch (ins) {
			case "add":
				System.out.print( "RS+RT = "+rsVal+"+"+rtVal+" = ");
					rdVal=rsVal+rtVal;
				break;
			case "sub":
				System.out.print( "RS-RT = "+rsVal+"-"+rtVal+" = ");
				rdVal=rsVal-rtVal;
				break;
			default:
				throw new InvalidParameterException( "Instruction:{"+ins+"} not recognised or " +
				                                     "Implemented" );
		}
		System.out.println(rdVal);
		System.out.println( "Writing Result\n\tValue: "+rdVal+" to register RD["
		                    +RD+"]");
		Register_Bank.store(RD,rdVal);
		return true;
	}
}
