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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;


public class GoogleGeolocationProvider extends UniversalGeolocationProvider {

    private static final String URL = "https://www.googleapis.com/geolocation/v1/geolocate";
    private final Client client;
    private final String url;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public GoogleGeolocationProvider(Client client, String key) {
        super(client, URL, key);
        this.client = client;
        this.url = URL + "?key=" + key;
    }

    @Override
    public void getLocation(Network network, final LocationProviderCallback callback) {
        new Thread(() -> {
            try {
                network.setConsiderIp(false);
                /**String jsonBody = MAPPER.writeValueAsString(network);
                if (jsonBody.startsWith("{")) {
                    jsonBody = "{\"considerip\":false," + jsonBody.substring(1);
                }**/

                String jsonBody = MAPPER.writeValueAsString(network);
                System.err.println("[Geolocation] URL: " + url);
                System.err.println("[Geolocation] Body: " + jsonBody);

                URL urlObj = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                }

                int responseCode = conn.getResponseCode();
                System.err.println("[Geolocation] Response code: " + responseCode);

                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }

                    String responseBody = response.toString();
                    System.err.println("[Geolocation] Response: " + responseBody);

                    JsonObject json = jakarta.json.Json.createReader(
                            new java.io.StringReader(responseBody)).readObject();

                    if (json.containsKey("error")) {
                        callback.onFailure(new GeolocationException(
                                json.getJsonObject("error").getString("message")));
                    } else {
                        JsonObject location = json.getJsonObject("location");
                        double lat = location.getJsonNumber("lat").doubleValue();
                        double lng = location.getJsonNumber("lng").doubleValue();
                        double accuracy = json.getJsonNumber("accuracy").doubleValue();
                        System.err.println("[Geolocation] Success: lat=" + lat + ", lng=" + lng);
                        callback.onSuccess(lat, lng, accuracy);
                    }
                }
            } catch (Exception e) {
                System.err.println("[Geolocation] Error: " + e.getMessage());
                e.printStackTrace();
                callback.onFailure(e);
            }
        }).start();
    }
}
