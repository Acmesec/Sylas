package utils;

import burp.BurpExtender;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;


/**
 * @author linchen
 */
public class Config {
    /**
     * 配置文件
     */
    public static File sylasConfig = new File("sylas_setting.json");
    public static Map<String,String> getInitDatabaseSetting(){
        Map<String, String> initDatabaseSetting = new HashMap<String, String>(6);
        initDatabaseSetting.put("db_server","");
        initDatabaseSetting.put("host","");
        initDatabaseSetting.put("port","");
        initDatabaseSetting.put("username","");
        initDatabaseSetting.put("password","");
        initDatabaseSetting.put("database","");
        return initDatabaseSetting;
    }
    public static String getDomainIp(String domain) {
        try {
            return InetAddress.getByName(domain).getHostAddress();
        }catch (UnknownHostException ne){
            return "Unknown";
        }
    }

    public static Boolean isBuild(){
        return sylasConfig.exists()&& sylasConfig.isFile();
    }

    public static Map<String,String> parseJson() {
        try {
            BurpExtender.getStdout().println(Config.sylasConfig.getAbsolutePath());
            BufferedReader settingReader = new BufferedReader(new FileReader(Config.sylasConfig));
            Map<String,String> tmp = new Gson().fromJson(settingReader, new TypeToken<HashMap<String, String>>() {}.getType());
            if(tmp == null){
                return getInitDatabaseSetting();
            }
            return tmp;
        } catch (FileNotFoundException e) {
            BurpExtender.getStderr().println(e);
            return getInitDatabaseSetting();
        }
    }

    public static void writeJson(Map<String,String> setting){
        Gson gson = new GsonBuilder().setPrettyPrinting().enableComplexMapKeySerialization().serializeNulls().create();
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(Config.sylasConfig));
            writer.write(gson.toJson(setting));
            writer.flush();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
