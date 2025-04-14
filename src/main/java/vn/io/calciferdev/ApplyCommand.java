package vn.io.calciferdev;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

import org.yaml.snakeyaml.Yaml;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Slf4j
@Data
@Command(name = "apply", description = "Apply a series of commands from a YAML configuration file")
public class ApplyCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Configuration file path")
    private File configFile;

    @Option(names = {"-v", "--verbose"}, description = "Enable verbose output")
    private boolean verbose;

    @Data
    @NoArgsConstructor
    public static class Config {
        private List<CommandConfig> commands;
    }

    @Data
    @NoArgsConstructor
    public static class CommandConfig {
        private String type;
        private String command;
        private String destination;
        private String oldPattern;
        private String newPattern;
        private boolean replaceFolderNames;
        private String ignorePattern;
        private List<String> inputPaths;
    }

    private int executeRunCommand(CommandConfig cmdConfig) {
        RunCommand runCmd = new RunCommand();
        runCmd.setCommand(cmdConfig.getCommand());
        runCmd.setInputPaths(cmdConfig.getInputPaths());
        runCmd.setVerbose(verbose);
        return runCmd.call();
    }

    private int executeReplaceCommand(CommandConfig cmdConfig) {
        ReplaceCommand replaceCmd = new ReplaceCommand();
        replaceCmd.setOldPattern(cmdConfig.getOldPattern());
        replaceCmd.setNewPattern(cmdConfig.getNewPattern());
        replaceCmd.setReplaceFolderNames(cmdConfig.isReplaceFolderNames());
        replaceCmd.setIgnorePattern(cmdConfig.getIgnorePattern());
        replaceCmd.setInputPaths(cmdConfig.getInputPaths());
        replaceCmd.setVerbose(verbose);
        return replaceCmd.call();
    }

    private int executeCopyCommand(CommandConfig cmdConfig) {
        CopyCommand copyCmd = new CopyCommand();
        copyCmd.setDestination(cmdConfig.getDestination());
        copyCmd.setInputPaths(cmdConfig.getInputPaths());
        copyCmd.setVerbose(verbose);
        return copyCmd.call();
    }

    private int executeCommand(CommandConfig cmdConfig) {
        return switch (cmdConfig.getType().toLowerCase()) {
            case "run" -> executeRunCommand(cmdConfig);
            case "replace" -> executeReplaceCommand(cmdConfig);
            case "copy" -> executeCopyCommand(cmdConfig);
            default -> {
                log.error("Unknown command type: {}", cmdConfig.getType());
                yield 1;
            }
        };
    }

    @Override
    public Integer call() {
        try {
            Yaml yaml = new Yaml();
            Config config = yaml.loadAs(new FileInputStream(configFile), Config.class);

            if (config.getCommands() == null || config.getCommands().isEmpty()) {
                log.error("No commands found in configuration file");
                return 1;
            }

            for (CommandConfig cmdConfig : config.getCommands()) {
                if (verbose) {
                    log.info("Executing command of type: {}", cmdConfig.getType());
                }
                int result = executeCommand(cmdConfig);
                if (result != 0) {
                    return result;
                }
            }
            return 0;
        } catch (IOException e) {
            log.error("Error reading configuration file: {}", e.getMessage());
            return 1;
        }
    }
} 