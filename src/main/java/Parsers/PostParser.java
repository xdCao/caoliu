package Parsers;

import model.Constants;
import model.VideoContent;
import model.VideoType;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import redis.RedisClient;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @Author: buku.ch
 * @Date: 2018/10/24 1:49 PM
 */


public class PostParser extends AbstractParser {

    public static final String POST_REGEX = "htm_data/22/[0-9]+/[0-9]+.html";



    public List<VideoContent> parseBody(String body, Pattern pattern) {
        return null;
    }

    public List<VideoContent> parseUrl(VideoContent inContent) {

        List<VideoContent> contents = new ArrayList<VideoContent>();

        if (StringUtils.isBlank(inContent.getPageUrl()) || inContent.getType() == VideoType.PostURL || inContent.getType() == VideoType.VideoURL) {
            return contents;
        }

        try {
            Document document = getDocument(inContent.getPageUrl());
            Elements elements = document.select("tr.tr3");

            for (Element element : elements) {
                Elements links = element.getElementsByTag("a");
                for (Element link : links) {
                    String text = link.text();
                    String href = link.attr("href");
                    boolean matches = Pattern.matches(POST_REGEX, href);
                    if (matches && !".::".equals(text)) {
                        href = Constants.prefix + href;
                        System.out.println(text + "   :   " + href);

                        /*redis读，查询的结果决定是不是要进行下一步解析出视频真实地址*/

                        Jedis jedis = RedisClient.getInstance().getJedis();
                        Boolean exists = jedis.exists(href);
                        if (!exists) {
                            VideoContent videoContent = new VideoContent();
                            videoContent.setPageUrl(inContent.getPageUrl());
                            videoContent.setType(VideoType.PostURL);
                            videoContent.setCompleted(false);
                            videoContent.setPostUrl(href);
                            videoContent.setTitle(text);
                            contents.add(videoContent);
                        }
                    }
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
        videoContent.setPageUrl("http://cl.39u.xyz/thread0806.php?fid=22&amp;search=&amp;page=2");
        PostParser postParser = new PostParser();
        List<VideoContent> contents = postParser.parseUrl(videoContent);
        for (VideoContent videoContent1 : contents) {
            System.out.println(videoContent1.getPageUrl() + "   :   " + videoContent1.getTitle() + "   :   " + videoContent1.getPostUrl());
        }

    }


}
