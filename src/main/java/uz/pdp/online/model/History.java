package uz.pdp.online.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import uz.pdp.online.enums.Currency;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
public class History {
    private String customer;
    private String seller;
    private String goodsName;
    private Double quantity;
    private Double price;
    private Currency currency;
    private Double summa;
    private LocalDate date;
    private LocalTime time;

}
