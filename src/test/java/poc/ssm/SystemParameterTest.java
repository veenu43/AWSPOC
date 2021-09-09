package poc.ssm;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;

@Ignore
public class SystemParameterTest {
    SsmClient ssmClient;
    Region region = Region.AP_SOUTHEAST_1;

    @Before
    public void setUp(){
        AwsBasicCredentials awscred =   AwsBasicCredentials.create("AKIAWBE54CPXCGNW4IEU","+b8BSTG9LH3EvLuYruH1Prbc6UDuYJqox/3YNblz");
        AwsCredentialsProvider awsCredentialsProvider = StaticCredentialsProvider.create(awscred);
        ssmClient = SsmClient.builder().region(region).credentialsProvider(awsCredentialsProvider).build();
    }

   // @Test
    public void getParameter(){
        GetParameterRequest parameterRequest = GetParameterRequest.builder()
                .name("Vinit")
                .build();

        GetParameterResponse parameterResponse = ssmClient.getParameter(parameterRequest);
        System.out.println("The parameter Vinit value is "+parameterResponse.parameter().value());
    }

    @Test
    public void fetchParameter(){
        GetParameterRequest parameterRequest = GetParameterRequest.builder()
                .name("Vinit")
                .build();

        GetParameterResponse parameterResponse = ssmClient.getParameter(parameterRequest);
        System.out.println("The parameter Vinit value is "+parameterResponse.parameter().value());
    }

}
