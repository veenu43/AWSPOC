package poc.s3;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

//@Ignore
public class S3Test {

    private S3Client s3;
    private KmsClient kmsClient;
    String bucketName = "demo-vinit-2021";
    String keyID = "72659a73-b613-4c2a-8b1d-5ca74c29f6d6";
    Region region = Region.AP_SOUTHEAST_1;


    @Before
    public void init(){


        /*
         * System property based client creation
         */
        /*
            System.setProperty("aws.accessKeyId","AKIAWBE54CPXCGNW4IEU");
            System.setProperty("aws.secretAccessKey","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");
            s3 = S3Client.builder()
                    .region(region)
                    .build();
            kmsClient = KmsClient.builder()
                .region(region)
                .build();
        */

        /*
         * Credentail passed to Basic credential
         */
        AwsBasicCredentials awscred =   AwsBasicCredentials.create("AKIAWBE54CPXCGNW4IEU","+b8BSTG9LH3EvLuYruH1Prbc6UDuYJqox/3YNblz");
        AwsCredentialsProvider awsCredentialsProvider = StaticCredentialsProvider.create(awscred);
        s3 = S3Client.builder().region(Region.AP_SOUTHEAST_1).credentialsProvider(awsCredentialsProvider).build();
        kmsClient = KmsClient.builder()
                .region(region).credentialsProvider(awsCredentialsProvider)
                .build();
    }

    @Test
    public void listObjectsInBuckets() {
        try {
            ListObjectsRequest listObjects = ListObjectsRequest
                    .builder()
                    .bucket(bucketName)
                    .build();

            ListObjectsResponse res = s3.listObjects(listObjects);
            List<S3Object> objects = res.contents();

            for (ListIterator iterVals = objects.listIterator(); iterVals.hasNext(); ) {
                S3Object myValue = (S3Object) iterVals.next();
                System.out.print("\n The name of the key is " + myValue.key());
                System.out.print("\n The object is " + calKb(myValue.size()) + " KBs");
                System.out.print("\n The owner is " + myValue.owner());

            }

        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }

    @Test
    public void uploadObjectsInBuckets() {
        try {
            s3.putObject(PutObjectRequest.builder().bucket(bucketName).key("testNUS")
                    .build(), RequestBody.fromByteBuffer(getRandomByteBuffer(10000)));
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Test
    public void deleteObjectsInBuckets() {
        try {
            ArrayList<ObjectIdentifier> toDelete = new ArrayList<ObjectIdentifier>();
            toDelete.add(ObjectIdentifier.builder().key("testNUS").build());
            DeleteObjectsRequest dor = DeleteObjectsRequest.builder()
                    .bucket(bucketName)
                    .delete(Delete.builder().objects(toDelete).build())
                    .build();

            s3.deleteObjects(dor);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Test
    public void uploadAndDownloadKMSEncryptedData() {
        String name = "Vinit123";

        //PUT
        byte[] encryptData = encryptData( keyID,name.getBytes());
        s3.putObject(PutObjectRequest.builder().bucket(bucketName).key("testVinitNUC")
                .build(), RequestBody.fromBytes(encryptData));


        //GET
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .key("testVinitNUC")
                .bucket(bucketName)
                .build();
        ResponseBytes<GetObjectResponse> objectBytes = s3.getObjectAsBytes(objectRequest);
        byte[] data = objectBytes.asByteArray();

        byte[] unEncryptedData = decryptData(data, keyID);
        System.out.println("Data: "+ new String(unEncryptedData));
    }
    private static long calKb(Long val) {
        return val / 1024;
    }
    private static ByteBuffer getRandomByteBuffer(int size) throws IOException {
        byte[] b = new byte[size];
        new Random().nextBytes(b);
        return ByteBuffer.wrap(b);
    }

    @After
    public void cleanUp(){
        s3.close();
        kmsClient.close();
    }

    private byte[] encryptData(String keyId, byte[] data){
        SdkBytes myBytes = SdkBytes.fromByteArray(data);
        EncryptRequest encryptRequest = EncryptRequest.builder()
                .keyId(keyID)
                .plaintext(myBytes)
                .build();
        EncryptResponse response = kmsClient.encrypt(encryptRequest);
        String algorithm = response.encryptionAlgorithm().toString();
        System.out.println("The encryption algorithm is " + algorithm);

        // Return the encrypted data
        SdkBytes encryptedData = response.ciphertextBlob();
        return encryptedData.asByteArray();
    }

    private byte[] decryptData(byte[] data, String keyId) {

        try {
            SdkBytes encryptedData = SdkBytes.fromByteArray(data);

            DecryptRequest decryptRequest = DecryptRequest.builder()
                    .ciphertextBlob(encryptedData)
                    .keyId(keyId)
                    .build();

            DecryptResponse decryptResponse = kmsClient.decrypt(decryptRequest);
            SdkBytes plainText = decryptResponse.plaintext();
            return plainText.asByteArray();

        } catch (KmsException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }
        return null;
    }
}
