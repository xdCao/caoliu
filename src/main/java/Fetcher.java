import com.alibaba.fastjson.JSON;
import model.VideoContent;
import model.VideoType;
import redis.RedisClient;
import redis.clients.jedis.Jedis;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author: buku.ch
 * @Date: 2018/10/24 4:56 PM
 */


public class Fetcher{

    private static ExecutorService executorService = Executors.newFixedThreadPool(10);

    public void fetch(final VideoContent videoContent) {

        if (videoContent.getType() != VideoType.VideoURL || videoContent.getVideoUrl() == null) {
            return;
        }

        FetchRunnable fetchRunnable = new FetchRunnable();
        fetchRunnable.setContent(videoContent);

        executorService.submit(fetchRunnable);

    }

    public static void main(String[] args) {

        VideoContent videoContent = new VideoContent();
        videoContent.setPageUrl("");
        videoContent.setPostUrl("");
        videoContent.setTitle("");
        videoContent.setCreated(new Date());
        videoContent.setType(VideoType.Video);
        videoContent.setFilePath("");
        videoContent.setCompleted(true);

        Jedis jedis = RedisClient.getInstance().getJedis();
        String set = jedis.set("http", JSON.toJSONString(videoContent));
        System.out.println("redis insert: "+set);

        Boolean exists = jedis.exists(videoContent.getPostUrl());
        System.out.println(exists);

    }


}
