package burp;


import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import domain.DomainConsumer;
import domain.DomainProducer;
import ui.Sylas;
import ui.ControlSwitch;
import utils.Config;
import utils.DbUtil;

public class BurpExtender implements IBurpExtender, ITab, IHttpListener{

    private static IBurpExtenderCallbacks callbacks;
    //    public static HashSet<String> subDomainSet = new HashSet<>();
    public static HashMap<String, HashMap<String, String>> subDomainMap = new HashMap<>();
    public static HashMap<String, String> urlMap = new HashMap<>();
    public static HashMap<String, HashMap<String, String>> similarSubDomainMap = new HashMap<>();
    public static HashMap<String,HashMap<String,String>> subDomainBscanAliveMap = new HashMap<>();
    public static HashMap<String,HashMap<String,String>> similarSubDomainBscanAliveMap = new HashMap<>();
//    public static HashMap<String,HashMap<String,String>> similarDomainMap = new HashMap<>();
    public static HashMap<String,String> similarUrlMap = new HashMap<>();
    public static BlockingQueue<IHttpRequestResponse> inputQueue = new LinkedBlockingQueue<>();
    public static BlockingQueue<String> urlQueue = new LinkedBlockingQueue<>();
    public static BlockingQueue<String> subDomainQueue = new LinkedBlockingQueue<>();
    public static BlockingQueue<String> similarSubDomainQueue = new LinkedBlockingQueue<>();
    public static BlockingQueue<String> similarUrlQueue = new LinkedBlockingQueue<>();
    public static int subDomainCount = 0;
    public static int urlCount = 0;
    public static int similarSubDomainCount = 0;
    public static int similarUrlCount = 0;
    public static int subDomainBscanAliveCount = 0;
    public static int similarSubDomainBscanAliveCount = 0;
    public static DbUtil db;
    public static HashSet<String> currentRootDomainSet = new HashSet<>();
//    public static HashSet<String> rootSimilarDomainSet = new HashSet<>();
    public static Map<String,String> config = Config.getInitDatabaseSetting();
    public static DomainConsumer domainConsumer = new DomainConsumer();
    public static final String VERSION = "1.0.6";
    public static final String EXTENSION_NAME = "Sylas";
    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks){
        // TODO here
        BurpExtender.callbacks = callbacks;
        callbacks.setExtensionName(String.format("%s(%s)",EXTENSION_NAME,VERSION));
        // ????????????
        PrintWriter stdout = new PrintWriter(callbacks.getStdout(), true);
        stdout.println("@Author: Br0ken_5 && 0Chencc");
        stdout.println("@Github: https://github.com/Acmesec/Sylas");
        // ?????????????????????????????????????????????????????????????????????
        checkIsOldVersion();
        if(Config.isBuild()){
            config = Config.parseJson();
            db = new DbUtil(Arrays.binarySearch(ControlSwitch.DB_SERVER,config.get("db_server")));
        }else{
            db = new DbUtil(0);
        }
        callbacks.registerHttpListener(this);
        callbacks.addSuiteTab(this);
        //  ??????????????????????????? sleep=10s
        domainConsumer.start();
    }

    /**
     * ??????????????????????????????????????????????????????????????????
     */
    public void checkIsOldVersion(){
        File oldSettingFile = new File("burpDomain_setting.json");
        if (oldSettingFile.exists()){
            oldSettingFile.renameTo(new File("sylas_setting.json"));
        }
        File oldSqliteDatabase = new File("BurpDomain.db");
        if (oldSqliteDatabase.exists()){
            oldSqliteDatabase.renameTo(new File("Sylas.db"));
        }
    }
    public static IBurpExtenderCallbacks getCallbacks() {
        return callbacks;
    }

    public static PrintWriter getStdout() {
        //????????????????????????????????????????????????????????????
        PrintWriter stdout;
        try{
            stdout = new PrintWriter(callbacks.getStdout(), true);
        }catch (Exception e){
            stdout = new PrintWriter(System.out, true);
        }
        return stdout;
    }

    public static PrintWriter getStderr() {
        PrintWriter stderr;
        try{
            stderr = new PrintWriter(callbacks.getStderr(), true);
        }catch (Exception e){
            stderr = new PrintWriter(System.out, true);
        }
        return stderr;
    }

    @Override
    public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo) {
        if ((toolFlag == 64 || toolFlag == 32 || toolFlag == 4) && !messageIsRequest) {
            if(BurpExtender.db.isConnect && BurpExtender.currentRootDomainSet.size()>0){
                DomainProducer.handleMessage(messageInfo, DomainProducer.PASSIVE_MODE);
            }
        }
    }

    @Override
    public String getTabCaption() {
        return "Sylas";
    }

    @Override
    public Component getUiComponent() {
        return new Sylas();
    }
}