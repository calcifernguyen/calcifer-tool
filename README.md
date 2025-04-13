# Calcifer Tool

A command-line utility tool for running commands, replacing text, and copying files across multiple directories.

## Requirements

- Java 21
- Maven

## Building

To build the project, run:

```bash
mvn clean package
```

This will create a fat JAR file in the `target` directory named `calcifer-tool-1.0-SNAPSHOT-jar-with-dependencies.jar`.

## Usage

### Run Command

Run shell commands in specified directories:

```bash
# Run commands in a single directory
java -jar target/calcifer-tool-1.0-SNAPSHOT-jar-with-dependencies.jar run <folder_path> <command1> <command2> ...

# Run commands in multiple directories from a file
java -jar target/calcifer-tool-1.0-SNAPSHOT-jar-with-dependencies.jar run -f <folder_paths_list-file> <command1> <command2> ...
```

### Replace Command

Replace text patterns in files within specified directories:

```bash
# Replace in a single directory
java -jar target/calcifer-tool-1.0-SNAPSHOT-jar-with-dependencies.jar replace <folder_path> <old_pattern> <new_pattern>

# Replace in multiple directories from a file
java -jar target/calcifer-tool-1.0-SNAPSHOT-jar-with-dependencies.jar replace -f <folder_paths_list-file> <old_pattern> <new_pattern>
```

### Copy Command

Copy files from source directories to target directory:

```bash
# Copy from a single directory
java -jar target/calcifer-tool-1.0-SNAPSHOT-jar-with-dependencies.jar copy <folder_path> <target_folder>

# Copy from multiple directories from a file
java -jar target/calcifer-tool-1.0-SNAPSHOT-jar-with-dependencies.jar copy -f <folder_paths_list-file> <target_folder>
```

## Examples

1. Run commands in multiple directories:
```bash
java -jar target/calcifer-tool-1.0-SNAPSHOT-jar-with-dependencies.jar run -f folders.txt "git pull" "mvn clean install"
```

2. Replace text in multiple files:
```bash
java -jar target/calcifer-tool-1.0-SNAPSHOT-jar-with-dependencies.jar replace -f folders.txt "oldVersion" "newVersion"
```

3. Copy files from multiple directories:
```bash
java -jar target/calcifer-tool-1.0-SNAPSHOT-jar-with-dependencies.jar copy -f folders.txt /path/to/target
```

## Notes

- The folder paths list file should contain one directory path per line
- All commands support both single directory and multiple directories (via file) input
- The tool will create the target directory if it doesn't exist
- For the replace command, the old pattern is treated as a regular expression 