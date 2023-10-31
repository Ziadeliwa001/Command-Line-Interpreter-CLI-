
class Terminal {
    Parser parser;
    private final Path homeDirectory = Paths.get(System.getProperty("user.home"));
    private Path currentDirectory = homeDirectory;
    private final List<String> history = new ArrayList<>();

    public Terminal() {
        parser = new Parser();
    }

    public String pwd() {
        String currentDirectory = System.getProperty("user.dir");
        history.add("pwd ");
        return "Current directory is: " + currentDirectory;
    }

    public void cd(String[] args) {
        String currentDirectory = System.getProperty("user.dir");
        if (args ==null){
            cdHome();
            history.add("cd");
        }
        else if (args[0].equals("..")) {
            // Case 2: Change to the previous directory
            File currentDir = new File(currentDirectory);
            File parentDir = currentDir.getParentFile();
            if (parentDir != null) {
                System.setProperty("user.dir", parentDir.getAbsolutePath());
                System.out.println("Current directory is now: " + parentDir.getAbsolutePath());
                history.add("cd ..");
            } else {
                System.out.println("Cannot navigate to the previous directory. Already at the root.");
            }
        } else {
            // Case 3: Change to the specified directory
            File newDir = new File(args[0]);

            if (!newDir.isAbsolute()) {
                newDir = new File(currentDirectory, args[0]);
            }

            if (!newDir.exists() || !newDir.isDirectory()) {
                System.out.println("Directory does not exist.");
            } else {
                System.setProperty("user.dir", newDir.getAbsolutePath());
                System.out.println("Current directory is now: " + newDir.getAbsolutePath());
                history.add("cd " + newDir.getAbsolutePath());
            }
        }
    }

    public void cdHome() {
        // Case 1: Change to the home directory
        String userHomeDirectory = System.getProperty("user.home");
        System.setProperty("user.dir", userHomeDirectory);
        System.out.println("Current directory is now: " + userHomeDirectory);
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
                    history.add("cp " + sourceFile + ' ' + destinationFile);
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
                    history.add("cp -r " + sourceDirectory + ' ' + destinationDirectory);
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

    public void rmdir(String directory) {
        try {
            Path path = Paths.get(directory);
            if (Files.exists(path)) {
                Files.delete(path);
                history.add("rmdir " + directory);
            } else {
                System.out.println("Directory does not exist.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cat(String... files) {
        String hist = new String();
        hist = "cat ";
        for (String file : files) {
            try {
                List<String> lines = Files.readAllLines(Paths.get(file));
                for (String line : lines) {
                    System.out.println(line);
                }
                hist += file;
            } catch (IOException e) {
                e.printStackTrace();
                return ;
            }
        }
        history.add(hist);
    }
    public void print_history(){
        for (String command : history) {
            System.out.println(command);
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
            case "cat":
                cat(args);
                break;
            case "rmdir":
                rmdir(args[0]);
                break;
            case "history":
                print_history();
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
