package fr.polytech.service;

import io.minio.*;
import io.minio.errors.MinioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Service to interact with Minio.
 */
@Service
public class MinioService {

    private final Logger logger = LoggerFactory.getLogger(MinioService.class);

    // Initialize minioClient with MinIO server.
    private final MinioClient minioClient = MinioClient.builder()
            .endpoint(System.getenv("MINIO_LOCAL_URI"))
            .credentials(System.getenv("MINIO_USER"), System.getenv("MINIO_PASSWORD"))
            .region(System.getenv("MINIO_REGION"))
            .build();

    /**
     * Create a public bucket in Minio.
     *
     * @param bucketName: The name of the bucket.
     * @throws MinioException           if an error occurs.
     * @throws IOException              if an I/O error occurs.
     * @throws NoSuchAlgorithmException if an algorithm is not available.
     * @throws InvalidKeyException      if the key is invalid.
     */
    private void createPublicBucket(String bucketName) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {

        // Define the bucket policy.
        String config = "{\n" +
                "    \"Statement\": [\n" +
                "        {\n" +
                "            \"Action\": [\n" +
                "                \"s3:GetBucketLocation\",\n" +
                "                \"s3:ListBucket\"\n" +
                "            ],\n" +
                "            \"Effect\": \"Allow\",\n" +
                "            \"Principal\": \"*\",\n" +
                "            \"Resource\": \"arn:aws:s3:::" + bucketName + "\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"Action\": \"s3:GetObject\",\n" +
                "            \"Effect\": \"Allow\",\n" +
                "            \"Principal\": \"*\",\n" +
                "            \"Resource\": \"arn:aws:s3:::" + bucketName + "/*\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"Version\": \"2012-10-17\"\n" +
                "}";

        createBucket(bucketName, config);
    }

    /**
     * Create a private bucket in Minio.
     *
     * @param bucketName: The name of the bucket.
     * @throws MinioException           if an error occurs.
     * @throws IOException              if an I/O error occurs.
     * @throws NoSuchAlgorithmException if an algorithm is not available.
     * @throws InvalidKeyException      if the key is invalid.
     */
    private void createPrivateBucket(String bucketName) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {

        String config = "{\n" +
                "    \"Statement\": [\n" +
                "        {\n" +
                "            \"Action\": [\n" +
                "                \"s3:GetBucketLocation\",\n" +
                "                \"s3:ListBucket\"\n" +
                "            ],\n" +
                "            \"Effect\": \"Allow\",\n" +
                "            \"Principal\": {\n" +
                "                \"AWS\": \"arn:aws:iam::user:root\"\n" +
                "            },\n" +
                "            \"Resource\": \"arn:aws:s3:::" + bucketName + "\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"Action\": \"s3:GetObject\",\n" +
                "            \"Effect\": \"Allow\",\n" +
                "            \"Principal\": {\n" +
                "                \"AWS\": \"arn:aws:iam::user:root\"\n" +
                "            },\n" +
                "            \"Resource\": \"arn:aws:s3:::" + bucketName + "/*\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"Version\": \"2012-10-17\"\n" +
                "}";

        createBucket(bucketName, config);
    }


    /**
     * Create a bucket in Minio.
     *
     * @param bucketName: The name of the bucket.
     * @param config:     The configuration of the bucket.
     * @throws MinioException           if an error occurs.
     * @throws IOException              if an I/O error occurs.
     * @throws NoSuchAlgorithmException if an algorithm is not available.
     * @throws InvalidKeyException      if the key is invalid.
     */
    private void createBucket(String bucketName, String config) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {

        // Create a new bucket.
        minioClient.makeBucket(
                MakeBucketArgs
                        .builder()
                        .bucket(bucketName)
                        .region(System.getenv("MINIO_REGION"))
                        .build()
        );

        // Setting the bucket policy.
        minioClient.setBucketPolicy(
                SetBucketPolicyArgs
                        .builder()
                        .bucket(bucketName)
                        .config(config)
                        .region(System.getenv("MINIO_REGION"))
                        .build()
        );
    }


    /**
     * Check if a bucket exists.
     *
     * @param bucketName: The name of the bucket.
     * @return True if the bucket exists, false otherwise.
     * @throws MinioException           if an error occurs.
     * @throws IOException              if an I/O error occurs.
     * @throws NoSuchAlgorithmException if an algorithm is not available.
     * @throws InvalidKeyException      if the key is invalid.
     */
    private boolean bucketExists(String bucketName) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
    }

    /**
     * Create a bucket if it does not exist.
     *
     * @param bucketName: The name of the bucket.
     * @param isPublic:   True if the bucket should be public, false otherwise.
     * @throws MinioException           if an error occurs.
     * @throws IOException              if an I/O error occurs.
     * @throws NoSuchAlgorithmException if an algorithm is not available.
     * @throws InvalidKeyException      if the key is invalid.
     */
    private void createBucketIfNotExists(String bucketName, boolean isPublic) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        if (!bucketExists(bucketName)) {
            if (isPublic) {
                createPublicBucket(bucketName);
            } else {
                createPrivateBucket(bucketName);
            }
        }
    }

    /**
     * Upload a file to Minio.
     *
     * @param bucketName:    The name of the bucket.
     * @param objectName:    The name of the object.
     * @param multipartFile: The file to upload.
     * @throws IOException              If an I/O error occurs.
     * @throws NoSuchAlgorithmException If the algorithm SHA-256 is not available.
     * @throws InvalidKeyException      If the key is invalid.
     */
    public void uploadFile(String bucketName, String objectName, MultipartFile multipartFile, boolean isPublicFile) throws IOException, NoSuchAlgorithmException, InvalidKeyException, MinioException {
        logger.info("Starting the upload of a file to Minio");

        createBucketIfNotExists(bucketName, isPublicFile);

        // Get the input stream.
        InputStream fileInputStream = multipartFile.getInputStream();

        // Upload the file to the bucket with putObject.
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .contentType(multipartFile.getContentType())
                        .stream(fileInputStream, fileInputStream.available(), -1)
                        .build());

        // Close the file stream.
        fileInputStream.close();

        logger.info("Completed the upload of a file to Minio");
    }

    /**
     * Delete a file from a bucket.
     *
     * @param bucketName: The name of the bucket.
     * @param objectName: The name of the object.
     * @throws MinioException           if an error occurs.
     * @throws IOException              if an I/O error occurs.
     * @throws NoSuchAlgorithmException if an algorithm is not available.
     * @throws InvalidKeyException      if the key is invalid.
     * @throws HttpClientErrorException if the bucket or the object is not found.
     */
    public void deleteFileFromBucket(String bucketName, String objectName) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException, HttpClientErrorException {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build()) || minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        ) != null;
        if (!found) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Bucket or object not found");
        }

        minioClient.removeObject(
                RemoveObjectArgs
                        .builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build());
    }
}
