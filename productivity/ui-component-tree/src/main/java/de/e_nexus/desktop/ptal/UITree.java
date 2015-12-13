package de.e_nexus.desktop.ptal;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

@Named
public final class UITree extends JFrame implements Runnable, ComponentListener {

	public void run() {
		try {
			while (isVisible()) {
				thread.sleep(1000);
				if (autoRefresh.isSelected()) {
					refresh.doClick();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private final DefaultMutableTreeNode root = new DefaultMutableTreeNode("<root>");
	private final JTree tree = new JTree(root);

	private final JScrollPane scrollPane = new JScrollPane(tree);

	private final JButton close = new JButton(new AbstractAction("Close") {

		public void actionPerformed(ActionEvent e) {
			dispose();

		}
	});
	private AbstractAction refreshAction = new AbstractAction("Refresh") {

		public void actionPerformed(ActionEvent e) {
			getRoot().removeAllChildren();
			addRecursive(getTarget(), getRoot(), "");
			DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
			model.nodeStructureChanged(getRoot());
			for (int i = 0; i < tree.getRowCount(); i++) {
				tree.expandRow(i);
			}
			tree.setRootVisible(false);
		}

		public void addRecursive(Container container, DefaultMutableTreeNode node, String comment) {
			DefaultMutableTreeNode relativeRoot = new DefaultMutableTreeNode(describe(container) + comment);
			comment = "";
			node.add(relativeRoot);
			Component[] components = container.getComponents();
			for (Component component : components) {
				if (component instanceof Container) {
					Container c = (Container) component;
					addRecursive(c, relativeRoot, comment);
					if (c instanceof JTabbedPane) {
						JTabbedPane tp = (JTabbedPane) c;
						for (int i = 0; i < tp.getTabCount(); i++) {
							Component cc = tp.getComponentAt(i);
							if (cc instanceof Container) {
								Container c2 = (Container) cc;
								addRecursive(c2, (DefaultMutableTreeNode) relativeRoot.getLastChild(),
										comment + "{" + tp.getTitleAt(i) + "}");
							}
						}
					}
				}
			}
		}

		private String describe(Container container) {
			String n = container.getClass().getSimpleName();
			Container p = container.getParent();
			if (p != null) {
				if (p.getLayout() instanceof BorderLayout) {
					BorderLayout borderLayout = (BorderLayout) p.getLayout();
					Object constraints = borderLayout.getConstraints(container);
					n += "[" + constraints + "]";
				}
			}

			n += "[" + container.getWidth() + "x" + container.getHeight() + "]";
			if (container instanceof JTree) {

				JTree t = (JTree) container;
				n += "[root:" + t.getModel().getRoot() + "]";
			}

			if (container instanceof BasicArrowButton) {
				BasicArrowButton b = (BasicArrowButton) container;
				switch (b.getDirection()) {
				case SwingConstants.NORTH:
					return n + " ▲";
				case SwingConstants.SOUTH:
					return n + " ▼";
				case SwingConstants.EAST:
					return n + " ►";
				case SwingConstants.WEST:
					return n + " ◄";

				default:
					return n;
				}
			} else if (container instanceof Frame) {
				Frame frame = (Frame) container;
				return n + " \"" + frame.getTitle() + "\"";
			} else if (container instanceof AbstractButton) {
				AbstractButton b = (AbstractButton) container;
				if (b.getText() == null) {
					return n;
				}
				return n + " " + b.getText();
			} else if (container instanceof AbstractButton) {
				AbstractButton b = (AbstractButton) container;
				return n + " \"" + b.getText() + "\"";
			} else if (container instanceof JScrollBar) {
				JScrollBar b = (JScrollBar) container;
				switch (b.getOrientation()) {
				case Adjustable.VERTICAL:
					return n + " (Vertical)";
				case Adjustable.HORIZONTAL:
					return n + " (Horizontal)";
				default:
					return n;
				}
			} else
				return n;
		}
	};

	private final JButton refresh = new JButton(refreshAction);

	private final JToggleButton autoRefresh = new JToggleButton("Auto");
	private Thread thread;
	private Container target;
	private UIContainerFrameRectangle targetFrameRectangle;

	public UITree() {
		super("UI Container Tree");
		close.setMargin(new Insets(6, 6, 6, 6));
		setLayout(new BorderLayout());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		add(scrollPane, BorderLayout.CENTER);
		JPanel allButtonPanel = new JPanel();
		allButtonPanel.setLayout(new BorderLayout());
		allButtonPanel.add(close, BorderLayout.EAST);
		JPanel refreshPanel = new JPanel();
		refreshPanel.setLayout(new BorderLayout());
		refreshPanel.add(refresh, BorderLayout.EAST);
		refreshPanel.add(autoRefresh, BorderLayout.WEST);
		allButtonPanel.add(refreshPanel, BorderLayout.WEST);
		add(allButtonPanel, BorderLayout.SOUTH);
		addComponentListener(this);
		pack();
	}

	@Inject private final 
	void register(UIContainerFrameRectangle r) {
		this.targetFrameRectangle = r;
		setBounds(r);
		setVisible(true);
		setTarget(r.getTarget());
		thread = new Thread(this, "UI Container Tree of " + getTarget());
		thread.start();
		if (r.autoRefresh()) {
			autoRefresh.doClick();
		} else {
			refresh.doClick();
		}
	}

	public void setTarget(Container target) {
		this.target = target;
	}

	public Container getTarget() {
		return target;
	}

	public DefaultMutableTreeNode getRoot() {
		return root;
	}

	public void componentResized(ComponentEvent e) {
		if(targetFrameRectangle!=null){
			targetFrameRectangle.setBounds(this.getBounds());
		}
	}

	public void componentMoved(ComponentEvent e) {
		if(targetFrameRectangle!=null){
			targetFrameRectangle.setBounds(this.getBounds());
		}
	}

	public void componentShown(ComponentEvent e) {
	}

	public void componentHidden(ComponentEvent e) {
	}
}
