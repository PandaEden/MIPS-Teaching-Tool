package model;

public class Register_Bank {
	private static final Register_Bank instance = new Register_Bank();;
	public static int[] registers = new int [32];
	
	private static void reset(){
		registers=new int[32];
	}
	private static Register_Bank getInstance(){
		return instance;
	}
	
	private static final int S_START=16;
	private static final int T_START=8;
	private static final int T2_START=24;
	
	
	
	/**
	 * @param register assumes $sX or $tX where X is an integer 0-9
	 *                 String index 2:s/t 3:int .. remainder ignored
	 * $zero is special R0
	 * $s0-7, $t0-7,8,9
	 * @return -1 if unsupported register, or value between 0-31 - index of register in
	 * register_bank
	 */
	public static int convert2r_reference(String register){
		register = register.toLowerCase();
		if (register.equals("$zero"))
			return 0;
		
		//s0 starts at R16, and s7 ends at S23
		if (register.contains("s"))
			return Integer.parseInt(register.substring(2))+S_START;
		
		//t0 starts at R8 till t7 at R15,  then t8,t9 are R24,R25
		if (register.contains("t")) {
			int t = Integer.parseInt(register.substring(2));
			if (t<8)
				return (t+T_START);
			else return (t-8+T2_START);
		}
		
		return -1;
	}
	
	public static String convertFromR_reference(int i){
		if (i==0)
			return "$ZERO";
		
		if (i>=T_START) {
			if (i<S_START)
				return "$T"+(i-T_START);
			else if (i<T2_START)
				return "$S"+(i-S_START);
			else if (i<=T2_START+1)
				return "$T"+(i-T2_START+8);
		}
		return "unsupported";
	}
	
	public static void store(String register, int value){
		store(convert2r_reference(register), value);
	}
	public static void store(int register, int value){
		registers[register]=value;
	}
	
	public static int read(String register){
		return read(convert2r_reference(register));
	}
	public static int read(int register){
		return registers[register];
	}
	
	public static void printN(){
		System.out.print("\t\tidx");
		for(int i=0; i<10; i++){
			System.out.print( i+": ");
		}
		System.out.println(" ");
	}
	public static void printS(){
		System.out.print("\t\tS { ");
		for(int i=S_START; i<S_START+8; i++){
			System.out.print( /**i+": "+**/registers[i]+(i==7?" ":", ") );
		}
		System.out.println("}");
	}
	public static void printT(){
		System.out.print("\t\tT { ");
		for(int i=T_START; i<S_START; i++){
			System.out.print( /**i+": "+**/registers[i]+", " );
		}
		System.out.print( /**T2_START+": "+**/registers[T2_START]+", " );
		System.out.print( /**(T2_START+1)+": "+**/registers[T2_START+1]+" " );
		System.out.println("}");
	}
}
