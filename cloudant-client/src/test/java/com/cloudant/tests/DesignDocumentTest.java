/*
 * Copyright (c) 2016 IBM Corp. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

package com.cloudant.tests;

import com.cloudant.client.api.model.DesignDocument;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Rhys Short on 24/02/2016.
 */
@RunWith(Enclosed.class)
public class DesignDocumentTest {

    static Gson gson = new GsonBuilder().create();

    enum Field {
        mrView,
        listFunction,
        showFunction,
        updateFunction,
        filterFunction,
        rewriteRule,
        indexes,
        validateDocUpdate
    }

    private static DesignDocument getDesignDocument() {
        return getDesignDocumentWithFields(EnumSet.allOf(Field.class));
    }

    private static DesignDocument getDesignDocumentWithFields(EnumSet<Field> fields) {
        DesignDocument designDocument = new DesignDocument();
        for (Field f : fields) {
            switch (f) {
                case mrView:
                    mrView(designDocument);
                    break;
                case listFunction:
                    listFunction(designDocument);
                    break;
                case showFunction:
                    showFunction(designDocument);
                    break;
                case updateFunction:
                    updateFunction(designDocument);
                    break;
                case filterFunction:
                    filterFunction(designDocument);
                    break;
                case rewriteRule:
                    rewriteRule(designDocument);
                    break;
                case indexes:
                    indexes(designDocument);
                    break;
                case validateDocUpdate:
                    validateDocUpdate(designDocument);
                    break;
                default:
                    break;
            }
        }
        return designDocument;
    }

    private static DesignDocument getDesignDocumentWithDifferent(Field f) {
        DesignDocument designDocument = getDesignDocument();

        switch (f) {
            case mrView:
                DesignDocument.MapReduce mapReduce = new DesignDocument.MapReduce();
                mapReduce.setMap("function(doc){emit(doc.hello);}");
                mapReduce.setReduce("_count");
                mapReduce.setDbCopy("myOtherDB");
                designDocument.getViews().put("view2", mapReduce);
                break;
            case listFunction:
                designDocument.getLists().put("myList2", "function(head,req){ send(toJson(getRow)" +
                        "; }");
                break;
            case showFunction:
                designDocument.getShows().put("myShow2", "function(doc,req){ if(doc){return " +
                        "\"hello " +
                        "world!\"}");
                break;
            case updateFunction:
                designDocument.getUpdates().put("myUpdate2", "function(doc,req){return [doc, " +
                        "'Edited" +
                        " " +
                        "World!'];}");
                break;
            case filterFunction:
                designDocument.getFilters().put("myOtherFilter", "function(doc,req){return false;" +
                        "}");
                break;
            case rewriteRule:
                List<Map<String, Object>> rewrites = new ArrayList<Map<String, Object>>();
                Map<String, Object> rewrite = new HashMap<String, Object>();
                rewrite.put("from", "/index.php");
                rewrite.put("to", "index.html");
                rewrite.put("method", "GET");
                rewrite.put("query", new HashMap<String, String>());
                rewrites.add(0, rewrite);

                JsonArray rewriteJson = (JsonArray) gson.toJsonTree(rewrites);
                designDocument.setRewrites(rewriteJson);
                break;
            case indexes:
                Map<String, Map<String, String>> indexes = new HashMap<String, Map<String,
                        String>>();

                Map<String, String> index = new HashMap<String, String>();
                index.put("index", "function(doc){....}");
                indexes.put("movie", index);

                JsonObject indexesJson = (JsonObject) gson.toJsonTree(indexes);
                designDocument.setIndexes(indexesJson);
                break;
            case validateDocUpdate:
                designDocument.setValidateDocUpdate("throw({ unauthorized: 'Error message here.' " +
                        "});");
                break;
            default:
                break;
        }
        return designDocument;
    }

    public static class OneOffEqualityTests {
        @Test
        public void testDesignDocEqualsForAllFields() {
            Assert.assertEquals(getDesignDocument(), getDesignDocument());
        }
    }

    @RunWith(Parameterized.class)
    public static final class ParameterizedEqualityTests {

