package com.gh.professorsam.azmc;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Base64;

public class AzureMCBootstrapper {

    private static final Logger logger = LogManager.getLogger("Bootstrapper");
    private static String functionsDomain;
    private static String blobstring;

    public static void main(String[] args) {
        logger.info("Starting bootstrapper");
        if(args.length != 0){
            logger.info("Found domain and blob connection string as arguments!");
            functionsDomain = new String(Base64.getDecoder().decode(args[0]));
            blobstring = new String(Base64.getDecoder().decode(args[1]));
        } else {
            logger.info("Loading domain and blob connection string from environment!");
            functionsDomain = Dotenv.load().get("FUNCTIONS_DOMAIN");
            blobstring = Dotenv.load().get("BLOB_STRING");
        }
        try {
            ServerDownloader.downloadLatestPaperBuild();
        } catch (IOException e){
            logger.error(e);
            logger.info("Shutting down VM");
            shutdownVMFunctionCall();
            return;
        }
        logger.info("Downloading server data");
        ServerData.downloadServerData();
        logger.info("Downloading compatibility plugin");
        try {
            ServerDownloader.downloadPlugin();
        } catch (Exception e){
            logger.error(e);
            shutdownVMFunctionCall();
            return;
        }
        logger.info("Starting Minecraft Server");
        try {
            startServerAndWait();
        } catch (Exception e){
            logger.error(e);
            shutdownVMFunctionCall();
            return;
        }
        logger.info("Uploading server data");
        try {
            ServerData.uploadServerData();
        } catch (Exception e){
            logger.error(e); //FUCK!
        }
        logger.info("Request VM removal...");
        shutdownVMFunctionCall();
    }

    private static void startServerAndWait() throws Exception {
        ProcessBuilder process = new ProcessBuilder( "bash" , "-c", "java -Xmx4G -Xms4G -XX:SoftMaxHeapSize=3G -XX:+UnlockExperimentalVMOptions -XX:+UseZGC -Dcom.mojang.eula.agree=true -jar /tmp/azmcserver/server.jar --universe /tmp/azmcserver --nogui");
        process.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        process.redirectError(ProcessBuilder.Redirect.INHERIT);
        process.start().waitFor();
    }

    private static void shutdownVMFunctionCall(){
        String url = "https://" + functionsDomain + "/api/stopVM";
        OkHttpClient httpClient = new OkHttpClient();
        Request request = new Request.Builder().url(url).post(RequestBody.create(MediaType.parse("text/plain"), "stop")).build();
        httpClient.newCall(request);
    }

    public static String getBlobstring(){
        return blobstring;
    }
}
