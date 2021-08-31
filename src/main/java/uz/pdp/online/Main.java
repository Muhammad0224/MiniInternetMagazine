package uz.pdp.online;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import uz.pdp.online.enums.CardType;
import uz.pdp.online.enums.Currency;
import uz.pdp.online.enums.UserType;
import uz.pdp.online.exceptions.CardTypeNotMatch;
import uz.pdp.online.exceptions.ConfirmException;
import uz.pdp.online.exceptions.Incompatible;
import uz.pdp.online.exceptions.Invalid;
import uz.pdp.online.exceptions.NotEnough;
import uz.pdp.online.helper.GsonGoodsHelper;
import uz.pdp.online.helper.GsonHistoryHelper;
import uz.pdp.online.helper.GsonUserHelper;
import uz.pdp.online.model.Card;
import uz.pdp.online.model.Goods;
import uz.pdp.online.model.History;
import uz.pdp.online.model.Storage;
import uz.pdp.online.model.User;
import uz.pdp.online.service.SwitchService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

// todo sale for goods (in add)

public class Main {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    GsonUserHelper gsonUserHelper = new GsonUserHelper();
    GsonGoodsHelper gsonGoodsHelper = new GsonGoodsHelper();
    GsonHistoryHelper gsonHistoryHelper = new GsonHistoryHelper();
    LocalDateTime localDateTime;

    File usersFile = new File("src/main/resources/users.txt");
    File logFile = new File("src/main/resources/logs.txt");
    File historyFile = new File("src/main/resources/history.txt");
    File goodsFile = new File("src/main/resources/goods.txt");

    User user = null;

    List<User> users = new ArrayList<>();
    List<Goods> allGoods = new ArrayList<>();
    List<Goods> activeGoods = new ArrayList<>();
    List<Goods> soldGoods = new ArrayList<>();
    List<Goods> basketGoods = new ArrayList<>();
    List<Goods> boughtGoods = new ArrayList<>();
    List<History> histories = new ArrayList<>();

