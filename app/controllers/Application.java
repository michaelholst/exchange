package controllers;

import org.codehaus.jackson.node.ObjectNode;
import play.*;
import play.libs.F;
import play.libs.Json;
import play.mvc.*;

import views.html.*;

import java.util.concurrent.Callable;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render());
    }

    public static Result json() {
        F.Promise<String> promiseOfString = play.libs.Akka.future(
                new Callable<String>() {
                    public String call() {
                        return "OK";
                    }
                }
        );
        return async(
                promiseOfString.map(
                        new F.Function<String, Result>() {
                            public Result apply(String s) {
                                ObjectNode result = Json.newObject();
                                result.put("test", "OK");

                                return ok(result);
                            }
                        }
                )
        );
    }

}
