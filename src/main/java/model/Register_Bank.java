package model;

public class Register_Bank {
	private static final Register_Bank INSTANCE= new Register_Bank();;
	public static int[] registers = new int [32];
	
	private static void reset(){
		registers=new int[32];
	}
	private static Register_Bank getInstance(){
		return INSTANCE;
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
	
	public static void printIDs(){
		System.out.print("\t\tid's");
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
