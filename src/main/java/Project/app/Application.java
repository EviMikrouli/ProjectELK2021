package Project.app;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.json.JSONException;

import Project.tools.*;

import java.io.IOException;

import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.Objects;
import java.util.Scanner;


@SpringBootApplication

public class Application {
    private static String[] _args;

    public static void main(String[] args)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, NoSuchProviderException {

        SpringApplication.run(Application.class, args);

        ESManager EsManager = new ESManager();
        RestHighLevelClient HighClient = EsManager.CreateHighLevelClient();
        RestClient LowLevelClient = EsManager.CreateLowLevelClient(HighClient);
        InfoService infoService = new InfoService(HighClient);
        IndexService indexService = new IndexService(HighClient);
        System.out.println(ConsoleColors.BLUE_BRIGHT + "Number of Indexes    ----> " + ConsoleColors.RESET
                + infoService.CountIndexes());

        String request = "filebeat-7.13.4-2021.10.11-000001";

        try
        {
            System.out.println(ConsoleColors.BLUE_BRIGHT + "(our beat:) " + ConsoleColors.RESET
                    + infoService.GetIndexName(request));

            System.out.print("\n");
        }
        catch(IOException e1)
        {
            e1.printStackTrace();
        }
        catch(JSONException e2)
        {
            e2.printStackTrace();
        }

        System.out.print("\n");
        System.out.println(ConsoleColors.RED_BRIGHT + "Let the search begin!! " + ConsoleColors.RESET);


        boolean moreSearches = false;
        Scanner userInput = new Scanner(System.in);

        String fieldname;
        String searchvalue;
        String userReply;

        System.out.println(ConsoleColors.BLUE_BRIGHT + "Insert the index name of the beat you are working: " + ConsoleColors.RESET
                + userInput.nextLine());

        String request1= userInput.nextLine();

        SearchRequest searchRequest = new SearchRequest(request);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        if(Objects.equals(request1, request)){
            do {

            System.out.println(ConsoleColors.BLUE_BRIGHT + "Insert the Field you want to specify: " + ConsoleColors.CYAN);
            fieldname = userInput.nextLine();

            do{
                System.out.println(ConsoleColors.BLUE_BRIGHT + "Insert the value of the field: "
                        + ConsoleColors.CYAN);

                searchvalue = userInput.nextLine();
            }while(searchvalue.length() == 0);


            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder(fieldname, searchvalue);



            searchSourceBuilder.query(matchQueryBuilder);

            searchRequest.source(searchSourceBuilder);

            searchSourceBuilder.sort("@timestamp", SortOrder.ASC);

            searchSourceBuilder.size(5);

            searchRequest.source(searchSourceBuilder);



            try {

                SearchResponse searchResponse = HighClient.search(searchRequest, RequestOptions.DEFAULT);

                SearchHit[] values = searchResponse.getHits().getHits();

                if (values.length > 0) {
                    int i = 0;
                    for (SearchHit s : values) {
                        System.out.println(ConsoleColors.RED_BRIGHT + " [ " + i + " ]" + ConsoleColors.RESET);
                        System.out.println(s.getSourceAsString());
                        System.out.println();
                        i++;
                    }
                } else {
                    System.out.println(ConsoleColors.PURPLE + "No logs found for your request!" + ConsoleColors.RESET);



                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println(ConsoleColors.CYAN_BRIGHT + "Search again? Y/N" + ConsoleColors.RESET);
            userReply = userInput.nextLine();

            if(userReply.equalsIgnoreCase("Y") || userReply.contains("Y"))
                moreSearches = true;
            else {
                moreSearches = false;
                System.out.println(ConsoleColors.CYAN_BRIGHT + "Thanks for searching!" + ConsoleColors.RESET);
            }


          }while(moreSearches);

        }else{
            System.out.println(ConsoleColors.PURPLE + "There is no such beat.Please check for typos" + ConsoleColors.RESET);
        }

    }

}
