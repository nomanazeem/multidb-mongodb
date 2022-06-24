package com.nazeem.multidb.mongodb.util;

import com.nazeem.multidb.mongodb.feeds.AvantLinkReturnsFeed;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.tomcat.util.http.fileupload.FileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class FTPUpload {
    private static final Logger logger = Logger.getLogger(FileUpload.class.getName());

    public boolean upload(FTPSettings ftpSettings, File file) {
        boolean completed = false;
        //todo

        String server = ftpSettings.getServer();
        int port = ftpSettings.getPort();
        String user = ftpSettings.getUsername();
        String pass = ftpSettings.getPassword();
        String directory = ftpSettings.getDirectory();

        FTPClient ftpClient = new FTPClient();
        try {

            ftpClient.connect(server, port);
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            String firstRemoteFile = directory +"/"+ file.getName();
            InputStream inputStream = new FileInputStream(file);

            logger.info("Start uploading file....");

            completed = ftpClient.storeFile(firstRemoteFile, inputStream);
            inputStream.close();

            if (completed) {
                logger.info("The file is uploaded successfully.");
            }
        } catch (IOException ex) {
            //System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();

            logger.log(Level.SEVERE, ex.getStackTrace().toString());
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                logger.log(Level.SEVERE, ex.getStackTrace().toString());
            }
        }

        if (!completed) logger.info("There were some error while ftp file...");

        return completed;
    }
}
