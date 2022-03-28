package com.pmart5a;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmart5a.myclass.PostApiNasa;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

public class Main {

    public static final String REMOTE_SERVICE_URI =
            "https://api.nasa.gov/planetary/apod?api_key=GNeY5adJRtkMax1LOfzLrGjqzUmT6WzL2ebQAaRA";
    public static final ObjectMapper mapper = new ObjectMapper();

    public static void writeFileToDisk(String fullFileName, byte[] buffer) {
        try (FileOutputStream fos = new FileOutputStream(fullFileName);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            bos.write(buffer, 0, buffer.length);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static boolean checkExistsFile(String fullFileName) {
        File fileOnDisk = new File(fullFileName);
        return fileOnDisk.exists();
    }

    public static String getCanonicalFileName(String fileName) {
        File fileOnDisk = new File(fileName);
        String nameCanonicalFile = "";
        try {
            nameCanonicalFile = String.valueOf(fileOnDisk.getCanonicalFile());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return nameCanonicalFile;
    }

    public static void main(String[] args) {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)
                        .setSocketTimeout(30000)
                        .setRedirectsEnabled(false)
                        .build())
                .build()) {
            CloseableHttpResponse response = httpClient.execute(new HttpGet(REMOTE_SERVICE_URI));
            PostApiNasa post = mapper.readValue(response.getEntity().getContent(), new TypeReference<>() {});
            if (!post.getFieldsUnrecognized().isEmpty()) {
                System.out.println("Нераспознанные поля и их значения:");
                for (Map.Entry<String, String> field : post.getFieldsUnrecognized().entrySet()) {
                    System.out.println(field);
                }
            }
            response = httpClient.execute(new HttpGet(post.getUrl()));
            byte[] buffer = response.getEntity().getContent().readAllBytes();
            String fullFileName = getCanonicalFileName("./" + post.getUrl().substring(post.getUrl().lastIndexOf("/")));
            writeFileToDisk(fullFileName, buffer);
            if (checkExistsFile(fullFileName)) {
                System.out.format("Файл %s записан на диск.\n", fullFileName);
            } else {
                System.out.format("Ошибка: файл %s не найден.\n", fullFileName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}