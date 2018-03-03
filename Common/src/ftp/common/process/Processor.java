package ftp.common.process;

import ftp.common.net.ControlConnection;
import ftp.common.net.DataConnection;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Processor implements Runnable
{

    protected UUID id = null;
    protected File workingDirectory;

    protected List commandList;

    protected ControlConnection controlConnection;
    protected DataConnection dataConnection;

    protected boolean running;

    //------------------------------------------------------------------------------------------------------------------
    protected Processor(UUID id, ControlConnection controlConnection, DataConnection dataConnection) throws IOException, IllegalAccessException, ClassNotFoundException
    {
        this.id = id;
        this.running = true;
        this.workingDirectory = new File(new File(".").getCanonicalPath());
        this.controlConnection = controlConnection;
        this.dataConnection = dataConnection;

        commandList = new ArrayList();

        Class c = Class.forName("ftp.common.Commands");
        Field[] fields = c.getFields();

        for (Field f : fields)
        {
            commandList.add(((String) f.get(new String())).toUpperCase());
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    public boolean isRunning()
    {
        return running;
    }
}
