package _test.providers;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import model.components.DataMemory;
import model.components.InstrMemory;

import java.util.*;
import java.util.stream.Stream;

import static _test.providers.Trim.toArgs;

/** Format ( String opcode )*/
public class InstrProvider implements ArgumentsProvider {
	private static final List<String> op_NO_OPS=List.of(
			"exit", "halt"
	);
	private static final List<String> op_RD_RS_RT=List.of(
			"add", "sub"
	);
	private static final List<String> op_RT_RS_IMM=List.of(
			"addi"
	);
	private static final List<String> op_RT_MEM=List.of(
			"lw", "sw"
	);
	private static final List<String> op_RT_RS_INSTR=List.of(
			"beq","bne", "bgt","bge", "blt","ble"
	);
	private static final List<String> op_J=List.of(
			"j", "jal"
	);
	
	private static final List<String> op_Invalid=List.of(
			"panda", "l.d", "s.d", "la", "lui"
	);
	
	public Stream<Arguments> provideArguments(ExtensionContext context) {
		return toArgs( op_NO_OPS, op_RD_RS_RT, op_RT_RS_IMM, op_RT_RS_INSTR, op_RT_MEM, op_J );
	}
	
	public static class Invalid implements ArgumentsProvider {
		public Stream<Arguments> provideArguments(ExtensionContext context) { return toArgs( op_Invalid ); }
		
		public static class Limit_Two implements ArgumentsProvider {
			public Stream<Arguments> provideArguments(ExtensionContext context) { return toArgs( op_Invalid.stream( ).limit( 2 )); }
			
		}
	}
	
	public static final HashMap<String, Integer> labelsMap=new HashMap<>( Map.ofEntries(
			Map.entry( "instr", InstrMemory.BASE_INSTR_ADDRESS ),
			Map.entry( "instr_15_top", InstrMemory.BASE_INSTR_ADDRESS+(4*32767)+4+4 ),
			Map.entry( "instr_top", InstrMemory.OVER_SUPPORTED_INSTR_ADDRESS-4 ),
			Map.entry( "not_instr", InstrMemory.OVER_SUPPORTED_INSTR_ADDRESS ),
			Map.entry( "data", DataMemory.BASE_DATA_ADDRESS ),
			Map.entry( "data_top", DataMemory.OVER_SUPPORTED_DATA_ADDRESS-8 ),
			Map.entry( "not_data", DataMemory.OVER_SUPPORTED_DATA_ADDRESS )
	));
	
	/**Returns all the Keys in the LabelsMap, excluding the one provided*/
	public static Set<String> KeysExcluding(String... keys){
		Set<String> set =new HashSet<>( Set.copyOf( InstrProvider.labelsMap.keySet( ) ) );
		Arrays.stream( keys )
			  .forEach(k -> {
				  if (!set.contains(k))
					  throw new IllegalArgumentException("Check the Keys in the labelMap");}
			  );
		set.removeAll( Arrays.asList( keys ) );
		
		return set;
	}
	public static class NO_OPS implements ArgumentsProvider {
		public Stream<Arguments> provideArguments(ExtensionContext context) { return toArgs( op_NO_OPS ); }
		public static final String OPS = "";
		
	}
	
	public static class RD_RS_RT implements ArgumentsProvider {
		public Stream<Arguments> provideArguments(ExtensionContext context) { return toArgs( op_RD_RS_RT ); }
		public static final String OPS = "$1 , r1, $at";
	}
	
	public static class I {
		public static class RT_RS_IMM implements ArgumentsProvider {
			public Stream<Arguments> provideArguments(ExtensionContext context) { return toArgs( op_RT_RS_IMM ); }
			public static final String OPS = "$1 , r1, -40";
		}
		
		public static class RT_RS_INSTR implements ArgumentsProvider {
			public Stream<Arguments> provideArguments(ExtensionContext context) { return toArgs( op_RT_RS_INSTR ); }
			public static final String OPS_LABEL = "$4 , $12, instr";
			public static final String OPS_IMM = "$1 , r1, 5";
			
		}
		
		public static class RT_MEM implements ArgumentsProvider {
			public Stream<Arguments> provideArguments(ExtensionContext context) { return toArgs( op_RT_MEM ); }
			public static final String OPS_IMM_RS = "$1 , -8 ($1)";
			public static final String OPS_LABEL = "$4 , data";
			
		}
		
	}
	
	public static class J implements ArgumentsProvider {
		public Stream<Arguments> provideArguments(ExtensionContext context) { return toArgs( op_J ); }
		public static final String OPS_LABEL = "instr";
		public static final String OPS_IMM = "0x100020";
		
	}
	
	public static Stream<String> OperandsList_ExcludingNoOps () {
		return Stream.of( RD_RS_RT.OPS, I.RT_RS_IMM.OPS,
						  I.RT_RS_INSTR.OPS_IMM,I.RT_RS_INSTR.OPS_LABEL, // 3 Ops
						  I.RT_MEM.OPS_IMM_RS, 	I.RT_MEM.OPS_LABEL,//2 Ops
						  J.OPS_IMM, 			J.OPS_LABEL); // 1 Op
	}
	
}
