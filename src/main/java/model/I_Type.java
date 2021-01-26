package model;

public class I_Type extends Instruction {
	
	private enum SubType {EX, LOAD, STORE, BRANCH}
	private final SubType subType;
	private int RS;
	private int RT;
	private int IMM;
	private int ADDRESS;
	
	I_Type( String ins, String[] operands, boolean isBranch ){
		super(ins);
		if (isBranch) this.subType=SubType.BRANCH;
		else if (ins.contains("s")) this.subType=SubType.STORE;
		else if (ins.contains("l")) this.subType=SubType.LOAD;
		else this.subType=SubType.EX;
		
		int firstOp=Register_Bank.convert2r_reference(operands[0]);
		String secondOp=operands[1];
		//2 Operands are guaranteed for an I-Instruction, the 3rd might be null
		//Second operand might not be register
		
		//set first operand
		if (subType==SubType.BRANCH) //operands [rs, label] or [rs, rt, label]
			RS=firstOp;
		else //operands [rt, rs, imm] or [rt, imm(rs)] or [rt, imm]
			RT=firstOp;
		
		//second operand depends on number of operands
		// >2 operands mean three comma-space ", " separated values.
		// while Imm($rs) are two operands, they will be read as one.
		//if only 2 operands
		if (operands.length==2)//Branch[rs, label] or Load/Store[rt, imm/imm(rs)]
			allocateImm(secondOp);
		else {//Branch[rs, rt, Label] or arithmetic[rt, rs, imm]
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
			String[] split= ImmRs.split("\\(");
			IMM = Integer.parseInt(split[0]);
			//split[1] contains elements '$' 's/t' 'int' ')' . need to remove the ')'
			String temp = split[1].split("\\)")[0];
			RS=Register_Bank.convert2r_reference(temp);
		}else{ //Imm or Label
			IMM = Integer.parseInt(ImmRs);
		}
	}
	
	@Override
	public void execute( ){
		super.execute( );
		if (subType==SubType.EX) {
			ex( );
		}
	}
	
	private boolean ex( ){
		System.out.print( "Reading register RS["+Register_Bank.convertFromR_reference(RS)+" ");
		int rsVal = Register_Bank.read(RS);
		System.out.println( rsVal+"], [IMMEDIATE: "+IMM+"]");
		System.out.print( "Calculating Result:\n\tRT = ");
		System.out.print( "RS+IMMEDIATE = "+rsVal+"+"+IMM+" = ");
		int rtVal = rsVal+IMM;
		System.out.println(rtVal);
		System.out.println( "Writing Result:\n\tValue: "+rtVal+" to register RT["
		                    +Register_Bank.convertFromR_reference(RT)+"]");
		Register_Bank.store(RT,rtVal);
		return true;
	}
	
	private int calculateImmRs(){
//		IMM;
//		RS;
		return -1;//TODO:
	}
}
