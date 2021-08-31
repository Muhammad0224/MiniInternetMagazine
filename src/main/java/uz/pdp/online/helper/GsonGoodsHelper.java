package uz.pdp.online.helper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import uz.pdp.online.model.Goods;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GsonGoodsHelper {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public List<Goods> converter(Reader reader, File file) {

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            return new ArrayList<>(Arrays.asList(gson.fromJson(bufferedReader, Goods[].class)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
