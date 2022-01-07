package Utils;

import burp.BurpExtender;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;


/**
 * @author linchen
 */
public class Config {
    /**
     * 配置文件
     */
    public static File burpDomainConfig = new File("burpDomain_setting.json");
    public static HashMap<String, String> initDatabaseSetting = new HashMap<String, String>(5){{
        put("host","");
        put("port","");
        put("username","");
        put("password","");
        put("database","");
    }};
    public static String getDomainIp(String domain) {
        try {
            return InetAddress.getByName(domain).getHostAddress();
        }catch (UnknownHostException ne){
            return "Unknown";
        }
    }

    public static Boolean isBuild(){
        return burpDomainConfig.exists()&&burpDomainConfig.isFile();
    }

    public static HashMap<String,String> parseJson() {
        try {
            BurpExtender.getStdout().println(Config.burpDomainConfig.getAbsolutePath());
            BufferedReader settingReader = new BufferedReader(new FileReader(Config.burpDomainConfig));
            HashMap<String,String> tmp = new Gson().fromJson(settingReader, new TypeToken<HashMap<String, String>>() {}.getType());
            if(tmp == null){
                return initDatabaseSetting;
            }
            return tmp;
        } catch (FileNotFoundException e) {
            BurpExtender.getStderr().println(e);
            return initDatabaseSetting;
        }
    }

    public static void writeJson(HashMap<String,String> setting){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(Config.burpDomainConfig));
            writer.write(gson.toJson(setting));
            writer.flush();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try{
            URL u = new URL("https://portal.huaxincem.com:8383");
            System.out.println(u.getProtocol());
            System.out.println(u.getHost());
            String port = u.getPort() == -1 ? String.valueOf(u.getDefaultPort()) : String.valueOf(u.getPort());
            System.out.println(u.getPath());
            System.out.println("".equals(u.getPath()) ? "/" : u.getPath());
            System.out.println("Host: bpm.corpautohome.com".substring(5));

        }catch (Exception e){

        }
    }
}
