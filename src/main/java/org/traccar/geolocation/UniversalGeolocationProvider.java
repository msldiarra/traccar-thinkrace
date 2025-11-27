/*
 * Copyright 2016 - 2022 Anton Tananaev (anton@traccar.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.geolocation;

import org.traccar.model.Network;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.InvocationCallback;
import jakarta.ws.rs.core.MediaType;

public class UniversalGeolocationProvider implements GeolocationProvider {

    private final Client client;
    private final String url;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public UniversalGeolocationProvider(Client client, String url, String key) {
        this.client = client;
        this.url = url + "?key=" + key;
    }

    @Override
    public void getLocation(Network network, final LocationProviderCallback callback) {
        try {
            String jsonBody = MAPPER.writeValueAsString(network);
            System.err.println("[Geolocation] URL: " + url);
            System.err.println("[Geolocation] Body: " + jsonBody);

            //client.target(url).request().async().post(Entity.json(network), new InvocationCallback<JsonObject>() {
            client.target(url)
            .request()
            .async()
            .post(Entity.entity(jsonBody, MediaType.APPLICATION_JSON), new InvocationCallback<JsonObject>() {
                @Override
                public void completed(JsonObject json) {
                    System.err.println("[Geolocation] Response: " + json);
                    if (json.containsKey("error")) {
                        callback.onFailure(new GeolocationException(json.getJsonObject("error").getString("message")));
                    } else {
                        JsonObject location = json.getJsonObject("location");
                        double lat = location.getJsonNumber("lat").doubleValue();
                        double lng = location.getJsonNumber("lng").doubleValue();
                        double accuracy = json.getJsonNumber("accuracy").doubleValue();
                        System.err.println("[Geolocation] Parsed: lat=" + lat + ", lng=" + lng);
                        callback.onSuccess(lat, lng, accuracy);
                    }
                }

                @Override
                public void failed(Throwable throwable) {
                    callback.onFailure(throwable);
                }
            });
        } catch (Exception e) {
            System.err.println("[Geolocation] Error: " + e.getMessage());
            callback.onFailure(new GeolocationException(e.getMessage()));
        }
    }

}