        /**
         * Parameters for these tests so we run each test multiple times.
         * We run with a single key or a complex key and both ascending and descending.
         */
        @Parameterized.Parameters(name = "Field:{0}")
        public static Collection<Field> data() {
            return EnumSet.allOf(Field.class);
        }

        @Parameterized.Parameter
        public Field field;

        /**
         * Tests the design docs are equal for each field in turn.
         */
        @Test
        public void testDesignDocEqualsForEachField() {
            Assert.assertEquals(getDesignDocumentWithFields(EnumSet.of(field)),
                    getDesignDocumentWithFields(EnumSet.of(field)));
        }

        /**
         * Tests the design docs are not equal when each field is empty in one of the compared docs.
         *
         * @throws Exception
         */
        @Test
        public void testDesignDocNotEqualEmpty() throws Exception {
            Assert.assertNotEquals(getDesignDocument(), getDesignDocumentWithFields(EnumSet
                    .complementOf(EnumSet.of(field))));
        }

        /**
         * Tests the design docs are not equal when each field is different in one of the
         * compared docs.
         *
         * @throws Exception
         */
        @Test
        public void testDesignDocNotEqualDifferent() throws Exception {
            Assert.assertNotEquals(getDesignDocument(), getDesignDocumentWithDifferent(field));
        }

    }

    private static void indexes(DesignDocument designDocument) {
        Map<String, Map<String, String>> indexes = new HashMap<String, Map<String, String>>();

        Map<String, String> index = new HashMap<String, String>();
        index.put("index", "function(doc){....}");
        indexes.put("animal", index);

        JsonObject indexesJson = (JsonObject) gson.toJsonTree(indexes);
        designDocument.setIndexes(indexesJson);
    }

    private static void rewriteRule(DesignDocument designDocument) {
        List<Map<String, Object>> rewrites = new ArrayList<Map<String, Object>>();
        Map<String, Object> rewrite = new HashMap<String, Object>();
        rewrite.put("from", "/");
        rewrite.put("to", "index.html");
        rewrite.put("method", "GET");
        rewrite.put("query", new HashMap<String, String>());
        rewrites.add(0, rewrite);

        JsonArray rewriteJson = (JsonArray) gson.toJsonTree(rewrites);
        designDocument.setRewrites(rewriteJson);
    }

    private static void filterFunction(DesignDocument designDocument) {
        Map<String, String> filterFunctions = new HashMap<String, String>();
        filterFunctions.put("myFilter", "function(doc,req){return false;}");
        designDocument.setFilters(filterFunctions);
    }

    private static void updateFunction(DesignDocument designDocument) {
        Map<String, String> updateFunctions = new HashMap<String, String>();
        updateFunctions.put("myUpdate", "function(doc,req){return [doc, 'Edited World!'];}");
        designDocument.setUpdates(updateFunctions);
    }

    private static void showFunction(DesignDocument designDocument) {
        Map<String, String> showFunctions = new HashMap<String, String>();
        showFunctions.put("myShow", "function(doc,req){ if(doc){return \"hello world!\"}");
        designDocument.setShows(showFunctions);
    }

    private static void listFunction(DesignDocument designDocument) {
        Map<String, String> listFunctions = new HashMap<String, String>();
        listFunctions.put("myList", "function(head,req){ send(toJson(getRow); }");
        designDocument.setLists(listFunctions);
    }

    private static void mrView(DesignDocument designDocument) {
        DesignDocument.MapReduce mapReduce = new DesignDocument.MapReduce();
        mapReduce.setMap("function(doc){emit(doc.hello);}");
        mapReduce.setReduce("_stats");
        mapReduce.setDbCopy("myOtherDB");
        Map<String, DesignDocument.MapReduce> views = new HashMap<String, DesignDocument
                .MapReduce>();
        views.put("helloWorldView", mapReduce);
        designDocument.setViews(views);
    }

    private static void validateDocUpdate(DesignDocument designDocument) {
        designDocument.setValidateDocUpdate("throw({ forbidden: 'Error message here.' });");
    }

}
