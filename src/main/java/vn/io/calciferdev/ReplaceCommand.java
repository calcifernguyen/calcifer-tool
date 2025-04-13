package vn.io.calciferdev;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Slf4j
@Command(name = "replace", description = "Replace text patterns in files within specified directories")
public class ReplaceCommand implements Callable<Integer> {

    @Option(names = {"-f", "--file"}, description = "File containing list of folder paths")
    private File folderListFile;

    @Option(names = {"-a", "--all"}, description = "Replace in folder names as well")
    private boolean replaceFolderNames;

    @Option(names = {"--ignore"}, description = "Skip files and folders matching this pattern")
    private String ignorePattern;

    @Parameters(index = "0", description = "Old pattern to replace")
    private String oldPattern;

    @Parameters(index = "1", description = "New pattern to replace with")
    private String newPattern;

    @Parameters(index = "2..*", description = "Folder paths")
    private List<String> folderPaths;

    private List<Path> getTargetFolders() throws IOException {
        List<Path> targetFolders = new ArrayList<>();
        
        if (folderListFile != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(folderListFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Path path = Path.of(line.trim());
                    if (Files.exists(path) && Files.isDirectory(path)) {
                        targetFolders.add(path);
                    }
                }
            }
        } else {
            for (String folderPath : folderPaths) {
                Path path = Path.of(folderPath);
                if (Files.exists(path) && Files.isDirectory(path)) {
                    targetFolders.add(path);
                }
            }
        }
        return targetFolders;
    }

    private boolean shouldIgnore(Path path) {
        if (ignorePattern == null) {
            return false;
        }
        return path.toString().matches(ignorePattern);
    }

    private void processFile(Path file, Pattern pattern) {
        if (shouldIgnore(file)) {
            log.debug("Skipping ignored file: {}", file);
            return;
        }

        try {
            String content = Files.readString(file);
            String newContent = pattern.matcher(content).replaceAll(newPattern);
            if (!content.equals(newContent)) {
                Files.writeString(file, newContent);
                log.info("Updated: {}", file);
            }
        } catch (IOException e) {
            log.error("Error processing file {}: {}", file, e.getMessage());
        }
    }

    private void renameFolder(Path folder, Pattern pattern) {
        if (shouldIgnore(folder)) {
            log.debug("Skipping ignored folder: {}", folder);
            return;
        }

        String folderName = folder.getFileName().toString();
        String newFolderName = pattern.matcher(folderName).replaceAll(newPattern);
        if (!folderName.equals(newFolderName)) {
            try {
                Path newPath = folder.getParent().resolve(newFolderName);
                Files.move(folder, newPath);
                log.info("Renamed folder: {} -> {}", folder, newPath);
            } catch (IOException e) {
                log.error("Error renaming folder {}: {}", folder, e.getMessage());
            }
        }
    }

    @Override
    public Integer call() {
        try {
            List<Path> targetFolders = getTargetFolders();
            Pattern pattern = Pattern.compile(oldPattern);
            
            for (Path folder : targetFolders) {
                if (shouldIgnore(folder)) {
                    log.debug("Skipping ignored folder: {}", folder);
                    continue;
                }

                log.info("Processing files in: {}", folder);
                Files.walk(folder)
                    .filter(Files::isRegularFile)
                    .forEach(file -> processFile(file, pattern));

                if (replaceFolderNames) {
                    Files.walk(folder)
                        .filter(Files::isDirectory)
                        .forEach(dir -> renameFolder(dir, pattern));
                }
            }
            return 0;
        } catch (IOException e) {
            log.error("Error replacing patterns: {}", e.getMessage());
            return 1;
        }
    }
} 