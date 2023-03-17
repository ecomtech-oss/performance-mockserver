package org.samokat.performance.mockserver.mocks;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.model.JsonPathBody.jsonPath;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import org.apache.commons.lang3.RandomStringUtils;
import org.mockserver.mock.Expectation;
import org.mockserver.model.HttpClassCallback;
import org.samokat.performance.mockserver.core.initializer.Command;
import org.samokat.performance.mockserver.core.initializer.Utils;
import org.samokat.performance.mockserver.utils.ExternalField;

public class BananaBread implements Command {

    @Override
    public Expectation[] initializeExpectations() throws IOException {
        return new Expectation[]{

            new Expectation(
                request()
                    .withMethod("POST")
                    .withPath("/graphql")
                    .withBody(Utils.getFile("bananabread/get_scheme.json"))
            ).thenRespond(
                response()
                    .withBody(
                        json(Utils.getFile("bananabread/scheme.json"), StandardCharsets.UTF_8)
                    )
            ),
            // заглушка экстернал полей
            new Expectation(
                request()
                    .withMethod("POST")
                    .withPath("/graphql")
                    .withBody(jsonPath("$.variables.representations[*]"))
            ).thenRespond(
                HttpClassCallback.callback().withCallbackClass(ExternalFieldBananaBread.class)
            ),
        };
    }


    public static class ExternalFieldBananaBread implements ExternalField {

        @Override
        public String parametrizeByRule(String name) {
            if (name.equals("id")) {
                List<String> id = List.of(
                    "bab28819-bae3-4891-96f9-e2e214265fa3",
                    "424c1588-4fa4-4ccc-93ea-d50cedbb51b4");
                return id.get(new Random().nextInt(id.size()));
            } else {
                return RandomStringUtils.randomAlphabetic(10);
            }
        }
    }
}
