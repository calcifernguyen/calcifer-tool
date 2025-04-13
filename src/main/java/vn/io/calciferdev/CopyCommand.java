package vn.io.calciferdev;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Slf4j
@Command(name = "copy", description = "Copy files from source directories to target directory")
public class CopyCommand implements Callable<Integer> {

    @Option(names = {"-f", "--file"}, description = "File containing list of folder paths")
    private File folderListFile;

    @Parameters(index = "0", description = "Target folder path")
    private String targetFolder;

    @Parameters(index = "1..*", description = "Folder paths")
    private List<String> folderPaths;

    private List<Path> getSourceFolders() throws IOException {
        List<Path> sourceFolders = new ArrayList<>();
        
        if (folderListFile != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(folderListFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Path path = Path.of(line.trim());
                    if (Files.exists(path) && Files.isDirectory(path)) {
                        sourceFolders.add(path);
                    }
                }
            }
        } else {
            for (String folderPath : folderPaths) {
                Path path = Path.of(folderPath);
                if (Files.exists(path) && Files.isDirectory(path)) {
                    sourceFolders.add(path);
                }
            }
        }
        return sourceFolders;
    }

    private void copyFile(Path sourceFile, Path sourceFolder, Path targetPath) {
        try {
            Path relativePath = sourceFolder.relativize(sourceFile);
            Path targetFile = targetPath.resolve(relativePath);
            Files.createDirectories(targetFile.getParent());
            Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
            log.info("Copied: {}", targetFile);
        } catch (IOException e) {
            log.error("Error copying file {}: {}", sourceFile, e.getMessage());
        }
    }

    @Override
    public Integer call() {
        try {
            List<Path> sourceFolders = getSourceFolders();
            Path targetPath = Path.of(targetFolder);
            
            if (!Files.exists(targetPath)) {
                Files.createDirectories(targetPath);
            }

            for (Path sourceFolder : sourceFolders) {
                log.info("Copying from: {}", sourceFolder);
                Files.walk(sourceFolder)
                    .filter(Files::isRegularFile)
                    .forEach(file -> copyFile(file, sourceFolder, targetPath));
            }
            return 0;
        } catch (IOException e) {
            log.error("Error copying files: {}", e.getMessage());
            return 1;
        }
    }
} 