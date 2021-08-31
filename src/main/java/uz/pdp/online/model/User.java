package uz.pdp.online.model;

import lombok.Data;
import uz.pdp.online.enums.UserType;

import java.util.ArrayList;
import java.util.List;

@Data
public class User {
    private String login;
    private String password;
    private String id;
    private UserType type;
    private Storage myStorage;  //ombor
    private Storage basket;  // korzinka
    private Storage myGoods;  // sotib olingan tovarlar
    private Storage soldGoods; // sotilgan tovarlar
    private List<Card> cards = new ArrayList<>();

    public User(String login, String password, String id, UserType type) {
        this.login = login;
        this.password = password;
        this.id = id;
        this.type = type;
    }

    public Double getUserRate() {
        Double userRate = 0D;
        if (!soldGoods.getGoods().isEmpty()) {
            for (Goods good : soldGoods.getGoods()) {
                userRate += good.getAverageMark();
            }
            return userRate / soldGoods.getGoods().size();
        }else return 0D;
    }
}
