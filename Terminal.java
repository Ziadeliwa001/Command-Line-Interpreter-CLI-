import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Scanner;
import java.util.Arrays;
public class Terminal {
    Parser parser;

    public Terminal() {
        parser = new Parser();
    }

    public String pwd() {
        String currentDirectory = System.getProperty("user.dir");
        return "Current directory is: " + currentDirectory;
    }

    public void cd(String[] args) {
        String currentDirectory = System.getProperty("user.dir");
        if (args ==null){
            cdHome();
        }
        else if (args[0].equals("..")) {
            // Case 2: Change to the previous directory
            File currentDir = new File(currentDirectory);
            File parentDir = currentDir.getParentFile();
            if (parentDir != null) {
                System.setProperty("user.dir", parentDir.getAbsolutePath());
                System.out.println("Current directory is now: " + parentDir.getAbsolutePath());
            }
            else
                System.out.println("Cannot navigate to the previous directory, Already at the root.");

        }
        else
        {
            // Case 3: Change to the specified directory
            File newDir = new File(args[0]);

            if (!newDir.isAbsolute()) {
                newDir = new File(currentDirectory, args[0]);
            }

            if (!newDir.exists() || !newDir.isDirectory()) {
                System.out.println("Directory does not exist.");
            }
            else
            {
                System.setProperty("user.dir", newDir.getAbsolutePath());
                System.out.println("Current directory is now: " + newDir.getAbsolutePath());
            }
        }
    }

    public void cdHome() {
        // Case 1: Change to the home directory
        String userHomeDirectory = System.getProperty("user.home");
        System.setProperty("user.dir", userHomeDirectory);
        System.out.println("Current directory is now: " + userHomeDirectory);
    }
    public void ls(){
        String currentDirectoryPath = System.getProperty("user.dir");
        File currentDirectory = new File(currentDirectoryPath);

        // Check if the path exists and is a directory
        if (currentDirectory.exists() && currentDirectory.isDirectory()) {
            // List the files in the directory
            File[] files = currentDirectory.listFiles();

            if (files != null) {
                // Sort the files alphabetically
                Arrays.sort(files);

                // Print the sorted file names
                for (File file : files)
                {
                    System.out.println(file.getName());
                }
            }
            else
                System.out.println("No files found in the current directory.");
        }
        else
        {
            System.err.println("The current directory is invalid or does not exist.");
        }

    }
    public void ls_r(){
        String currentDirectoryPath = System.getProperty("user.dir");
        File currentDirectory = new File(currentDirectoryPath);

        // Check if the path exists and is a directory
        if (currentDirectory.exists() && currentDirectory.isDirectory()) {
            // List the files in the directory
            File[] files = currentDirectory.listFiles();

            if (files != null) {
                // Sort the files alphabetically in reverse order
                Arrays.sort(files, Collections.reverseOrder());

                // Print the sorted file names
                for (File file : files)
                {
                    System.out.println(file.getName());
                }
            }
            else
                System.out.println("No files found in the current directory.");
        }
        else
        {
            System.err.println("The current directory is invalid or does not exist.");
        }

    }


    public void cp(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: cp <sourceFile> <destinationFile>");
        } else {
            String sourceFile = args[0];
            String destinationFile = args[1];

            try {
                File inputFile = new File(sourceFile);
                File outputFile = new File(destinationFile);

                if (!inputFile.exists()) {
                    System.out.println("Source file does not exist.");
                } else if (outputFile.exists()) {
                    System.out.println("Destination file already exists.");
                } else {
                    InputStream inputStream = new FileInputStream(inputFile);
                    OutputStream outputStream = new FileOutputStream(outputFile);

                    byte[] buffer = new byte[1024];
                    int length;

                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }

                    inputStream.close();
                    outputStream.close();

                    System.out.println("File copied successfully.");
                }
            } catch (IOException e) {
                System.out.println("Error copying the file: " + e.getMessage());
            }
        }
    }

    public void cp_r(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: cp -r <sourceDirectory> <destinationDirectory>");
        } else {
            String sourceDirectory = args[0];
            String destinationDirectory = args[1];

            File sourceDir = new File(sourceDirectory);
            File destinationDir = new File(destinationDirectory);

            if (!sourceDir.exists() || !sourceDir.isDirectory()) {
                System.out.println("Source directory does not exist or is not a directory.");
            } else if (destinationDir.exists() && !destinationDir.isDirectory()) {
                System.out.println("Destination exists but is not a directory.");
            } else {
                try {
                    copyDirectory(sourceDir, destinationDir);
                    System.out.println("Directory copied successfully.");
                } catch (IOException e) {
                    System.out.println("Error copying the directory: " + e.getMessage());
                }
            }
        }
    }

    private void copyDirectory(File sourceDir, File destinationDir) throws IOException {
        if (sourceDir.isDirectory()) {
            if (!destinationDir.exists()) {
                destinationDir.mkdir();
            }

            String[] files = sourceDir.list();
            for (String file : files) {
                File srcFile = new File(sourceDir, file);
                File destFile = new File(destinationDir, file);
                copyDirectory(srcFile, destFile);
            }
        } else {
            InputStream in = new FileInputStream(sourceDir);
            OutputStream out = new FileOutputStream(destinationDir);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            in.close();
            out.close();
        }
    }

    public void chooseCommandAction() {
        String commandName = parser.getCommandName();
        String[] args = parser.getArgs();
        switch (commandName) {
            case "pwd":
                String result = pwd();
                System.out.println(result);
                break;
            case "cp -r":
                cp_r(args);
                break;
            case "cp":
                cp(args);
                break;
            case "cd":
                cd(args);
                break;
            case "ls":
                ls();
                break;
            case "ls -r":
                ls_r();
                break;
            default:
                System.out.println("Command not found: " + commandName);
        }
    }

    public static void main(String[] args) {
        Terminal terminal = new Terminal();

        System.out.println("Welcome to the Command Line Interpreter!");
        System.out.println("Enter a command or type 'exit' to quit.");

        while (true) {
            System.out.print("> "); // Prompt for user input
            Scanner scanner = new Scanner(System.in);
            String userInput = scanner.nextLine().trim();

            if (userInput.equalsIgnoreCase("exit")) {
                System.out.println("Exiting the CLI. Goodbye!");
                break; // Exit the loop and terminate the program
            }
            else if (userInput.isEmpty()) {
                continue; // Skip to the next iteration of the loop
            }

            if (terminal.parser.parse(userInput)) {
                terminal.chooseCommandAction();
            } else {
                System.out.println("Invalid input. Please enter a valid command.");
            }
        }
    }
}