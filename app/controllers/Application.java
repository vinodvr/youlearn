package controllers;

import models.User;
import models.Video;
import play.mvc.Controller;
import play.mvc.Result;
import utils.AuthUtils;
import views.html.index;

public class Application extends Controller {

    public Result index() {
        User user = AuthUtils.getUserInfo(session());
        return ok(index.render(Video.getUserVideos(user), Video.getPopularVideos()));
    }


    public Result oauth2Redirect() {
        String stateToken = AuthUtils.generateStateToken();
        String redirectURL = getRedirectURL(stateToken);
        flash("stateToken", stateToken);
        return redirect(redirectURL);
    }

    public Result oauth2Callback(String code, String state) {
        String expectedStateToken = flash().get("stateToken");

        if (expectedStateToken == null || !expectedStateToken.equals(state)) {
            return badRequest("Invalid token detected!");
        }

        User user;
        try {
            user = AuthUtils.getUserInfo(code);
            loginUser(user);
        } catch (Exception e) {
            internalServerError("Error while getting userInfo");
        }

        return redirect(routes.Application.index());
    }



    public Result logout() {
        User user = AuthUtils.getUserInfo(session());
        logoutUser();
        return redirect(routes.Application.index());
    }



    private void logoutUser() {
        session().remove("email");
        session().remove("name");
        session().remove("imageUrl");
        session().remove("loggedIn");
    }

    private void loginUser(User user) {
        flash().put("loggedIn", "true");

        session().put("email", user.email);
        session().put("name", user.name);
        session().put("imageUrl", user.imageUrl);
        session().put("loggedIn", "true");
    }


    private String getRedirectURL(String stateToken) {
        return "https://accounts.google.com/o/oauth2/auth?" +
                "scope=email%20profile" +
                "&state=" + stateToken +
                "&redirect_uri=" + AuthUtils.OAUTH2_REDIRECT_URI +
                "&response_type=code" +
                "&client_id=" + AuthUtils.GOOGLE_CLIENT_ID +
                "&approval_prompt=force";

    }


}
