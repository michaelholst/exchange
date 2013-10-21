package controllers;

import db.CassandraClient;
import model.ExchangeRate;
import org.codehaus.jackson.node.ObjectNode;
import play.*;
import play.libs.F;
import play.libs.Json;
import play.libs.WS;
import play.mvc.*;

import views.html.*;
import xml.ExchangeRateParser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render());
    }

    /**
     * Async DB read of ExchangeRates
     * @param currency Currency filter
     * @return JSON response of ExchangeRate items
     */
    public static Result exchangeRates(final String currency) {
        F.Promise<List<ExchangeRate>> promiseOfList = play.libs.Akka.future(
                new Callable<List<ExchangeRate>>() {
                    public List<ExchangeRate> call() {
                        CassandraClient db = new CassandraClient();
                        return db.read(currency);
                    }
                }
        );
        return async(
                promiseOfList.map(
                        new F.Function<List<ExchangeRate>, Result>() {
                            public Result apply(List<ExchangeRate> list) {
                                return ok(Json.toJson(list));
                            }
                        }
                )
        );
    }

    /**
     * Async pull of XML feed
     * @param currency Currency filter
     * @return 200 or 500 status codes
     */
    public static Result refresh(final String currency) {
        F.Promise<Boolean> promiseOfBoolean = play.libs.Akka.future(
                new Callable<Boolean>() {
                    public Boolean call() {
                        // Read the XML feed (notice the .get() that may block)
                        return WS.url("http://www.ecb.europa.eu/stats/eurofxref/eurofxref-hist-90d.xml").get().map(
                                new F.Function<WS.Response, Boolean>() {
                                    public Boolean apply(WS.Response response) {
                                        // Parse XML
                                        ExchangeRateParser parser = new ExchangeRateParser();
                                        List<ExchangeRate> rates = parser.parseRates(response.getBodyAsStream(), currency);

                                        // Write to DB
                                        CassandraClient db = new CassandraClient();
                                        return db.write(currency, rates);
                                    }
                                }
                        ).get();
                    }
                }
        );
        return async(
                promiseOfBoolean.map(
                        new F.Function<Boolean, Result>() {
                            public Result apply(Boolean success) {
                                if (success) {
                                    return ok();
                                } else {
                                    return internalServerError();
                                }
                            }
                        }
                )
        );
    }
}
