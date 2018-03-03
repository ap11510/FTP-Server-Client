package ftp.common.util;


import ftp.common.Commands;

public class MessageWriter
{

    //------------------------------------------------------------------------------------------------------------------
    public static void writeMessage(String message)
    {
        writeMessage(new String[]{message});
    }

    //------------------------------------------------------------------------------------------------------------------
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

    //------------------------------------------------------------------------------------------------------------------
    public static void writeError(String message, Throwable throwable)
    {
        writeError(new String[]{message}, throwable);
    }

    //------------------------------------------------------------------------------------------------------------------
    public static void writeError(String[] messageList, Throwable throwable)
    {
        writeMessage(messageList);
        throwable.printStackTrace();
    }
}
