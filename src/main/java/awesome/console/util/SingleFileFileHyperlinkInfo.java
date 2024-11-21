package awesome.console.util;

import static awesome.console.util.FileUtils.findFileByPath;
import static awesome.console.util.FileUtils.resolveSymlink;
import static awesome.console.util.LazyInit.lazyInit;

import com.intellij.execution.filters.FileHyperlinkInfoBase;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author anyesu
 */
@SuppressWarnings("unused")
public class SingleFileFileHyperlinkInfo extends FileHyperlinkInfoBase {

    public static final String DISPOSAL_EXCEPTION_MESSAGE = "Editor is already disposed";

    private final String filePath;

    private final Supplier<VirtualFile> file;

    private final Supplier<VirtualFile> resolvedFile;

    private final BooleanSupplier resolveSymlink;

    public SingleFileFileHyperlinkInfo(
            @NotNull Project project, @NotNull String filePath,
            int row, int col, boolean resolveSymlink
    ) {
        this(project, filePath, row, col, () -> resolveSymlink);
    }

    public SingleFileFileHyperlinkInfo(
            @NotNull Project project, @NotNull String filePath,
            int row, int col, @NotNull BooleanSupplier resolveSymlink
    ) {
        super(project, row > 0 ? row - 1 : 0, col > 0 ? col - 1 : 0);
        this.filePath = filePath;
        this.resolveSymlink = resolveSymlink;
        file = lazyInit(() -> findFileByPath(filePath));
        resolvedFile = lazyInit(() -> findFileByPath(resolveSymlink(filePath, true)));
    }

    @Nullable
    @Override
    protected VirtualFile getVirtualFile() {
        return (resolveSymlink.getAsBoolean() ? resolvedFile : file).get();
    }

    @Override
    public void navigate(@NotNull Project project) {
        VirtualFile file = getVirtualFile();
        if (null == file || !file.isValid()) {
            Messages.showErrorDialog(
                    project,
                    "Cannot find file " + StringUtil.trimMiddle(filePath, 150),
                    "Cannot Open File"
            );
            return;
        }
        try {
            super.navigate(project);
        } catch (RuntimeException e) {
            // ignore DisposalException: Editor is already disposed
            // caused by `IDEA Resolve Symlinks` plugin
            if (!DISPOSAL_EXCEPTION_MESSAGE.equals(e.getMessage())) {
                throw e;
            }
        }
    }

}
