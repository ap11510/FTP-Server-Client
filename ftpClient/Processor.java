import java.io.*;
import java.lang.reflect.Field;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Processor
{
    private File workingDirectory;
    private Connection connection;

    List commandList = new ArrayList();

    /**
     * Constructor
     * @param connection
     */
    public Processor(Connection connection)
    {
        this.workingDirectory = new File(".");
        this.connection = connection;
    }

    /**
     * Main method that reads user inputs, send commands to remote host, receive remote host responses
     * and writes user output
     *
     * @throws IOException
     */
    public void run() throws IOException
    {
        String request ;
        String response ;

        try
        {
            Class c = Class.forName("FTPCommands");
            Field[] fields = c.getFields();

            for (Field f : fields)
            {
                commandList.add(f.get(new String()));
            }

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
                            Utils.writeOutput("Invalid Command", null);
                        }
                        else
                        {

                            switch (command)
                            {
                                case FTPCommands.RETR:
                                    response = execute_RETR(argument);
                                    break;
                                case FTPCommands.STOR:
                                    response = execute_STOR(argument);
                                    break;
                                case FTPCommands.QUIT:
                                    connection.sendMessage(command);
                                    running = false;
                                    break;
                                default:
                                    response = execute_ServerCommand(request);
                            }
                            if (response != null)
                            {
                                Utils.writeOutput("Received :", response);

                            }
                        }
                    }
                    System.out.print("\n");
                }
                while (running);
            }
            catch (IOException exception)
            {
                Utils.writeOutput("Connection terminated by remote host : " + connection.getRemoteHostName() +  "Port: " + connection.getPort(), null);
                throw exception;
            }
        }
        catch (Exception exception)
        {
            Utils.writeOutput("Runtime Error. Cannot Execute", null);
            exception.printStackTrace();
        }

    }

    private String execute_RETR(String argument) throws IOException
    {
        String response = null;

        if (argument.equals(""))
        {
            Utils.writeOutput("Please specify File Name", null);
        }
        else
        if (argument.startsWith(".") || argument.startsWith("/"))
        {
            Utils.writeOutput("Invalid File Name", null);
        }
        else
        {
            String name = new StringBuffer(workingDirectory.getCanonicalPath()).append("/").append(argument).toString();
            File file = new File(argument);

            if (file.exists())
            {
                Utils.writeOutput("File already exists", null);
            }
            else
            {

                response = execute_ServerCommand(FTPCommands.RETR + " " + argument);

                if (response.startsWith(FTPResponseCodes.R_100))
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
            Utils.writeOutput("Please specify File Name", null);
        }
        else
        if (argument.startsWith(".") || argument.startsWith("/"))
        {
            Utils.writeOutput("Invalid File Name", null);
        }
        else
        {
            String name = new StringBuffer(workingDirectory.getCanonicalPath()).append("/").append(argument).toString();
            File file = new File(argument);

            if (!file.exists())
            {
                Utils.writeOutput("File does not exists", null);
            }
            else
            {

                response = execute_ServerCommand(FTPCommands.STOR + " " + argument);

                if (response.startsWith(FTPResponseCodes.R_100))
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

    private String  execute_ServerCommand(String request) throws IOException
    {
        connection.sendMessage(request);
        return connection.receiveMessage();
    }

    /**
     * readInput
     *
     * Read user Input
     *
     * @param message - Message to be displayed
     *
     * @return input provided by the user
     *
     * @throws IOException - exception thrown with user input
     */
    private String readInput(String message) throws IOException
    {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(message);
        return in.readLine();
    }

}
