package models;

import com.google.api.client.util.Lists;
import com.google.common.collect.Maps;

import java.io.File;
import java.util.*;

/**
 * Created by vinodvr on 21/06/15.
 */
public class Video {
    public String id;
    public String title;
    public File path;
    public User uploadedBy;
    public Date uploadedAt;
    public long totalViews;


    private static Map<String, Video> videos = Maps.newConcurrentMap();
    static {
        User user = new User();
        user.email = "vinodvr@flipkart.com";
        user.name = "Vinod";

        Video video = new Video();
        video.id = "2";
        video.title = "Popular Video";
        video.totalViews = 10000;
        video.uploadedAt = new Date();
        video.uploadedBy = user;
        video.path = new File("/Users/vinodvr/src/learning/youlearn/public/sample_videos/mov_bbb.mp4");

        Video video1 = new Video();
        video1.id = "1";
        video1.title = "First Video";
        video1.totalViews = 100;
        video1.uploadedAt = new Date();
        video1.uploadedBy = user;
        video1.path = new File("/Users/vinodvr/src/learning/youlearn/public/sample_videos/mov_bbb.mp4");

        videos.put(video.id, video);
        videos.put(video1.id, video1);
    }
    public static List<Video> getUserVideos(User user) {
        if (user.email == null) return Collections.EMPTY_LIST;
        List<Video> userVids = Lists.newArrayList();
        for (Video video : videos.values()) {
            if (video.uploadedBy.email != null && video.uploadedBy.email.equals(user.email)) {
                userVids.add(video);
            }
        }
        return userVids;
    }


    public static List<Video> getPopularVideos() {
        ArrayList<Video> list = new ArrayList<>(videos.values());
        Collections.sort(list, (o1, o2) -> (int) o2.totalViews - (int) o1.totalViews);
        return  list;
    }

    public static Video getById(String id) {
        return videos.get(id);
    }

    public static void addVideo(Video video) {
        videos.put(video.id, video);
    }

    public static void incrementView(String id) {
        videos.get(id).totalViews++;
    }
}
