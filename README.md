# Calcifer Tool

A command-line tool for automating file operations and text replacements.

## Features

- **Run Command**: Execute shell commands in specified directories
- **Replace Command**: Replace text in files and optionally folder names
- **Copy Command**: Copy files and folders using native OS commands
- **Apply Command**: Execute a series of commands from a YAML configuration file
- **Verbose Mode**: Detailed logging for debugging and monitoring

## Installation

```bash
# Clone the repository
git clone https://github.com/yourusername/calcifer-tool.git
cd calcifer-tool

# Build the project
mvn clean package

# Run the tool
java -jar target/calcifer-tool-1.0-SNAPSHOT.jar
```

## Usage

### Run Command

Execute shell commands in specified directories:

```bash
# Basic usage
calcifer run "command" -i path1 path2

# With verbose output
calcifer run "command" -i path1 path2 -v

# Using a folder list file
calcifer run "command" -f folders.txt
```

### Replace Command

Replace text in files and optionally folder names:

```bash
# Basic usage
calcifer replace "oldPattern" "newPattern" -i path1 path2

# With verbose output
calcifer replace "oldPattern" "newPattern" -i path1 path2 -v

# Replace folder names as well
calcifer replace "oldPattern" "newPattern" -i path1 path2 --folder-names

# Ignore specific folders
calcifer replace "oldPattern" "newPattern" -i path1 path2 --ignore "pattern"
```

### Copy Command

Copy files and folders using native OS commands:

```bash
# Basic usage
calcifer copy destination -i path1 path2

# With verbose output
calcifer copy destination -i path1 path2 -v

# Using a folder list file
calcifer copy destination -f folders.txt
```

### Apply Command

Execute a series of commands from a YAML configuration file:

```yaml
commands:
  - type: run
    command: "git stash -u && git switch master && git pull && mvn clean"
    inputPaths:
      - "."

  - type: replace
    oldPattern: "oldText"
    newPattern: "newText"
    inputPaths:
      - "src/main/java"
      - "src/main/resources"
    replaceFolderNames: true
    ignorePattern: "target"

  - type: copy
    destination: "dest"
    inputPaths:
      - "src/main/java"
      - "src/main/resources"
      - "pom.xml"
```

Run the configuration:

```bash
# Basic usage
calcifer apply config.yaml

# With verbose output
calcifer apply config.yaml -v
```

## Command Options

### Common Options

- `-v, --verbose`: Enable verbose output
- `-i, --input`: Input folder paths (0 or more)
- `-f, --folder-list`: File containing list of folders

### Run Command Options

- `command`: The shell command to execute

### Replace Command Options

- `oldPattern`: Pattern to replace
- `newPattern`: Replacement text
- `--folder-names`: Replace folder names as well
- `--ignore`: Pattern to ignore folders

### Copy Command Options

- `destination`: Destination folder path

### Apply Command Options

- `configFile`: Path to YAML configuration file

## Examples

### Git and Maven Operations

```yaml
commands:
  - type: run
    command: "git stash -u && git switch master && git pull && mvn clean"
    inputPaths:
      - "."
```

### Text Replacement and Copy

```yaml
commands:
  - type: replace
    oldPattern: "com.example"
    newPattern: "com.newpackage"
    inputPaths:
      - "src/main/java"
    replaceFolderNames: true

  - type: copy
    destination: "backup"
    inputPaths:
      - "src/main/java"
      - "pom.xml"
```

## License

This project is licensed under the MIT License - see the LICENSE file for details. 