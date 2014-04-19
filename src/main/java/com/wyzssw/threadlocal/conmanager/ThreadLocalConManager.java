package com.wyzssw.threadlocal.conmanager;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 替代连接池的连接管理器,每个线程使用一个连接，提升并发能力
 * 用户需定制 connectionFactory，返回实现Connectin接口的连接对象
 * @author wyzssw 2012-10-28 下午02:06:59
 */
public class ThreadLocalConManager<C>{
	
	//默认连接存活时间
	private  static  final  long  defaultLiveTime = 300000;
	//默认提前多长时间销毁随机值
	private  static  final  long  defaultAdditionMaxTime = 120000;
	
	protected final Log logger = LogFactory.getLog(getClass());
    private final  Map<Thread, ConnectionWrapper> connMap = new ConcurrentHashMap<Thread, ConnectionWrapper>(50); 
    
    private final  CopyOnWriteArraySet<ConnectionWrapper> brokenClientSet = new CopyOnWriteArraySet<ConnectionWrapper>();
    
    //值为-1时默认不销毁连接
    private  long liveTime = defaultLiveTime;
    private  final ConnectionFactory<C> connectionFactory;
    private  Random random;
    
    public ThreadLocalConManager(ConnectionFactory<C> connectionFactory){
    	this(defaultLiveTime, defaultAdditionMaxTime, connectionFactory);
    }
    
    public ThreadLocalConManager(long liveTime,long additionMaxTime,ConnectionFactory<C> connectionFactory){
    	assert (liveTime==-1||liveTime>0&&additionMaxTime>0)&&connectionFactory!=null;
    	this.liveTime = liveTime;
    	this.connectionFactory = connectionFactory;
    	random= new Random(additionMaxTime);
    	
    	init();
    }
    

    ThreadLocal<ConnectionWrapper> threadLocal = new ThreadLocal<ConnectionWrapper>();
    
    private void init(){
        ScheduledExecutorService scheduledThread = Executors.newSingleThreadScheduledExecutor();
        scheduledThread.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    cleanBrokenClient();
                    expungeNouseJedis(connMap);
                } catch (Exception e) {
                    logger.error(e.getMessage(),e);
                }
           }
        }, 0, 90, TimeUnit.SECONDS);
    }
    
    /**
     * 清理过期与坏掉的连接，有间隔时间，避免与业务线程冲突
     */
    private void cleanBrokenClient(){
        Iterator<ConnectionWrapper> it = brokenClientSet.iterator();
        while (it.hasNext()) {
        	ConnectionWrapper connectionWrapper = (ConnectionWrapper)it.next();
            //清理掉
        	connectionWrapper.getConnection().close();
            brokenClientSet.remove(connectionWrapper);
        }
    }
    
    /**
     * 及时关闭死掉线程所用连接，将过期连接放入brokenclientset，下次关闭并删掉
     * @param map
     * @param shards
     */
    private void expungeNouseJedis(Map<Thread, ConnectionWrapper> map) {
        
        for (Map.Entry<Thread, ConnectionWrapper> item : map.entrySet()) {            
            if (!item.getKey().isAlive()) {
                 item.getValue().getConnection().close();
                 item.setValue(new ConnectionWrapper(null, -1L));
            }else if (liveTime>0&&item.getValue().getTimeStamp()<System.currentTimeMillis()-(liveTime+random.nextLong())) {
            	  ConnectionWrapper connectionWrapper = item.getValue();
                 item.getValue().setConnection(connectionFactory.getConnection());   
                 item.getValue().setTimeStamp(System.currentTimeMillis());
                 brokenClientSet.add(connectionWrapper);
            }
        }
        Iterator<Map.Entry<Thread, ConnectionWrapper>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Thread, ConnectionWrapper> entry = (Map.Entry<Thread, ConnectionWrapper>) iterator.next();
            if (entry.getValue().getTimeStamp() == -1) {
                iterator.remove();
            }
        }
    }
    
    
    public Connection<C> getConnection(){ 
        boolean broken = false;
        ConnectionWrapper connectionWrapper;
        if ((connectionWrapper=threadLocal.get())==null||(broken=connectionWrapper.getConnection().isBroken())) {
            if (broken) {
                brokenClientSet.add(connectionWrapper);
            }
           
            connectionWrapper= new ConnectionWrapper(connectionFactory.getConnection(), System.currentTimeMillis());
            //注册到map中，及时关闭连接
            connMap.put(Thread.currentThread(),connectionWrapper);
            threadLocal.set(connectionWrapper);
        }
        return connectionWrapper.getConnection();
    }
    
    
    
    
    
    private   class ConnectionWrapper {
    	
    	private volatile Connection<C> connection;
        private volatile Long  timeStamp;
        

        public   ConnectionWrapper(Connection<C> connection,Long timeStamp){
            this.timeStamp=timeStamp;
            this.connection = connection;
        }
        
        public Connection<C> getConnection(){
        	return connection;
        }
        
        public void setConnection(Connection<C> c){
        	assert c!=null;
        	this.connection=c;
        }
        
        public Long getTimeStamp() {
            return timeStamp;
        }

        public void setTimeStamp(Long timeStamp) {
            this.timeStamp = timeStamp;
        }
    }
    
}
