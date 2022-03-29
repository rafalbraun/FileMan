package fileman;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

public class FileTableModel extends AbstractTableModel {

    private File[] files;
    private FileSystemView fileSystemView = FileSystemView.getFileSystemView();

    private String[] columns = {
            "Icon",
            "File",
            "Path/name",
            "Size",
            "Last Modified",
    };

    FileTableModel() {
        this.files = new File[0];
    }

    public String getColumnName(int column) {
        return columns[column];
    }

    public int getRowCount() {
        return files.length;
    }

    public int getColumnCount() {
        return columns.length;
    }

    public Object getValueAt(int row, int column) {
        File file = files[row];
        switch (column) {
            case 0:
                return fileSystemView.getSystemIcon(file);
            case 1:
                return fileSystemView.getSystemDisplayName(file);
            case 2:
                return file.getPath();
            case 3:
                return humanReadableByteCountSI(file.length());
            case 4:
                return file.lastModified();
            default:
                System.err.println("Logic Error");
        }
        return "";
    }

    /**
     * @href https://www.baeldung.com/java-comparator-comparable
     * @param files
     */
    public void setFiles(File[] files) {
        this.files = files;
        Arrays.sort(files, new Comparator<File>() {
            public int compare(File o1, File o2) {
                return Boolean.compare(o2.isDirectory(), o1.isDirectory());
            }
        });
        fireTableDataChanged();
    }

    public Class<?> getColumnClass(int column) {
        switch (column) {
            case 0:
                return ImageIcon.class;
            case 4:
                return Date.class;
            default:
                return String.class;
        }
    }

    /**
     * @href https://stackoverflow.com/questions/3758606/how-can-i-convert-byte-size-into-a-human-readable-format-in-java
     */
    public static String humanReadableByteCountSI(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }

}
