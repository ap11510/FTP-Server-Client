package ftp.common.net;

import java.io.IOException;

public abstract class ConnectionFactory
{
    protected boolean initialized = false;

    protected int controlSocketPortNumber;
    protected int dataSocketPortNumber;

    public abstract ControlConnection getControlConnection() throws IOException;

    public abstract DataConnection getDataConnection() throws IOException;

    public int getControlSocketPortNumber()
    {
        return controlSocketPortNumber;
    }

    public int getDataSocketPortNumber()
    {
        return dataSocketPortNumber;
    }
}
