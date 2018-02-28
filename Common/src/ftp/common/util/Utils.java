package ftp.common.util;


import ftp.common.application.Config;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Utils
{

    public static String[] parseRequest(String request)
    {

        String[] requestArguments = new String[2];

        request = request.trim();
        if (!request.equals(""))
        {
            String command;

            String argument = "";

            if (request.indexOf(" ") != -1)
            {
                command = request.substring(0, request.indexOf(" ")).toUpperCase();
                argument = request.substring(request.indexOf(" ") + 1);
            }
            else
            {
                command = request.toUpperCase();
            }

            requestArguments[0] = command;
            requestArguments[1] = argument;

        }

        return requestArguments;
    }

    public static void writeMessage(String message)
    {
        writeMessage(new String[]{message});
    }

    public static void writeMessage(String[] messageList)
    {
        if (messageList != null && messageList.length > 0)
        {
            for (String message : messageList)
            {
                if (message != null)
                {
                    System.out.println(message);
                }
            }
            System.out.println("\n");
        }
    }

    public static void writeError(String message, Throwable throwable)
    {
        writeError(new String[]{message}, throwable);
    }

    public static void writeError(String[] messageList, Throwable throwable)
    {
        writeMessage(messageList);
        throwable.printStackTrace();
    }
}
