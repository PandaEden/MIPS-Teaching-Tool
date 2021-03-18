package _test.providers;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import util.Convert;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static _test.providers.Trim.toArgs;

public class RegisterProvider {
	public static List<String> addSign(List<String> list){
		return list.stream().map(li-> "$"+li).collect( Collectors.toList());
	}
	public static List<String> getR(){
		List<String> rtn = new LinkedList<>();
		for ( int i=0; i<32; i++ ) {
			rtn.add( "r"+i );
		}
		return rtn;
	}
	public static List<String> getNamed(){
		return Arrays.asList( Convert.namedRegisters.clone( ) );
	}
	/** (Index, R, Named, $index, $R, $Named)*/
	public static List<String[]> getRegisters () {
		List<String[]> args = new LinkedList<>();
		List<String> named = getNamed();
		int i=0;
		for (String n:named){
			args.add( new String[]{""+i,"r"+i, n, "$"+i, "$r"+i, "$"+n } );
			i++;
		}
		return args;
	}
	
	/** (Index, R, Named, $index, $R, $Named)*/
	public Stream<Arguments> provideArguments (ExtensionContext context) {
		List<Arguments> args = new LinkedList<>();
		List<String> named = getNamed();
		int i=0;
		for (String n:named){
			args.add( Arguments.of(i,"r"+i, n, "$"+i, "$r"+i, "$"+n ) );
			i++;
		}
		return toArgs(args);
	}
	
	public static class ZERO implements ArgumentsProvider {
		private final List<String> zero = List.of( "$zero", "zero", "$r0", "r0", "$0" );
		public Stream<Arguments> provideArguments(ExtensionContext context) {
			return toArgs( zero );
		}
		public boolean isZero(String reg){ return (zero.contains( reg )); }
	}
	
}
