package controllers.forms;

import static play.data.validation.Constraints.Required;

/**
 * Created by vinodvr on 23/06/15.
 */
public class VideoUploadForm {

    @Required
    public String title;

}
