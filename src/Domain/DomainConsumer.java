package Domain;

import UI.BurpDomain;
import burp.BurpExtender;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

//负责将收集的域名信息写入数据库
public class DomainConsumer extends Thread {


    @Override
    public void run() {
        while(true){
            try {
                QueueToResult();
                sleep(1000);
            } catch (Exception error) {
                error.printStackTrace(BurpExtender.getStderr());
            }
        }
    }

    public static void QueueToResult(){
        if(BurpExtender.db.isConnect){
            BurpExtender.db.insertSubDomainQueueToDb(BurpExtender.subDomainQueue);
            BurpExtender.db.insertUrlQueueToDb(BurpExtender.urlQueue);
        }
    }

}