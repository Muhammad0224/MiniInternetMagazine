package uz.pdp.online.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.online.enums.Currency;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Goods {
    private String name;
    private Double price;
    private Double quantity;
    private String seller;
    private Currency currency;
    private List<Double> marks = new ArrayList<>();
    private Double sale;

    public Goods(String name, String seller) {
        this.name = name;
        this.seller = seller;
    }

    public Double getAverageMark() {
        Double averageMark = 0D;
        if (!marks.isEmpty()) {
            for (Double mark : this.marks) {
                averageMark += mark;
            }
            return averageMark / this.marks.size();
        } else return 0D;
    }

    public Double getPriceSale() {
        return quantity * price * (1 - sale / 100);
    }
}
