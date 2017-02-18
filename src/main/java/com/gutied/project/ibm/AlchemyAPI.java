package com.gutied.project.ibm;

import com.ibm.watson.developer_cloud.alchemy.v1.AlchemyLanguage;
import com.ibm.watson.developer_cloud.alchemy.v1.model.DocumentSentiment;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class AlchemyAPI {

    private AlchemyLanguage al = new AlchemyLanguage();

    public AlchemyAPI() throws IOException {
//        String env = System.getenv("VCAP_SERVICES");
//        if (env == null){
//            env = Utils.readFileToString(new File
// ("D:\\Users\\david\\Project\\alchemy\\target\\classes\\vcap_services.json"));
//        }
//
//        CredentialUtils.setServices(env);
//        al.setApiKey(CredentialUtils.getAPIKey("alchemy_api"));
        al.setApiKey("f0ca89811c65e12d7adb5535bb5e86f8d949746a");
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        AlchemyAPI alchemyAPI = new AlchemyAPI();
        alchemyAPI.analyseSentence("I don't like this at all");

    }


    public void analyseSentence(String sentence) {
        Map<String, Object> params = new HashMap<>();
        params.put(AlchemyLanguage.TEXT, sentence);
        DocumentSentiment sentiment = al.getSentiment(params).execute();
        System.out.println(sentiment);
    }


}
