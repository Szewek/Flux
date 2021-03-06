package szewek.fl.util;

import net.minecraft.util.Direction;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * Simple caching mechanism for getting specific capabilities from neighbors.
 * Invalidated lazy objects are removed automatically.
 * @param <T> Capability class type
 */
public class SideCached<T> {
	@SuppressWarnings("unchecked")
	private final LazyOptional<T>[] cacheArray = (LazyOptional<T>[]) new LazyOptional[6];
	private final Function<Direction, LazyOptional<T>> getFromDir;

	public SideCached(Function<Direction, LazyOptional<T>> dirFn) {
		getFromDir = dirFn;
	}

	@Nullable
	public T getCached(Direction dir) {
		final int d = dir.ordinal();
		LazyOptional<T> lazy = cacheArray[d];
		if (lazy == null) {
			lazy = getFromDir.apply(dir);
			if (lazy.isPresent()) {
				lazy.addListener(l -> cacheArray[d] = null);
				cacheArray[d] = lazy;
			}
		}
		return lazy.orElse(null);
	}

	public void clear() {
		for (int i = 0; i < 6; i++) {
			cacheArray[i] = null;
		}
	}
}
