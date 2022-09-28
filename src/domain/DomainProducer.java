package domain;

import org.apache.commons.text.StringEscapeUtils;
import burp.*;

import java.net.URL;
import java.net.URLDecoder;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DomainProducer extends Thread{
    /**
     * 静态变量，burp插件的逻辑中，是可以保证它被初始化的。
     */
    private static final IBurpExtenderCallbacks CALL_BACKS = BurpExtender.getCallbacks();
    public static IExtensionHelpers helpers = CALL_BACKS.getHelpers();
    /**
     * 可以使用以下正则作为替代
     * ^((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}$
     * */
    public static final String DOMAIN_NAME_PATTERN = "((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}";
    private static final String URL_PATTERN = "((http|https)://)(www.)?[a-zA-Z0-9@:%._\\+~#?&//=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%._\\+~#?&//=]*)";
    private static final int DATA_MAX_SIZE = 5 * 1024 * 1024 ;
    public static final int PASSIVE_MODE = 0;
    public static final int ACTIVE_MODE = 1;
    public static final String USELESS_EXTENSIONS = "3g2|3gp|7z|aac|abw|aif|aifc|aiff|arc|au|avi|azw|bin|bmp|bz|bz2|" +
            "cmx|cod|csh|css|csv|doc|docx|eot|epub|gif|gz|ico|ics|ief|jar|jfif|jpe|jpeg|jpg|m3u|mid|midi|mjs|mp2|mp3" +
            "|mpa|mpe|mpeg|mpg|mpkg|mpp|mpv2|odp|ods|odt|oga|ogv|ogx|otf|pbm|pdf|pgm|png|pnm|ppm|ppt|pptx|ra|ram|rar" +
            "|ras|rgb|rmi|rtf|snd|svg|swf|tar|tif|tiff|ttf|vsd|wav|weba|webm|webp|woff|woff2|xbm|xls|xlsx|xpm|xul|xwd" +
            "|zip|zip";
    public static final String USELESS_URL_EXTENSIONS = "js|css|" + USELESS_EXTENSIONS;

    @Override
    public void run() {
        while(!BurpExtender.inputQueue.isEmpty()){
            try {
                IHttpRequestResponse message = BurpExtender.inputQueue.take();
                DomainProducer.handleMessage(message, ACTIVE_MODE);
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean checkOrigin(List<String> headers){
        boolean result = false;
        try{
            String host = "", refererHost = "", originHost = "";
            for (String header : headers) {
                header = header.toLowerCase();
                if (header.startsWith("host:")) {
                    host = header.substring(4).trim();
                }
                if (header.startsWith("referer:")) {
                    refererHost = new URL(header.substring(8).trim()).getHost();

                }
                if (header.startsWith("origin:")) {
                    originHost = new URL(header.substring(7).trim()).getHost();
                }
            }
            for (String s : BurpExtender.currentRootDomainSet) {
                if (host.contains(s) || refererHost.contains(s) || originHost.contains(s)) {
                    result = true;
                    break;
                }
            }
            for(String s : BurpExtender.collectDataFromDomainList){
                if(host.contains(s)){
                    result = true;
                    break;
                }
            }
            return result;

        }catch (Exception e){
            BurpExtender.getStderr().println(e);
        }
        return false;
    }

    /**
     * 对流量进行搜索分析
     * @param message
     * @param mode PASSIVE_MODE=0 被动搜索 ACTIVE_MODE=1 主动搜索
     */
    public static void handleMessage(IHttpRequestResponse message, int mode){
        String reqUrl = helpers.analyzeRequest(message).getUrl().toString();
        List<String> headers = helpers.analyzeRequest(message).getHeaders();
        String reqText = headers.toString();

        // 为了提高搜索效率
        // 1. 过滤了文件名后缀。
        // 2. 校验Host，Origin，Referer，当他们其中一者在currentRootDomainList中就会收集信息
        // 3. 用户可以自定义collectDomainList（现在还没有写这个功能）
        if(!uselessExtension(reqUrl, USELESS_EXTENSIONS) && checkOrigin(headers)){
            Set<String> domains = grepDomain(reqText);
            Set<String> urls = new HashSet<>();
            byte[] response = message.getResponse();
            if (response != null ) {
                List<String> respHeaders = helpers.analyzeResponse(message.getResponse()).getHeaders();
                if(checkContentType(respHeaders)){
                    //避免大数据包卡死整个程序
                    if (response.length >= DATA_MAX_SIZE) {
                        response = subByte(response, 0, DATA_MAX_SIZE);
                    }
                    String decodeRespText = decodeResp(new String(response));
                    Set<String> respDomains = grepDomain(decodeRespText);
                    Set<String> respUrls = grepUrls(decodeRespText);
                    domains.addAll(respDomains);
                    urls.addAll(respUrls);
                }
            }
            //对子域名以及网址进行搜索，如果一个for一次是不是有点笨呢？
            for (String domain : domains) {
                handleDomain(domain, mode);
                //handleSimilarSubDomain(domain,mode);
            }
            for (String url : urls) {
                handleUrl(url);

            }
            //在这个地方添加similarDomain
        }
    }

    /**
     * 确认content的类型
     * @param headers
     * @return
     */
    public static boolean checkContentType(List<String> headers){
        String contentType = "";
        for (String header : headers) {
            if(header.toLowerCase().startsWith("content-type:")){
                contentType = header.substring(12).trim();
            }
        }
        if("".equals(contentType)){
            return false;
        }
        String[] allowContentType = {"application/javascript", "text/", "application/json", "application/xml"};
        for (String s : allowContentType) {
            if(contentType.contains(s)){
                return true;
            }
        }
        return false;
    }

    /**
     * 提取子域名，如果是主动搜索还会提取ip信息
     * @param domain 传入域名进行判断是否为子域名
     * @param mode 判断是被动搜索还是主动搜索
     */
    public static void handleDomain(String domain, int mode){
        if(isSubdomain(domain)){
            // subDomainQueue会定时清空，所有子域名会存在subDomainMap，所以还要加个判断
            if(!BurpExtender.subDomainQueue.contains(domain)&&!BurpExtender.subDomainMap.containsKey(domain)){
                BurpExtender.subDomainQueue.add(domain);
                HashMap<String, String> data = new HashMap<>();
//                BurpExtender
                BurpExtender.subDomainMap.put(domain, data);
                String time = getCurrentTime();
                data.put("time", time);
                // 如果是主动搜索可以直接获取IP，即使DNS卡住也不会导致burp堵塞
                // 通过流量被动收集，会定时获取IP，具体实现在DBUtil.insertSubDomainQueueToDb()中
//                if(mode == ACTIVE_MODE){
//                    String ip = Config.getDomainIp(domain);
//                    data.put("ipAddress", ip);
//                }
            }
        }else if(isSimilarSubDomain(domain)){
            if(!BurpExtender.similarSubDomainQueue.contains(domain)&&!BurpExtender.similarSubDomainMap.containsKey(domain)&&!BurpExtender.currentRootDomainSet.contains(domain)){
                BurpExtender.similarSubDomainQueue.add(domain);
                HashMap<String, String> data = new HashMap<>();
                BurpExtender.similarSubDomainMap.put(domain, data);
                String time = getCurrentTime();
                data.put("time", time);
//                if(mode == ACTIVE_MODE){
//                    String ip = Config.getDomainIp(domain);
//                    data.put("ipAddress", ip);
//                }
//                BurpExtender.similarSubDomainMap.put(domain, data);
            }
        }
    }

//    /**
//     * 提取相似域名(暂时没有这个需求，不要使用这个接口)
//     * @param domain 传入域名进行判断是否为相似域名
//     * @param mode
//     * @deprecated
//     */
//    public static void handleSimilarDomain(String domain,int mode){
//        if (isSimilarDomain(domain)){
//            BurpExtender.rootSimilarDomainSet.add(domain);
//        }
//    }

/*    *//**
     *收集相似域名的子域名
     * @param domain
     * @param mode
     *//*
    public static void handleSimilarSubDomain(String domain,int mode){
        if(isSimilarSubDomain(domain)){
            // subDomainQueue会定时清空，所有子域名会存在subDomainMap，所以还要加个判断
            if(!BurpExtender.similarSubDomainQueue.contains(domain)&&!BurpExtender.similarSubDomainMap.containsKey(domain)){
                BurpExtender.similarSubDomainQueue.add(domain);
                HashMap<String, String> data = new HashMap<>();
                BurpExtender.similarSubDomainMap.put(domain, data);
                String time = getCurrentTime();
                data.put("time", time);
                // 如果是主动搜索可以直接获取IP，即使DNS卡住也不会导致burp堵塞
                // 通过流量被动收集，会定时获取IP，具体实现在DBUtil.insertSubDomainQueueToDb()中
                if(mode == ACTIVE_MODE){
                    String ip = Config.getDomainIp(domain);
                    data.put("ipAddress", ip);
                }
            }
        }
    }*/
    /**
     * 提取关联网址
     * @param url 传入url进行提取
     */
    public static void handleUrl(String url){
        try{
            URL u = new URL(url);
            String domain = u.getHost();
            String path = "".equals(u.getPath()) ? "/" : u.getPath();
            if(u.getPort() == -1){
                url = u.getProtocol() + "://" + u.getHost() + path;
            }else{
                String port = String.valueOf(u.getPort());
                url = u.getProtocol() + "://" + u.getHost() + ":" + port + path;
            }
            if (isSubdomain(domain)){
                if(!uselessExtension(path, USELESS_URL_EXTENSIONS)){
                    if(!BurpExtender.urlQueue.contains(url)&&!BurpExtender.urlMap.containsKey(url)){
                        BurpExtender.urlQueue.add(url);
                        BurpExtender.urlMap.put(url, getCurrentTime());
                    }
                }
            }else if (isSimilarSubDomain(domain)&!BurpExtender.currentRootDomainSet.contains(domain)){
                if(!uselessExtension(path, USELESS_URL_EXTENSIONS)){
                    if(!BurpExtender.similarUrlQueue.contains(url)&&!BurpExtender.similarUrlMap.containsKey(url)){
                        BurpExtender.similarUrlQueue.add(url);
                        BurpExtender.similarUrlMap.put(url, getCurrentTime());
                    }
                }
            }
        }catch (Exception e){
            BurpExtender.getStderr().println(e);
        }
    }

//    public static String getFormatURL(String url){
//        try{
//
//        }catch (Exception e){
//            BurpExtender.getStderr().println(url);
//        }
//        return url;
//    }

    public static String getCurrentTime(){
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }

    public static byte[] subByte(byte[] b, int srcPos, int length){
        byte[] b1 = new byte[length];
        System.arraycopy(b, srcPos, b1, 0, length);
        return b1;
    }

    public static boolean uselessExtension(String urlPath, String extensions) {
        String[] extList = extensions.split("\\|");
        for ( String item:extList) {
            if(urlPath.endsWith("."+item)) {
                return true;
            }
        }
        return false;
    }

    public static String decodeResp(String resp){
        if (urlCode(resp)) {
            while (true) {
                try {
                    int oldlen = resp.length();
                    resp = URLDecoder.decode(resp);
                    int currentlen = resp.length();
                    if (oldlen > currentlen) {
                        continue;
                    }else {
                        break;
                    }
                }catch(Exception e) {
                    break;
                }
            }
        }
        if (unicodeCode(resp)) {
            //unicode解码
            while (true) {
                try {
                    int oldlen = resp.length();
                    resp = StringEscapeUtils.unescapeJava(resp);
                    int currentlen = resp.length();
                    if (oldlen > currentlen) {
                        continue;
                    }else {
                        break;
                    }
                }catch(Exception e) {
                    break;
                }
            }
        }
        return resp;
    }

    /**
     * 将流量传入到该函数进行解析提取域名
     * @return
     */
    public static Set<String> grepDomain(String httpResponse) {
        Set<String> domains = new HashSet<>();
        Pattern pDomainNameOnly = Pattern.compile(DOMAIN_NAME_PATTERN);
        Matcher matcher = pDomainNameOnly.matcher(httpResponse);
        while(matcher.find()) {
            String domain = matcher.group().toLowerCase();
            // 替换掉因正则缺陷匹配到以URL、Unicode编码开头的域名，例如2fwww.baidu.com, 252fwww.baidu.com, 002fwww.baidu.com
            if(domain.startsWith("2f")){
                domain = domain.replaceFirst("2f", "");
            }
            if(domain.startsWith("252f")){
                domain = domain.replaceFirst("252f", "");
            }
            if(domain.startsWith("3a")){
                domain = domain.replaceFirst("3a", "");
            }
            if(domain.startsWith("253a")){
                domain = domain.replaceFirst("253a", "");
            }
            if(domain.startsWith("u002f")){
                domain = domain.replaceFirst("u002f", "");
            }
            domains.add(domain);
        }
        return domains;
    }

    public static Set<String> grepUrls(String httpResponse){
        Set<String> urls = new HashSet<>();
        Pattern pDomainNameOnly = Pattern.compile(URL_PATTERN);
        Matcher matcher = pDomainNameOnly.matcher(httpResponse);
        while(matcher.find()){
            String url = matcher.group();
            if(url.contains("?")){
                url = url.substring(0, url.indexOf( "?"));
            }
            urls.add(url);
        }
        return urls;
    }

    /**
     * 判断是否为子域名
     * @param domain
     * @return
     */
    public static boolean isSubdomain(String domain){
        for (String s : BurpExtender.currentRootDomainSet) {
            if(domain.endsWith("."+s)){
                return true;
            }
        }
        return false;
    }

//    /**
//     * 判断是否为相似域名
//     * @param domain
//     * @return
//     * @deprecated
//     */
//    public static boolean isSimilarDomain(String domain){
//        for(String s:BurpExtender.currentRootDomainSet){
//            //思路：考虑将rootdomain进行切割，例如baidu.com使用切割成baidu com，然后对baidu进行相似度匹配
//            String[] tmp = s.split("\\.");
//            //通过切割的长度取需要匹配的部分，通过这个来避免当用户设置根域名为www.baidu.com的时候，会匹配成www,baidu的问题，目前直接取baidu,com
//            String similarRegex = String.format("(?!-)[A-Za-z0-9-]{0,63}%s[A-Za-z0-9-]{0,63}(?<!-).%s",tmp[tmp.length-2],tmp[tmp.length-1]);
//            Pattern similarPattern = Pattern.compile(similarRegex);
//            Matcher matcher = similarPattern.matcher(domain);
//            return matcher.find();
//        }
//        return false;
//    }

    /**
     * 判断是否为相似域名的子域名
     * @param domain
     * @return
     */
    public static boolean isSimilarSubDomain(String domain){
        for(String s:BurpExtender.currentRootDomainSet){
            //思路：考虑将rootdomain进行切割，例如baidu.com使用切割成baidu com，然后对baidu进行相似度匹配
            String[] tmp = s.split("\\.");
            //通过切割的长度取需要匹配的部分，通过这个来避免当用户设置根域名为www.baidu.com的时候，会匹配成www,baidu的问题，目前直接取baidu,com
            String similarRegex = String.format("((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)*(?!-)[A-Za-z0-9-]{0,63}%s[A-Za-z0-9-]{0,63}(?<!-)\\.%s",
                    tmp[tmp.length-2],tmp[tmp.length-1]);
            Pattern similarPattern = Pattern.compile(similarRegex);
            Matcher matcher = similarPattern.matcher(domain);
            return matcher.find();
        }
//        for(String s:BurpExtender.rootSimilarDomainSet){
//            if (similarDomain.endsWith("."+s)){
//                return true;
//            }
//        }
        return false;
    }

    /**
     * 判断是否为url编码
     * @param line
     * @return
     */
    public static boolean urlCode(String line) {
        String patternRule = "(%(\\p{XDigit}{2}))";
        Pattern pattern = Pattern.compile(patternRule);
        Matcher matcher = pattern.matcher(line.toLowerCase());
        return matcher.find();
    }

    /**
     * 判断是否为unicode
     * @param line
     * @return
     */
    public static boolean unicodeCode(String line) {
        String patternRule = "(\\\\u(\\p{XDigit}{4}))";
        Pattern pattern = Pattern.compile(patternRule);
        Matcher matcher = pattern.matcher(line.toLowerCase());
        return matcher.find();
    }
}
