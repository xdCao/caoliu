package Parsers;


import model.VideoContent;
import model.VideoType;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import redis.RedisClient;
import redis.clients.jedis.Jedis;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: buku.ch
 * @Date: 2018/10/24 3:25 PM
 */


public class VideoUrlParser extends AbstractParser {

    public static final String VIDEO_PAGE_REGEXP = "(http*://.*/embed/[0-9]+)";

    public static final Pattern VIDEO_PAGE_PATTERN = Pattern.compile(VIDEO_PAGE_REGEXP);

    public static final String VIDEO_REGEXP = "(http.*://[0-9a-zA-Z]+.[0-9a-zA-Z]+.com/get_file/[0-9a-zA-Z]+/[0-9a-zA-Z]+/[0-9]+/[0-9]+/[0-9]+.mp4)";

    public static final Pattern VIDEO_PATTERN = Pattern.compile(VIDEO_REGEXP);

    @Override
    public List<VideoContent> parseUrl(VideoContent inContent) {

        List<VideoContent> contents = new ArrayList<VideoContent>();


        if (StringUtils.isBlank(inContent.getPostUrl()) || inContent.getType() != VideoType.PostURL) {
            return contents;
        }

        try {

            Jedis jedis = RedisClient.getInstance().getJedis();
            Boolean exists = jedis.exists(inContent.getPostUrl());
            if (exists) {
                return contents;
            }


            Document document = getDocument(inContent.getPostUrl());
            Matcher videoPageMatcher = VIDEO_PAGE_PATTERN.matcher(document.body().toString());

            if (videoPageMatcher.find()) {
//                System.out.println("find video page    "+videoPageMatcher.group(0)+" title: "+inContent.getTitle());

                String videoPage = videoPageMatcher.group(0).replace("______", ".");
                videoPage = videoPage.replace("http://www.viidii.info/?", "");

//                System.out.println("Changed page:   "+videoPage);

                Document videoDoc = getDocument(videoPage);

                Matcher videoMatcher = VIDEO_PATTERN.matcher(videoDoc.body().toString());
                if (videoMatcher.find()) {
                    System.out.println("find video !!!" + videoMatcher.group(0));
                    VideoContent videoContent = new VideoContent();
                    videoContent.setPostUrl(inContent.getPostUrl());
                    videoContent.setPageUrl(inContent.getPageUrl());
                    videoContent.setTitle(inContent.getTitle());
                    videoContent.setType(VideoType.VideoURL);
                    videoContent.setVideoUrl(videoMatcher.group(0));
                    contents.add(videoContent);
                    return contents;
                }
            }

            return contents;

        } catch (IOException e) {
            e.printStackTrace();
            return contents;
        }


    }

    public static void main(String[] args) {
        VideoContent videoContent = new VideoContent();
        videoContent.setType(VideoType.PostURL);
        videoContent.setPostUrl("http://cl.39u.xyz/htm_data/22/1810/3318013.html");
        VideoUrlParser videoUrlParser = new VideoUrlParser();
        videoUrlParser.parseUrl(videoContent);
    }

}
