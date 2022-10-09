package web;

import burp.BurpExtender;
import ui.Sylas;
import utils.DbUtil;

import java.util.HashMap;

/**
 * 负责将收集的域名信息写入数据库
 * @author Br0ken_5
 */
public class WebConsumer extends Thread {
    @Override
    public void run() {
        if(BurpExtender.db.mode==DbUtil.MYSQL_DB && BurpExtender.db.isConnect){
            while(true){
                try {
                    HashMap<String, HashMap<String, String>> newWebMap = BurpExtender.db.getWebMap(BurpExtender.currentRootDomainSet);
                    for (String key : newWebMap.keySet()) {
                        if (!BurpExtender.webMap.containsKey(key)) {
                            HashMap<String, String> web = newWebMap.get(key);
                            BurpExtender.webMap.put(key, web);
                            BurpExtender.subDomainBscanAliveCount += 1;
                            Sylas.addAliveDomainToUI(key, web.get("title"), web.get("status"), web.get("length"), web.get("createTime"));
                        }
                    }
                    sleep(10000);
                } catch (Exception error) {
                    error.printStackTrace(BurpExtender.getStderr());
                }
            }
        }
    }
}
