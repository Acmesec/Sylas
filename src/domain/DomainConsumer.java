package domain;

import burp.BurpExtender;

//负责将收集的域名信息写入数据库
public class DomainConsumer extends Thread {


    @Override
    public void run() {
        while(true){
            try {
                QueueToResult();
                sleep(5000);
            } catch (Exception error) {
                error.printStackTrace(BurpExtender.getStderr());
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