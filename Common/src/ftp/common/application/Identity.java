package ftp.common.application;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;

public class Identity
{
    private String userName;
    private String machineName;

    Level loggingLevel;

    protected Identity()
    {
        userName = System.getProperty("user.name");

        try
        {
            machineName = InetAddress.getLocalHost().getCanonicalHostName();
        }
        catch (UnknownHostException exception)
        {
            machineName = System.getenv("hostname");
            if (machineName == null)
            {
                machineName = System.getenv("computername");
            }
        }
    }

    public String getUserName()
    {
        return userName;
    }

    public String getMachineName()
    {
        return machineName;
    }

    public Level getLoggingLevel()
    {
        return loggingLevel;
    }

    public void setLoggingLevel(Level loggingLevel)
    {
        this.loggingLevel = loggingLevel;
    }
}
