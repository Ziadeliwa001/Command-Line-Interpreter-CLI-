import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

class Parser {
    String commandName;
    String[] args;

    public boolean parse(String input) {
        List<String> parts = new ArrayList<>();
        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(input);
        while (m.find()) {
            parts.add(m.group(1).replace("\"", ""));
        }
        commandName = parts.get(0);
        if (parts.size() > 1 && parts.get(1).equals("-r")) {
            commandName += " " + parts.get(1);
            parts.remove(1);
        }
        args = parts.subList(1, parts.size()).toArray(new String[0]);
        return true;
    }



    public String getCommandName() {
        return commandName;
    }

    public String[] getArgs() {
        return args;
    }
}

class Terminal {
    private final Path homeDirectory = Paths.get(System.getProperty("user.home"));
    private Path currentDirectory = homeDirectory;
    private final List<String> history = new ArrayList<>();
    private boolean isDirectoryEmpty(Path directory) throws IOException {
        try (Stream<Path> dirStream = Files.list(directory)) {
            return !dirStream.findAny().isPresent();
        }
    }

    public void addCommandToHistory(String command) {
        history.add(command);
    }

    private void copy(Path source, Path target) {
        try {
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void cd(String... args) throws IOException {
        if (args.length == 0) {
            currentDirectory = homeDirectory;
        } else if (args[0].equals("..")) {
            currentDirectory = currentDirectory.getParent();
        } else {
            Path newPath = Paths.get(args[0]);
            if (newPath.isAbsolute()) {
                currentDirectory = newPath;
            } else {
                currentDirectory = currentDirectory.resolve(newPath);
            }
        }
        currentDirectory = currentDirectory.toRealPath();
        System.out.println("Current directory: " + currentDirectory);
        addCommandToHistory("cd " + String.join(" ", args));
    }

    public void pwd() {
        System.out.println(currentDirectory);
        addCommandToHistory("pwd");
    }

    public void cat(String... args) throws IOException {
        for (String arg : args) {
            Path path = currentDirectory.resolve(arg);
            System.out.println(new String(Files.readAllBytes(path)));
        }
        addCommandToHistory("cat " + String.join(" ", args));
    }

    public void history() {
        for (String command : history) {
            System.out.println(command);
        }
    }

    public void mkdir(String... args) throws IOException {
        for (String arg : args) {
            Files.createDirectories(currentDirectory.resolve(arg));
        }
        addCommandToHistory("mkdir " + String.join(" ", args));
    }

    public void rmdir(String... args) throws IOException {
        if (args.length == 0) {
            // Remove all empty directories in the current path
            try (Stream<Path> paths = Files.walk(currentDirectory)) {
                paths.filter(Files::isDirectory)
                        .filter(path -> {
                            try {
                                return isDirectoryEmpty(path);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .forEach(path -> path.toFile().delete());
            }
            addCommandToHistory("rmdir");
        } else {
            for (String arg : args) {
                Files.delete(currentDirectory.resolve(arg));
            }
            addCommandToHistory("rmdir " + String.join(" ", args));
        }
    }

    public void cp(String... args) throws IOException {
        if (args.length != 2) {
            throw new IllegalArgumentException("cp command requires 2 arguments");
        }
        Path source = currentDirectory.resolve(args[0]);
        Path target = currentDirectory.resolve(args[1]);
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        addCommandToHistory("cp " + String.join(" ", args));
    }

    public void cp_r(String... args) throws IOException {
        if (args.length != 2) {
            throw new IllegalArgumentException("cp -r command requires 2 arguments");
        }
        Path source = currentDirectory.resolve(args[0]);
        Path target = currentDirectory.resolve(args[1]);

        try (Stream<Path> paths = Files.walk(source)) {
            paths.forEach(src -> copy(src, target.resolve(source.relativize(src))));
        }
        addCommandToHistory("cp -r " + String.join(" ", args));
    }


    public void touch(String... args) throws IOException {
        for (String arg : args) {
            Files.createFile(currentDirectory.resolve(arg));
        }
        addCommandToHistory("touch " + String.join(" ", args));
    }

    public void ls(String... args) throws IOException {
        try (Stream<Path> paths = Files.list(currentDirectory)) {
            paths.map(path -> path.getFileName().toString())
                    .sorted()
                    .forEach(System.out::println);
        }
        addCommandToHistory("ls " + String.join(" ", args));
    }

    public void ls_r(String... args) throws IOException {
        try (Stream<Path> paths = Files.list(currentDirectory)) {
            paths.map(path -> path.getFileName().toString())
                    .sorted(Comparator.reverseOrder())
                    .forEach(System.out::println);
        }
        addCommandToHistory("ls -r " + String.join(" ", args));
    }



    public void rm(String... args) throws IOException {
        for (String arg : args) {
            Files.delete(currentDirectory.resolve(arg));
        }
        addCommandToHistory("rm " + String.join(" ", args));
    }


    public void chooseCommand(String commandName, String... args) throws IOException {
        switch (commandName) {
            case "cd":
                cd(args);
                break;
            case "pwd":
                pwd();
                break;
            case "cat":
                cat(args);
                break;
            case "history":
                history();
                break;
            case "mkdir":
                mkdir(args);
                break;
            case "rmdir":
                rmdir(args);
                break;
            case "cp":
                cp(args);
                break;
            case "cp -r":
                cp_r(args);
                break;
            case "touch":
                touch(args);
                break;
            case "ls":
                ls(args);
                break;
            case "ls -r":
                ls_r(args);
                break;
            case "rm":
                rm(args);
                break;
            default:
                System.out.println("Unknown command: " + commandName);
        }
    }

    public static void main(String[] args) throws IOException {
        Terminal terminal = new Terminal();
        Parser parser = new Parser();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("exit")) {
                break;
            }
            if (parser.parse(input)) {
                terminal.chooseCommand(parser.getCommandName(), parser.getArgs());
            }
        }
    }
}
