package todo.controllers;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.annotations.View;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.templates.Template;

@Controller
@Path("")
public class HomeController extends DefaultController {

    @View("home")
    private Template home;


    @Route(method = HttpMethod.GET, uri = "")
    public Result getList() {
        return ok(render(home));
    }
}
