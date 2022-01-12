package Utils;

import java.sql.*;

import UI.BurpDomain;
import burp.BurpExtender;
import com.google.common.base.Joiner;

import java.util.Arrays;
import java.util.HashMap;
import com.google.common.net.InternetDomainName;

import java.util.HashSet;
import java.util.concurrent.BlockingQueue;

import static java.lang.Thread.sleep;

/**
 * @author linchen
 */
public class DBUtil {
    public DBUtil(int mode){
        this.mode = mode;
    }
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
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(jdbcUrl, username, password);
            isConnect = true;
        } catch (SQLException | ClassNotFoundException e) {
            BurpExtender.getStderr().println(e);
            e.printStackTrace();
        }
    }
    public void setConn(){
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("JDBC:sqlite:BurpDomain.db");
            isConnect = true;
        } catch (Exception e) {
            BurpExtender.getStderr().println(e);
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
        HashMap<String, String> tables = new HashMap<String, String>(4){
            {
                put("Project","CREATE TABLE `Project` (\n" +
                        "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                        "  `projectName` varchar(64) NOT NULL,\n" +
                        "  PRIMARY KEY (`id`),\n" +
                        "  UNIQUE KEY `Project_projectName_uindex` (`projectName`)\n" +
                        ");");
                put("RootDomain","CREATE TABLE `RootDomain` (\n" +
                        "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                        "  `rootDomainName` varchar(64) NOT NULL,\n" +
                        "  `projectName` varchar(64) NOT NULL,\n" +
                        "  PRIMARY KEY (`id`),\n" +
                        "  UNIQUE KEY `RootDomain_domainName_uindex` (`rootDomainName`)\n" +
                        ");");
                put("SubDomain","CREATE TABLE `SubDomain` (\n" +
                        "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                        "  `subDomainName` varchar(128) NOT NULL,\n" +
                        "  `rootDomainName` varchar(64) DEFAULT NULL,\n" +
                        "  `ipAddress` varchar(64) DEFAULT NULL,\n" +
                        "  `createTime` datetime NOT NULL,\n" +
                        "  PRIMARY KEY (`id`),\n" +
                        "  UNIQUE KEY `SubDomain_subDomainName_uindex` (`subDomainName`)\n" +
                        ");");
                put("Url","CREATE TABLE `Url` (\n" +
                        "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                        "  `url` varchar(256) NOT NULL,\n" +
                        "  `projectName` varchar(64) NOT NULL,\n" +
                        "  `createTime` datetime NOT NULL,\n" +
                        "  PRIMARY KEY (`id`),\n" +
                        "  UNIQUE KEY `SubDomain_subDomainName_uindex` (`url`)\n" +
                        ");");
            }
        };
        try{
            ResultSet set = conn.getMetaData().getTables(db,null,"%",null);
            while (set.next()){
                String table = set.getString("TABLE_NAME");
                tables.remove(table);
            }
            for (String sql:tables.values()){
                conn.createStatement().executeLargeUpdate(sql);
            }
        }catch (SQLException e) {
            BurpExtender.getStdout().println("is init");
            BurpExtender.getStderr().println(e);
        }
    }

    /**
     * 初始化Sqlite数据库
     * @param db
     */
    private void initSqlite(String db){
        HashMap<String, String> tables = new HashMap<String, String>(4){
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
            }
        };
        try{
            ResultSet set = conn.getMetaData().getTables(db,null,"%",null);
            while (set.next()){
                String table = set.getString("TABLE_NAME");
                tables.remove(table);
            }
            for (String sql:tables.values()){
                BurpExtender.getStdout().println(sql);
                conn.createStatement().execute(sql);
            }
        }catch (SQLException e) {
            BurpExtender.getStderr().println(e);
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
            BurpExtender.getStderr().println(e);
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
            BurpDomain.addSubDomainToUI(subDomain, ip, data.get("time"));
            psSQL.setString(1, subDomain);
            psSQL.setString(2, getRootDomain(subDomain));
            psSQL.setString(3, ip);
            psSQL.setString(4, data.get("time"));
            // BurpExtender.subDomainMap.get(subDomain).put("status", "1");
            psSQL.addBatch();
        }
            psSQL.executeBatch();
        } catch (Exception e) {
            BurpExtender.getStderr().println(e);
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
                BurpDomain.addURLToUI(url, createTime);
                pSQL.setString(1, url);
                pSQL.setString(2, createTime);
                pSQL.setString(3, currentProject);
                pSQL.addBatch();
            }
            pSQL.executeBatch();
        }catch (Exception e){
            BurpExtender.getStderr().println(e);
        }
    }

    /**
     *
     * @param subDomain
     * @return
     * (用了新的正则后，这个问题解决了)这里有BUG，目前的正则会匹配到-elis-ecocdn.pingan.com.cn这样错误的域名，然后调用Google这个API就会抛异常
     */
    private String getRootDomain(String subDomain){
        String suffix = InternetDomainName.from(subDomain).publicSuffix().toString();
        String[] tmpDomain = subDomain.substring(0, subDomain.lastIndexOf(suffix)-1).split("\\.");
        return tmpDomain[tmpDomain.length-1]+"."+suffix;
    }

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
            e.printStackTrace();
        }
    }
    public void removeProject(String projectName){
        String sql = "DELETE FROM Project WHERE projectName = ?;";
        try{
            PreparedStatement preSQl = conn.prepareStatement(sql);
            preSQl.setString(1, projectName);
            preSQl.execute();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void  addRootDomain(String projectName, String domainName){
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
            e.printStackTrace();
        }
    }

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
    public HashSet<String> getProjectSet(){
        String sql = "select ProjectName from Project";
        ResultSet set = null;
        HashSet<String> projectList = new HashSet<>();
        try {
            PreparedStatement preSQl = conn.prepareStatement(sql);
            set = preSQl.executeQuery();
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
            BurpExtender.getStderr().println(e);
        }
        return rootDomainList;
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
            BurpExtender.getStderr().println(e);
        }
        return urlMap;
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
            BurpExtender.getStderr().println(e);
        }
        return subDomainMap;
    }

//    public HashMap<String, HashMap<String, String>> getSubDomainMap(String projectName){
//        HashMap<String, HashMap<String, String>> subDomainMap = new HashMap<>();
//        Object[] rootDomainList = getRootDomainSet(projectName).toArray();
//        //不确定存不存在sql注入，或者说更帅一点的写法，先这样
//        String sql = "select subDomainName,rootDomainName,ipAddress,createTime from SubDomain where rootDomainName in (?)";
//        try {
//            PreparedStatement preSQl = conn.prepareStatement(sql);
//            preSQl.setString(1,"'"+Joiner.on("','").join(rootDomainList)+"'");
//            ResultSet set = preSQl.executeQuery();
//            while(set.next()){
//                HashMap<String, String> data = new HashMap<>();
//                data.put("ipAddress", set.getString("ipAddress"));
//                data.put("createTime", set.getString("createTime"));
//                data.put("status", "1");
//                subDomainMap.put(set.getString("subDomainName"), data);
//            }
//        } catch (SQLException e) {
//            BurpExtender.getStderr().println(e);
//        }
//        BurpExtender.getStdout().println(subDomainMap);
//        return subDomainMap;
//    }
}

