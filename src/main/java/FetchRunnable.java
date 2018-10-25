import com.alibaba.fastjson.JSON;
import model.VideoContent;
import model.VideoType;
import redis.RedisClient;
import redis.clients.jedis.Jedis;

import java.util.Date;

/**
 * @Author: buku.ch
 * @Date: 2018/10/24 9:17 PM
 */


public class FetchRunnable implements Runnable {

    private VideoContent videoContent;

    public void setContent(VideoContent content) {
        this.videoContent = content;
    }

    public void run() {
        videoContent.setCreated(new Date());
        videoContent.setType(VideoType.Video);
        videoContent.setCompleted(true);

        String path = HttpDownload.download(videoContent.getVideoUrl());

        if (!"fail".equals(path)) {
            System.out.println("download complete:   "+path+"   url:   "+videoContent.getVideoUrl());
        }

        videoContent.setFilePath(path);

        /*存到redis*/
        Jedis jedis = RedisClient.getInstance().getJedis();
        String set = jedis.set(videoContent.getPostUrl(), JSON.toJSONString(videoContent));
//        System.out.println("redis insert: "+set);
//        System.out.printf("key:   "+videoContent.getPostUrl()+"\n"+jedis.get(videoContent.getPostUrl()));
    }
}
