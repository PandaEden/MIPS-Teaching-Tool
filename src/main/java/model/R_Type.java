package model;

public class R_Type extends Instruction{
private int RD;
private int RS;
private int RT;
	
	 R_Type( String ins, String[] operands, String comment, String tag ){
		super(ins, operands, comment, tag);
		RD=Register_Bank.convert2r_reference(operands[0]);
		RS=Register_Bank.convert2r_reference(operands[1]);
		RT=Register_Bank.convert2r_reference(operands[2]);
	}
		
		@Override
	public boolean execute( ){
		super.execute( );
		System.out.println( "\t\t RD :"+Register_Bank.convertFromR_reference(RD) );
		System.out.print( "\t\t\t RS :"+Register_Bank.convertFromR_reference(RS) );
		System.out.println( "\t\t\t RT :"+Register_Bank.convertFromR_reference(RT) );
		return ex();
	}
		
	private boolean ex( ){
	    int rs = Register_Bank.read(RS);
		int rt = Register_Bank.read(RT);
		int rd;
		switch (ins) {
			case "add":
				rd=rs+rt;
				break;
			case "sub":
				rd=rs-rt;
				break;
			default:
				rd=Register_Bank.read(RD);
				break;
		}
		
		System.out.println( "\t\t\tRD new value:"+rd+"\n");
		return true;
	}
}
