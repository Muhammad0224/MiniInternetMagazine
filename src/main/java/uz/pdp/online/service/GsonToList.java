package uz.pdp.online.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public interface GsonToList<T> {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    default List<T> converter(Reader reader){
        Type listType = new TypeToken<ArrayList<T>>(){}.getType();
        return new ArrayList<>(gson.fromJson(reader, listType));
    }
}
