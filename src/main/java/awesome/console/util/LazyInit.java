package awesome.console.util;

import java.util.function.Supplier;

/**
 * Thread-safe lazy initialization
 *
 * @author anyesu
 */
public class LazyInit<T> implements Supplier<T> {

    private final Supplier<T> supplier;

    private volatile T value;

    public LazyInit(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public static <V> Supplier<V> lazyInit(Supplier<V> supplier) {
        return new LazyInit<>(supplier);
    }

    @Override
    public T get() {
        if (null == value) {
            synchronized (this) {
                if (null == value) {
                    value = supplier.get();
                }
            }
        }
        return value;
    }
}
