package com.gh.professorsam.azmc;

import com.squareup.okhttp.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ServerDownloader {
    private static final Logger logger = LogManager.getLogger("ServerJar");
    private static final String API_VERSIONS_URL = "https://api.papermc.io/v2/projects/paper";
    private static final OkHttpClient client = new OkHttpClient();

    public static void downloadLatestPaperBuild() throws IOException {
        logger.info("Downloading latest paper build...");
        Request requestMcVersion = new Request.Builder().url(API_VERSIONS_URL).build();
        String latestMCVersion;
        long latestBuildNumber;
        String jarName;
        try {
            Response response = client.newCall(requestMcVersion).execute();
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(response.body().string());
            JSONArray versions = (JSONArray) jsonObject.get("versions");
            latestMCVersion = (String) versions.get(versions.size() - 1);
            logger.info("Found latest MC version: {}", latestMCVersion);
        } catch (Exception e){
            logger.error(e);
            throw new RuntimeException(e);
        }
        Request requestBuildNumber = new Request.Builder().url("https://api.papermc.io/v2/projects/paper/versions/" + latestMCVersion + "/builds").build();
        try {
            Response response = client.newCall(requestBuildNumber).execute();
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(response.body().string());
            JSONArray jsonArray = (JSONArray) jsonObject.get("builds");
            JSONObject buildsObject = (JSONObject) jsonArray.get(jsonArray.size() - 1);
            latestBuildNumber = (Long) buildsObject.get("build");
            logger.info("Found latest build number: {}", latestBuildNumber);
            JSONObject applicationObject = (JSONObject) ((JSONObject) buildsObject.get("downloads")).get("application");
            jarName = (String) applicationObject.get("name");
            logger.info("Found jar: {}", jarName);
        } catch (Exception e){
            logger.error(e);
            throw new RuntimeException(e);
        }
        String downloadURL = "https://api.papermc.io/v2/projects/paper/versions/" + latestMCVersion + "/builds/" + latestBuildNumber + "/downloads/" + jarName;
        logger.info("URL: {}", downloadURL);
        Request request = new Request.Builder().url(downloadURL).build();
        try {
            Response response = client.newCall(request).execute();
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IllegalStateException("Response doesn't contain a file");
            }
            File file = new File("/tmp/azmcserver/");
            if(!file.exists()){
                file.mkdirs();
            }
            InputStream inputStream = responseBody.byteStream();
            FileOutputStream outputStream = new FileOutputStream("/tmp/azmcserver/server.jar");
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            outputStream.close();
        } catch (IOException e){
            logger.error(e);
            throw new RuntimeException(e);
        }
        logger.info("Downloaded latest build {} successfully!", latestBuildNumber);
    }

    public static void downloadPlugin(){
        File pluginsDir = new File("/tmp/azmcserver/plugins");
        if(!pluginsDir.exists()){
            pluginsDir.mkdirs();
        }
        String url = "https://github.com/ProfessorSam/AzureMCPlugin/releases/download/v1.0.0/AzureMcPlugin-1.0.0.jar";
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            client.setFollowRedirects(true);
            Response response = client.newCall(request).execute();
            FileOutputStream outputStream = new FileOutputStream("/tmp/azmcserver/plugins/AzureMCPlugin-1.0.0.jar");
            InputStream inputStream = response.body().byteStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            outputStream.close();
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
