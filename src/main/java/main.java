import Parsers.PageParser;
import Parsers.PostParser;
import Parsers.VideoUrlParser;
import com.alibaba.fastjson.JSON;
import model.Constants;
import model.VideoContent;
import model.VideoType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import redis.RedisClient;

import java.io.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: buku.ch
 * @Date: 2018/10/21 1:57 PM
 */


public class main {

    public static void main(String[] args) {




        final PageParser pageParser = new PageParser();
        final PostParser postParser = new PostParser();
        final VideoUrlParser videoUrlParser = new VideoUrlParser();
        final Fetcher fetcher =new Fetcher();

        final ArrayBlockingQueue<VideoContent> queue = new ArrayBlockingQueue<VideoContent>(100000);

        try {
            VideoContent seed = new VideoContent();
            seed.setPageUrl(Constants.seedUrl);
            seed.setType(VideoType.PageURL);
            seed.setCompleted(false);
            seed.setCreated(new Date());

            List<VideoContent> pageContents = pageParser.parseUrl(seed);
            List<VideoContent> postContents = postParser.parseUrl(seed);
            List<VideoContent> videoUrlContents = videoUrlParser.parseUrl(seed);
            List<VideoContent> all =new ArrayList<VideoContent>();
            all.addAll(pageContents);
            all.addAll(postContents);
            all.addAll(videoUrlContents);


            for (VideoContent videoContent:all) {
                try {
                    queue.put(videoContent);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


            Thread productor = new Thread(new Runnable() {
                public void run() {
                    while (!queue.isEmpty()) {
                        VideoContent take = null;
                        try {
                            take = queue.take();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (take!=null) {
                            List<VideoContent> pageContents = pageParser.parseUrl(take);
                            List<VideoContent> postContents = postParser.parseUrl(take);
                            List<VideoContent> videoUrlContents = videoUrlParser.parseUrl(take);
                            List<VideoContent> all =new ArrayList<VideoContent>();

                            all.addAll(pageContents);
                            all.addAll(postContents);
                            all.addAll(videoUrlContents);

                            for (VideoContent content:all) {
                                try {

                                    if (content.getType()==VideoType.VideoURL) {
//                                        System.out.println("传入的content: "+ JSON.toJSONString(content));
                                        fetcher.fetch(content);
                                        System.out.println("                            fetch: "+content.getVideoUrl());
                                    } else {

                                        System.out.println("                new in queue: "+content.getPageUrl());
//                                        System.out.println("                new in queue: "+content.getPostUrl());
                                        queue.put(content);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }


                    }
                }
            });

            productor.start();
        }catch (Exception e) {

        }finally {
            RedisClient.getInstance().close();
        }


    }


}
