package ftp.common.util;


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
            System.out.println();
            for (String message : messageList)
            {
                if (message != null)
                {
                    System.out.println(message);
                }
            }
            System.out.println();
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
