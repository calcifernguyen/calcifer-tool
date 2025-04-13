# Software Requirements Specification (SRS)
# Calcifer Tool

## 1. Introduction

### 1.1 Purpose
Calcifer is a command-line utility designed to perform batch operations on multiple directories. It provides functionality for running commands, replacing text patterns, and copying files across multiple directories.

### 1.2 Scope
The tool is designed to help developers and system administrators perform repetitive tasks across multiple directories efficiently. It supports operations on both files and directories, with options for pattern matching and selective processing.

## 2. System Features

### 2.1 Run Command
The `run` command executes a specified command in multiple directories.

#### 2.1.1 Features
- Execute commands in multiple directories
- Support for file-based input of directory paths
- Logging of command execution results
- Error handling and reporting

#### 2.1.2 Usage
```bash
calcifer run [options] <command> <folder_paths...>
```

#### 2.1.3 Options
- `-f, --file`: File containing list of folder paths
- `-h, --help`: Show help message

### 2.2 Replace Command
The `replace` command replaces text patterns in files and optionally in folder names.

#### 2.2.1 Features
- Replace text patterns in file contents
- Option to replace patterns in folder names
- Support for file-based input of directory paths
- Pattern-based file/folder exclusion
- Logging of replacements and changes

#### 2.2.2 Usage
```bash
calcifer replace [options] <old_pattern> <new_pattern> <folder_paths...>
```

#### 2.2.3 Options
- `-f, --file`: File containing list of folder paths
- `-a, --all`: Replace in folder names as well
- `--ignore`: Skip files and folders matching this pattern
- `-h, --help`: Show help message

### 2.3 Copy Command
The `copy` command copies files from source to destination directories.

#### 2.3.1 Features
- Copy files between directories
- Support for file-based input of directory paths
- Logging of copy operations
- Error handling and reporting

#### 2.3.2 Usage
```bash
calcifer copy [options] <source> <destination> <folder_paths...>
```

#### 2.3.3 Options
- `-f, --file`: File containing list of folder paths
- `-h, --help`: Show help message

## 3. Technical Requirements

### 3.1 System Requirements
- Java 11 or higher
- Maven for building
- Log4j 2.x for logging

### 3.2 Dependencies
- Picocli for command-line interface
- SLF4J for logging abstraction
- Log4j 2.x for logging implementation
- Lombok for code generation

### 3.3 Logging
- Uses SLF4J with Log4j 2.x implementation
- Log levels: ERROR, WARN, INFO, DEBUG
- Console output with formatted logging pattern

## 4. Error Handling

### 4.1 General Error Handling
- All commands return exit codes (0 for success, 1 for failure)
- Detailed error messages logged
- Graceful handling of file system operations

### 4.2 Specific Error Cases
- Invalid directory paths
- File permission issues
- Pattern matching errors
- Command execution failures

## 5. Usage Examples

### 5.1 Run Command Examples
```bash
# Run git status in multiple directories
calcifer run "git status" /path/to/dir1 /path/to/dir2

# Run command using file input
calcifer run -f folders.txt "npm install"
```

### 5.2 Replace Command Examples
```bash
# Replace text in files
calcifer replace "oldText" "newText" /path/to/dir

# Replace in folder names too
calcifer replace -a "oldName" "newName" /path/to/dir

# Skip specific files/folders
calcifer replace "old" "new" /path/to/dir --ignore ".*\\.git.*"

# Use file input
calcifer replace -f folders.txt "old" "new"
```

### 5.3 Copy Command Examples
```bash
# Copy file to multiple directories
calcifer copy source.txt destination.txt /path/to/dir1 /path/to/dir2

# Use file input
calcifer copy -f folders.txt source.txt destination.txt
```

## 6. Future Enhancements
- Support for more complex pattern matching
- Additional file operations
- Configuration file support
- Progress indicators for long-running operations
- Dry-run mode for testing operations 