package awesome.console.util;

import com.intellij.execution.filters.FileHyperlinkInfo;
import com.intellij.execution.filters.HyperlinkInfoBase;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * fix built-in MultipleFilesHyperlinkInfo, `popup.showInFocusCenter()` should be changed to `popup.showCenteredInCurrentWindow(project)`
 * <p>
 * ref: https://github.com/JetBrains/intellij-community/blob/212.5080/platform/lang-impl/src/com/intellij/execution/filters/impl/MultipleFilesHyperlinkInfo.java#L116
 *
 * @author anyesu
 */
public class MultipleFilesHyperlinkInfoWrapper extends HyperlinkInfoBase implements FileHyperlinkInfo {

    private final HyperlinkInfoBase hyperlinkInfoBase;

    public MultipleFilesHyperlinkInfoWrapper(@NotNull HyperlinkInfoBase hyperlinkInfoBase) {
        this.hyperlinkInfoBase = hyperlinkInfoBase;
    }

    @Override
    public void navigate(@NotNull Project project, @Nullable RelativePoint hyperlinkLocationPoint) {
        if (null == hyperlinkLocationPoint) {
            // Because we don’t know the actual width of the `popup`, we can only temporarily set the
            // coordinates of the upper left corner of the `popup` to the center of the project window.
            hyperlinkLocationPoint = getProjectCenter(project);
        }
        hyperlinkInfoBase.navigate(project, hyperlinkLocationPoint);
    }

    private RelativePoint getProjectCenter(@NotNull Project project) {
        try {
            JFrame frame = WindowManager.getInstance().getFrame(project);
            if (null == frame) {
                return null;
            }
            JRootPane rootPane = SwingUtilities.getRootPane(frame);
            if (null == rootPane) {
                return null;
            }
            return RelativePoint.getCenterOf(rootPane);
        } catch (Throwable ignored) {
        }
        return null;
    }

    @Nullable
    @Override
    public OpenFileDescriptor getDescriptor() {
        if (hyperlinkInfoBase instanceof FileHyperlinkInfo) {
            return ((FileHyperlinkInfo) hyperlinkInfoBase).getDescriptor();
        }
        return null;
    }
}
