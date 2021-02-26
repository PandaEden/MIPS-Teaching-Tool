package model;

public class J_Type extends Instruction{
	private final String label;
	
	/**
	 * @param ins
	 * 	- NotNull use code 'no_ins' if not an instruction
	 */
	J_Type( String ins, String label ){
		super(ins);
		this.label=label;
	}
	
	@Override
	public void execute( ){
		System.out.println( "\n\tJ_Type"+" - "+ins+"");
		System.out.print( "\tConverted LABEL: "+label+" to ADDRESS: ");
		long ADDRESS = Memory.getAddress(label);
		System.out.println(ADDRESS);
		System.out.println( "\t\tPC="+Memory.ProgramCounter);
		System.out.println( "\t\t\tDifference="+(ADDRESS-Memory.ProgramCounter));
		Memory.ProgramCounter=ADDRESS;
	}
}
