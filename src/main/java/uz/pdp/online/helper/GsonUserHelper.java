package uz.pdp.online.helper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import uz.pdp.online.model.Goods;
import uz.pdp.online.model.User;
import uz.pdp.online.service.GsonToList;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GsonUserHelper {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public List<User> converter(Reader reader, File file) {
//        Type listType = new TypeToken<ArrayList<T>>(){}.getType();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            return new ArrayList<>(Arrays.asList(gson.fromJson(bufferedReader, User[].class)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
