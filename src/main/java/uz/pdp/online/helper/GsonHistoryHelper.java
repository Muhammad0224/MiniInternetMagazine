package uz.pdp.online.helper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import uz.pdp.online.model.History;
import uz.pdp.online.model.User;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GsonHistoryHelper {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public List<History> converter(Reader reader, File file) {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            if (file.length() != 0) {
                return new ArrayList<>(Arrays.asList(gson.fromJson(bufferedReader, History[].class)));
            }
            return new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
