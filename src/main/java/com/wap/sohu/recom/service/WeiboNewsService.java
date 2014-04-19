package com.wap.sohu.recom.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wap.sohu.recom.constants.TopNewsRedisKeyConstants;
import com.wap.sohu.recom.core.redis.StringRedisTemplateExt;
import com.wap.sohu.recom.model.FilterBuild;
import com.wap.sohu.recom.utils.TopNewsStrategy;

/**
 * 类WeiboNewsService.java的实现描述：
 *
 * @author yeyanchao Aug 28, 2013 2:47:32 PM
 */
@Service
public class WeiboNewsService {

    @Autowired
    private StringRedisTemplateExt stringRedisTemplateToutiao;

    public List<Integer> getWeiboTagNews(long cid, long pid, FilterBuild filterBuild, int size) {

        List<Integer> resultList = new ArrayList<Integer>();

        if (cid <= 0 || pid <= 0) return resultList;

        String key = String.format(TopNewsRedisKeyConstants.USER_WEIBO_LIKE_HASH, pid);
        String tags = (String) stringRedisTemplateToutiao.opsForHash().get(key,
                                                                           TopNewsRedisKeyConstants.USER_WEIBO_LIKE_TAG);
        String[] taglist = StringUtils.split(tags, ",");

        if (taglist != null && taglist.length > 0) {
            int index = TopNewsStrategy.queryShortCatIndex(taglist.length);
            String tag = taglist[index];
            List<Integer> tagNewsIdList = queryWeiboTagNews(Integer.parseInt(tag));

            int counter = 0;
            for (Integer newsId : tagNewsIdList) {
                if (counter >= size) break;
                if (filterBuild.checkNewsInvalid(newsId)) {
                    continue;
                }
                resultList.add(newsId);
                filterBuild.addNews(cid, newsId);

                // add newsid counter
                counter++;
            }
        }

        return resultList;
    }

    private List<Integer> queryWeiboTagNews(int tagId) {
        String key = String.format(TopNewsRedisKeyConstants.TAG_NEWS_MATRIX_2_DAY, tagId);

        Set<String> tagNewsList = stringRedisTemplateToutiao.opsForZSet().range(key, 0, 160);

        List<Integer> tagNewsIdList = new ArrayList<Integer>();
        if (tagNewsList != null && !tagNewsList.isEmpty()) {
            for (String tag : tagNewsList) {
                tagNewsIdList.add(Integer.parseInt(tag));
            }
        }
        return tagNewsIdList;
    }
}
