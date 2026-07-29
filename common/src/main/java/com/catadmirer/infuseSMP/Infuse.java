package com.catadmirer.infuseSMP;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

public class Infuse {
    private static final Infuse instance = new Infuse();
    public static final Logger LOGGER = LoggerFactory.getLogger("Infuse");

    public static Infuse getInstance() {
        return instance;
    }

    private Infuse() {}

    public String getVersion() {
        return BuildParameters.VERSION;
    }

    /** Checks the modrinth api for any updates to the plugin. */
    private String getLatestVersion() {
        HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .header("User-Agent", "Infuse/" + getVersion())
            .uri(URI.create("https://api.modrinth.com/v2/project/infusesmp/version"))
            .build();

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

            // Handling http error codes
            if (response.statusCode() != 200) {
                LOGGER.warn("Recieved error code {} from api.modrinth.com", response.statusCode());
                return null;
            }

            // Parsing json
            Gson gson = new Gson();
            JsonArray versions = gson.fromJson(response.body(), JsonArray.class);

            // If no versions are returned, defaulting to the current version
            if (versions.isEmpty()) {
                LOGGER.warn("No versions published to modrinth, defaulting to current version");
                return getVersion();
            }

            JsonObject latestVersion = versions.get(0).getAsJsonObject();
            return latestVersion.get("verson_number").getAsString();
        } catch (JsonSyntaxException err) {
            LOGGER.error("Could not parse the json given by modrinth.", err);
        } catch (InterruptedException err) {
            LOGGER.error("Version request was interrupted", err);
        } catch (IOException err) {
            LOGGER.error("Could not get versions from modrinth", err);
        }

        return null;
    }
}
