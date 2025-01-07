package org.ajsmith.jadx.plugins.nativelibraries;

import jadx.api.JavaMethod;
import jadx.api.ResourceFile;
import jadx.api.plugins.JadxPluginContext;
import jadx.gui.treemodel.JNode;
import jadx.gui.ui.MainWindow;
import jadx.gui.ui.dialog.UsageDialog;
import jadx.gui.ui.filedialog.FileDialogWrapper;
import jadx.gui.ui.filedialog.FileOpenMode;
import org.ajsmith.jadx.plugins.nativelibraries.components.NativeLibrary;
import org.ajsmith.jadx.plugins.nativelibraries.components.NativeMethod;
import org.ajsmith.jadx.plugins.nativelibraries.components.NativeObject;
import org.ajsmith.jadx.plugins.nativelibraries.components.NativeRoot;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.List;

public class PluginDialog extends JDialog {
	private static final Logger LOG = LoggerFactory.getLogger(PluginDialog.class);

	JadxPluginContext context;
	PluginOptions options;
	JTree tree;

	public PluginDialog(JFrame parent, JadxPluginContext context, PluginOptions options) {
		super(parent, "Native Libraries", false);
		this.context = context;
		this.options = options;

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		getContentPane().setLayout(new BorderLayout());

		NativeRoot root = new NativeRoot(context);
		root.loadFromResources();

		tree = new JTree(root);
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.setCellRenderer(new NativeLibrariesTreeCellRenderer());

		// Auto expand all tree rows
		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}

		JScrollPane scrollPane = new JScrollPane(tree);
		JPanel buttonPanel = new JPanel();
		add(scrollPane);

		JButton openInGhidraBtn = new JButton("Open in Ghidra");
		openInGhidraBtn.setEnabled(false);
		openInGhidraBtn.addActionListener(e -> {
			if (options.getGhidraPath().isEmpty()) {
				showGhidraFilePicker();
				return;
			}

			TreeNode selected = (TreeNode) tree.getLastSelectedPathComponent();
			if (!(selected instanceof NativeObject)) return;
			openInGhidra(((NativeObject) selected).getLibrary());
		});

		JButton goToMethodBtn = new JButton("Go to Method");
		goToMethodBtn.setEnabled(false);
		goToMethodBtn.addActionListener(e -> goToTreeNode((TreeNode) tree.getLastSelectedPathComponent()));

		JButton findUsageBtn = new JButton("Find Usage");
		findUsageBtn.setEnabled(false);
		findUsageBtn.addActionListener(e -> {
			if (context.getGuiContext() == null) return;
			MainWindow mw = (MainWindow) context.getGuiContext().getMainFrame();

			TreeNode treeNode = (TreeNode) tree.getLastSelectedPathComponent();
			if (!(treeNode instanceof NativeMethod)) return;
			NativeMethod nativeMethod = (NativeMethod) treeNode;

			JavaMethod javaMethod = nativeMethod.getJavaMethod();
			if (javaMethod == null) {
				showMethodNotFoundDialog(treeNode);
				return;
			}

			JNode node = mw.getCacheObject().getNodeCache().makeFrom(javaMethod);
			if (node == null) return;
			new UsageDialog(mw, node).setVisible(true);
		});

		JButton closeBtn = new JButton("Close");
		closeBtn.addActionListener(e -> dispose());

		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
				// On double-click
				if (e.getClickCount() != 2) return;

				int clickedRow = tree.getRowForLocation(e.getX(), e.getY());
				if (clickedRow == -1) return;

				TreePath path = tree.getPathForRow(clickedRow);
				if (path == null) return;

