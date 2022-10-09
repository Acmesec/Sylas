package domain;

import burp.BurpExtender;
import ui.Sylas;
import utils.DbUtil;

import java.util.HashMap;

//负责将收集的域名信息写入数据库
public class DomainConsumer extends Thread {


    @Override
    public void run() {
        while(true){
            try {
                QueueToResult();
                sleep(5000);
                if(BurpExtender.db.mode == DbUtil.MYSQL_DB && BurpExtender.db.isConnect){
                    HashMap<String, HashMap<String, String>> map = BurpExtender.db.getSubDomainMap(BurpExtender.config.get("currentProject"));
                    for (String key : map.keySet()) {
                        if (!BurpExtender.subDomainMap.containsKey(key)) {
                            HashMap<String, String> domain = map.get(key);
                            BurpExtender.subDomainMap.put(key, domain);
                            BurpExtender.subDomainCount += 1;
                            Sylas.addSubDomainToUI(key, domain.get("ipAddress"), domain.get("createTime"));
                        }
                    }
                }
            } catch (Exception error) {
                BurpExtender.getStderr().println(error);
            }
        }
    }

    public static void QueueToResult(){
        if(BurpExtender.db.isConnect){
            BurpExtender.db.insertSubDomainQueueToDb(BurpExtender.subDomainQueue);
            BurpExtender.db.insertUrlQueueToDb(BurpExtender.urlQueue);
            BurpExtender.db.insertSimilarSubDomainQueueToDb(BurpExtender.similarSubDomainQueue);
            BurpExtender.db.insertSimilarUrlQueueToDb(BurpExtender.similarUrlQueue);
        }
    }

}