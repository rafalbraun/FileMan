package fileman;

import javax.swing.*;
import java.awt.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

//TODO
//1 Create ProgressBar that helps track how many files have been loaded
//2 Add method to FileManager Eager that expands all nodes
public class FileManagerEager extends JPanel {
    private final FileSystemView fileSystemView = FileSystemView.getFileSystemView();

    private JTable table;
    private JTree tree;
    private FileTableModel model;
    private JScrollPane treeView;
    private JScrollPane tableScroll;

    private final int rowIconPadding = 6;

    public FileManagerEager(String topDir) {
        super(new GridLayout(1,0));

        //Create a tree that allows one selection at a time
        DefaultMutableTreeNode top = createFilesystemNodesEager(Paths.get(topDir));
        tree = new JTree(top);
        treeView = new JScrollPane(tree);
        tree.addTreeSelectionListener(new CustomTreeSelectionListener());
        tree.setCellRenderer(new FileTreeCellRenderer());
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        //Create table that lists all files under selected directory
        table = new JTable();
        model = new FileTableModel();
        table.setModel(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(true);
        table.setAutoCreateRowSorter(true);
        table.setShowVerticalLines(false);
        tableScroll = new JScrollPane(table);

        //Add the scroll panes to a split pane.
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setTopComponent(treeView);
        splitPane.setBottomComponent(tableScroll);
        splitPane.setDividerLocation(350);

        //Add the split pane to this panel.
        add(splitPane);
    }

    private DefaultMutableTreeNode createFilesystemNodesEager(Path path) {
        DefaultMutableTreeNode top = new DefaultMutableTreeNode(path.toFile());
        DefaultMutableTreeNode node = null;

        File[] files = fileSystemView.getFiles(path.toFile(), true);
        Arrays.sort(files);
        for (File file : files) {
            if(file.isDirectory()) {
                if (containsDirs(file.toPath())) {
                    node = createFilesystemNodesEager(file.toPath());
                    top.add(node);
                } else {
                    node = new DefaultMutableTreeNode(file);
                    top.add(node);
                }
            }
        }
        return top;
    }

    /**
     * Add the files that are contained within the directory of this node.
     */
    private void showChildren(final DefaultMutableTreeNode node) {

        SwingWorker<Void, File> worker = new SwingWorker<Void, File>() {
            @Override
            public Void doInBackground() {
                File file = (File) node.getUserObject();
                if (file.isDirectory()) {
                    File[] files = fileSystemView.getFiles(file, true); //!!
                    if (node.isLeaf()) {
                        for (File child : files) {
                            if (child.isDirectory()) {
                                publish(child);
                            }
                        }
                    }
                    setTableData(files);
                }
                return null;
            }

            @Override
            protected void process(List<File> chunks) {
                for (File child : chunks) {
                    node.add(new DefaultMutableTreeNode(child));
                }
            }
        };
        worker.execute();
    }

    private void setColumnWidth(int column, int width) {
        TableColumn tableColumn = table.getColumnModel().getColumn(column);
        if (width<0) {
            JLabel label = new JLabel( (String)tableColumn.getHeaderValue() );
            Dimension preferred = label.getPreferredSize();
            width = (int)preferred.getWidth()+14;
        }
        tableColumn.setPreferredWidth(width);
        tableColumn.setMaxWidth(width);
        tableColumn.setMinWidth(width);
    }

    private void setTableData(final File[] files) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (model==null) {
                    model = new FileTableModel();
                    table.setModel(model);
                }
                model.setFiles(files);

                // add file/directory icon
                Icon icon = fileSystemView.getSystemIcon(files[0]);

                // size adjustment to better account for icons
                table.setRowHeight( icon.getIconHeight()+rowIconPadding );
                setColumnWidth(0,-1);
                setColumnWidth(3,100);
                setColumnWidth(4,90);
            }
        });
    }

    public class CustomTreeSelectionListener implements TreeSelectionListener {
        public void valueChanged(TreeSelectionEvent e) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
            File file = (File) node.getUserObject();
            System.out.println(file.getPath());
            showChildren(node);
        }
    }

    /**
     * Checks if directory has any contents
     * @href https://www.baeldung.com/java-check-empty-directory
     */
    public boolean containsDirs(Path path) {
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> directory = Files.newDirectoryStream(path)) {
                for (Path p : directory) {
                    if (p.toFile().isDirectory()) {
                        return true;
                    }
                }
            } catch (IOException e) {
                System.err.println("Error: are you sure that " + path + " directory exists?");
                e.printStackTrace();
            }
        }
        return false;
    }

    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("FileMan");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Load path of project directory
        try {
            String projectDir = new File(".").getCanonicalPath();
            frame.add(new FileManagerEager(projectDir));
        } catch (IOException ex) {
            System.err.println("Error: No such path");
        }

        //Set size
        frame.setPreferredSize(new Dimension(1000, 1000));

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

}
