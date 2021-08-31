package uz.pdp.online.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Storage {
    private List<Goods> goods = new ArrayList<>();
}
