package org.hobbit.benchmark.faceted_browsing.v2.main;

import java.util.function.Supplier;

import org.aksw.commons.accessors.SingleValuedAccessorDirect;


/**
 * Utils for stream like operations on suppliers.
 * TODO Probably get rid of these utils and use streams instead.
 * 
 * @author Claus Stadler, Oct 23, 2018
 *
 */
public class SupplierUtils {
	// Interleave in items of b between each pair of items of a
	// If there is 0 or 1 items in a, b will not be called
	// [a1] [b1] ... [an-1] [bn-1] [an]
	public static <T> Supplier<T> interleave(Supplier<T> a, Supplier<T> b) {
		int toggle[] = {0}; // 0: a-then-b, 1: b-then-a, 2: only-a, 3: only-null
		
		return () -> {
			T r;
			for(;;) {
				if(toggle[0] == 0 || toggle[0] == 2) {
					r = a.get();
					
					toggle[0] = r == null ? 3 : 1;
				} else if(toggle[0] == 1) {
					r = b.get();
					if(r == null) {
						toggle[0] = 2;
						continue;
					} else {
						toggle[0] = 0;
					}
				} else {
					r = null;
				}
				break;
			}

			return r;
		};
	}
	
	public static <T> Supplier<T> limit(int n, Supplier<T> supplier) {
		int i[] = {0};
		return () -> {
			T r = i[0]++ < n ? supplier.get() : null;
			return r;
		};
	}
	
	/**
	 * Flat maps a supplier of suppliers by assuming an inner supplier is finished upon returning null
	 * 
	 * @param supplier
	 * @return
	 */
	public static <T> Supplier<T> flatMap(Supplier<? extends Supplier<? extends T>> supplier) {
		
		// When done, ref's value will be set to null
		// On init, ref's value is a *reference* (to null)
		SingleValuedAccessorDirect<SingleValuedAccessorDirect<Supplier<? extends T>>> ref =
				new SingleValuedAccessorDirect<>(new SingleValuedAccessorDirect<>(null));
				
		return () -> {
			SingleValuedAccessorDirect<Supplier<? extends T>> held = ref.get();
			T r = null;
			if(held != null) {
				while(r == null) {
					Supplier<? extends T> current = held.get();
					
					if(current == null) {
						Supplier<? extends T> next = supplier.get();
						// If we get null for the next supplier, we assume we are done
						if(next == null) {
							ref.set(null);
							break;
						} else {
							held.set(next);
						}
						continue;
					}

					r = current.get();
				}
			}
			return r;
		};
	}

}