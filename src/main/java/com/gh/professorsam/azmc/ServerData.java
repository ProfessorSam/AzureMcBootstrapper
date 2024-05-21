package com.gh.professorsam.azmc;

import com.azure.storage.blob.*;
import io.github.cdimascio.dotenv.Dotenv;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class ServerData {

    private static final Logger logger = LogManager.getLogger("ServerData");

    private static final String connectionString;
    private static final BlobServiceClient blobClient;
    private static final BlobContainerClient containerClient;

    static {
        Dotenv dotenv = Dotenv.load();
        connectionString = dotenv.get("BLOB_STRING");
        blobClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
        containerClient = blobClient.createBlobContainerIfNotExists("azmcdata");
    }

    public static void downloadServerData() throws RuntimeException{
        if(containerClient.createIfNotExists()){
            logger.info("No container existed for server data");
            return;
        }
        BlobClient client = containerClient.getBlobClient("data.zip");
        if(!client.exists()){
            logger.warn("No data found in storage blob");
            return;
        }
        File file = new File("/tmp/azmcserver/data.zip");
        if(file.exists()) file.delete();
        client.downloadToFile("/tmp/azmcserver/data.zip");
        logger.info("Downloaded server data");
        try (ZipFile zipFile = new ZipFile(file)){
            zipFile.extractAll("/tmp/azmcserver");
            logger.info("Successfully extracted server data");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            file.delete();
            logger.info("Successfully deleted zip file");
        }
    }

    public static void uploadServerData() throws IOException {
        File lockFile = new File("/tmp/azmcserver/world/session.lock");
        if (lockFile.exists()) lockFile.delete();
        File file = new File("/tmp/azmcserver/data.zip");
        if(file.exists()) file.delete();
        ZipFile zipFile = new ZipFile(file);
        for(File iFile : new File("/tmp/azmcserver").listFiles()){
            if(iFile.getName().equalsIgnoreCase("server.jar") || iFile.getName().equalsIgnoreCase("data.zip")){
                continue;
            }
            if(iFile.isDirectory()){
                zipFile.addFolder(iFile);
                continue;
            }
            zipFile.addFile(iFile);
        }
        zipFile.close();
        logger.info("Zipped server data");
        BlobClient client = containerClient.getBlobClient("data.zip");
        client.uploadFromFile("/tmp/azmcserver/data.zip", true);
        logger.info("Successfully uploaded server data");
    }
}
