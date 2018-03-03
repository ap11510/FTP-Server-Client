package ftp.common.util;

import ftp.common.Commands;

public class InputParser
{
    //------------------------------------------------------------------------------------------------------------------
    public static String[] parseRequest(String request)
    {

        String[] requestArguments = new String[3];

        request = request.trim();
        if (!request.equals(""))
        {
            String command;
            String argument = "";
            String suffix = "";

            if (request.indexOf(" ") != -1)
            {
                command = request.substring(0, request.indexOf(" ")).toUpperCase();
                argument = request.substring(request.indexOf(" ") + 1).trim();
            }
            else
            {
                command = request.toUpperCase();
            }

            if (command.endsWith(Commands.COMMAND_SUFFIX))
            {
                command = command.substring(0, command.length()-1).trim();
            }

            if (argument.endsWith(Commands.COMMAND_SUFFIX))
            {
                argument = argument.substring(0, argument.length()-1).trim();
                suffix = Commands.COMMAND_SUFFIX;
            }
            requestArguments[0] = command;
            requestArguments[1] = argument;
            requestArguments[2] = suffix;

        }

        return requestArguments;
    }
}
