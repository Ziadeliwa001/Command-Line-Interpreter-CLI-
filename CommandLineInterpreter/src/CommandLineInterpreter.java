import java.io.*;
import java.util.*;


class Parser {
    private static String commandName;
    private static String[] args;
    public boolean parse(String userInput) {
        String[] parsedInput = userInput.split(" ");
        // i want to check if the first word is a command only or parsedInput[1] is a command like -r
        if(parsedInput.length == 1)
        {
            commandName = parsedInput[0];
            args = null;
            return true;
        }
        else if(Objects.equals(parsedInput[1], "-r"))
        {
            commandName = parsedInput[0] + " " + parsedInput[1];
            args = Arrays.copyOfRange(parsedInput, 2, parsedInput.length);
            return true;
        }
        else
        {
            commandName = parsedInput[0];
            args = Arrays.copyOfRange(parsedInput, 1, parsedInput.length);
            return true;
        }
    }

    public static String getCommandName()
    {
        return commandName;
    }
    public static String[] getArgs()
    {
        return args;
    }

}

class Terminal {
    Parser parser;

    public Terminal() {
        parser = new Parser();
    }

    public String pwd() {
        String currentDirectory = System.getProperty("user.dir");
        return "Current directory is: " + currentDirectory;
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

