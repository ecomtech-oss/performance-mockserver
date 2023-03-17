package org.samokat.performance.mockserver.mocks;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import net.minidev.json.JSONObject;
import org.mockserver.mock.Expectation;
import org.mockserver.mock.action.ExpectationResponseCallback;
import org.mockserver.model.HttpClassCallback;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.MediaType;
import org.samokat.performance.mockserver.core.initializer.Command;
import org.samokat.performance.mockserver.core.initializer.Utils;

public class Croissant implements Command {

    @Override
    public Expectation[] initializeExpectations() throws IOException {
        return new Expectation[]{

            new Expectation(
                request()
                    .withMethod("POST")
                    .withPath("/quaso/stock")
            ).thenRespond(
                response()
                    .withBody(
                        json(Utils.getFile("croissant/stock.json"), StandardCharsets.UTF_8)
                    )
            ),

            new Expectation(
                request()
                    .withMethod("GET").withPath("/quaso/.+/info")
            ).thenRespond(
                HttpClassCallback.callback().withCallbackClass(CroissantCall.class)
            ),

            new Expectation(
                request()
                    .withMethod("GET")
                    .withPath("/quaso")
            ).thenRespond(
                HttpClassCallback.callback().withCallbackClass(RandomCroissantCall.class)
            ),

        };
    }


    public static class CroissantCall implements ExpectationResponseCallback {

        @Override
        public HttpResponse handle(HttpRequest httpRequest) {
            JSONObject json = new JSONObject();
            var uuid = httpRequest.getPath().getValue()
                .replaceFirst("/quaso/", "")
                .replaceFirst("/info", "");
            json.put("id", uuid);

            List<String> types = Arrays.asList("chocolate", "almond", "raspberry");
            Random rand = new Random();
            String type = types.get(rand.nextInt(types.size()));
            json.put("type", type);
            json.put("taste", "fantastic");
            return response().withBody(json(json))
                .withContentType(MediaType.APPLICATION_JSON_UTF_8);
        }
    }

    public static class RandomCroissantCall implements ExpectationResponseCallback {

        @Override
        public HttpResponse handle(HttpRequest httpRequest) {
            JSONObject json = new JSONObject();
            UUID uuid = UUID.randomUUID();
            json.put("id", uuid);

            List<String> types = Arrays.asList("chocolate", "almond", "raspberry");
            Random rand = new Random();
            String type = types.get(rand.nextInt(types.size()));
            json.put("type", type);
            return response().withBody(json(json))
                .withContentType(MediaType.APPLICATION_JSON_UTF_8);
        }
    }
}