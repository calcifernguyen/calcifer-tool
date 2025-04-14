package vn.io.calciferdev;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
@Command(name = "run", description = "Execute a command in each specified folder")
public class RunCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Command to execute")
    private String command;

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

            ProcessResult result = new ProcessResult();
            processFolders(targetFolders, result);
            return logFinalResult(result);
        } catch (Exception e) {
            log.error("Error executing command: {}", e.getMessage());
            return 1;
        }
    }

    private List<String> getTargetFolders() throws IOException {
        List<String> folders = new ArrayList<>();
        if (folderListFile != null && inputPaths != null) {
            throw new IllegalArgumentException("Cannot use both -f and -i options simultaneously");
        }
        if (folderListFile != null) {
            try (BufferedReader reader = new BufferedReader(new java.io.FileReader(folderListFile))) {
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

    private void processFolders(List<String> folders, ProcessResult result) {
        for (String folder : folders) {
            if (verbose) {
                log.info("Processing folder: {}", folder);
            }
            int exitCode = executeCommand(folder);
            if (exitCode == 0) {
                result.successCount++;
                if (verbose) {
                    log.info("Successfully executed command in folder: {}", folder);
                }
            } else {
                result.failureCount++;
                if (verbose) {
                    log.error("Failed to execute command in folder: {}", folder);
                }
            }
        }
    }

    private int logFinalResult(ProcessResult result) {
        if (result.failureCount == 0) {
            log.info("Command execution completed successfully in all {} folders", result.successCount);
            return 0;
        } else {
            log.error("Command execution completed with {} successes and {} failures", 
                result.successCount, result.failureCount);
            return 1;
        }
    }

    private int executeCommand(String folder) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                processBuilder.command("powershell", "-Command", command);
            } else {
                processBuilder.command("sh", "-c", command);
            }
            processBuilder.directory(new File(folder));
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (verbose) {
                        log.info(line);
                    }
                }
            }

            int exitCode = process.waitFor();
            if (verbose) {
                log.info("Command exited with code: {}", exitCode);
            }
            return exitCode;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Command execution interrupted in folder {}: {}", folder, e.getMessage());
            return 1;
        } catch (IOException e) {
            log.error("Error executing command in folder {}: {}", folder, e.getMessage());
            return 1;
        }
    }
} 