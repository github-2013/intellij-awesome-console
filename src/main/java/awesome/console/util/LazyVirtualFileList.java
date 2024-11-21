package awesome.console.util;

import static awesome.console.util.LazyInit.lazyInit;

import com.intellij.openapi.vfs.VirtualFile;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

/**
 * @author anyesu
 */
@SuppressWarnings("unused")
public class LazyVirtualFileList extends ListDecorator<VirtualFile> {

    private final BooleanSupplier resolveSymlink;

    private final Supplier<List<VirtualFile>> resolvedFiles;

    public LazyVirtualFileList(@NotNull List<VirtualFile> files, boolean resolveSymlink) {
        this(files, () -> resolveSymlink);
    }

    public LazyVirtualFileList(@NotNull List<VirtualFile> files, @NotNull BooleanSupplier resolveSymlink) {
        super(files);
        this.resolveSymlink = resolveSymlink;
        resolvedFiles = lazyInit(() -> FileUtils.resolveSymlinks(list, true));
    }

    @Override
    protected List<VirtualFile> getList() {
        return resolveSymlink.getAsBoolean() ? resolvedFiles.get() : list;
    }
}
