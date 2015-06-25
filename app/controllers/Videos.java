package controllers;

import controllers.forms.VideoUploadForm;
import models.Video;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utils.AuthUtils;
import views.html.video;
import views.html.videoUpload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by vinodvr on 21/06/15.
 */
public class Videos extends Controller {

    private Form<VideoUploadForm> videoForm = Form.form(VideoUploadForm.class);
    private static List<String> SUPPORTED_VIDEO_MIME_TYPES = Collections.singletonList("video/mp4");
    private static AtomicLong counter = new AtomicLong(0);
    public static File DATA_DIR = new File("/Users/vinodvr/src/learning/data");


    public Result show(String id) {
        Video.incrementView(id);
        return ok(video.render(Video.getById(id)));
    }

    public Result uploadVideoForm() {
        return ok(videoUpload.render(videoForm));
    }

    public Result uploadVideo() throws IOException {
        Form<VideoUploadForm> boundForm = videoForm.bindFromRequest();

        if(boundForm.hasErrors()) {
            flash("error", "Please correct the form below.");
            return badRequest(videoUpload.render(boundForm));
        }

        VideoUploadForm videoUploadForm = boundForm.get();

        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart part = body.getFile("videoData");
        if (part == null) {
            flash("error", "Please choose a video file.");
            boundForm.reject("videoData", "Please choose a video file.");
            return badRequest(videoUpload.render(boundForm));
        }
        File videoFile = part.getFile();

        String contentType = part.getContentType();
        if (!SUPPORTED_VIDEO_MIME_TYPES.contains(contentType)) {
            flash("error", "Please choose a video file.");
            boundForm.reject("videoData", "Please choose a mp4 video file.");
            return badRequest(videoUpload.render(boundForm));
        }

        String title = videoUploadForm.title;

        Video video = new Video();
        video.id = String.valueOf(counter.incrementAndGet());
        video.title = title;
        video.totalViews = 0;
        video.uploadedAt = new Date(System.currentTimeMillis());
        video.uploadedBy = AuthUtils.getUserInfo(session());
        File destFile = new File(DATA_DIR, video.id + ".mp4");
        FileUtils.copyFile(videoFile, destFile);;
        video.path = destFile;

        Video.addVideo(video);

        return redirect(routes.Application.index());
    }

    public Result getVideoContent(String id, String mimeType) throws Exception {
        Video video = Video.getById(id);
        if (video == null) {
            return notFound();
        }
        File path = video.path;
        String range = request().getHeader("Range");
        if (range != null) {
            long totalSize = FileUtils.sizeOf(path);
            int stByte = Integer.valueOf(range.split("=")[1].split("-")[0]);
            InputStream stream = new FileInputStream(path);
            IOUtils.skip(stream, stByte);

            int bufSize = 1024 * 1024;
            byte[] buffer = new byte[bufSize];
            int actualRead = IOUtils.read(stream, buffer, 0, bufSize);

            response().setHeader("Content-Type", mimeType);
            response().setHeader("Content-Length", String.valueOf(actualRead));
            response().setHeader("Accept-Ranges", "bytes");
            response().setHeader("Content-Range", "bytes " + stByte + "-" + String.valueOf(stByte + actualRead - 1) + "/" + String.valueOf(totalSize));
            response().setHeader("Content-Disposition", "attachment; filename=" + path.getName());
            return status(206, buffer);
        } else {
            return ok(path);
        }
    }


}
