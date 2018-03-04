package ftp.common.application;

public class Config
{
    private static Config instance;
    private static String environmentType = Environment.SERVER;
    private static boolean initialized = false;
    private Identity identity;

    //------------------------------------------------------------------------------------------------------------------
    public static Config getInstance()
    {
        if (instance == null)
        {
            instance = new Config();
            instance.identity = new Identity();
        }
        return instance;
    }

    //------------------------------------------------------------------------------------------------------------------
    public static String getEnvironmentType()
    {
        return environmentType;
    }

    //------------------------------------------------------------------------------------------------------------------
    public static void initialize(String environmentType)
    {
        if (!initialized)
        {
            Config.environmentType = environmentType;
            initialized = true;
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    public Identity getIdentity()
    {
        return identity;
    }

    //------------------------------------------------------------------------------------------------------------------
    public interface Environment
    {
        String CLIENT = "client";
        String SERVER = "server";
    }

}
