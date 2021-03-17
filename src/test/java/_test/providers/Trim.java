package _test_util.providers;

import java.util.List;

public class Trim {
	static <T> List<T> listOf1(List<T> list){return list.subList( 0, 1 );}
	static <E> E first(List<E> list){return list.get( 0 );}
}
