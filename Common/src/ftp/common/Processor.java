package ftp.common;

import ftp.common.net.Connection;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class Processor
{
    protected File workingDirectory;

    protected List commandList;

    protected Connection connection;

    protected Processor(Connection connection) throws IOException, IllegalAccessException, ClassNotFoundException
    {
        this.workingDirectory = new File(new File(".").getCanonicalPath());
        this.connection = connection;
        initialize();
    }

    private void initialize() throws ClassNotFoundException, IllegalAccessException
    {
        commandList = new ArrayList();

        Class c = Class.forName("ftp.common.Commands");
        Field[] fields = c.getFields();

        for (Field f : fields)
        {
            commandList.add(((String) f.get(new String())).toUpperCase());
        }
    }
}
