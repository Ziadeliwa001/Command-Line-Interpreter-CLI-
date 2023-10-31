import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.Scanner;
import java.util.Arrays;
public class Parser {
    private static String commandName;
    private static String[] args;
    public boolean parse(String userInput) {
        String[] parsedInput = userInput.split(" ");

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
