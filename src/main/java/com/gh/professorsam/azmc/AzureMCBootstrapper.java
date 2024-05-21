package com.gh.professorsam.azmc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class AzureMCBootstrapper {

    private static final Logger logger = LogManager.getLogger("Bootstrapper");

    public static void main(String[] args) {
        logger.info("Starting bootstrapper");
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
        //TODO implement
    }
}
