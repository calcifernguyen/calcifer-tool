package vn.io.calciferdev;

import java.util.concurrent.Callable;

import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Slf4j
@Command(name = "calcifer", mixinStandardHelpOptions = true, version = "calcifer 1.0",
        description = "A utility tool for running commands, replacing text, and copying files across multiple directories.")
public class Calcifer implements Callable<Integer> {

    @Override
    public Integer call() {
        CommandLine.usage(this, System.out);
        return 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Calcifer())
                .addSubcommand("run", new RunCommand())
                .addSubcommand("replace", new ReplaceCommand())
                .addSubcommand("copy", new CopyCommand())
                .execute(args);
        System.exit(exitCode);
    }
} 