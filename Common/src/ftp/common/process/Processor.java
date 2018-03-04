package ftp.common.process;

import ftp.common.net.ControlConnection;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class Processor implements Runnable
{
    protected File workingDirectory;

    protected List commandList;

    protected ControlConnection controlConnection;

    protected boolean running;

    protected String sessionId;

    //------------------------------------------------------------------------------------------------------------------
    protected Processor() throws IOException, IllegalAccessException, ClassNotFoundException
    {
        this.running = true;
        this.workingDirectory = new File(new File(".").getCanonicalPath());

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
