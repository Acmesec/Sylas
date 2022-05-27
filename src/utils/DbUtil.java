package utils;

import java.sql.*;

import domain.DomainProducer;
import ui.Sylas;
import burp.BurpExtender;
import com.google.common.base.Joiner;

import java.util.Arrays;
import java.util.HashMap;
import com.google.common.net.InternetDomainName;

import java.util.HashSet;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;

import static java.lang.Thread.sleep;

/**
 * @author linchen
 */
public class DbUtil {
    public DbUtil(int mode){
        this.mode = mode;
    }

    /**
     * 数据库连接模式，0为Mysql，1为SQLITE
     */
    public int mode;
    /**
     * 使用Mysql
     */
    public static final int MYSQL_DB = 0;
    /**
     * 使用Sqlite
     */
    public static final int SQLITE_DB = 1;
    /**
     * 数据库连接状态
     */
    public boolean isConnect = false;
    /**
     * 数据库连接
     */
    public Connection conn = null;
    /**
     * 当前连接的库名
     */
    public String database;
    /**
     * bscan是否已经创建相关表
     */
    public boolean bscanReady = false;
    /**
     * 切换数据库
     */
    public void switchConn(int mode){
        this.mode = mode;
        closeConn();
        setConn();
    }
    /**
     * 连接数据库
     * */
    public void setConn(String host, String port, String username, String password, String database) {
        String jdbcUrl = String.format("jdbc:mysql://%s:%s/%s?useServerPrepStmts=true", host, port,
                database);
        this.database = database;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(jdbcUrl, username, password);
            isConnect = true;
        } catch (SQLException | ClassNotFoundException e) {
            BurpExtender.getStderr().println("Mysql连接失败:"+e);
            e.printStackTrace();
        }
    }
    public void setConn(){
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("JDBC:sqlite:Sylas.db");
            isConnect = true;
        } catch (Exception e) {
            BurpExtender.getStderr().println("Sqlite连接失败:"+e);
            e.printStackTrace();
        }
    }
    public void closeConn(){
        if(isConnect){
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            isConnect = false;
        }
    }

    /**
     * 初始化mysql数据库
     * @param db
     */
    private void initMysql(String db){
        HashMap<String, String> tables = new HashMap<String, String>(6){
            {
                //项目表
                put("PROJECT","CREATE TABLE `Project` (\n" +
                        "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                        "  `projectName` varchar(64) NOT NULL,\n" +
                        "  PRIMARY KEY (`id`),\n" +
                        "  UNIQUE KEY `Project_projectName_uindex` (`projectName`)\n" +
                        ");");
                //根域名表
                put("ROOTDOMAIN","CREATE TABLE `RootDomain` (\n" +
                        "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                        "  `rootDomainName` varchar(64) NOT NULL,\n" +
                        "  `projectName` varchar(64) NOT NULL,\n" +
                        "  PRIMARY KEY (`id`),\n" +
                        "  UNIQUE KEY `RootDomain_domainName_uindex` (`rootDomainName`)\n" +
                        ");");
                //子域名表
                put("SUBDOMAIN","CREATE TABLE `SubDomain` (\n" +
                        "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                        "  `subDomainName` varchar(128) NOT NULL,\n" +
                        "  `rootDomainName` varchar(64) DEFAULT NULL,\n" +
                        "  `ipAddress` varchar(64) DEFAULT NULL,\n" +
                        "  `createTime` datetime NOT NULL,\n" +
                        "  `scanned` int default 0 null,\n" +
                        "  PRIMARY KEY (`id`),\n" +
                        "  UNIQUE KEY `SubDomain_subDomainName_uindex` (`subDomainName`)\n" +
                        ");");
                //url表
                put("URL","CREATE TABLE `Url` (\n" +
                        "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                        "  `url` varchar(256) NOT NULL,\n" +
                        "  `projectName` varchar(64) NOT NULL,\n" +
                        "  `createTime` datetime NOT NULL,\n" +
                        "  PRIMARY KEY (`id`),\n" +
                        "  UNIQUE KEY `Url_url_uindex` (`url`)\n" +
                        ");");
                //相似域名表
                put("SIMILARSUBDOMAIN","CREATE TABLE `SimilarSubDomain`(\n" +
                        "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                        "  `subDomainName` varchar(128) NOT NULL,\n" +
                        "  `rootDomainName` varchar(64) DEFAULT NULL,\n" +
                        "  `ipAddress` varchar(64) DEFAULT NULL,\n" +
                        "  `createTime` datetime NOT NULL,\n" +
                        "  `scanned` int default 0 null,\n" +
                        "  PRIMARY KEY (`id`),\n" +
                        "   UNIQUE KEY `SimilarSubDomain_similarDomainName_uindex` (`subDomainName`)\n" +
                        ");");
                //相似域名子域名表
                put("SIMILARURL","CREATE TABLE `SimilarUrl`(\n" +
                        "   `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                        "   `url` varchar(64) NOT NULL,\n" +
                        "   `projectName` varchar(64) NOT NULL,\n" +
                        "  `createTime` datetime NOT NULL,\n" +
                        "   PRIMARY KEY (`id`),\n" +
                        "   UNIQUE KEY `SimilarUrl_url_uindex` (`url`)\n" +
                        ");");
            }
        };
        String[] subDomainTables = new String[]{"SubDomain","SimilarSubDomain"};
        for (String i:subDomainTables) {
            scannedFiledIsAlert(i);
        }
        try{
            ResultSet set = conn.getMetaData().getTables(db,null,"%",null);
            while (set.next()){
                String table = set.getString("TABLE_NAME");
                if("SubDomainBscanAlive".equals(table)){
                    this.bscanReady = true;
                    BurpExtender.getStdout().println("Bscan ready");
                }
                tables.remove(table.toUpperCase());
            }
            for (String sql:tables.values()){
                conn.createStatement().executeLargeUpdate(sql);
            }
        }catch (SQLException e) {
            BurpExtender.getStderr().println("Mysql初始化失败:"+e);
        }
    }
    /**
     * 此处是用来判断是否为旧版本插件，新版本需要在subdomain以及similarDomain中加入scan字段以支持
     * @param table
     */
    public void scannedFiledIsAlert(String table){
        try {
            PreparedStatement scanColumnNameCreateSql = conn.prepareStatement("select count(*) as iscreated from information_schema.COLUMNS where TABLE_NAME = ? and TABLE_SCHEMA = ? and COLUMN_NAME = 'scanned'");
            scanColumnNameCreateSql.setString(1,table);
            scanColumnNameCreateSql.setString(2,database);
            ResultSet set = scanColumnNameCreateSql.executeQuery();
            int isCreated = 0;
            while (set.next()){
                isCreated = set.getInt("iscreated");
            }
            if (isCreated == 0){
                PreparedStatement createScanSql = conn.prepareStatement("alter table ? add scanned int default 0 not null;");
                createScanSql.setString(1,table);
                createScanSql.execute();
            }
        } catch (SQLException e) {
            BurpExtender.getStderr().println("扫描scanned字段失败:"+e);
        }
    }

    /**
     * 初始化Sqlite数据库
     * @param db
     */
    private void initSqlite(String db){
        HashMap<String, String> tables = new HashMap<String, String>(6){
            {
                put("Project","CREATE TABLE \"main\".\"Project\" (\n" +
                        "  \"id\" integer NOT NULL,\n" +
                        "  \"projectName\" TEXT,\n" +
                        "  CONSTRAINT \"id\" PRIMARY KEY (\"id\")\n" +
                        "  CONSTRAINT \"Project_projectName_uindex\" UNIQUE (\"projectName\")"+
                        ");");
                put("RootDomain","CREATE TABLE \"main\".\"RootDomain\" (\n" +
                        "  \"id\" integer NOT NULL,\n" +
                        "  \"rootDomainName\" TEXT,\n" +
                        "  \"projectName\" TEXT,\n" +
                        "  CONSTRAINT \"id\" PRIMARY KEY (\"id\")\n" +
                        "  CONSTRAINT \"RootDomain_projectName_uindex\" UNIQUE (\"rootDomainName\")"+
                        ");");
                put("SubDomain","CREATE TABLE \"main\".\"SubDomain\" (\n" +
                        "  \"id\" integer NOT NULL,\n" +
                        "  \"subDomainName\" TEXT,\n" +
                        "  \"rootDomainName\" TEXT,\n" +
                        "  \"ipAddress\" TEXT,\n" +
                        "  \"createTime\" TEXT,\n" +
                        "  \"scan\" integers DEFAULT 0, \n" +
                        "  CONSTRAINT \"id\" PRIMARY KEY (\"id\")\n" +
                        "  CONSTRAINT \"SubDomain_projectName_uindex\" UNIQUE (\"subDomainName\")"+
                        ");");
                put("Url","CREATE TABLE \"main\".\"Url\" (\n" +
                        "  \"id\" integer NOT NULL,\n" +
                        "  \"url\" TEXT,\n" +
                        "  \"projectName\" TEXT,\n" +
                        "  \"createTime\" TEXT,\n" +
                        "  CONSTRAINT \"id\" PRIMARY KEY (\"id\")\n" +
                        "  CONSTRAINT \"Url_projectName_uindex\" UNIQUE (\"url\")"+
                        ");");
                put("SimilarSubDomain","CREATE TABLE \"main\".\"SimilarSubDomain\" (\n" +
                        "  \"id\" integer NOT NULL,\n" +
                        "  \"SubDomainName\" TEXT,\n" +
                        "  \"rootDomainName\" TEXT,\n" +
                        "  \"ipAddress\" TEXT,\n" +
                        "  \"createTime\" TEXT,\n" +
                        "  CONSTRAINT \"id\" PRIMARY KEY (\"id\")\n" +
                        "  CONSTRAINT \"SimilarSubDomain_projectName_uindex\" UNIQUE (\"SubDomainName\")"+
                        ");");
                put("SimilarUrl","CREATE TABLE \"main\".\"SimilarUrl\" (\n" +
                        "  \"id\" integer NOT NULL,\n" +
                        "  \"url\" TEXT,\n" +
                        "  \"projectName\" TEXT,\n" +
                        "  \"createTime\" TEXT,\n" +
                        "  CONSTRAINT \"id\" PRIMARY KEY (\"id\")\n" +
                        "  CONSTRAINT \"SimilarUrl_projectName_uindex\" UNIQUE (\"url\")"+
                        ");");
            }
        };
        // 原先想要给sqlite增加检测url存活的功能，但是想了想实现有点麻烦。
        try{
            ResultSet set = conn.getMetaData().getTables(db,null,"%",null);
            while (set.next()){
                String table = set.getString("TABLE_NAME");
                tables.remove(table);
            }
            for (String sql:tables.values()){
                conn.createStatement().execute(sql);
            }
        }catch (SQLException e) {
            BurpExtender.getStderr().println("Sqlite初始化失败"+e);
        }
    }
    public void init(String db){
        switch (mode){
            case MYSQL_DB:
                initMysql(db);
                break;
            case SQLITE_DB:
                initSqlite(db);
                break;
            default:
                break;
        }

    }
    public Boolean projectExist(String project){
        String sql = "select count(id) as project from Project where projectName = ?";
        try{
            PreparedStatement psSQL = conn.prepareStatement(sql);
            psSQL.setString(1,project);
            ResultSet set = psSQL.executeQuery();
            int projectCount = 0;
            while (set.next()){
                projectCount = set.getInt("project");
            }
            return projectCount != 0;
        }catch (SQLException e){
            BurpExtender.getStderr().println("查找不到project:"+e);
        }
        return false;
    }
    /**
     * 对RootDomain表插入数据
     */
    public void insertData(String rootDomainName,String projectName){
        String sql = null;
        switch (mode){
            case MYSQL_DB:
                sql = "insert ignore into Project (rootDomainName,projectName) values (?,?)";
                break;
            case SQLITE_DB:
                sql = "insert or ignore into Project ([rootDomainName],[projectName]) values (?,?)";
                break;
            default:
                break;
        }
        try {
            PreparedStatement psSQL = conn.prepareStatement(sql);
            psSQL.setString(1,rootDomainName);
            psSQL.setString(2,projectName);
            psSQL.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 往数据库增加子域名
     * @param queue
     */
    public void insertSubDomainQueueToDb(BlockingQueue<String> queue) {
        String sql = null;
        switch (mode){
            case MYSQL_DB:
                sql = "insert ignore into SubDomain (subDomainName,rootDomainName,ipAddress,createTime) values (?,?,?,?)";
                break;
            case SQLITE_DB:
                sql = "insert or ignore into SubDomain ([subDomainName],[rootDomainName],[ipAddress],[createTime]) values (?,?,?,?)";
                break;
            default:
                break;
        }
        try {
            PreparedStatement psSQL = conn.prepareStatement(sql);
        while (!queue.isEmpty()){
            String subDomain = queue.take();
            HashMap<String, String> data = BurpExtender.subDomainMap.get(subDomain);
            // if(data==null||"1".equals(data.get("status"))){
            if(data==null){
                continue;
            }
            String ip = data.get("ipAddress");
            if(ip==null) {
                // 用后台的线程去获取IP，防止DNS延迟造成burp流量堵塞
                ip = Config.getDomainIp(subDomain);
                BurpExtender.subDomainMap.get(subDomain).put("ipAddress", ip);
            }
            BurpExtender.subDomainCount += 1;
            Sylas.addSubDomainToUI(subDomain, ip, data.get("time"));
            psSQL.setString(1, subDomain);
            psSQL.setString(2, getRootDomain(subDomain));
            psSQL.setString(3, ip);
            psSQL.setString(4, data.get("time"));
            psSQL.addBatch();
        }
            psSQL.executeBatch();
        } catch (Exception e) {
            BurpExtender.getStderr().println("子域名入库失败:"+e);
        }
    }

    /**
     * 插入相似域名的信息
     * @param queue
     */
    public void insertSimilarSubDomainQueueToDb(BlockingQueue<String> queue) {
        String sql = null;
        switch (mode){
            case MYSQL_DB:
                sql = "insert ignore into SimilarSubDomain (subDomainName,rootDomainName,ipAddress,createTime) values (?,?,?,?)";
                break;
            case SQLITE_DB:
                sql = "insert or ignore into SimilarSubDomain ([subDomainName],[rootDomainName],[ipAddress],[createTime]) values (?,?,?,?)";
                break;
            default:
                break;
        }
        try {
            PreparedStatement psSQL = conn.prepareStatement(sql);
            while (!queue.isEmpty()){
                String subDomain = queue.take();
                HashMap<String, String> data = BurpExtender.similarSubDomainMap.get(subDomain);
                if(data==null){
                    continue;
                }
                String ip = data.get("ipAddress");
                if(ip==null) {
                    // 用后台的线程去获取IP，防止DNS延迟造成burp流量堵塞
                    ip = Config.getDomainIp(subDomain);
                    BurpExtender.similarSubDomainMap.get(subDomain).put("ipAddress", ip);
                }
                BurpExtender.similarSubDomainCount += 1;
                Sylas.addSimilarSubDomainToUI(subDomain, ip, data.get("time"));
                psSQL.setString(1, subDomain);
                psSQL.setString(2, getSimilarRootDomain(subDomain));
                psSQL.setString(3, ip);
                psSQL.setString(4, data.get("time"));
                psSQL.addBatch();
            }
            psSQL.executeBatch();
        } catch (Exception e) {
            BurpExtender.getStderr().println("无法插入相似域名:"+e);
        }
    }

    public void insertUrlQueueToDb(BlockingQueue<String> queue){
        String sql = null;
        switch (mode){
            case MYSQL_DB:
                sql = "insert ignore into Url (url, createTime, projectName) values (?,?,?)";
                break;
            case SQLITE_DB:
                sql = "insert or ignore into Url ([url], [createTime], [projectName]) values (?,?,?)";
                break;
            default:
                break;
        }
        String currentProject = BurpExtender.config.get("currentProject");
        try{
            PreparedStatement pSQL = conn.prepareStatement(sql);
            while(!queue.isEmpty()){
                String url = queue.take();
                String createTime = BurpExtender.urlMap.get(url);
                if(createTime==null){
                    continue;
                }
                BurpExtender.urlCount += 1;
                Sylas.addURLToUI(url, createTime);
                pSQL.setString(1, url);
                pSQL.setString(2, createTime);
                pSQL.setString(3, currentProject);
                pSQL.addBatch();
            }
            pSQL.executeBatch();
        }catch (Exception e){
            BurpExtender.getStderr().println("无法添加url:"+e);
        }
    }

    public void insertSimilarUrlQueueToDb(BlockingQueue<String> queue){
        String sql = null;
        switch (mode){
            case MYSQL_DB:
                sql = "insert ignore into SimilarUrl (url, createTime, projectName) values (?,?,?)";
                break;
            case SQLITE_DB:
                sql = "insert or ignore into SimilarUrl ([url], [createTime], [projectName]) values (?,?,?)";
                break;
            default:
                break;
        }
        String currentProject = BurpExtender.config.get("currentProject");
        try{
            PreparedStatement pSQL = conn.prepareStatement(sql);
            while(!queue.isEmpty()){
                String url = queue.take();
                String createTime = BurpExtender.similarUrlMap.get(url);
                if(createTime==null){
                    continue;
                }
                BurpExtender.similarUrlCount += 1;
                Sylas.addSimilarUrlToUI(url, createTime);
                pSQL.setString(1, url);
                pSQL.setString(2, createTime);
                pSQL.setString(3, currentProject);
                pSQL.addBatch();
            }
            pSQL.executeBatch();
        }catch (Exception e){
            BurpExtender.getStderr().println("无法添加相似url:"+e);
        }
    }

    /**
     * 获取根域名
     * @param subDomain
     * @return
     * (用了新的正则后，这个问题解决了)这里有BUG，目前的正则会匹配到-elis-ecocdn.pingan.com.cn这样错误的域名，然后调用Google这个API就会抛异常
     */
    private String getRootDomain(String subDomain){
        String suffix = InternetDomainName.from(subDomain).publicSuffix().toString();
        String[] tmpDomain = subDomain.substring(0, subDomain.lastIndexOf(suffix)-1).split("\\.");
        return tmpDomain[tmpDomain.length-1]+"."+suffix;
    }

    /**
     *
     */
    private String getSimilarRootDomain(String subDomain){
        for (String s:BurpExtender.currentRootDomainSet){
            if(DomainProducer.isSimilarSubDomain(subDomain)){
                return s;
            }
        }
        return null;
    }
    /**
     * 创建项目
     * @param projectName
     */
    public void addProject(String projectName){
        String sql = null;
        switch (mode){
            case MYSQL_DB:
                sql = "insert ignore into Project (projectName) values (?)";
                break;
            case SQLITE_DB:
                sql = "insert or ignore into Project ([projectName]) values (?)";
                break;
            default:
                break;
        }
        try{
            PreparedStatement preSQl = conn.prepareStatement(sql);
            preSQl.setString(1, projectName);
            preSQl.execute();
        }catch (SQLException e){
            BurpExtender.getStderr().println("无法创建项目:"+e);
        }
    }

    /**
     * 删除项目
     * @param projectName
     */
    public void removeProject(String projectName){
        String sql = "DELETE FROM Project WHERE projectName = ?;";
        try{
            PreparedStatement preSQl = conn.prepareStatement(sql);
            preSQl.setString(1, projectName);
            preSQl.execute();
        }catch (SQLException e){
            BurpExtender.getStderr().println("删除项目失败:"+e);
        }
    }

    /**
     * 添加根域名
     * @param projectName
     * @param domainName
     */
    public void addRootDomain(String projectName, String domainName){
        String sql = null;
        switch (mode){
            case MYSQL_DB:
                sql = "insert ignore into RootDomain (projectName,rootDomainName) values (?,?)";
                break;
            case SQLITE_DB:
                sql = "insert or ignore into RootDomain ([projectName],[rootDomainName]) values (?,?)";
                break;
            default:
                break;
        }
        try{
            PreparedStatement preSQl = conn.prepareStatement(sql);
            preSQl.setString(1, projectName);
            preSQl.setString(2, domainName);
            preSQl.execute();
        }catch (SQLException e){
            BurpExtender.getStderr().println("无法添加根域名:"+e);
        }
    }

    /**
     * 删除根域名
     * @param projectName
     * @param domainName
     */
    public void removeRootDomain(String projectName,String domainName){
        String sql = "delete from RootDomain where projectName = ? and rootDomainName = ?";
        try{
            PreparedStatement preSQl = conn.prepareStatement(sql);
            preSQl.setString(1, projectName);
            preSQl.setString(2, domainName);
            preSQl.execute();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    /**
     * 获取项目的Set
     * @return
     */
    public HashSet<String> getProjectSet(){
        String sql = "select ProjectName from Project";
        HashSet<String> projectList = new HashSet<>();
        try {
            PreparedStatement preSQl = conn.prepareStatement(sql);
            ResultSet set = preSQl.executeQuery();
            while(set.next()){
                projectList.add(set.getString("ProjectName"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return projectList;
    }
    public HashSet<String> getRootDomainSet(String projectName){
        String sql = "select rootDomainName from RootDomain where ProjectName=?";
        HashSet<String> rootDomainList = new HashSet<>();
        ResultSet set;
        try {
            PreparedStatement preSQl = conn.prepareStatement(sql);
            preSQl.setString(1, projectName);
            set = preSQl.executeQuery();
            while(set.next()){
                rootDomainList.add(set.getString("rootDomainName"));
            }
        } catch (SQLException e) {
            BurpExtender.getStderr().println("无法获取到根域名:"+e);
        }
        return rootDomainList;
    }

    public HashMap<String, HashMap<String, String>> getSubDomainMap(String projectName){
        HashMap<String, HashMap<String, String>> subDomainMap = new HashMap<>();
        Object[] rootDomainList = getRootDomainSet(projectName).toArray();
        String[] inSql = new String[rootDomainList.length];
        Arrays.fill(inSql, "?");
        // 还是改回来了，不知道为啥，下面的那个方法取不到数据
        String sql = "select subDomainName,rootDomainName,ipAddress,createTime from SubDomain where rootDomainName in ("+Joiner.on(",").join(inSql)+")";
        try {
            PreparedStatement preSQl = conn.prepareStatement(sql);
            for (int i=0;i<rootDomainList.length;i++){
                preSQl.setString(i+1, (String) rootDomainList[i]);
            }
            ResultSet set = preSQl.executeQuery();
            while(set.next()){
                HashMap<String, String> data = new HashMap<>();
                data.put("ipAddress", set.getString("ipAddress"));
                data.put("createTime", set.getString("createTime"));
                data.put("status", "1");
                subDomainMap.put(set.getString("subDomainName"), data);
            }
        } catch (SQLException e) {
            BurpExtender.getStderr().println("获取不到子域名"+e);
        }
        return subDomainMap;
    }

    public HashMap<String, HashMap<String, String>> getSimilarSubDomainMap(String projectName){
        HashMap<String, HashMap<String, String>> subDomainMap = new HashMap<>();
        Object[] rootDomainList = getRootDomainSet(projectName).toArray();
        String[] inSql = new String[rootDomainList.length];
        Arrays.fill(inSql, "?");
        // 还是改回来了，不知道为啥，下面的那个方法取不到数据
        String sql = "select subDomainName,rootDomainName,ipAddress,createTime from SimilarSubDomain where rootDomainName in ("+Joiner.on(",").join(inSql)+")";
        try {
            PreparedStatement preSQl = conn.prepareStatement(sql);
            for (int i=0;i<rootDomainList.length;i++){
                preSQl.setString(i+1, (String) rootDomainList[i]);
            }
            ResultSet set = preSQl.executeQuery();
            while(set.next()){
                HashMap<String, String> data = new HashMap<>();
                data.put("ipAddress", set.getString("ipAddress"));
                data.put("createTime", set.getString("createTime"));
                data.put("status", "1");
                subDomainMap.put(set.getString("subDomainName"), data);
            }
        } catch (SQLException e) {
            BurpExtender.getStderr().println("无法获取相似域名:"+e);
        }
        return subDomainMap;
    }
    public HashMap<String,HashMap<String,String>> getSubDomainAlive(String projectName){
        HashMap<String, HashMap<String, String>> subDomainAliveMap = new HashMap<>();
        Object[] rootDomainList = getRootDomainSet(projectName).toArray();
        String[] inSql = new String[rootDomainList.length];
        Arrays.fill(inSql, "?");
        String sql = "select url,title,status,rootDomainName from SubDomainBscanAlive where rootDomainName in ("+Joiner.on(",").join(inSql)+")";
        try{
            PreparedStatement preSQL = conn.prepareStatement(sql);
            for (int i=0;i<rootDomainList.length;i++){
                preSQL.setString(i+1, (String) rootDomainList[i]);
            }
            ResultSet set = preSQL.executeQuery();
            while (set.next()){
                HashMap<String,String> data = new HashMap<>();
                data.put("rootDomain",set.getString("rootDomainName"));
                data.put("status",set.getString("status"));
                data.put("title",set.getString("title"));
                subDomainAliveMap.put(set.getString("url"),data);
            }
        }catch (SQLException e){
            BurpExtender.getStderr().println("无法获取Bscan对域名的测活情况:"+e);
        }
        return subDomainAliveMap;
    }
    public HashMap<String,HashMap<String,String>> getSimilarSubDomainAlive(String projectName){
        HashMap<String, HashMap<String, String>> similarDomainAliveMap = new HashMap<>();
        Object[] rootDomainList = getRootDomainSet(projectName).toArray();
        String[] inSql = new String[rootDomainList.length];
        Arrays.fill(inSql, "?");
        String sql = "select url,title,status,rootDomainName from SimilarDomainBscanAlive where rootDomainName in ("+Joiner.on(",").join(inSql)+")";
        try{
            PreparedStatement preSQL = conn.prepareStatement(sql);
            for (int i=0;i<rootDomainList.length;i++){
                preSQL.setString(i+1, (String) rootDomainList[i]);
            }
            ResultSet set = preSQL.executeQuery();
            while (set.next()){
                HashMap<String,String> data = new HashMap<>();
                data.put("rootDomain",set.getString("rootDomainName"));
                data.put("status",set.getString("status"));
                data.put("title",set.getString("title"));
                similarDomainAliveMap.put(set.getString("url"),data);
            }
        }catch (SQLException e){
            BurpExtender.getStderr().println("无法获取Bscan对相似域名的测活情况:"+e);
        }
        return similarDomainAliveMap;
    }
    public HashMap<String, String> getUrlMap(String projectName){
        HashMap<String, String> urlMap = new HashMap<>();
        String sql = "select url, createTime from Url where projectName=?";
        ResultSet set;
        try{
            PreparedStatement preSQL = conn.prepareStatement(sql);
            preSQL.setString(1, projectName);
            set = preSQL.executeQuery();
            while(set.next()){
                urlMap.put(set.getString("url"), set.getString("createTime"));
            }
        }catch (SQLException e){
            BurpExtender.getStderr().println("无法获取url:"+e);
        }
        return urlMap;
    }
    public HashMap<String, String> getSimilarUrlMap(String projectName){
        HashMap<String, String> urlMap = new HashMap<>();
        String sql = "select url, createTime from SimilarUrl where projectName=?";
        ResultSet set;
        try{
            PreparedStatement preSQL = conn.prepareStatement(sql);
            preSQL.setString(1, projectName);
            set = preSQL.executeQuery();
            while(set.next()){
                urlMap.put(set.getString("url"), set.getString("createTime"));
            }
        }catch (SQLException e){
            BurpExtender.getStderr().println("无法获取相似url:"+e);
        }
        return urlMap;
    }
}