				if (!(path.getLastPathComponent() instanceof NativeMethod)) return;
				NativeMethod selectedNode = (NativeMethod) path.getLastPathComponent();
				goToTreeNode(selectedNode);
			}
		});

		tree.addTreeSelectionListener(e -> {
			TreeNode selectedNode = (TreeNode) tree.getLastSelectedPathComponent();
			openInGhidraBtn.setEnabled(selectedNode != null);
			if (selectedNode == null) return;

			boolean isJniMethod = selectedNode instanceof NativeMethod;
			goToMethodBtn.setEnabled(isJniMethod);
			findUsageBtn.setEnabled(isJniMethod);
		});

		buttonPanel.add(openInGhidraBtn);
		buttonPanel.add(goToMethodBtn);
		buttonPanel.add(findUsageBtn);
		buttonPanel.add(closeBtn);

		add(buttonPanel, BorderLayout.SOUTH);

		pack();
		setMinimumSize(getSize());
		setSize(800, 600);
	}

	public void showModal() {
		setLocationRelativeTo(getParent());
		setVisible(true);
	}

	private void goToTreeNode(@Nullable TreeNode treeNode) {
		if (!(treeNode instanceof NativeMethod)) return;
		NativeMethod nativeMethod = (NativeMethod) treeNode;
		JavaMethod javaMethod = nativeMethod.getJavaMethod();
		if (javaMethod == null) {
			showMethodNotFoundDialog(treeNode);
			return;
		}
		if (context.getGuiContext() == null) return;
		context.getGuiContext().open(javaMethod.getCodeNodeRef());
	}

	private void showMethodNotFoundDialog(@Nullable TreeNode node) {
		String message = "Method not found";
		if (node instanceof NativeMethod) {
			message = "Method '" + ((NativeMethod) node).getFullName() + "' not found. It may be unused.";
		}
		JOptionPane.showMessageDialog(this, message);
	}

	private void showGhidraFilePicker() {
		if (context.getGuiContext() == null) return;
		MainWindow mw = (MainWindow) context.getGuiContext().getMainFrame();
		FileDialogWrapper fd = new FileDialogWrapper(mw, FileOpenMode.CUSTOM_OPEN);
		fd.setTitle("Select Ghidra");
		fd.setFileExtList(Arrays.asList("launch.sh", "launch.bat"));
		fd.setSelectionMode(JFileChooser.FILES_ONLY);
		List<Path> files = fd.show();
		if (files.size() == 1) {
			String ghidraPath = files.get(0).toAbsolutePath().toString();
			LOG.info("Set Ghidra path to: {}", ghidraPath);
			options.setGhidraPath(ghidraPath);
		}
	}

	private void openInGhidra(@Nullable NativeLibrary library) {
		if (library == null) return;
		String ghidraPath = options.getGhidraPath();
		if (ghidraPath == null || ghidraPath.isEmpty()) {
			JOptionPane.showMessageDialog(this, "You must set the path to your Ghidra installation.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		try {
			ResourceFile resourceFile = library.getResourceFile();
			final String projectName = "project123";
			final Path projectContainerDir = Path.of("C:\\test");
			final Path projectDir = Path.of(projectContainerDir.toString(), projectName);
			final Path libOutputPath = Path.of(projectDir.toString(), resourceFile.getOriginalName().replace("/", "_").replace("\\", "_"));

			// Create dirs if they don't exist
			Files.createDirectories(projectDir);

			// Export library to file
			library.saveToFile(libOutputPath.toString());

			Ghidra ghidra = new Ghidra(ghidraPath);
			Ghidra.Project project = new Ghidra.Project(projectDir, projectName);

			ProgressDialog progressDialog = new ProgressDialog(this, "Analyzing " + resourceFile.getOriginalName());
			progressDialog.setVisible(true);
			ghidra.importFile(project, libOutputPath.toString()).thenAccept(process -> {
				try {
					progressDialog.setMessage("Checking project");
					ghidra.checkSuccess(process);
					progressDialog.setMessage("Launching Ghidra");
					ghidra.launchGhidra(project).thenAccept(launchProcess -> {
						progressDialog.close();
					});
				} catch (IOException | ConcurrentModificationException e) {
					progressDialog.close();
					showErrorMessage("Could not launch Ghidra: " + e.getMessage());
				}
			});


		} catch (IOException e) {
			showErrorMessage("Could not open Ghidra: " + e.getMessage());
		}
	}

	private void showErrorMessage(String message) {
		JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
	}
}
