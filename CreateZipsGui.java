import javax.swing.*;
import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

public class CreateZipsGui {

    public static void main(String[] args) throws IOException {
        // Create a GUI file chooser for the user to select a path (ZIP file or directory)
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES); // Allow selection of files and directories
        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String rootPathString = selectedFile.getAbsolutePath();
            Path rootPath = Paths.get(rootPathString);

            // Check if the input is a ZIP file
            if (Files.isRegularFile(rootPath) && rootPathString.endsWith(".zip")) {
                // If it's a ZIP file, unzip it first and process the contents
                Path unzipDirectory = unzipFile(rootPath);
                processDirectory(unzipDirectory);
            } else if (Files.isDirectory(rootPath)) {
                // If it's a directory, process it normally
                processDirectory(rootPath);
            } else {
                System.out.println("The specified path is neither a valid directory nor a ZIP file.");
            }
        } else {
            System.out.println("No file or directory selected.");
        }
    }

    // Unzips the given ZIP file and returns the path to the directory where it's unzipped
    private static Path unzipFile(Path zipPath) throws IOException {
        Path unzipDir = Files.createTempDirectory("unzipped_");

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath.toFile()))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path newFile = unzipDir.resolve(entry.getName());

                // Create directories for entries that are directories
                if (entry.isDirectory()) {
                    Files.createDirectories(newFile);
                } else {
                    // Ensure parent directory exists
                    if (newFile.getParent() != null) {
                        Files.createDirectories(newFile.getParent());
                    }

                    // Write file contents
                    try (FileOutputStream fos = new FileOutputStream(newFile.toFile())) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }

        System.out.println("Unzipping complete: " + unzipDir.toString());
        return unzipDir;
    }

    // Processes a directory by creating a ZIP for each file
    private static void processDirectory(Path rootPath) throws IOException {
        Files.walk(rootPath)
             .filter(Files::isRegularFile) // Only process files (no directories)
             .forEach(file -> {
                 try {
                     createZipForFile(file, rootPath);
                 } catch (IOException e) {
                     System.err.println("Error creating ZIP for " + file + ": " + e.getMessage());
                 }
             });

        System.out.println("ZIP files have been successfully created.");
    }

    // Creates a ZIP file for the specified file
    private static void createZipForFile(Path file, Path rootPath) throws IOException {
        // ZIP file name: filename.zip
        String zipFileName = file.getFileName().toString() + ".zip";
        
        // Create the ZIP file
        try (FileOutputStream fos = new FileOutputStream(zipFileName);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            // Get the relative path of the file inside the ZIP
            Path relativePath = rootPath.relativize(file);

            // Create the ZIP entry
            ZipEntry zipEntry = new ZipEntry(relativePath.toString());
            zos.putNextEntry(zipEntry);

            // Write the file contents to the ZIP
            try (FileInputStream fis = new FileInputStream(file.toFile())) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
            }

            zos.closeEntry();
        }

        System.out.println("Created ZIP: " + zipFileName);
    }
}
