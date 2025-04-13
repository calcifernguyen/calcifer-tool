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

import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Slf4j
@Command(name = "run", description = "Run shell commands in specified directories")
public class RunCommand implements Callable<Integer> {

    @Option(names = {"-f", "--file"}, description = "File containing list of folder paths")
    private File folderListFile;

    @Parameters(index = "0", description = "Command to execute", arity = "1")
    private String command;

    @Parameters(index = "1..*", description = "Folder paths")
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

    private int executeCommand(Path folder) throws IOException, InterruptedException {
        log.info("Executing command in: {}", folder);
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(folder.toFile());
        processBuilder.command("sh", "-c", command);
        
        Process process = processBuilder.start();
        int exitCode = process.waitFor();
        
        if (exitCode != 0) {
            log.error("Command failed in {}: {}", folder, command);
        }
        return exitCode;
    }

    @Override
    public Integer call() {
        try {
            List<Path> targetFolders = getTargetFolders();
            
            for (Path folder : targetFolders) {
                int exitCode = executeCommand(folder);
                if (exitCode != 0) {
                    return exitCode;
                }
            }
            return 0;
        } catch (IOException e) {
            log.error("Error executing commands: {}", e.getMessage());
            return 1;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Command execution interrupted: {}", e.getMessage());
            return 1;
        }
    }
} 