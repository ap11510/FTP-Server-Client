import java.io.*;
import java.lang.reflect.Field;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Processor {

    private Connection connection;
    private File workingDirectory;

    List commandList;

    Processor(Connection connection)
    {
        this.workingDirectory = new File(".");
        this.connection = connection;
    }

    /**
     *
     */
    public void run() throws Exception
    {
        String request;

        init();

        boolean running =  true;
        try
        {
            do
            {
                request = connection.receiveMessage();

                if (request == null)
                {
                    running = false;
                }
                else
                {
                    Utils.writeOutput("Message Received: ", request);

                    request = request.trim();
                    if (!request.equals(""))
                    {
                        String[] requestArguments = Utils.parseRequest(request);

                        String command = requestArguments[0];

                        String argument = requestArguments[1];

                        if (!commandList.contains(command))
                        {
                            connection.sendMessage(FTPResponseCodes.R_500 + "Invalid Command");
                        }
                        else
                        {

                            switch (command)
                            {
                                case FTPCommands.PWD:
                                    execute_PWD();
                                    break;
                                case FTPCommands.LIST:
                                    execute_LIST();
                                    break;
                                case FTPCommands.CWD:
                                    execute_CWD(argument);
                                    break;
                                case FTPCommands.MKD:
                                    execute_MKD(argument);
                                    break;
                                case FTPCommands.DELE:
                                    execute_DELE(argument);
                                    break;
                                case FTPCommands.RETR:
                                    execute_RETR(argument);
                                    break;
                                case FTPCommands.STOR:
                                    execute_STOR(argument);
                                    break;

                                case FTPCommands.QUIT:
                                    execute_QUIT();
                                    running = false;
                                    break;
                                default:
                                    connection.sendMessage(FTPResponseCodes.R_500 + "Command not Implemented");
                            }
                        }
                    }
                }
            }
            while (running);
        }
        catch (IOException exception)
        {
            Utils.writeOutput("Communication Error. Connection with remote host terminated.", null);
            exception.printStackTrace();
        }
    }

    /**
     * Send the Directory/File name list in the working directory
     *
     */
    private void execute_LIST()
    {
        File[] fileList = workingDirectory.listFiles();

        StringBuffer response = new StringBuffer();

        response.append(FTPResponseCodes.R_200);

        for(File file: fileList)
        {
            if(file.isFile())
            {
                response.append("File     : ");
            }
            else if(file.isDirectory())
            {
                response.append("Directory: ");
            }

            response.append(file.getName());

            response.append("\n");

        }
        connection.sendMessage(response.toString());
    }


    /**
     * Get the name of the working directory
     *
     * @throws IOException - thrown when sending to remote host
     */
    private void execute_PWD() throws IOException
    {
        connection.sendMessage(
                    (new StringBuffer(FTPResponseCodes.R_200)
                    .append("Remote Host Working Directory: ")
                    .append(workingDirectory.getCanonicalPath())
                    .toString()));
    }

    /**
     * Change working directory
     *
     * @param argument - directory name
     * @throws IOException - thrown when sending to remote host
     */
    private void execute_CWD(String argument) throws IOException
    {
        String response;
        if(argument.equals(""))
        {
            response = (FTPResponseCodes.R_500 + "Improper Usage: Need either <Directory> or <..>");
        }
        else
        {
            StringBuffer newDirectoryName = new StringBuffer();

            if(argument.startsWith("/"))
            {
                newDirectoryName.append(argument);
            }
            else
            if(argument.equals(".."))
            {
                if (workingDirectory.getCanonicalPath().equals("/"))
                {
                    newDirectoryName.append("/");
                }
                else
                {
                    String[] pathElements = workingDirectory.getCanonicalPath().split("/");
                    for (int i = 0; i < pathElements.length - 1 ; i++)
                    {
                        newDirectoryName.append("/");
                        newDirectoryName.append(pathElements[i]);
                    }
                }
            }
            else
            {
                newDirectoryName.append(workingDirectory.getCanonicalPath());
                newDirectoryName.append("/");
                newDirectoryName.append(argument);
            }

            File newDirectory = new File(newDirectoryName.toString());
            if (newDirectory.exists())
            {
                if (newDirectory.isDirectory())
                {
                    this.workingDirectory = newDirectory;
                    response =
                            (new StringBuffer(FTPResponseCodes.R_200))
                            .append("Remote Host Working Directory changed to: ")
                            .append(workingDirectory.getCanonicalPath())
                            .toString();
                }
                else
                {
                    response =
                            (new StringBuffer(FTPResponseCodes.R_500))
                            .append(newDirectoryName.toString())
                            .append(" - This is a file not a Directory")
                            .toString();
                }
            }
            else
            {
                response =
                        (new StringBuffer(FTPResponseCodes.R_500))
                        .append("Directory doesn't exist")
                        .toString();
             }
        }
        connection.sendMessage(response);
    }

    /**
     * Create Directory
     *
     * @param argument - Name of Directory to be created
     *
     * @throws IOException - thrown when sending to remote host
     */
    private void execute_MKD(String argument) throws IOException {
        String response;

        if (argument.equals(""))
        {
            response =
                    (new StringBuffer(FTPResponseCodes.R_500))
                            .append("Invalid argument, missing directory name")
                            .toString();
        }
        else if (argument.startsWith(".") || argument.startsWith("/"))
        {
            response =
                    (new StringBuffer(FTPResponseCodes.R_500))
                            .append("Invalid argument: ")
                            .append(argument)
                            .toString();
        } else
            {

            String name = new StringBuffer(workingDirectory.getCanonicalPath()).append("/").append(argument).toString();

            File file = new File(name);


            if (file.exists()) {
                if (file.isDirectory())
                {
                    response =
                            (new StringBuffer(FTPResponseCodes.R_500))
                            .append("Directory already exists")
                            .append(name)
                            .toString();
                }
                else
                {
                    response =
                            (new StringBuffer(FTPResponseCodes.R_500))
                            .append("File with the same name already exists")
                            .append(name)
                            .toString();
                }
            }
            else
            {
                try
                {
                    if (file.mkdir())
                    {
                        response =
                                (new StringBuffer(FTPResponseCodes.R_200))
                                .append("Directory was created successfully : ")
                                .append(name)
                                .toString();
                    }
                    else
                    {
                        response =
                                (new StringBuffer(FTPResponseCodes.R_500))
                                .append("Directory was not created successfully : ")
                                .append(name)
                                .toString();
                    }

                } catch (SecurityException exception) {
                    response =
                            (new StringBuffer(FTPResponseCodes.R_500))
                                    .append("You don't have permissions to create directory : ")
                                    .append(name)
                                    .toString();
                }
            }
        }
        connection.sendMessage(response);
    }

    /**
     * Delete file
     *
     * @param argument - file name
     *
     * @throws IOException - thrown when sending to remote host
     */
    private void execute_DELE(String argument) throws IOException
    {
        String response;
        if(argument.equals(""))
        {
            response =
                    (new StringBuffer(FTPResponseCodes.R_500))
                            .append("Improper Usage: Command is delete <remote_file_name>")
                            .toString();
        }
        else
        {
            String name = (new StringBuffer(workingDirectory.getCanonicalPath())).append("/").append(argument).toString();

            File file = new File(name);

            if(!file.isDirectory())
            {
                if(file.exists())
                {
                    try
                    {
                        if (file.delete())
                        {
                            response =
                                    (new StringBuffer(FTPResponseCodes.R_200))
                                            .append("File was deleted successfully: ")
                                            .append(name)
                                            .toString();

                        }
                        else
                        {
                            response =
                            (new StringBuffer(FTPResponseCodes.R_500))
                                    .append("File was not deleted successfully: ")
                                    .append(name)
                                    .toString();
                        }

                    }
                    catch (SecurityException exception)
                    {
                        response =
                                (new StringBuffer(FTPResponseCodes.R_500))
                                        .append("You don't have permissions to delete File: ")
                                        .append(name)
                                        .toString();
                    }
                }
                else
                {
                    response =
                            (new StringBuffer(FTPResponseCodes.R_500))
                                    .append("File does not exist: ")
                                    .append(name)
                                    .toString();
                }
            }
            else
            {
                response =
                        (new StringBuffer(FTPResponseCodes.R_500))
                                .append("Cannot delete a directory : ")
                                .append(name)
                                .append("\n Use command: ")
                                .append(FTPCommands.RMD)
                                .toString();
            }
        }
        connection.sendMessage(response);
    }

    /**
     * Get the name of the working directory
     *
     * @throws IOException - thrown when sending to remote host
     */
    private void execute_RETR(String argument) throws IOException
    {
        String response;
        String name = (new StringBuffer(workingDirectory.getCanonicalPath())).append("/").append(argument).toString();

        File file = new File(name);

        if (!file.exists())
        {
            response =
                    (new StringBuffer(FTPResponseCodes.R_400))
                            .append("File does not exist on remote host: ")
                            .append(name)
                            .toString();
            connection.sendMessage(response);
        }
        else
        {
            response = FTPResponseCodes.R_100;
            connection.sendMessage(response);

            FileInputStream is = null;

            try
            {
                is = new FileInputStream(file);

                connection.sendData(file.length(), is);

                is.close();
            }
            finally
            {
                is = null;
            }
            response = FTPResponseCodes.R_200 + "File transfer successfull";
            connection.sendMessage(response);

        }
    }

    /**
     * Get the name of the working directory
     *
     * @throws IOException - thrown when sending to remote host
     */
    private void execute_STOR(String argument) throws IOException
    {
        String response;
        String name = (new StringBuffer(workingDirectory.getCanonicalPath())).append("/").append(argument).toString();

        File file = new File(name);

        if (file.exists())
        {
            response =
                    (new StringBuffer(FTPResponseCodes.R_400))
                            .append("File already exists on remote host: ")
                            .append(name)
                            .toString();
            connection.sendMessage(response);
        }
        else
        {
            response = FTPResponseCodes.R_100;
            connection.sendMessage(response);

            FileOutputStream os = null;

            try
            {
                os = new FileOutputStream(file);

                connection.receiveData(os);

                os.close();
            }
            finally
            {
                os = null;
            }
            response = FTPResponseCodes.R_200 + "File transfer successfull";
            connection.sendMessage(response);
        }
    }



    /**
     * Terminates a client session
     * @throws IOException - thrown when sending to remote host
     */
    private void execute_QUIT() throws IOException
    {
        connection.stop();
        connection = null;
        Utils.writeOutput("Server Now Ready To Accept New Connection", null);
    }

    private void init() throws Exception
    {
        commandList = new ArrayList();

        Class c = Class.forName("FTPCommands");
        Field[] fields = c.getFields();

        for (Field f : fields)
        {
            commandList.add(((String)f.get(new String())).toUpperCase());
        }

    }
}

