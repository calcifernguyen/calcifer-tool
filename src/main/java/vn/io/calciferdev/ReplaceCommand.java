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
import java.util.stream.Stream;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Slf4j
@Data
@Command(name = "replace", description = "Replace text in files and optionally folder names")
public class ReplaceCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Pattern to replace")
    private String oldPattern;

    @Parameters(index = "1", description = "Replacement text")
    private String newPattern;

    @Option(names = {"-f", "--folder-list"}, description = "File containing list of folders")
    private File folderListFile;

    @Option(names = {"-i", "--input"}, description = "Input folder paths", arity = "0..*")
    private List<String> inputPaths;

    @Option(names = {"-n", "--folder-names"}, description = "Replace folder names as well")
    private boolean replaceFolderNames;

    @Option(names = {"--ignore"}, description = "Pattern to ignore folders")
    private String ignorePattern;

    @Option(names = {"-v", "--verbose"}, description = "Enable verbose output")
    private boolean verbose;

    private static class ProcessResult {
        int filesProcessed;
        int filesModified;
        int foldersRenamed;
    }

    @Override
    public Integer call() {
        try {
            List<String> targetFolders = getTargetFolders();
            if (targetFolders.isEmpty()) {
                log.error("No folders specified. Use either -f or -i option.");
                return 1;
            }

            Pattern pattern = Pattern.compile(oldPattern);
            Pattern ignorePatternCompiled = this.ignorePattern != null ? Pattern.compile(this.ignorePattern) : null;

            ProcessResult result = new ProcessResult();
            processAllFolders(targetFolders, pattern, ignorePatternCompiled, result);
            return logFinalResult(result);
        } catch (Exception e) {
            log.error("Error during replacement: {}", e.getMessage());
            return 1;
        }
    }

    private List<String> getTargetFolders() throws IOException {
        List<String> folders = new ArrayList<>();
        if (folderListFile != null && inputPaths != null) {
            throw new IllegalArgumentException("Cannot use both -f and -i options simultaneously");
        }
        if (folderListFile != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(folderListFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    folders.add(line.trim());
                }
            }
        } else if (inputPaths != null) {
            folders.addAll(inputPaths);
        } else {
            throw new IllegalArgumentException("Either -f or -i option must be specified");
        }
        return folders;
    }

    private boolean shouldIgnore(String folder, Pattern ignorePattern) {
        if (ignorePattern == null) {
            return false;
        }
        return ignorePattern.matcher(folder).find();
    }

    private void processAllFolders(List<String> folders, Pattern pattern, Pattern ignorePattern, ProcessResult result) {
        for (String folder : folders) {
            if (verbose) {
                log.info("Processing folder: {}", folder);
            }
            if (shouldIgnore(folder, ignorePattern)) {
                if (verbose) {
                    log.info("Skipping ignored folder: {}", folder);
                }
                continue;
            }
            try {
                processFolder(Path.of(folder), pattern);
                result.filesProcessed++;
                if (verbose) {
                    log.info("Successfully processed folder: {}", folder);
                }
            } catch (Exception e) {
                if (verbose) {
                    log.error("Failed to process folder {}: {}", folder, e.getMessage());
                }
            }
        }
    }

    private int logFinalResult(ProcessResult result) {
        if (result.filesProcessed > 0) {
            log.info("Text replacement completed successfully in {} folders", result.filesProcessed);
            return 0;
        } else {
            log.error("Text replacement failed in all folders");
            return 1;
        }
    }

    private void processFolder(Path folder, Pattern pattern) throws IOException {
        ProcessResult result = new ProcessResult();
        processFiles(folder, pattern, result);
        if (replaceFolderNames) {
            processFolders(folder, pattern, result);
        }
        logFolderResult(folder, result);
    }

    private void processFiles(Path folder, Pattern pattern, ProcessResult result) throws IOException {
        try (Stream<Path> stream = Files.walk(folder)) {
            stream.filter(Files::isRegularFile)
                .forEach(file -> {
                    try {
                        if (verbose) {
                            log.info("Processing file: {}", file);
                        }
                        String content = Files.readString(file);
                        String newContent = pattern.matcher(content).replaceAll(newPattern);
                        if (!content.equals(newContent)) {
                            Files.writeString(file, newContent);
                            result.filesModified++;
                            if (verbose) {
                                log.info("Replaced content in file: {}", file);
                            }
                        }
                        result.filesProcessed++;
                    } catch (IOException e) {
                        log.error("Error processing file {}: {}", file, e.getMessage());
                    }
                });
        }
    }

    private void processFolders(Path folder, Pattern pattern, ProcessResult result) throws IOException {
        try (Stream<Path> stream = Files.walk(folder)) {
            stream.filter(Files::isDirectory)
                .forEach(dir -> {
                    try {
                        String dirName = dir.getFileName().toString();
                        String newDirName = pattern.matcher(dirName).replaceAll(newPattern);
                        if (!dirName.equals(newDirName)) {
                            Path newPath = dir.resolveSibling(newDirName);
                            Files.move(dir, newPath);
                            result.foldersRenamed++;
                            if (verbose) {
                                log.info("Renamed folder from {} to {}", dir, newPath);
                            }
                        }
                    } catch (IOException e) {
                        log.error("Error renaming folder {}: {}", dir, e.getMessage());
                    }
                });
        }
    }

    private void logFolderResult(Path folder, ProcessResult result) {
        if (verbose) {
            log.info("Folder {} processed: {} files processed, {} files modified, {} folders renamed", 
                folder, result.filesProcessed, result.filesModified, result.foldersRenamed);
        }
    }
} 