package model;

public class R_Type extends Instruction{
	private final int RD;
	private final int RS;
	private final int RT;
	
	 R_Type( String ins, String[] operands, String comment){
		super(ins, operands, comment);
		RD=Register_Bank.convert2r_reference(operands[0]);
		RS=Register_Bank.convert2r_reference(operands[1]);
		RT=Register_Bank.convert2r_reference(operands[2]);
	}
		
		@Override
	public void execute( ){
		super.execute( );
			ex( );
		}
		
	private boolean ex( ){
		System.out.print( "Reading register RS["+Register_Bank.convertFromR_reference(RS)+": ");
		int rsVal = Register_Bank.read(RS);
		System.out.print( rsVal+"], Reading register RT["+Register_Bank.convertFromR_reference(RT)+": ");
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
				rdVal=Register_Bank.read(RD);//TODO: this shouldn't run, throw exception
				break;
		}
		System.out.println(rdVal);
		System.out.println( "Writing Result\n\tValue: "+rdVal+" to register RD["
		                    +Register_Bank.convertFromR_reference(RD)+"]");
		Register_Bank.store(RD,rdVal);
		return true;
	}
}
