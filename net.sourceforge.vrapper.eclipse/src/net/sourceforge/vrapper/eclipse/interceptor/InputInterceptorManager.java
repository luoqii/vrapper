package net.sourceforge.vrapper.eclipse.interceptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.vrapper.eclipse.activator.Activator;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.MultiEditor;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * Listener which adds an {@link InputInterceptor} from the underlying factory
 * to editors which are opened.
 *
 * @author Matthias Radig
 */
public class InputInterceptorManager implements IPartListener {

    private static final Method METHOD_GET_PAGE_COUNT = getMultiPartEditorMethod("getPageCount");
    private static final Method METHOD_GET_EDITOR = getMultiPartEditorMethod("getEditor", Integer.TYPE);

    private final InputInterceptorFactory factory;
    private final IWorkbenchWindow window;
    private final Map<IWorkbenchPart, InputInterceptor> interceptors;
    private boolean active;

    public InputInterceptorManager(InputInterceptorFactory factory, IWorkbenchWindow window) {
        super();
        this.factory = factory;
        this.window = window;
        this.interceptors = new HashMap<IWorkbenchPart, InputInterceptor>();
        this.active = true;
    }

    public void deactivate() {
        active = false;
        removeIfNecessary();
    }

    public void partDeactivated(IWorkbenchPart arg0) {
    }

    public void partOpened(IWorkbenchPart part) {
        if (!active) {
            return;
        }
        if (part instanceof AbstractTextEditor) {
            AbstractTextEditor editor = (AbstractTextEditor) part;
            try {
                Method me = AbstractTextEditor.class
                .getDeclaredMethod("getSourceViewer");
                me.setAccessible(true);
                Object viewer = me.invoke(editor);
                // test for needed interfaces
                ITextViewerExtension textViewer = (ITextViewerExtension) viewer;
                InputInterceptor interceptor = factory.createInterceptor(window, editor, (ITextViewer)textViewer);
                textViewer.prependVerifyKeyListener(interceptor);
                interceptors.put(part, interceptor);
                Activator.getDefault().registerEditor(editor);
            } catch (SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        } else if (part instanceof MultiPageEditorPart) {
            multiPartOpened(part);
        } else if (part instanceof MultiEditor) {
            for (IEditorPart subPart : ((MultiEditor) part).getInnerEditors()) {
                partOpened(subPart);
            }
        }
    }

    private void multiPartOpened(IWorkbenchPart part) {
        try {
            MultiPageEditorPart mPart = (MultiPageEditorPart) part;
            int pageCount = ((Integer) METHOD_GET_PAGE_COUNT.invoke(part)).intValue();
            for (int i = 0; i < pageCount; i++) {
                IEditorPart subPart = (IEditorPart) METHOD_GET_EDITOR.invoke(mPart, i);
                partOpened(subPart);
            }
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void partClosed(IWorkbenchPart part) {
        InputInterceptor interceptor = interceptors.remove(part);
        // remove the listener in case the editor gets cached
        if (interceptor != null) {
                try {
                    Method me = AbstractTextEditor.class
                        .getDeclaredMethod("getSourceViewer");
                    me.setAccessible(true);
                    Object viewer = me.invoke(part);
                    // test for needed interfaces
                    ITextViewerExtension textViewer = (ITextViewerExtension) viewer;
                    textViewer.removeVerifyKeyListener(interceptor);
                } catch (SecurityException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        }
        if (part instanceof IEditorPart) {
            Activator.getDefault().unregisterEditor((IEditorPart)part);
        }
        if (part instanceof MultiPageEditorPart) {
            multiPartClosed(part);
        } else if (part instanceof MultiEditor) {
            for (IEditorPart subPart : ((MultiEditor) part).getInnerEditors()) {
                partClosed(subPart);
            }
        }
        removeIfNecessary();
    }

    private void multiPartClosed(IWorkbenchPart part) {
        try {
            MultiPageEditorPart mPart = (MultiPageEditorPart) part;
            int pageCount = ((Integer) METHOD_GET_PAGE_COUNT.invoke(part)).intValue();
            for (int i = 0; i < pageCount; i++) {
                IEditorPart subPart = (IEditorPart) METHOD_GET_EDITOR.invoke(mPart, i);
                partClosed(subPart);
            }
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void partActivated(IWorkbenchPart arg0) {
    }

    public void partBroughtToTop(IWorkbenchPart arg0) {
    }

    private void removeIfNecessary() {
        if (!active && interceptors.isEmpty()) {
            window.getPartService().removePartListener(this);
        }
    }

    private static Method getMultiPartEditorMethod(String name, Class<?>... args) {
        try {
            Method m = MultiPageEditorPart.class.getDeclaredMethod(name, args);
            m.setAccessible(true);
            return m;
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

}
