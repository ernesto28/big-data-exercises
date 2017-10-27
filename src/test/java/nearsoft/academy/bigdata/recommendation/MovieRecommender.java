package nearsoft.academy.bigdata.recommendation;

import com.google.common.collect.HashBiMap;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class MovieRecommender {

    private UserBasedRecommender recommender;
    private HashBiMap<String,Integer> products;
    private HashBiMap<String,Integer> users;
    private long totalReviews;

    public MovieRecommender(String path){
        DataModel model = null;
        totalReviews = 0;
        products = HashBiMap.create();
        users = HashBiMap.create();
        try {
            GZIPInputStream input = new GZIPInputStream(new FileInputStream(path));
            BufferedReader br = new BufferedReader(new InputStreamReader(input));
            File movies = new File("src/test/testInput.txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(movies));
            writer.write("");
            String line, prod="";
            while ((line = br.readLine()) != null) {
                if(line.startsWith("product/productId:")){
                    totalReviews++;
                    if(!products.containsKey(line.split(" ")[1]))
                        products.put(line.split(" ")[1],products.size());
                    prod=products.get(line.split(" ")[1])+",";
                }else if(line.startsWith("review/userId:")){
                    if(!users.containsKey(line.split(" ")[1]))
                        users.put(line.split(" ")[1],users.size());
                    writer.append(users.get(line.split(" ")[1])+","+prod);
                }else if(line.startsWith("review/score:")){
                    writer.append(line.split(" ")[1]+"\n");
                }
            }
            writer.close();

            model = new FileDataModel(movies);
            UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
            UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
            recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TasteException e) {
            e.printStackTrace();
        }

    }

    public long getTotalReviews(){
        return totalReviews;
    }

    public long getTotalProducts(){
        return products.size();
    }

    public long getTotalUsers(){
        return users.size();
    }

    public List<String> getRecommendationsForUser(String userID){
        ArrayList<String> recommendations = new ArrayList<String>();
        try {
            if(users.containsKey(userID)){
                List<RecommendedItem> recommends = recommender.recommend(users.get(userID),3);
                for(RecommendedItem item:recommends){
                    recommendations.add(products.inverse().get((int)item.getItemID()));
                }
            }
        } catch (TasteException e) {
            e.printStackTrace();
        }
        return recommendations;
    }
}
