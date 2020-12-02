package model;

public class I_Type extends Instruction {
	
	private enum SubType {EX, LOAD, STORE, BRANCH}
	SubType subType;
	private int RS;
	private int RT;
	private int IMM;
	private String LABEL;
	private int ADDRESS;
	
	I_Type( String ins, String[] operands, String comment, String tag, boolean isBranch ){
		super(ins, operands, comment, tag);
		if (isBranch) this.subType=SubType.BRANCH;
		else if (ins.contains("s")) this.subType=SubType.STORE;
		else if (ins.contains("l")) this.subType=SubType.LOAD;
		else this.subType=SubType.EX;
		
		int firstOp=Register_Bank.convert2r_reference(operands[0]);
		String secondOp=operands[1];
		//2 Opperands are garuntted for an I-Instruction, the 3rd might be null
		//Second opperand might not be register
		
		//set first opperand
		if (subType==SubType.BRANCH) //opperands [rs, label] or [rs, rt, label]
			RS=firstOp;
		else //opperands [rt, rs, imm] or [rt, imm(rs)] or [rt, imm]
			RT=firstOp;
		
		//second opperand depends on number of opperands
		// >2 opperands mean three comma-space ", " seperated values.
		// while Imm($rs) are two opperands, they will be read as one.
		//if only 2 opperands
		if (operands.length==2)//Branch[rs, label] or Load/Store[rt, imm/imm(rs)]
			allocateImm(secondOp);
		else {//Branch[rs, rt, Label] or arithmatic[rt, rs, imm]
			int registerNum=Register_Bank.convert2r_reference(secondOp);
			if (subType==SubType.BRANCH)
				RT = registerNum;
			else
				RS = registerNum;
			
			allocateImm(operands[2]);//3rd Operand either Label or Imm
		}
	}
	
	private  void allocateImm(String ImmRs){
		//check if String contains '(', then it contains $RS
		if (ImmRs.contains("(")){//split Imm($RS) into Imm and RS
			String split[] = ImmRs.split("\\(");
			IMM = Integer.parseInt(split[0]);
			//split[1] contains elements '$' 's/t' 'int' ')' . need to remove the ')'
			String temp = split[1].split("\\)")[0];
			RS=Register_Bank.convert2r_reference(temp);
		}else{ //Imm or Label
			if (subType==SubType.BRANCH)
				LABEL = ImmRs;
			else
				IMM = Integer.parseInt(ImmRs); //TODO Branch Labels are text,
			// the releative address should be calculated here for Branch instructions
		}
	}
	
	@Override
	public boolean execute( ){
		super.execute( );
		if (subType==SubType.EX)
			return ex( );
		else
			return true;
	}
	
	private boolean ex( ){
		System.out.print( "\tReading register RS: "+Register_Bank.convertFromR_reference(RS) +"[");
		int rsVal = Register_Bank.read(RS);
		System.out.println( "], IMMEDIATE : "+IMM+"]");
		System.out.print( "\t Calculating Result = ");
		System.out.print( "RS+IMMEDIATE = "+rsVal+"+"+IMM+" = ");
		int rtVal = rsVal+IMM;
		System.out.println(rtVal);
		System.out.println( "\tWriting Result Value: "+rtVal+" to register RT: "+RT+"\n");
		Register_Bank.store(RT,rtVal);
		return true;
	}
	
	private int calculateImmRs(){
//		IMM;
//		RS;
		return -1;//TODO:
	}
}
