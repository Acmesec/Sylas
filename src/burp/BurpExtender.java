package burp;


import java.awt.*;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import Domain.DomainConsumer;
import Domain.DomainProducer;
import UI.BurpDomain;
import Utils.Config;
import Utils.DBUtil;

public class BurpExtender implements IBurpExtender, ITab, IHttpListener{

    private static IBurpExtenderCallbacks callbacks;
    //    public static HashSet<String> subDomainSet = new HashSet<>();
    public static HashMap<String, HashMap<String, String>> subDomainMap = new HashMap<>();
    public static HashMap<String, String> urlMap = new HashMap<>();
    public static BlockingQueue<IHttpRequestResponse> inputQueue = new LinkedBlockingQueue<>();
    public static BlockingQueue<String> urlQueue = new LinkedBlockingQueue<>();
    public static BlockingQueue<String> subDomainQueue = new LinkedBlockingQueue<>();
    public static Integer subDomainCount = 0;
    public static Integer urlCount = 0;
    public static DBUtil db;
    public static HashSet<String> currentRootDomainSet = new HashSet<>();
    public static HashMap<String,String> config = Config.initDatabaseSetting;
    public static DomainConsumer domainConsumer = new DomainConsumer();
    public static final String VERSION = "1.0.2";
    public static final String EXTENSION_NAME = "BurpDomain";
    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks){
        // TODO here
        BurpExtender.callbacks = callbacks;
        callbacks.setExtensionName(String.format("%s(%s)",EXTENSION_NAME,VERSION));
        // 定义输出
        PrintWriter stdout = new PrintWriter(callbacks.getStdout(), true);
        stdout.println("@Author: Br0ken_5 && 0Chencc");
        stdout.println("@Github: https://github.com/Br0ken/BurpDomain");
        db = new DBUtil();
        if(Config.isBuild()){
            config = Config.parseJson();
        }
        callbacks.registerHttpListener(this);
        callbacks.addSuiteTab(this);
        //  消费者抓取域名入库 sleep=10s
        domainConsumer.start();
    }

    public static IBurpExtenderCallbacks getCallbacks() {
        return callbacks;
    }

    public static PrintWriter getStdout() {
        //不同的时候调用这个参数，可能得到不同的值
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
        return "BurpDomain";
    }

    @Override
    public Component getUiComponent() {
        return new BurpDomain();
    }
}