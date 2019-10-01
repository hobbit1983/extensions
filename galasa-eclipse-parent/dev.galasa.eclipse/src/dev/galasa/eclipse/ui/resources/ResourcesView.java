package dev.galasa.eclipse.ui.resources;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.part.ViewPart;

import dev.galasa.eclipse.ui.CollapseAllAction;
import dev.galasa.eclipse.ui.ExpandAllAction;
import dev.galasa.eclipse.ui.ICollapseAllListener;
import dev.galasa.eclipse.ui.IExpandAllListener;
import dev.galasa.eclipse.ui.runs.RunsContentProvider;
import dev.galasa.eclipse.ui.runs.RunsLabelProvider;

/**
 * Displays a view with all the resources in use the Galasa ecosystem
 * 
 * The ResourcesParent will run a job to retrieve all current resources.  It will also watch for updates to resources.
 * There is is a chance the view might get slightly out of date, due to the time between
 * retrieve all and the watches.
 * 
 * TODO add a refresh button
 * 
 * @author Michael Baylis
 *
 */
public class ResourcesView extends ViewPart implements ICollapseAllListener, IExpandAllListener {
	
	public static final String ID = "dev.galasa.eclipse.ui.runs.ResourcesView";
	
	private TreeViewer viewer;
	
	private ResourcesTreeBase treeBase = new ResourcesTreeBase();

	private boolean disposed = false;

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(2, false));
		parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		
		
		GridData gridData = new GridData(); // container for treeViewer
		gridData.horizontalSpan = 1;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;

		viewer.getControl().setLayoutData(gridData);
		
		MenuManager contextMenu = new MenuManager();
		Menu menu = contextMenu.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(contextMenu, viewer);
		getSite().setSelectionProvider(viewer);

		viewer.setContentProvider(new RunsContentProvider());
		ColumnViewerToolTipSupport.enableFor(viewer);
		viewer.setLabelProvider(new RunsLabelProvider());
//		viewer.setSorter(new AutomationRestSorter());

		viewer.setAutoExpandLevel(2);
		viewer.setInput(treeBase);
		
		treeBase.setView(this);
		
		IToolBarManager toolBarMngr = getViewSite().getActionBars().getToolBarManager();
		toolBarMngr.add(new CollapseAllAction(this));
		toolBarMngr.add(new ExpandAllAction(this));
	
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
		
	}
	
	@Override
	public void dispose() {
		this.disposed = true;
		this.treeBase.dispose();
		super.dispose();
	}

	public void refresh(Object element) {
		if (disposed) {
			return;
		}
		
		//*** This method has to run on the UI thread, so switch if required
		if (Display.getCurrent() == null) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					refresh(element);
				}
			});
			return;
		}

		//*** Now running on the UI thread

		viewer.refresh(element, true);
	}

	public void expand(Object element) {
		if (disposed) {
			return;
		}
		
		//*** This method has to run on the UI thread, so switch if required
		if (Display.getCurrent() == null) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					expand(element);
				}
			});
			return;
		}

		//*** Now running on the UI thread

		viewer.expandToLevel(element, 1);
	}

	@Override
	public void expandAll() {
		this.viewer.expandAll();
	}

	@Override
	public void collapseAll() {
		this.viewer.collapseAll();
	}

}
