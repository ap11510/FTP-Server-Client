package ftp.client;

import ftp.client.net.ClientConnection;
import ftp.common.Commands;
import ftp.common.Codes;
import ftp.common.Processor;
import ftp.common.util.Utils;

import java.io.*;

public class ClientProcessor extends Processor
{
    /**
     * Constructor
     *
     * @param connection
     */
    public ClientProcessor(ClientConnection connection) throws IOException, ClassNotFoundException, IllegalAccessException
    {
        super(connection);
    }

    /**
     * Main method that reads user inputs, send commands to remote host, receive remote host responses
     * and writes user output
     *
     */
    public void run()
    {
        String request;
        String response;

        try
        {
            boolean running = true;
            do
            {
                request = readInput("Command > ").trim();

                response = null;
                if (!request.equals(""))
                {
                    String[] requestArguments = Utils.parseRequest(request);

                    String command = requestArguments[0];

                    String argument = requestArguments[1];

                    if (!commandList.contains(command))
                    {
                        Utils.writeMessage(new String[]{"Invalid Command", command});
                    }
                    else
                    {

                        switch (command)
                        {
                            case Commands.RETR:
                                response = execute_RETR(argument);
                                break;
                            case Commands.STOR:
                                response = execute_STOR(argument);
                                break;
                            case Commands.QUIT:
                                connection.sendMessage(Commands.QUIT);
                                running = false;
                                break;
                            default:
                                response = execute_ServerCommand(request);
                        }
                        if (response != null)
                        {
                            Utils.writeMessage(new String[]{response});
                        }
                    }
                }
            }
            while (running);
        }
        catch (IOException exception)
        {
            Utils.writeError("ClientConnection terminated by remote host : " + connection.getRemoteHostName() + ":" + connection.getRemoteHostCommandPort(), exception);
        }
    }

    private String execute_RETR(String argument) throws IOException
    {
        String response = null;

        if (argument.equals(""))
        {
            Utils.writeMessage("Please specify File Name");
        }
        else if (argument.startsWith(".") || argument.startsWith("/"))
        {
            Utils.writeMessage(new String[]{"Invalid File Name", argument});
        }
        else
        {
//            String name = new StringBuffer(workingDirectory.getCanonicalPath()).append(workingDirectory.toPath().getFileSystem().getSeparator()).append(argument).toString();
            File file = new File(argument);

            if (file.exists())
            {
                Utils.writeMessage(new String[]{"File already exists", argument});
            }
            else
            {

                response = execute_ServerCommand(Commands.RETR + " " + argument);

                if (response.startsWith(Codes.R_100))
                {
                    FileOutputStream fileOutputStream = new FileOutputStream(file);

                    connection.receiveData(fileOutputStream);

                    fileOutputStream.close();
                    response = connection.receiveMessage();
                }

            }
        }
        return response;
    }

    private String execute_STOR(String argument) throws IOException
    {
        String response = null;

        if (argument.equals(""))
        {
            Utils.writeMessage("Please specify File Name");
        }
        else if (argument.startsWith(".") || argument.startsWith("/"))
        {
            Utils.writeMessage(new String[]{"Invalid File Name", argument});
        }
        else
        {
//            String name = new StringBuffer(workingDirectory.getCanonicalPath()).append("/").append(argument).toString();
            File file = new File(argument);

            if (!file.exists())
            {
                Utils.writeMessage(new String[]{"File does not exists", argument});
            }
            else
            {

                response = execute_ServerCommand(Commands.STOR + " " + argument);

                if (response.startsWith(Codes.R_100))
                {
                    FileInputStream fileInputStream = new FileInputStream(file);

                    connection.sendData(file.length(), fileInputStream);

                    fileInputStream.close();
                    response = connection.receiveMessage();
                }

            }
        }
        return response;
    }

    private String execute_ServerCommand(String request) throws IOException
    {
        connection.sendMessage(request);
        return connection.receiveMessage();
    }

    /**
     * readInput
     * <p>
     * Read user Input
     *
     * @param promptMessage - Message to be displayed
     * @return input provided by the user
     * @throws IOException - exception thrown with user input
     */
    private String readInput(String promptMessage) throws IOException
    {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(promptMessage);
        return in.readLine();
    }

}
