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

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Slf4j
@Data
@Command(name = "copy", description = "Copy files and folders")
public class CopyCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Destination folder")
    private String destination;

    @Option(names = {"-f", "--folder-list"}, description = "File containing list of folders")
    private File folderListFile;

    @Option(names = {"-i", "--input"}, description = "Input folder paths", arity = "0..*")
    private List<String> inputPaths;

    @Option(names = {"-v", "--verbose"}, description = "Enable verbose output")
    private boolean verbose;

    private static class ProcessResult {
        int successCount;
        int failureCount;
    }

    @Override
    public Integer call() {
        try {
            List<String> targetFolders = getTargetFolders();
            if (targetFolders.isEmpty()) {
                log.error("No folders specified. Use either -f or -i option.");
                return 1;
            }

            Path destPath = createDestinationDirectory();
            ProcessResult result = new ProcessResult();
            processFolders(targetFolders, destPath, result);
            return logFinalResult(result);
        } catch (Exception e) {
            log.error("Error during copy: {}", e.getMessage());
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

    private Path createDestinationDirectory() throws IOException {
        Path destPath = Path.of(destination);
        if (!Files.exists(destPath)) {
            Files.createDirectories(destPath);
            if (verbose) {
                log.info("Created destination directory: {}", destPath);
            }
        }
        return destPath;
    }

    private void processFolders(List<String> folders, Path destPath, ProcessResult result) {
        for (String folder : folders) {
            if (verbose) {
                log.info("Processing folder: {}", folder);
            }
            try {
                copyUsingNativeCommand(folder, destPath.toString());
                result.successCount++;
                if (verbose) {
                    log.info("Successfully copied folder: {}", folder);
                }
            } catch (Exception e) {
                result.failureCount++;
                if (verbose) {
                    log.error("Failed to copy folder {}: {}", folder, e.getMessage());
                }
            }
        }
    }

    private int logFinalResult(ProcessResult result) {
        if (result.failureCount == 0) {
            log.info("Copy operation completed successfully in all {} folders", result.successCount);
            return 0;
        } else {
            log.error("Copy operation completed with {} successes and {} failures", 
                result.successCount, result.failureCount);
            return 1;
        }
    }

    private void copyUsingNativeCommand(String source, String destination) throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        ProcessBuilder processBuilder;
        
        if (os.contains("win")) {
            // Windows: Use xcopy with /E for recursive copy and /I to assume destination is directory
            processBuilder = new ProcessBuilder("xcopy", "/E", "/I", "/Y", source, destination);
        } else {
            // Unix-like: Use cp with -r for recursive copy
            processBuilder = new ProcessBuilder("cp", "-r", source, destination);
        }

        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                throw new IOException("Native copy command failed with exit code: " + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Copy operation interrupted", e);
        }
    }
}