    {
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!historyFile.exists()) {
            try {
                historyFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!goodsFile.exists()) {
            try {
                goodsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    {
        if (!usersFile.exists()) {
            try {
                usersFile.createNewFile();

                User adminMain = new User("Admin777", "7770224", "7777777", UserType.ADMIN_MAIN);
                adminMain.setBasket(new Storage());
                adminMain.setMyGoods(new Storage());
                adminMain.setMyStorage(new Storage());
                adminMain.setSoldGoods(new Storage());
                List<Card> cards = new ArrayList<>();
                Card card = new Card("8600050622143651", CardType.UZCARD, 1500000D, 1);
                card.setMain(true);
                cards.add(card);
                adminMain.setCards(cards);
                users.add(adminMain);
                writeUser(users);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private final Scanner scannerS = new Scanner(System.in);
    private final Scanner scannerI = new Scanner(System.in);
    private final Scanner scannerD = new Scanner(System.in);

    public static void main(String[] args) {
        Main main = new Main();
        main.start();
    }

    public void start() {
        users = getUsersList();

        System.out.println("\nSign in -> 1");
        System.out.println("Sign up -> 2");
        switch (SwitchService.operator(scannerI.nextInt(), 2)) {
            case 1:
                signIn();
                break;
            case 2:
                signUp();
                start();
                break;
        }
        start();
    }

    public void signIn() {
        users = getUsersList();
        user = null;
        while (user == null) {
            System.out.println("\nEnter the login: ");
            String login = scannerS.nextLine();
            user = findUser(login);
            if (user == null) {
                System.err.println("Login incompatible");
                writeLog(new Incompatible("Login incompatible").getMessage());
            }
        }
        System.out.println("\nEnter the password: ");
        String password = scannerS.nextLine();

        if (!user.getPassword().equals(password)) {
            System.err.println("Password incompatible");
            writeLog(new Incompatible("Password incompatible").getMessage());
            signIn();
        } else if (user.getType().equals(UserType.ADMIN_MAIN)) {
            adminMainPanel();
        } else if (user.getType().equals(UserType.ADMIN)) {
            adminPanel();
        } else userPanel();
    }

    public void signUp() {
        users = getUsersList();
        System.out.println("\nCreate login: ");
        String login = scannerS.nextLine();
        if (!checkLogin(login)) {
            System.out.println("\nCreate password: ");
            String password = scannerS.nextLine();
            if (!checkPassword(password)) {
                System.out.println("\nPassword should at least 8 character!!!");

            } else {
                System.out.println("\nConfirm password: ");
                String confirm = scannerS.nextLine();

                if (password.equals(confirm)) {
                    User user = new User(login, password, generatorId(), UserType.USER);
                    user.setBasket(new Storage());
                    user.setMyGoods(new Storage());
                    user.setMyStorage(new Storage());
                    user.setSoldGoods(new Storage());

                    System.out.println("\nThe process has successfully finished!!!");
                    users.add(user);
                    writeUser(users);
                } else {
                    System.err.println("\nThe confirmation is not the same as the password");
                    writeLog(new ConfirmException().getMessage());
                }
            }
        } else {
            System.out.println("\nThis login is used");
            signUp();
        }
    }


    //todo Panels
    public void adminMainPanel() {
        user = getUser();
        System.out.println("\nOperations with users -> 1");
        System.out.println("Operations with my storage -> 2");
        System.out.println("Operations my basket -> 3");
        System.out.println("About my goods -> 4");
        System.out.println("Operation with cards -> 5");
        System.out.println("Reports -> 6");
        System.out.println("\n\nBack -> 0");

        switch (SwitchService.operator(scannerI.nextInt(), 6)) {
            case 1:
                operationUsers();
                break;
            case 2:
                myStorage();
                break;
            case 3:
                operationBasket();
                break;
            case 4:
                aboutStorage(user.getMyGoods());
                break;
            case 5:
                operationCards();
                break;
            case 6:
                reports();
                break;
            case 0:
                start();
        }
        adminMainPanel();
    }

    public void adminPanel() {
        user = getUser();
        System.out.println("\nOperations with my storage -> 1");
        System.out.println("Operations my basket -> 2");
        System.out.println("Operations with my cards -> 3");
        System.out.println("About my goods -> 4");
        System.out.println("\n\nBack -> 0");

        switch (SwitchService.operator(scannerI.nextInt(), 4)) {
            case 1:
                myStorage();
                break;
            case 2:
                operationBasket();
                break;
            case 3:
                operationCards();
                break;
            case 4:
                aboutStorage(user.getMyGoods());
                break;
            case 0:
                start();
        }
        adminPanel();
    }

    public void userPanel() {
        user = getUser();
        System.out.println("\nOperations with basket -> 1");
        System.out.println("About my goods -> 2");
        System.out.println("Operation with cards -> 3");
        System.out.println("\n\nBack -> 0");

        switch (SwitchService.operator(scannerI.nextInt(), 3)) {
            case 1:
                operationBasket();
                break;
            case 2:
                aboutStorage(user.getMyGoods());
                break;
            case 3:
                operationCards();
                break;
            case 0:
                start();
        }
        userPanel();
    }


    //todo General methods (admins && user)
    public void operationBasket() {
        user = getUser();
        Storage myBasket = user.getBasket();

        System.out.println("\nChoose goods -> 1");
        System.out.println("Buy the goods -> 2");
        System.out.println("Delete the goods -> 3");
        System.out.println("Info -> 4");
        System.out.println("\n\nBack -> 0");

        switch (SwitchService.operator(scannerI.nextInt(), 4)) {
            case 1:
                chooseGoods(myBasket);
                break;
            case 2:
                buyGoods(myBasket);
                break;
            case 3:
                deleteGoodsBasket(myBasket);
                break;
            case 4:
                info(myBasket);
                break;
            case -1:
                operationBasket();
                break;
            case 0:
                user.setBasket(myBasket);
                if (user.getType().equals(UserType.ADMIN_MAIN)) {
                    adminMainPanel();
                } else if (user.getType().equals(UserType.ADMIN)) {
                    adminPanel();
                } else userPanel();
        }
    }

    public void aboutStorage(Storage storage) {
        if (!storage.getGoods().isEmpty()) {
            storage.getGoods().forEach(goods -> System.out.println("\n" + goods.getName() + ": \n" +
                    "\tQuantity: " + goods.getQuantity() + "\n\tPrice: " + goods.getPrice() + " " +
                    goods.getCurrency().getDescription() + "\n\tSale: " + goods.getSale() +
                    "\n\tAverage mark: " + goods.getAverageMark()));
        } else System.out.println("\nStorage is empty!!!");
    }

    public void operationCards() {
        user = getUser();
        System.out.println("\nAdd new card -> 1");
        System.out.println("Change main card -> 2");
        System.out.println("Replenish the account -> 3");
        System.out.println("Delete card -> 4");
        System.out.println("Info -> 5");
        System.out.println("\n\nBack -> 0");

        switch (SwitchService.operator(scannerI.nextInt(), 5)) {
            case 1:
                addNewCard();
                break;
            case 2:
                changeMainCard();
                break;
            case 3:
                replenish();
                break;
            case 4:
                deleteCard();
                break;
            case 5:
                infoCards();
                break;
            case 0:
                if (user.getType().equals(UserType.ADMIN_MAIN)) {
                    adminMainPanel();
                } else if (user.getType().equals(UserType.ADMIN)) {
                    adminPanel();
                } else userPanel();
                break;
        }
        operationCards();
    }

    public void infoCards() {
        user = getUser();
        List<Card> cards = user.getCards();
        if (!cards.isEmpty()) {
            System.out.println();
            for (Card card : cards) {
                if (card.isMain()) {
                    System.out.println("Main card: Card Id: " + card.getCardId() + "\nCard Type: " + card.getCardType() + "\nBalance: " + card.getBalance());
                }
            }
            System.out.println();
            for (Card card : cards) {
                if (!card.isMain()) {
                    System.out.println("Card Id: " + card.getCardId() + "\nCard Type: " + card.getCardType() + "\nBalance: " + card.getBalance());
                }
            }
        } else {
            System.out.println("\nThere is no cards");
        }
    }


    //todo operations with basket
    public void chooseGoods(Storage myBasket) {
        activeGoods = getActiveGoods();
        user = getUser();
        List<Goods> myBasketGoods = myBasket.getGoods();

        List<Goods> list = new ArrayList<>(activeGoods);
        list.removeIf(goods1 -> goods1.getSeller().equals(user.getLogin()));
        if (!list.isEmpty()) {
            showActiveGoods(list);

            Goods chosenGood = null;

            while (chosenGood == null) {
                System.out.println("\nSelect the goods: ");
                String chosenGoodsName = scannerS.nextLine();
                for (Goods allGood : list) {
                    if (allGood.getName().equalsIgnoreCase(chosenGoodsName)) {
                        chosenGood = allGood;
                    }
                }
            }

            showGoodsWithSeller(chosenGood, activeGoods);
            List<User> sellers = getGoodsWithSeller(chosenGood);
            User seller = null;
            while (seller == null) {
                System.out.println("\nSelect the seller: ");
                String sellerName = scannerS.nextLine();
                for (User seller1 : sellers) {
                    if (seller1.getLogin().equalsIgnoreCase(sellerName)) {
                        chosenGood.setSeller(seller1.getLogin());
                        seller = seller1;
                    }
                }
                for (Goods good : seller.getMyStorage().getGoods()) {
                    if (good.getName().equalsIgnoreCase(chosenGood.getName())) {
                        chosenGood.setCurrency(good.getCurrency());
                        chosenGood.setSale(good.getSale());
                    }
                }
            }
            List<Goods> sellersStorageGoods = seller.getMyStorage().getGoods();
            Goods sellerGoods = findGoodsByName(sellersStorageGoods, chosenGood.getName());

            Double chosenQuantity = null;

            while (chosenQuantity == null) {
                System.out.println("\nEnter the quantity: ");
                chosenQuantity = scannerD.nextDouble();

                if (chosenQuantity > 0) {
                    if (chosenQuantity <= sellerGoods.getQuantity()) {
                        chosenGood.setQuantity(chosenQuantity);
                    } else {
                        chosenQuantity = null;
                        System.err.println("\nThere is no product in this quantity in seller's storage");
                        writeLog(new NotEnough("There is no product in this quantity in seller's storage").getMessage());
                    }
                } else {
                    System.err.println("\nInvalid number");
                    writeLog(new Invalid("Invalid number").getMessage());
                }
            }
            //todo add to my basket
            boolean has = false;
            for (Goods myBasketGood : myBasketGoods) {
                if (myBasketGood.getName().equalsIgnoreCase(chosenGood.getName()) && myBasketGood.getSeller().equalsIgnoreCase(chosenGood.getSeller())) {
                    myBasketGood.setQuantity(myBasketGood.getQuantity() + chosenGood.getQuantity());
                    has = true;
                }
            }
            if (!has) {
                myBasketGoods.add(chosenGood);
                myBasket.setGoods(myBasketGoods);
            }
            System.out.println("\n" + chosenGood.getName() + " is added to your basket!!!");

            //todo remove from seller
            for (Goods sellersStorageGood : sellersStorageGoods) {
                if (sellersStorageGood.getName().equalsIgnoreCase(chosenGood.getName())) {
                    sellersStorageGood.setQuantity(sellersStorageGood.getQuantity() - chosenQuantity);
                }
            }

            users = getUsersList();
            for (User user1 : users) {
                if (user1.getLogin().equals(user.getLogin())) {
                    user1.setBasket(myBasket);
                }
                if (user1.getLogin().equals(seller.getLogin())) {
                    user1.getMyStorage().setGoods(sellersStorageGoods);
                }
            }
            writeUser(users);
        } else {
            System.out.println("\nThere is no active goods!!!");
        }
    }

    public Goods findGoodsByName(List<Goods> sellersStorageGoods, String name) {
        for (Goods goods : sellersStorageGoods) {
            if (goods.getName().equalsIgnoreCase(name)) {
                return goods;
            }
        }
        return null;
    }

    public void buyGoods(Storage myBasket) {
        user = getUser();
        List<Card> myCards = user.getCards();
        if (!myCards.isEmpty()) {
            List<Goods> myBasketGoodsList = myBasket.getGoods();
            List<Goods> myGoods = user.getMyGoods().getGoods();

            if (!myBasketGoodsList.isEmpty()) {
                showGoods(myBasketGoodsList);
                Goods chosenGoods = new Goods();

                boolean has = false;
                while (!has) {
                    System.out.println("\nSelect goods: ");
                    String goodsName = scannerS.nextLine();

                    for (Goods goods : myBasketGoodsList) {
                        if (goods.getName().equalsIgnoreCase(goodsName)) {
                            chosenGoods.setName(goodsName);
                            has = true;
                            showGoodsWithSeller(goods, myBasketGoodsList);
                        }
                    }
                }

                User seller = null;
                while (seller == null) {
                    System.out.println("\nSelect seller: ");
                    String sellerName = scannerS.nextLine();

                    for (Goods goods : myBasketGoodsList) {
                        if (goods.getName().equalsIgnoreCase(chosenGoods.getName()) && goods.getSeller().equalsIgnoreCase(sellerName)) {
                            seller = findUser(goods.getSeller());
                            chosenGoods.setSeller(seller.getLogin());
                            chosenGoods.setPrice(goods.getPrice());
                            chosenGoods.setCurrency(goods.getCurrency());
                            chosenGoods.setQuantity(goods.getQuantity());
                            chosenGoods.setSale(goods.getSale());
                        }
                    }
                }

                List<Card> sellerCards = seller.getCards();
                List<Goods> soldGoods = seller.getSoldGoods().getGoods();
                aboutGoods(chosenGoods);
                Double chosenQuantity;
                while (true) {
                    System.out.println("\nEnter the quantity: ");
                    chosenQuantity = scannerD.nextDouble();

                    if (chosenQuantity > 0) {
                        if (chosenQuantity > chosenGoods.getQuantity()) {
                            System.out.println(new NotEnough("Chosen goods type quantity is insufficient to chosen quantity").getMessage());
                            writeLog(new NotEnough("Chosen goods type quantity is insufficient to chosen quantity").getMessage());
                        } else {
                            chosenGoods.setQuantity(chosenQuantity);
                            break;
                        }
                    } else {
                        System.err.println("\nInvalid number");
                        writeLog(new Invalid("Invalid number").getMessage());
                    }

                }

                Double summa = chosenGoods.getPrice() * chosenQuantity;
                Double summaWithSale = chosenGoods.getPriceSale();
                System.out.println("\n" + chosenGoods.getName() + ": \n\tQuantity:" + chosenQuantity + "\n\tSumma: "
                        + summa + " " + chosenGoods.getCurrency().getDescription()
                        + "\n\tSumma with sale: " + summaWithSale + " " + chosenGoods.getCurrency().getDescription());

                showCards(myCards);
                boolean card = false;
                while (!card) {
                    System.out.println("\nSelect by ordinal: ");
                    int ordinal = scannerI.nextInt();

                    for (Card myCard : myCards) {
                        if (myCard.getCardOrdinal() == ordinal) {
                            if (myCard.getCardType().getCurrency().equals(getMainCard(seller).getCardType().getCurrency())) {
                                if (!myCard.getCardType().getCurrency().equals(chosenGoods.getCurrency())) {
                                    summaWithSale = (chosenGoods.getCurrency().equals(Currency.SUM) ? (summaWithSale / Currency.converter()) : summaWithSale * Currency.converter());
                                    System.out.println("Summa with sale: " + summaWithSale);
                                }
                                card = true;
                                System.out.println("\nDo you want to buy? \nYes -> 1 \nNo -> 2");
                                int permission = scannerI.nextInt();
                                if (permission == 1) {
                                    if (myCard.getBalance() >= summaWithSale) {

                                        //todo pay for goods
                                        myCard.setBalance(myCard.getBalance() - summaWithSale);

                                        //todo income for seller
                                        for (Card sellerCard : sellerCards) {
                                            if (sellerCard.isMain()) {
                                                sellerCard.setBalance(sellerCard.getBalance() + summaWithSale);
                                            }
                                        }

                                        has = false;
                                        while (!has) {
                                            System.out.println("\nSet mark for goods (0-5): ");
                                            Double mark = scannerD.nextDouble();
                                            if (mark >= 0 && mark <= 5) {
                                                chosenGoods.getMarks().add(mark);
                                                has = true;
                                            } else {
                                                System.err.println("\nInvalid mark number");
                                                writeLog(new Invalid("Invalid mark number").getMessage());
                                            }

                                        }
                                        //todo add to seller's sold storage
                                        soldGoods.add(chosenGoods);

                                        //todo add to myGoods
                                        myGoods.add(chosenGoods);

                                        //todo remove from myBasket // with seller
                                        for (Goods goods : myBasketGoodsList) {
                                            if (goods.getName().equalsIgnoreCase(chosenGoods.getName()) && goods.getSeller().equalsIgnoreCase(chosenGoods.getSeller())) {
                                                goods.setQuantity(goods.getQuantity() - chosenQuantity);
                                            }
                                        }

                                        writeHistory(chosenGoods, user);
                                        System.out.println("\nThe process was completed successfully");
                                    } else {
                                        System.err.println(new NotEnough("Not enough money").getMessage());
                                        writeLog(new NotEnough("Not enough money").getMessage());
                                    }
                                }
                            } else {
                                System.err.println(new CardTypeNotMatch().getMessage());
                                writeLog(new CardTypeNotMatch().getMessage());
                            }
                        } else {
                            System.err.println(new Invalid("Invalid number").getMessage());
                            writeLog(new Invalid("Invalid number").getMessage());
                        }
                    }
                }
                myBasket.setGoods(myBasketGoodsList);

                users = getUsersList();
                for (User user1 : users) {
                    if (user1.getLogin().equals(user.getLogin())) {
                        user1.setCards(myCards);
                        user1.setBasket(myBasket);
                        user1.getMyGoods().setGoods(myGoods);
                    }
                    if (user1.getLogin().equals(seller.getLogin())) {
                        user1.setCards(sellerCards);
                        user1.getSoldGoods().setGoods(soldGoods);
                    }
                }
                writeUser(users);
            } else {
                System.out.println("\nYour basket is empty");
            }
        } else {
            System.out.println("\nYou do not have a card");
        }
    }

    public void deleteGoodsBasket(Storage myBasket) {
        user = getUser();
        List<Goods> goods = myBasket.getGoods();
        if (!goods.isEmpty()) {

            showGoods(goods);

            Goods removableGoods = null;
            while (removableGoods == null) {
                System.out.println("\nSelect the goods");
                String goodsName = scannerS.nextLine();

                for (Goods goods1 : goods) {
                    if (goods1.getName().equalsIgnoreCase(goodsName)) {
                        removableGoods = goods1;
                    }
                }
            }

            System.out.println("\nDo you want to remove goods from your basket? \nYes -> 1 \nNo -> 2");
            int permission = scannerI.nextInt();
            if (permission == 1) {
                goods.remove(removableGoods);
                myBasket.setGoods(goods);
                users = getUsersList();

                User seller = findUser(removableGoods.getSeller());
                List<Goods> sellerStorageGoods = seller.getMyStorage().getGoods();

                for (Goods storageGood : sellerStorageGoods) {
                    if (storageGood.getName().equalsIgnoreCase(removableGoods.getName())) {
                        storageGood.setQuantity(storageGood.getQuantity() + removableGoods.getQuantity());
                    }
                }

                for (User user1 : users) {
                    if (user1.getId().equals(user.getId())) {
                        user1.setBasket(myBasket);
                    }
                    if (user1.getLogin().equals(seller.getLogin())) {
                        seller.getMyStorage().setGoods(sellerStorageGoods);
                    }
                }
                writeUser(users);
            }
        } else System.err.println("\nYour basket is empty!!!");
    }


    //todo operations with cards
    public void addNewCard() {
        user = getUser();

        List<Card> cards = user.getCards();

        String cardNumber = "";
        while (cardNumber.length() != 16) {
            System.out.println("\nEnter the card number(16-digit): ");
            cardNumber = scannerS.nextLine();
            if (cardNumber.length() == 16) {
                if (cardNumber.contains("-")) {
                    cardNumber.replace("-", "");
                }
                try {
                    long i = Long.parseLong(cardNumber);
                } catch (NumberFormatException e) {
                    System.err.println("\n" + e.getMessage());
                    writeLog(e.getMessage());
                    addNewCard();
                }
            } else {
                System.err.println("\nIncompatible card number");
                writeLog(new Incompatible("Incompatible card number").getMessage());
            }
        }

        CardType cardType = null;
        while (cardType == null) {
            CardType.showCardTypes();
            System.out.println("\nChoose the card type: ");
            cardType = CardType.chooseCardType(scannerI.nextInt());

            if (cardType == null) {
                System.err.println(new Invalid("Invalid card type").getMessage());
                writeLog(new Invalid("Invalid card type").getMessage());
            }
        }

        while (true) {
            System.out.println("\nSet balance: ");
            Double balance = scannerD.nextDouble();
            if (balance > 0) {
                Card newCard = new Card(cardNumber, cardType, balance, (cards.size() + 1));
                newCard.setMain(newCard.getCardOrdinal() == 1);
                cards.add(newCard);
                break;
            } else {
                System.err.println("\nInvalid number");
                writeLog(new Invalid("Invalid number").getMessage());
            }
        }
        users = getUsersList();
        for (User user1 : users) {
            if (user1.getLogin().equals(user.getLogin())) {
                user1.setCards(cards);
            }
        }
        writeUser(users);
        System.out.println("\nThe card is successfully added!");
        operationCards();
    }

    public void changeMainCard() {
        user = getUser();

        List<Card> cards = user.getCards();
        if (!cards.isEmpty()) {
            if (cards.size() != 1) {
                showCards(cards);

                boolean has = false;
                while (!has) {
                    System.out.println("\nChoose by ordinal: ");
                    int ordinal = scannerI.nextInt();

                    for (Card card1 : cards) {
                        if (card1.getCardOrdinal() == ordinal) {
                            has = true;
                            card1.setMain(true);
                        }
                        if (card1.isMain()) {
                            card1.setMain(false);
                        }
                    }
                }

                users = getUsersList();
                for (User user1 : users) {
                    if (user1.getLogin().equals(user.getLogin())) {
                        user1.setCards(cards);
                    }
                }
                writeUser(users);

            } else {
                System.err.println("\nYou have only 1 card");
            }
        } else {
            System.err.println("\nYou do not have any cards!");
        }
    }

    public void replenish() {
        user = getUser();

        List<Card> cards = user.getCards();
        if (!cards.isEmpty()) {
            showCards(cards);

            boolean has = false;
            while (!has) {
                System.out.println("\nChoose by ordinal: ");
                int ordinal = scannerI.nextInt();

                for (Card card1 : cards) {
                    if (card1.getCardOrdinal() == ordinal) {
                        has = true;

                        while (true) {
                            System.out.println("\nEnter money: ");
                            Double money = scannerD.nextDouble();
                            if (money > 0) {
                                card1.setBalance(card1.getBalance() + money);
                                break;
                            } else {
                                System.err.println("\nInvalid number");
                                writeLog(new Invalid("Invalid number").getMessage());
                            }
                        }

                    }
                }
            }
            users = getUsersList();
            for (User user1 : users) {
                if (user1.getLogin().equals(user.getLogin())) {
                    user1.setCards(cards);
                }
            }
            writeUser(users);
        } else System.err.println("\nYou do not have any cards!");
    }

    public void deleteCard() {
        user = getUser();

        List<Card> cards = user.getCards();
        if (!cards.isEmpty()) {
            showCards(cards);

            Card card = null;
            while (card == null) {
                System.out.println("\nChoose by ordinal: ");
                int ordinal = scannerI.nextInt();

                for (Card card1 : cards) {
                    if (card1.getCardOrdinal() == ordinal) {
                        card = card1;
                    }
                }
            }
            cards.remove(card);
            users = getUsersList();
            for (User user1 : users) {
                if (user1.getLogin().equals(user.getLogin())) {
                    user1.setCards(cards);
                }
            }
            writeUser(users);
        } else System.err.println("\nYou do not have any cards!");
    }


    //todo general methods (admin)
    public void myStorage() {
        user = getUser();
        Storage myStorage = user.getMyStorage();

        System.out.println("\nOperations with goods -> 1");
        System.out.println("Info -> 2");
        System.out.println("\n\nBack -> 0");

        switch (SwitchService.operator(scannerI.nextInt(), 2)) {
            case 1:
                operationGoods(myStorage);
                break;
            case 2:
                info(myStorage);
                break;
            case 0:
                user.setMyStorage(myStorage);
                if (user.getType().equals(UserType.ADMIN_MAIN)) {
                    adminMainPanel();
                } else if (user.getType().equals(UserType.ADMIN)) {
                    adminPanel();
                }
        }
        myStorage();
    }

    // todo myStorage methods
    public void operationGoods(Storage myStorage) {

        System.out.println("\nAdd new goods -> 1");
        System.out.println("Edit goods -> 2");
        System.out.println("Delete goods -> 3");
        System.out.println("\n\nBack -> 0");

        switch (SwitchService.operator(scannerI.nextInt(), 3)) {
            case 1:
                addNewGoods(myStorage);
                break;
            case 2:
                editGoods(myStorage);
                break;
            case 3:
                deleteGoods(myStorage);
                break;
            case 0:
                myStorage();
        }
        operationGoods(myStorage);
    }

    //todo empty!!! (For excel)
    public void info(Storage myStorage) {
        List<Goods> goodsList = myStorage.getGoods();
        File file = new File("src/main/resources/reports/info.xlsx");
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            XSSFWorkbook workbook = new XSSFWorkbook();

            //todo sheet goods
            XSSFSheet sheet = workbook.createSheet("Storage Goods");
            XSSFCellStyle myStyle = workbook.createCellStyle();
            XSSFFont myFontBold = workbook.createFont();
            myFontBold.setBold(true);

            myStyle.setFont(myFontBold);
            myStyle.setBorderTop(BorderStyle.MEDIUM);
            myStyle.setBorderRight(BorderStyle.MEDIUM);
            myStyle.setBorderBottom(BorderStyle.MEDIUM);
            myStyle.setBorderLeft(BorderStyle.MEDIUM);

            XSSFCellStyle mySty = workbook.createCellStyle();
            XSSFFont myFont = workbook.createFont();

            mySty.setFont(myFont);
            mySty.setBorderTop(BorderStyle.MEDIUM);
            mySty.setBorderRight(BorderStyle.MEDIUM);
            mySty.setBorderBottom(BorderStyle.MEDIUM);
            mySty.setBorderLeft(BorderStyle.MEDIUM);

            XSSFRow row1 = sheet.createRow(0);
            Cell cell = row1.createCell(0);
            cell.setCellStyle(myStyle);
            cell.setCellValue("Goods name");
            cell = row1.createCell(1);
            cell.setCellStyle(myStyle);
            cell.setCellValue("Seller");
            cell = row1.createCell(2);
            cell.setCellStyle(myStyle);
            cell.setCellValue("Price");
            cell = row1.createCell(3);
            cell.setCellStyle(myStyle);
            cell.setCellValue("Currency");
            cell = row1.createCell(4);
            cell.setCellStyle(myStyle);
            cell.setCellValue("Quantity");
            cell = row1.createCell(5);
            cell.setCellStyle(myStyle);
            cell.setCellValue("Summa");
            cell = row1.createCell(6);
            cell.setCellStyle(myStyle);
            cell.setCellValue("Sale");
            cell = row1.createCell(7);
            cell.setCellStyle(myStyle);
            cell.setCellValue("Summa with sale");

            for (int i = 0; i < goodsList.size(); i++) {
                row1 = sheet.createRow(i + 1);

                cell = row1.createCell(0);
                cell.setCellStyle(mySty);
                cell.setCellValue(goodsList.get(i).getName());
                cell = row1.createCell(1);
                cell.setCellStyle(mySty);
                cell.setCellValue(goodsList.get(i).getSeller());
                cell = row1.createCell(2);
                cell.setCellStyle(mySty);
                cell.setCellValue(goodsList.get(i).getPrice());
                cell = row1.createCell(3);
                cell.setCellStyle(mySty);
                cell.setCellValue(goodsList.get(i).getCurrency().name());
                cell = row1.createCell(4);
                cell.setCellStyle(mySty);
                cell.setCellValue(goodsList.get(i).getQuantity());
                cell = row1.createCell(5);
                cell.setCellStyle(mySty);
                cell.setCellValue(goodsList.get(i).getQuantity() * goodsList.get(i).getPrice());
                cell = row1.createCell(6);
                cell.setCellStyle(mySty);
                cell.setCellValue(goodsList.get(i).getSale());
                cell = row1.createCell(7);
                cell.setCellStyle(mySty);
                cell.setCellValue(goodsList.get(i).getPriceSale());
            }

            for (int i = 0; i <= row1.getLastCellNum(); i++) {
                sheet.setColumnWidth(i, 5000);
            }

            createSheet(workbook, user.getSoldGoods().getGoods(), "Sold Goods");
            workbook.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(file.getAbsolutePath());
    }


    //todo operations with goods
    public void addNewGoods(Storage myStorage) {
        user = getUser();
        List<Goods> goods = myStorage.getGoods();

        Goods newGoods;
        System.out.println("\nEnter goods name: ");
        String goodsName = scannerS.nextLine();
        Double quantity;
        while (true) {
            System.out.println("\nEnter the quantity: ");
            quantity = scannerD.nextDouble();
            if (quantity > 0) {
                break;
            } else {
                System.err.println("\nInvalid number");
                writeLog(new Invalid("Invalid number").getMessage());
            }
        }
        boolean hasGoods = false;
        if (!goods.isEmpty()) {
            for (Goods good : goods) {
                if (good.getName().equalsIgnoreCase(goodsName)) {
                    hasGoods = true;
                    good.setQuantity(good.getQuantity() + quantity);
                    System.out.println("\nGoods has successfully updated!!!");
                }
            }
            if (!hasGoods) {
                Double price;
                while (true) {
                    System.out.println("\nSet the price: ");
                    price = scannerD.nextDouble();
                    if (price > 0) {
                        break;
                    } else {
                        System.err.println("\nInvalid number");
                        writeLog(new Invalid("Invalid number").getMessage());
                    }
                }

                Double sale;
                while (true) {
                    System.out.println("\nSet the sale (0-100): ");
                    sale = scannerD.nextDouble();
                    if (sale >= 0 && sale <= 100) {
                        break;
                    } else {
                        System.err.println("\nInvalid number");
                        writeLog(new Invalid("Invalid number").getMessage());
                    }
                }

                Currency currency = null;
                while (currency == null) {
                    Currency.showCurrency();
                    System.out.println("\nChoose the currency: ");
                    currency = Currency.chooseCardType(scannerI.nextInt());
                }

                newGoods = new Goods(goodsName, user.getLogin());
                newGoods.setSale(sale);
                newGoods.setPrice(price);
                newGoods.setQuantity(quantity);
                newGoods.setCurrency(currency);
                goods.add(newGoods);
                System.out.println("\n" + newGoods.getName() + " has successfully added to your storage!!!");
            }
        } else {
            Double price;
            while (true) {
                System.out.println("\nSet the price: ");
                price = scannerD.nextDouble();
                if (price > 0) {
                    break;
                } else {
                    System.err.println("\nInvalid number");
                    writeLog(new Invalid("Invalid number").getMessage());
                }
            }

            Double sale;
            while (true) {
                System.out.println("\nSet the sale (0-100): ");
                sale = scannerD.nextDouble();
                if (sale >= 0 && sale <= 100) {
                    break;
                } else {
                    System.err.println("\nInvalid number");
                    writeLog(new Invalid("Invalid number").getMessage());
                }
            }

            Currency currency = null;
            while (currency == null) {
                Currency.showCurrency();
                System.out.println("\nChoose the currency: ");
                currency = Currency.chooseCardType(scannerI.nextInt());
            }

            newGoods = new Goods(goodsName, user.getLogin());
            newGoods.setSale(sale);
            newGoods.setPrice(price);
            newGoods.setQuantity(quantity);
            newGoods.setCurrency(currency);
            goods.add(newGoods);
            System.out.println("\n" + newGoods.getName() + " has successfully added to your storage!!!");
        }


        myStorage.setGoods(goods);

        users = getUsersList();
        for (User user1 : users) {
            if (user1.getId().equals(user.getId())) {
                user1.setMyStorage(myStorage);
            }
        }
        writeUser(users);
    }

    public void editGoods(Storage myStorage) {
        user = getUser();
        List<Goods> goods = myStorage.getGoods();
        showGoods(goods);
        boolean has = false;
        while (!has) {
            System.out.println("\nSelect the goods: ");
            String goodsName = scannerS.nextLine();

            for (Goods goods1 : goods) {
                if (goods1.getName().equalsIgnoreCase(goodsName)) {
                    has = true;

                    System.out.println("\nEdit goods name -> 1");
                    System.out.println("Edit goods price -> 2");
                    System.out.println("Edit goods quantity -> 3");
                    System.out.println("Edit goods sale -> 4");
                    System.out.println("\n\nBack -> 0");

                    switch (SwitchService.operator(scannerI.nextInt(), 4)) {
                        case 1:
                            System.out.println("\nEnter the new name: ");
                            goods1.setName(scannerS.nextLine());
                            System.out.println("\nName of goods is changed!!!");
                            break;
                        case 2:
                            System.out.println("\nEnter the new price: ");
                            Double price = scannerD.nextDouble();
                            if (price >= 0) {
                                goods1.setPrice(price);
                                System.out.println("\nThe price is changed");
                            } else {
                                System.err.println("\nInvalid number");
                                writeLog(new Invalid("Invalid number").getMessage());
                            }
                            break;
                        case 3:
                            System.out.println("\nEnter the new quantity: ");
                            Double quantity = scannerD.nextDouble();
                            if (quantity >= 0) {
                                goods1.setQuantity(quantity);
                                System.out.println("\nThe quantity is changed!!!");
                            } else {
                                System.err.println("\nInvalid number");
                                writeLog(new Invalid("Invalid number").getMessage());
                            }
                            break;
                        case 4:
                            System.out.println("\nEnter the new sale: ");
                            Double sale = scannerD.nextDouble();
                            if (sale >= 0 && sale <= 100) {
                                goods1.setSale(sale);
                                System.out.println("\nThe sale is changed!!!");
                            } else {
                                System.err.println("\nInvalid number");
                                writeLog(new Invalid("Invalid number").getMessage());
                            }
                            break;
                        case 0:
                            operationGoods(myStorage);
                            break;
                    }
                } else {
                    System.err.println("Entered value is incompatible!!!");
                    writeLog(new Incompatible("Entered value is incompatible!!!").getMessage());
                }
            }
        }
        myStorage.setGoods(goods);

        users = getUsersList();
        for (User user1 : users) {
            if (user1.getId().equals(user.getId())) {
                user1.setMyStorage(myStorage);
            }
        }
        writeUser(users);
    }

    public void deleteGoods(Storage myStorage) {
        user = getUser();
        List<Goods> goods = myStorage.getGoods();
        if (!goods.isEmpty()) {
            showGoods(goods);

            boolean has = false;
            while (!has) {
                System.out.println("\nSelect the goods: ");
                String goodsName = scannerS.nextLine();

                goods.removeIf(goods1 -> goods1.getName().equalsIgnoreCase(goodsName));
                has = true;

                System.out.println("\nGoods has successfully removed!!!");
                myStorage.setGoods(goods);

                users = getUsersList();
                for (User user1 : users) {
                    if (user1.getId().equals(user.getId())) {
                        user1.setMyStorage(myStorage);
                    }
                }
                writeUser(users);
            }
        } else System.err.println("\nYour storage is empty!!!");
    }

    //todo mainAdmin methods
    public void operationUsers() {
        System.out.println("\nAdd new user -> 1");
        System.out.println("Edit user -> 2");
        System.out.println("Delete user -> 3");
        System.out.println("Show top users -> 4");
        System.out.println("\n\nBack -> 0");

        switch (SwitchService.operator(scannerI.nextInt(), 4)) {
            case 1:
                signUp();
                break;
            case 2:
                editUser();
                break;
            case 3:
                deleteUser();
                break;
            case 4:
                showTops();
                break;
            case 0:
                adminMainPanel();
                break;
        }
        operationUsers();
    }

    public void reports() {
        File report = new File("src/main/resources/reports/goods.xlsx");
        try (FileOutputStream outputStream = new FileOutputStream(report)) {

            users = getUsersList();
//            users.sort(Comparator.comparingInt(o -> o.getType().ordinal()));

            activeGoods = getActiveGoods();
//            activeGoods.sort(Comparator.comparing(Goods::getName));

            basketGoods = getBasketGoods();
//            basketGoods.sort(Comparator.comparing(Goods::getName));

            boughtGoods = getBoughtGoods();
//            boughtGoods.sort(Comparator.comparing(Goods::getName));

            histories = getHistories();
            histories.sort(Comparator.comparing(History::getDate));

            XSSFWorkbook workbook = new XSSFWorkbook();

            //todo sheet goods
            createSheet(workbook, activeGoods, "Goods");
            createSheet(workbook, basketGoods, "Basket Goods");
            createSheet(workbook, boughtGoods, "Bought Goods");
            XSSFSheet sheet;
//            XSSFSheet sheet = workbook.createSheet("Goods");
            XSSFCellStyle myStyle = workbook.createCellStyle();
            XSSFFont myFontBold = workbook.createFont();
            myFontBold.setBold(true);

            myStyle.setFont(myFontBold);
            myStyle.setBorderTop(BorderStyle.MEDIUM);
            myStyle.setBorderRight(BorderStyle.MEDIUM);
            myStyle.setBorderBottom(BorderStyle.MEDIUM);
            myStyle.setBorderLeft(BorderStyle.MEDIUM);

            XSSFCellStyle mySty = workbook.createCellStyle();
            XSSFFont myFont = workbook.createFont();

            mySty.setFont(myFont);
            mySty.setBorderTop(BorderStyle.MEDIUM);
            mySty.setBorderRight(BorderStyle.MEDIUM);
            mySty.setBorderBottom(BorderStyle.MEDIUM);
            mySty.setBorderLeft(BorderStyle.MEDIUM);

            XSSFRow row1 /*= sheet.createRow(0)*/;
            Cell cell /*= row1.createCell(0)*/;
//            cell.setCellStyle(myStyle);
//            cell.setCellValue("Goods name");
//            cell = row1.createCell(1);
//            cell.setCellStyle(myStyle);
//            cell.setCellValue("Seller");
//            cell = row1.createCell(2);
//            cell.setCellStyle(myStyle);
//            cell.setCellValue("Price");
//            cell = row1.createCell(3);
//            cell.setCellStyle(myStyle);
//            cell.setCellValue("Currency");
//            cell = row1.createCell(4);
//            cell.setCellStyle(myStyle);
//            cell.setCellValue("Quantity");
//            cell = row1.createCell(5);
//            cell.setCellStyle(myStyle);
//            cell.setCellValue("Summa");
//
//            for (int i = 0; i < activeGoods.size(); i++) {
//                row1 = sheet.createRow(i + 1);
//
//                cell = row1.createCell(0);
//                cell.setCellStyle(mySty);
//                cell.setCellValue(activeGoods.get(i).getName());
//                cell = row1.createCell(1);
//                cell.setCellStyle(mySty);
//                cell.setCellValue(activeGoods.get(i).getSeller());
//                cell = row1.createCell(2);
//                cell.setCellStyle(mySty);
//                cell.setCellValue(activeGoods.get(i).getPrice());
//                cell = row1.createCell(3);
//                cell.setCellStyle(mySty);
//                cell.setCellValue(activeGoods.get(i).getCurrency().name());
//                cell = row1.createCell(4);
//                cell.setCellStyle(mySty);
//                cell.setCellValue(activeGoods.get(i).getQuantity());
//                cell = row1.createCell(5);
//                cell.setCellStyle(mySty);
//                cell.setCellValue(activeGoods.get(i).getQuantity() * activeGoods.get(i).getPrice());
//            }
//
//            for (int i = 0; i <= row1.getLastCellNum(); i++) {
//                sheet.setColumnWidth(i, 5000);
//            }

            //todo sheet users
            sheet = workbook.createSheet("Users");

            row1 = sheet.createRow(0);
            cell = row1.createCell(0);
            cell.setCellStyle(myStyle);
            cell.setCellValue("User login");
            cell = row1.createCell(1);
            cell.setCellStyle(myStyle);
            cell.setCellValue("User Type");
            cell = row1.createCell(2);
            cell.setCellStyle(myStyle);
            cell.setCellValue("Password");
            cell = row1.createCell(3);
            cell.setCellStyle(myStyle);
            cell.setCellValue("Id");
            cell = row1.createCell(4);
            cell.setCellStyle(myStyle);
            cell.setCellValue("User rate");
            cell = row1.createCell(5);
            cell.setCellStyle(myStyle);
            cell.setCellValue("Cards number");

            for (int i = 0; i < users.size(); i++) {
                row1 = sheet.createRow(i + 1);

                cell = row1.createCell(0);
                cell.setCellStyle(mySty);
                cell.setCellValue(users.get(i).getLogin());
                cell = row1.createCell(1);
                cell.setCellStyle(mySty);
                cell.setCellValue(users.get(i).getType().name());
                cell = row1.createCell(2);
                cell.setCellStyle(mySty);
                cell.setCellValue(users.get(i).getPassword());
                cell = row1.createCell(3);
                cell.setCellStyle(mySty);
                cell.setCellValue(users.get(i).getId());
                cell = row1.createCell(4);
                cell.setCellStyle(mySty);
                cell.setCellValue(users.get(i).getUserRate());
                cell = row1.createCell(5);
                cell.setCellStyle(mySty);
                cell.setCellValue(users.get(i).getCards().size());
            }

            for (int i = 0; i <= row1.getLastCellNum(); i++) {
                sheet.setColumnWidth(i, 5000);
            }

            //todo sheet histories
            sheet = workbook.createSheet("Histories");

            row1 = sheet.createRow(0);
            cell = row1.createCell(0);
            cell.setCellStyle(myStyle);
            cell.setCellValue("Customer");
            cell = row1.createCell(1);
            cell.setCellStyle(myStyle);
            cell.setCellValue("Seller");
            cell = row1.createCell(2);
            cell.setCellStyle(myStyle);
            cell.setCellValue("Goods name");
            cell = row1.createCell(3);
            cell.setCellStyle(myStyle);
            cell.setCellValue("Goods quantity");
            cell = row1.createCell(4);
            cell.setCellStyle(myStyle);
            cell.setCellValue("Goods price");
            cell = row1.createCell(5);
            cell.setCellStyle(myStyle);
            cell.setCellValue("Goods currency");
            cell = row1.createCell(6);
            cell.setCellStyle(myStyle);
            cell.setCellValue("Summa");
            cell = row1.createCell(7);
            cell.setCellStyle(myStyle);
            cell.setCellValue("Summa with sale");
            cell = row1.createCell(8);
            cell.setCellStyle(myStyle);
            cell.setCellValue("Date and time");

            for (int i = 0; i < histories.size(); i++) {
                row1 = sheet.createRow(i + 1);

                cell = row1.createCell(0);
                cell.setCellStyle(mySty);
                cell.setCellValue(histories.get(i).getCustomer());
                cell = row1.createCell(1);
                cell.setCellStyle(mySty);
                cell.setCellValue(histories.get(i).getSeller());
                cell = row1.createCell(2);
                cell.setCellStyle(mySty);
                cell.setCellValue(histories.get(i).getGoodsName());
                cell = row1.createCell(3);
                cell.setCellStyle(mySty);
                cell.setCellValue(histories.get(i).getQuantity());
                cell = row1.createCell(4);
                cell.setCellStyle(mySty);
                cell.setCellValue(histories.get(i).getPrice());
                cell = row1.createCell(5);
                cell.setCellStyle(mySty);
                cell.setCellValue(histories.get(i).getCurrency().name());
                cell = row1.createCell(6);
                cell.setCellStyle(mySty);
                cell.setCellValue(histories.get(i).getQuantity() * histories.get(i).getPrice());
                cell = row1.createCell(7);
                cell.setCellStyle(mySty);
                cell.setCellValue(histories.get(i).getSumma());
                cell = row1.createCell(8);
                cell.setCellStyle(mySty);
                cell.setCellValue(histories.get(i).getDate() + " " + histories.get(i).getTime());
            }

            for (int i = 0; i <= row1.getLastCellNum(); i++) {
                sheet.setColumnWidth(i, 5000);
            }

            workbook.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(report.getAbsolutePath());
    }

    public void createSheet(XSSFWorkbook workbook, List<Goods> activeGoods, String sheetName) {
        XSSFSheet sheet = workbook.createSheet(sheetName);
        XSSFCellStyle myStyle = workbook.createCellStyle();
        XSSFFont myFontBold = workbook.createFont();
        myFontBold.setBold(true);

        myStyle.setFont(myFontBold);
        myStyle.setBorderTop(BorderStyle.MEDIUM);
        myStyle.setBorderRight(BorderStyle.MEDIUM);
        myStyle.setBorderBottom(BorderStyle.MEDIUM);
        myStyle.setBorderLeft(BorderStyle.MEDIUM);

        XSSFCellStyle mySty = workbook.createCellStyle();
        XSSFFont myFont = workbook.createFont();

        mySty.setFont(myFont);
        mySty.setBorderTop(BorderStyle.MEDIUM);
        mySty.setBorderRight(BorderStyle.MEDIUM);
        mySty.setBorderBottom(BorderStyle.MEDIUM);
        mySty.setBorderLeft(BorderStyle.MEDIUM);

        XSSFRow row1 = sheet.createRow(0);
        Cell cell = row1.createCell(0);
        cell.setCellStyle(myStyle);
        cell.setCellValue("Goods name");
        cell = row1.createCell(1);
        cell.setCellStyle(myStyle);
        cell.setCellValue("Seller");
        cell = row1.createCell(2);
        cell.setCellStyle(myStyle);
        cell.setCellValue("Price");
        cell = row1.createCell(3);
        cell.setCellStyle(myStyle);
        cell.setCellValue("Currency");
        cell = row1.createCell(4);
        cell.setCellStyle(myStyle);
        cell.setCellValue("Quantity");
        cell = row1.createCell(5);
        cell.setCellStyle(myStyle);
        cell.setCellValue("Summa");
        cell = row1.createCell(6);
        cell.setCellStyle(myStyle);
        cell.setCellValue("Sale");
        cell = row1.createCell(7);
        cell.setCellStyle(myStyle);
        cell.setCellValue("Summa with sale");


        for (int i = 0; i < activeGoods.size(); i++) {
            row1 = sheet.createRow(i + 1);

            cell = row1.createCell(0);
            cell.setCellStyle(mySty);
            cell.setCellValue(activeGoods.get(i).getName());
            cell = row1.createCell(1);
            cell.setCellStyle(mySty);
            cell.setCellValue(activeGoods.get(i).getSeller());
            cell = row1.createCell(2);
            cell.setCellStyle(mySty);
            cell.setCellValue(activeGoods.get(i).getPrice());
            cell = row1.createCell(3);
            cell.setCellStyle(mySty);
            cell.setCellValue(activeGoods.get(i).getCurrency().name());
            cell = row1.createCell(4);
            cell.setCellStyle(mySty);
            cell.setCellValue(activeGoods.get(i).getQuantity());
            cell = row1.createCell(5);
            cell.setCellStyle(mySty);
            cell.setCellValue(activeGoods.get(i).getQuantity() * activeGoods.get(i).getPrice());
            cell = row1.createCell(6);
            cell.setCellStyle(mySty);
            cell.setCellValue(activeGoods.get(i).getSale());
            cell = row1.createCell(7);
            cell.setCellStyle(mySty);
            cell.setCellValue(activeGoods.get(i).getPriceSale());
        }

        for (int i = 0; i <= row1.getLastCellNum(); i++) {
            sheet.setColumnWidth(i, 5000);
        }
    }

    //todo operations with users
    public void editUser() {
        showUsers();

        User user = null;
        while (user == null) {
            System.out.println("\nEnter the login");
            String login = scannerS.nextLine();
            user = findUser(login);
            if (user == null) {
                System.err.println(new Incompatible("Login incompatible").getMessage());
                writeLog(new Incompatible("Login incompatible").getMessage());
            }
        }

        System.out.println("\nEdit login -> 1");
        System.out.println("Edit password -> 2");
        System.out.println("Make admin -> 3");
        System.out.println("\n\nBack -> 0");

        switch (SwitchService.operator(scannerI.nextInt(), 3)) {
            case 1:
                editLogin(user);
                break;
            case 2:
                editPassword(user);
                break;
            case 3:
                editAccess(user);
                break;
            case 0:
                operationUsers();
                break;
        }
        operationUsers();

    }

    public void deleteUser() {
        showUsers();
        User user = null;
        while (user == null) {
            System.out.println("\nEnter the login: ");
            String login = scannerS.nextLine();

            user = findUser(login);
            if (user == null) {
                System.err.println(new Incompatible("Login incompatible").getMessage());
                writeLog(new Incompatible("Login incompatible").getMessage());
            }
        }
        users = getUsersList();
        users.remove(user);
        System.out.println("\nUser has successfully removed!!!");
        writeUser(users);
    }

    public void showTops() {
        users = getUsersList();
        ArrayList<User> topUsers = new ArrayList<>(users);
        topUsers.sort((o1, o2) -> (int) (numberOfGoods(o2.getMyGoods().getGoods()) - numberOfGoods(o1.getMyGoods().getGoods())));
        int tops = 5;
        if (users.size() < 5) {
            tops = users.size();
        }
        System.out.println();
        for (int i = 0; i < tops; i++) {
            System.out.println(topUsers.get(i).getLogin() + " bought " + topUsers.get(i).getMyGoods().getGoods().size());
        }
    }

    public double numberOfGoods(List<Goods> goods) {
        double sumQuantity = 0;
        for (Goods good : goods) {
            sumQuantity += good.getQuantity();
        }
        return sumQuantity;
    }

    //todo edit user
    public void editLogin(User user) {
        users = getUsersList();
        for (User user1 : users) {
            if (user1.getId().equals(user.getId())) {
                System.out.println("\nCreate new login: ");
                String newLogin = scannerS.nextLine();

                boolean checkLogin = checkLogin(newLogin);
                if (!checkLogin) {
                    user1.setLogin(newLogin);
                    System.out.println("\nLogin changed");
                } else {
                    System.out.println("\nThis login is used");
                    editLogin(user);
                }
            }
        }
        writeUser(users);
    }

    public void editPassword(User user) {
        users = getUsersList();
        for (User user1 : users) {
            if (user1.getLogin().equals(user.getLogin())) {
                System.out.println("\nCreate password: ");
                String password = scannerS.nextLine();
                if (!checkPassword(password)) {
                    System.out.println("\nPassword should at least 8 character!!!");
                } else {
                    System.out.println("\nConfirm password: ");
                    String confirm = scannerS.nextLine();

                    if (password.equals(confirm)) {
                        user1.setPassword(password);
                        System.out.println("\nPassword has successfully changed");
                    } else {
                        System.err.println(new ConfirmException().getMessage());
                        writeLog(new ConfirmException().getMessage());
                        editPassword(user);
                    }
                }
            }
        }
        writeUser(users);
    }

    public void editAccess(User user) {
        users = getUsersList();
        if (!user.getCards().isEmpty()) {
            for (User user1 : users) {
                if (user1.getLogin().equals(user.getLogin())) {
                    System.out.println("\nDo you want to change access " + user1.getLogin() + " from " + user1.getType()
                            + " to " + ((user1.getType().ordinal() == 1) ? UserType.USER : UserType.ADMIN));
                    System.out.println("Yes -> 1");
                    System.out.println("No -> 2");
                    int permission = scannerI.nextInt();
                    if (permission == 1) {
                        if (user1.getType().equals(UserType.ADMIN)) {
                            user1.setType(UserType.USER);
                            System.out.println("\nType of user has successfully changed!!!");
                        } else {
                            user1.setMyStorage(new Storage());
                            user1.setType(UserType.ADMIN);
                        }
                    }
                }
            }
        } else {
            System.err.println("\nAdmin should have at least 1 card");
            writeLog(new Invalid("Not enough card number").getMessage());
        }
        writeUser(users);
    }


    //todo shows
    public void aboutGoods(Goods goods) {
        System.out.println("\n" + goods.getName() + ": \n" +
                "\tSeller: " + goods.getSeller() + "\n\tMain Card: " + getMainCard(findUser(goods.getSeller())).getCardType().name() + "\n\tPrice: " + goods.getPrice() + " " + goods.getCurrency().getDescription() + "\n\tQuantity: " + goods.getQuantity());
    }

    public void showGoodsWithSeller(Goods chosenGoods, List<Goods> myBasketGoodsList) {
        List<User> userList = getUsersList();
        user = getUser();
        userList.sort((o1, o2) -> (int) (o2.getUserRate() - o1.getUserRate()));
        for (User user1 : userList) {
            if (user1.getLogin().equalsIgnoreCase(user.getLogin())) {
                continue;
            }
            user1.getMyStorage().getGoods().forEach(goods -> {
                if (goods.getName().equals(chosenGoods.getName())) {
                    System.out.println("\n" + goods.getName() + ": \n" +
                            "\tSeller: " + goods.getSeller() + "\n\tSeller rate: " + user1.getUserRate() +
                            "\n\tMain Card: " + getMainCard(findUser(goods.getSeller())).getCardType().name() +
                            "\n\tPrice: " + goods.getPrice() + " " + goods.getCurrency().getDescription() +
                            "\n\tQuantity: " + goods.getQuantity() + "\n\tSale: " + goods.getSale());
                }
            });
        }


//        myBasketGoodsList.forEach(goods -> {
//            if (goods.getName().equals(chosenGoods.getName())) {
//                System.out.println("\n" + goods.getName() + ": \n" +
//                        "\tSeller: " + goods.getSeller() + "\n\tMain Card: " + getMainCard(findUser(goods.getSeller())).getCardType().name() + "\n\tPrice: " + goods.getPrice() + " " + goods.getCurrency().getDescription() + "\n\tQuantity: " + goods.getQuantity());
//            }
//        });
    }

    public void showCards(List<Card> myCards) {
        for (int i = 0; i < myCards.size(); i++) {
            myCards.get(i).setCardOrdinal(i + 1);
            System.out.println("\n" + myCards.get(i).getCardOrdinal() + ") " + myCards.get(i).getCardType().name() + ": "
                    + myCards.get(i).getCardId() + "\nBalance: " + myCards.get(i).getBalance());

        }
    }

    public void showUsers() {
        users = getUsersList();
        users.forEach(user1 -> {
            if (!user1.getLogin().equals(user.getLogin())) {
                System.out.println("\nLogin: " + user1.getLogin() + "\nId: " + user1.getId() + "\nType: " + user1.getType().name());
            }
        });
    }

    public void showGoods(List<Goods> goods) {
        goods.forEach(goods1 -> System.out.println("\nGoods name: " + goods1.getName()));
    }

    public void showActiveGoods(List<Goods> list) {
//        goods.forEach(goods1 -> {
//            if (!goods1.getSeller().equals(user.getLogin())) {
//                System.out.println("\nGoods name: " + goods1.getName());
//            }
//        });

        File actives = new File("src/main/resources/reports/actives.xlsx");
        try (FileOutputStream outputStream = new FileOutputStream(actives)) {

            XSSFWorkbook workbook = new XSSFWorkbook();

            //todo sheet goods
            XSSFSheet sheet = workbook.createSheet("Goods");
            XSSFCellStyle myStyle = workbook.createCellStyle();
            XSSFFont myFontBold = workbook.createFont();
            myFontBold.setBold(true);

            myStyle.setFont(myFontBold);
            myStyle.setBorderTop(BorderStyle.MEDIUM);
            myStyle.setBorderRight(BorderStyle.MEDIUM);
            myStyle.setBorderBottom(BorderStyle.MEDIUM);
            myStyle.setBorderLeft(BorderStyle.MEDIUM);

            XSSFCellStyle mySty = workbook.createCellStyle();
            XSSFFont myFont = workbook.createFont();

            mySty.setFont(myFont);
            mySty.setBorderTop(BorderStyle.MEDIUM);
            mySty.setBorderRight(BorderStyle.MEDIUM);
            mySty.setBorderBottom(BorderStyle.MEDIUM);
            mySty.setBorderLeft(BorderStyle.MEDIUM);

            XSSFRow row1 = sheet.createRow(0);
            Cell cell = row1.createCell(0);
            cell.setCellStyle(myStyle);
            cell.setCellValue("Goods name");
            cell = row1.createCell(1);
            cell.setCellStyle(myStyle);
            cell.setCellValue("Seller");
            cell = row1.createCell(2);
            cell.setCellStyle(myStyle);
            cell.setCellValue("Price");
            cell = row1.createCell(3);
            cell.setCellStyle(myStyle);
            cell.setCellValue("Currency");
            cell = row1.createCell(4);
            cell.setCellStyle(myStyle);
            cell.setCellValue("Quantity");
            cell = row1.createCell(5);
            cell.setCellStyle(myStyle);
            cell.setCellValue("Sale");

            for (int i = 0; i < list.size(); i++) {
                row1 = sheet.createRow(i + 1);

                cell = row1.createCell(0);
                cell.setCellStyle(mySty);
                cell.setCellValue(list.get(i).getName());
                cell = row1.createCell(1);
                cell.setCellStyle(mySty);
                cell.setCellValue(list.get(i).getSeller());
                cell = row1.createCell(2);
                cell.setCellStyle(mySty);
                cell.setCellValue(list.get(i).getPrice());
                cell = row1.createCell(3);
                cell.setCellStyle(mySty);
                cell.setCellValue(list.get(i).getCurrency().name());
                cell = row1.createCell(4);
                cell.setCellStyle(mySty);
                cell.setCellValue(list.get(i).getQuantity());
                cell = row1.createCell(5);
                cell.setCellStyle(mySty);
                cell.setCellValue(list.get(i).getSale());
            }

            for (int i = 0; i <= row1.getLastCellNum(); i++) {
                sheet.setColumnWidth(i, 5000);
            }

            workbook.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(actives.getAbsolutePath());
    }

    public List<User> getGoodsWithSeller(Goods goods) {
        users = getUsersList();
        List<User> sellers = new ArrayList<>();
        for (User user1 : users) {
            for (Goods good : user1.getMyStorage().getGoods()) {
                if (good.getName().equalsIgnoreCase(goods.getName())) {
                    sellers.add(findUser(good.getSeller()));
                }
            }
        }
        return sellers;
    }


    //todo gets
    public Card getMainCard(User user) {
        for (Card card : user.getCards()) {
            if (card.isMain()) {
                return card;
            }
        }
        return null;
    }

    public User findUser(String login) {
        users = getUsersList();
        for (User user : users) {
            if (user.getLogin().equalsIgnoreCase(login)) {
                return user;
            }
        }
        return null;
    }

    public List<User> getUsersList() {
        try (BufferedReader reader = new BufferedReader(new FileReader(usersFile));) {
            return gsonUserHelper.converter(reader, usersFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<History> getHistories() {
        try (BufferedReader reader = new BufferedReader(new FileReader(historyFile))) {
            return gsonHistoryHelper.converter(reader, historyFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Goods> getGoodsList() {
        try (BufferedReader reader = new BufferedReader(new FileReader(goodsFile))) {
            return gsonGoodsHelper.converter(reader, goodsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getUser() {
        users = getUsersList();
        for (User user1 : users) {
            if (user1.getLogin().equals(user.getLogin())) {
                return user1;
            }
        }
        return null;
    }


    //todo writes
    public void writeGoods() {
        users = getUsersList();
        activeGoods.clear();
        allGoods.clear();
        List<Goods> goods = new ArrayList<>();
        try (Writer writer = new FileWriter(goodsFile)) {
            for (User user1 : users) {
                goods.addAll(user1.getMyGoods().getGoods());
                goods.addAll(user1.getMyStorage().getGoods());
                activeGoods.addAll(user1.getMyStorage().getGoods());
                goods.addAll(user1.getBasket().getGoods());
            }
            goods.sort(Comparator.comparing(Goods::getName));
            allGoods = goods;
            writer.write(gson.toJson(goods));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Goods> removeEmptyGoods(List<Goods> goods) {
        goods.removeIf(goods1 -> goods1.getQuantity() == 0.0);
        return goods;
    }

    public List<Goods> getActiveGoods() {
        users = getUsersList();
        activeGoods.clear();
        for (User user1 : users) {
            activeGoods.addAll(user1.getMyStorage().getGoods());
        }
        return activeGoods;
    }

//    public List<Goods> getSoldGoods() {
//        users = getUsersList();
//        soldGoods.clear();
//        for (User user1 : users) {
//            soldGoods.addAll(user1.getSoldGoods().getGoods());
//        }
//        return soldGoods;
//    }

    public List<Goods> getBasketGoods() {
        users = getUsersList();
        basketGoods.clear();
        for (User user1 : users) {
            basketGoods.addAll(user1.getBasket().getGoods());
        }
        return basketGoods;
    }

    public List<Goods> getBoughtGoods() {
        users = getUsersList();
        boughtGoods.clear();
        for (User user1 : users) {
            boughtGoods.addAll(user1.getMyGoods().getGoods());
        }
        return boughtGoods;
    }

    public void writeUser(List<User> users) {
        try (Writer writer = new FileWriter(usersFile)) {
            for (User user1 : users) {
                user1.getMyStorage().setGoods(removeEmptyGoods(user1.getMyStorage().getGoods()));
                user1.getBasket().setGoods(removeEmptyGoods(user1.getBasket().getGoods()));
                user1.getSoldGoods().setGoods(removeEmptyGoods(user1.getSoldGoods().getGoods()));
                user1.getMyGoods().setGoods(removeEmptyGoods(user1.getMyGoods().getGoods()));
            }
            writer.write(gson.toJson(users));
        } catch (IOException e) {
            e.printStackTrace();
        }
        writeGoods();
    }

    public void writeLog(String log) {
        user = getUser();
        try (Writer writer = new FileWriter(logFile, true)) {
            localDateTime = LocalDateTime.now();
            writer.write(user.getLogin() + ": " + log + " -> " + localDateTime.toLocalDate() + " " + localDateTime.toLocalTime() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeHistory(Goods goods, User customer) {
        histories = getHistories();

        try (Writer writer = new FileWriter(historyFile)) {
            localDateTime = LocalDateTime.now();
            History history = new History(customer.getLogin(), goods.getSeller(), goods.getName(), goods.getQuantity(), goods.getPrice(), goods.getCurrency(),
                    goods.getPriceSale(), localDateTime.toLocalDate(), localDateTime.toLocalTime());
            histories.add(history);
            writer.write(gson.toJson(histories));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //todo additional
    public boolean checkLogin(String login) {
        boolean hasUser = false;
        for (User user : users) {
            if (user.getLogin().equalsIgnoreCase(login)) {
                hasUser = true;
            }
        }
        return hasUser;
    }

    public boolean checkPassword(String password) {
        return password.length() >= 8;
    }

    public String generatorId() {
        users = getUsersList();
        HashSet<String> ids = new HashSet<>();
        users.forEach(user -> ids.add(user.getId()));
        String id = "";
        while (id.equals("")) {
            for (int i = 0; i < 7; i++) {
                id += (int) (Math.random() * 10);
            }
            if (!ids.add(id)) {
                id = "";
            }
        }
        return id;
    }
}
