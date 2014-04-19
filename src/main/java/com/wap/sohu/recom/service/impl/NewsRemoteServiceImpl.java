
package com.wap.sohu.recom.service.impl;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import MyRMI.ClassifyServerInterface;

import com.sohu.tag.api.impl.RMI_sohu_tag_interface;
import com.wap.sohu.recom.service.NewsRemoteService;


/**
 * 调用微博远程服务得到tag列表 
 * @author hongfengwang 2012-9-3 下午05:33:45
 */
@Service("newsRemoteService")
public class NewsRemoteServiceImpl implements NewsRemoteService {
    
    private static final Logger LOGGER = Logger.getLogger(NewsRemoteServiceImpl.class);

    @Autowired
    private MessageSource messageSource;
    
    private String        tagRmiUrl;
    private String        catRmiUrl;
    
    
    
    private  List<RMI_sohu_tag_interface>  remoteList = new CopyOnWriteArrayList<RMI_sohu_tag_interface>();
    private  List<ClassifyServerInterface> remoteCatList = new CopyOnWriteArrayList<ClassifyServerInterface>();
    
    
    @PostConstruct
    private void initRmiAddr(){
        tagRmiUrl = messageSource.getMessage("tag_rmi", null, Locale.getDefault());
        catRmiUrl = messageSource.getMessage("cat_rmi", null, Locale.getDefault());
        String [] tagRmis = StringUtils.split(tagRmiUrl, ",");
        String [] catRmis = StringUtils.split(catRmiUrl, ",");
        try {
            for (String str : tagRmis) {
                remoteList.add((RMI_sohu_tag_interface) Naming.lookup(str));
            }
            for (String str : catRmis) {
                remoteCatList.add((ClassifyServerInterface) Naming.lookup(str));
            }
        } catch (MalformedURLException e) {
            LOGGER.error("tag rmi has error", e);
        } catch (RemoteException e) {
            LOGGER.error("tag rmi has error", e);
        } catch (NotBoundException e) {
            LOGGER.error("tag rmi has error", e);
        }catch (Throwable e) {
            LOGGER.error("tag rmi has error", e.getCause());
        }
    }
    
    @Override
    public Map<String, Double> getRmiTags(String title, String content) {
        if (title.length()>10000) {
            title =  StringUtils.substring(title, 0, 10000);
        }
        if (content.length()>10000) {
            content = StringUtils.substring(content, 0, 10000);
        }
        String msg = "{\"title\":\"" + title +"\",\"content\":\"" + content +"\",\"url\":\"This is a url\"}";        
        RMI_sohu_tag_interface remote = remoteList.get(RandomUtils.nextInt(remoteList.size()));
        String result = "";
        try {
            result =  remote.GetTags(msg);
        } catch (RemoteException e) {
            LOGGER.error("tag rmi has error", e);
            remoteList.remove(remote);
        }           
        return processResult(result);
    }

    /**
     * @param result
     * @return
     */
    private Map<String, Double> processResult(String result) {
        String[] tags = StringUtils.split(result, ";");
        if (tags==null) {
            return null;
        }
        Map<String, Double> tagMap  = new HashMap<String, Double>();
        for (String tagStr : tags) {
             String[] tagItems = StringUtils.split(tagStr, ",", 2);
             String   value = tagItems.length==2?StringUtils.substringBeforeLast(tagItems[1], ","):"0";
             tagMap.put(StringUtils.trim(tagItems[0]), Double.valueOf(value));
        }
        return tagMap;        
    }

    
    @Override
    public Map<String, Double> getRmiCat(String title, String content) {
        if (title.length()>10000) {
            title = StringUtils.substring(title, 0, 10000);
        }
//        if (content.length()>10000) {
//            content = StringUtils.substring(content, 0, 10000);
//        }
        //紧急方案就不传递内容了，响应会比较慢
        content="";
        String msg = "{\"title\":\"" + title +"\",\"content\":\"" + content +"\",\"url\":\"This is a url\"}";        
        byte[] result = null;
        String ret ="";
        ClassifyServerInterface remoteCat =  remoteCatList.get(RandomUtils.nextInt(remoteCatList.size()));
        try {
            result =  remoteCat.GetCategory(msg.getBytes("utf-8"),"utf-8");    
            ret    = new String(result,"utf-8");
        } catch (UnsupportedEncodingException e) {
            LOGGER.error(e.getMessage()+" tag rmi has error", e);
            remoteCatList.remove(remoteCat);
        }catch (RemoteException e) {
            LOGGER.error(e.getMessage()+" tag rmi has error", e);
            remoteCatList.remove(remoteCat);
        }
        return processResultCat(ret);
    }

    /**
     * @param result
     * @return
     */
    private Map<String, Double> processResultCat(String result) {
        Map<String, Double> map = new HashMap<String, Double>();
        String [] cats = StringUtils.split(result, " and ");
        for (String cat : cats) {
            cat = StringUtils.removeStart(cat, "知识库分类-");
            String[] kv = StringUtils.split(cat, "_");
            double   value = Double.valueOf(kv[1]);
            if (value==0) {
               continue;
            }             
            double temp           = Math.pow(10, 5);
            double valueNum       = ((int)(value * temp))/(temp);
            map.put(StringUtils.trim(kv[0]), valueNum);
       }
        return map;
    }

}
