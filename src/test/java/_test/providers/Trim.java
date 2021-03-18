package _test.providers;

import org.junit.jupiter.params.provider.Arguments;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Helper Methods for Providers to use. */
class Trim {
	static <T> List<T> subList_First(List<T> list){return list.subList( 0, 1 );}
	static <T> List<T> subList_Second(List<T> list){return list.subList( 1, 2 );}
	
	static <E> E first(List<E> list){return list.get( 0 );}
	static <E> E second(List<E> list){return list.get( 1 );}
	
	static <E> Stream<Arguments> toArgs(Stream<E> stream){ return stream.map( Arguments::of ); }
	static <E> Stream<Arguments> toArgs(List<E> list){ return toArgs(list.stream()); }
	@SafeVarargs
	static <E> Stream<E> flatMap(List<E>... values){ return Stream.of( values).flatMap( Collection::stream ); }
	@SafeVarargs
	static <E> List<E> combine(List<E>... values){ return flatMap( values ).collect( Collectors.toList() ); }
	@SafeVarargs
	static <E> Stream<Arguments> toArgs(List<E>... lists){ return flatMap( lists).map( Arguments::of );}
	@SafeVarargs
	static <T> Stream<Arguments> toArgs(T... values){ return toArgs( Stream.of( values)); }
	
}
