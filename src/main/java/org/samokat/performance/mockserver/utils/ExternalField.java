package org.samokat.performance.mockserver.utils;

import static org.mockserver.model.HttpResponse.response;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import graphql.language.Document;
import graphql.language.Field;
import graphql.language.InlineFragment;
import graphql.language.OperationDefinition;
import graphql.language.SelectionSet;
import graphql.parser.Parser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.RandomStringUtils;
import org.mockserver.mock.action.ExpectationResponseCallback;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.MediaType;
import org.samokat.performance.mockserver.helpers.externalfield.RepresentationsItem;
import org.samokat.performance.mockserver.helpers.externalfield.Response;

/**
 * Special kind of ExpectationResponseCallback for processing graphql external fields
 */
public interface ExternalField extends ExpectationResponseCallback {

    /**
     * Returns value of parameter by name. You can specify custom logic or constant values.
     *
     * @param name The name of requested parameter
     * @return String
     */
    default String parametrizeByRule(String name) {
        return RandomStringUtils.randomAlphabetic(10);
    }


    @Override
    default HttpResponse handle(HttpRequest httpRequest) {
        var rawBody = httpRequest.getBody().getValue().toString();

        Gson gson = new Gson();
        Response body = gson.fromJson(rawBody, Response.class);

        var query = body.getQuery().replaceFirst(".+?\\)", "");
        System.out.println(query);

        var params = getParams(query);

        HashMap<String, Object> variables = getVars(rawBody);

        var list = body.getVariables().getRepresentations();
        var json = new JsonObject();
        var entities = new JsonObject();
        entities.add("_entities", new JsonArray());

        for (RepresentationsItem el : list) {
            var type = el.getTypename();
            var externalData = new JsonObject();
            externalData.addProperty("__typename", type);
            if (!variables.isEmpty()) {
                for (Map.Entry<String, Object> entry : variables.entrySet()) {
                    externalData.addProperty(String.format("_%s", entry.getKey()),
                        entry.getValue().toString());
                }
            }
            for (Object param : params) {
                if (param instanceof String) {
                    externalData.addProperty((String) param,
                        RandomStringUtils.randomAlphabetic(10));
                } else if (param instanceof List) {
                    var objectParam = (List<?>) param;
                    var jsonParam = (JsonObject) objectParam.get(1);
                    var name = (String) objectParam.get(0);

                    externalData.add(name, jsonParam.getAsJsonObject(name));
                }
            }
            entities.getAsJsonArray("_entities").add(externalData);
        }
        json.add("data", entities);

        return response().withBody(json.toString())
            .withContentType(MediaType.APPLICATION_JSON_UTF_8);
    }


    private List<Object> getParams(String query) {
        Parser parser = new Parser();
        List<Object> paramList = new ArrayList<>();
        Document document = parser.parseDocument(query);
        OperationDefinition operation = null;
        for (var definition : document.getDefinitions()) {
            if (definition instanceof OperationDefinition) {
                operation = (OperationDefinition) definition;
            }
        }
        Field entities = null;
        for (var selection : Objects.requireNonNull(operation).getSelectionSet().getSelections()) {
            if (selection instanceof Field) {
                entities = (Field) selection;
            }
        }
        InlineFragment selectionInlineFragment = null;
        for (var selectionFragment : Objects.requireNonNull(entities).getSelectionSet()
            .getSelections()) {
            if (selectionFragment instanceof InlineFragment) {
                selectionInlineFragment = (InlineFragment) selectionFragment;
            }
        }
        Field mainField = null;
        for (var selectionField : Objects.requireNonNull(selectionInlineFragment).getSelectionSet()
            .getSelections()) {
            if (selectionField instanceof Field) {
                mainField = (Field) selectionField;
            }
        }
        if (Objects.requireNonNull(mainField).getSelectionSet() == null) {
            paramList.add(mainField.getName());
        } else {
            paramList.add(List.of(mainField.getName(), getSubField(
                mainField.getName(),
                mainField.getSelectionSet())));
        }
        return paramList;
    }


    private JsonObject getSubField(String name, SelectionSet value) {
        var result = new JsonObject();
        var subField = new JsonObject();
        for (var selection : value.getSelections()) {
            Field field = (Field) selection;
            if (field.getSelectionSet() != null) {
                subField.add(field.getName(),
                    getSubField(field.getName(), field.getSelectionSet()));
            } else {
                subField.addProperty(field.getName(), parametrizeByRule(field.getName()));
            }
        }
        result.add(name, subField);
        return result;
    }


    private HashMap<String, Object> getVars(String rawBody) {

        var vars = rawBody.replaceAll("[\n,\r ]", "")
            .replaceFirst(".+\"__typename\":\".+?\"", "")
            .replaceFirst("}]}.+", "")
            .replaceAll("\"", "")
            .split(",");

        HashMap<String, Object> variables = new HashMap<>();
        for (String v : vars) {
            var pair = v.split(":");
            variables.put(pair[0], pair[1]);
        }
        return variables;
    }
}