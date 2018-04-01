package files.archiver;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NamingUtil {

    private Map<String, String> rootFolder = new HashMap<>();
    private List<File> rootPaths; //both files and folders
    private RenameStrategy strategy;
    public NamingUtil(RenameStrategy strategy) {
        this.strategy = strategy;
        rootPaths = new ArrayList<>();
    }

    private static File ReplaceGetName(String base, String entry) {
        return new File(base + File.separator + entry);
    }

    private static File CombineGetName(String base, String entry) {
        File outFile = new File(base + File.separator + entry);

        //check if the file exist
        if (!outFile.exists()) {
            return outFile;
        }

        //prepare new name
        String suffix = "";
        String stem = entry;
        if (entry.lastIndexOf('.') > 0 && entry.lastIndexOf('.') > suffix.indexOf('/')) {
            suffix = entry.substring(entry.lastIndexOf('.'));
            stem = entry.substring(0, entry.lastIndexOf('.'));
        }

        int index = 1;
        File new_file = outFile;

        while (new_file.exists()) {
            index++;
            new_file = new File(base + File.separator + stem + "_" + index + suffix);
        }
        outFile = new_file;

        return outFile;
    }

    public File getUnconflictFileName(String base, String entry) {
        File f;
        switch (strategy) {
            case REPLACE:
                f = ReplaceGetName(base, entry);
                addToRootPath(base, entry, f);
                return f;
            case COMBINE_FOLDER:

                f = CombineGetName(base, entry);
                addToRootPath(base, entry, f);
                return f;
            case RENAME_ROOT:
                return renameGetName(base, entry);
        }
        return null;
    }

    private void addToRootPath(String base, String entry, File f) {
        if (entry.indexOf('/') == -1) {
            rootPaths.add(f);
        } else { //in a folder
            File folder = new File(base + File.separator + entry.substring(0, entry.indexOf('/')));
            if (!rootPaths.contains(folder)) rootPaths.add(folder);
        }
    }

    public List<File> getRootPaths() {
        return rootPaths;
    }

    private String getRootName(String base, String entry) {

        File outFile = new File(base + File.separator + entry);

        //check if the file exist
        if (!outFile.exists()) {
            return entry;
        }

        //prepare new name
        String suffix = "";
        String stem = entry;
        if (entry.lastIndexOf('.') > 0) {
            suffix = entry.substring(entry.lastIndexOf('.'));
            stem = entry.substring(0, entry.lastIndexOf('.'));
        }

        int index = 1;
        File new_file = outFile;

        while (new_file.exists()) {
            index++;
            new_file = new File(base + File.separator + stem + "_" + index + suffix);
        }

        return stem + "_" + index + suffix;
    }

    private File renameGetName(String base, String entry) {
        if (entry.indexOf('/') == -1) { //file itself
            File file = new File(base + File.separator + getRootName(base, entry));
            rootPaths.add(file);
            return file;
        }

        String stemFolder = entry.substring(0, entry.indexOf('/'));

        if (!rootFolder.containsKey(stemFolder)) {
            rootFolder.put(stemFolder, getRootName(base, stemFolder));
            rootPaths.add(new File(base + File.separator + rootFolder.get(stemFolder)));
        }

        return new File(base + File.separator + rootFolder.get(stemFolder) + entry.substring(entry.indexOf('/')));
    }

    public enum RenameStrategy {REPLACE, RENAME_ROOT, COMBINE_FOLDER}
}
