package com.grocerylist.app.utils;

import com.grocerylist.app.models.Category;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryPredictor {

    private static final Map<String, Category> KEYWORD_MAP = new HashMap<>();
    private static final List<String> SORTED_KEYWORDS = new ArrayList<>();

    private CategoryPredictor() {
        // Utility class - instantiation not allowed
    }

    static {
        // BROED (Bread)
        addKeywords(Category.BROED, "rugbrød", "rundstykker", "toast", "bagel", "croissant",
                "boller", "kage", "wienerbrød", "franskbrød", "morgenbrød", "pitabrød",
                "ciabatta", "focaccia", "burgerboller", "hotdogbrød");

        // GROENGSAGER (Vegetables)
        addKeywords(Category.GROENGSAGER, "grøntsager", "tomat", "tomater", "cherrytomater",
                "agurk", "agurker", "salat", "iceberg salat", "rucola", "feldsalat",
                "romanosalat", "løg", "rødløg", "forårsløg", "kartoffel", "kartofler",
                "gulerod", "gulerødder", "peber", "peberfrugt", "peberfrugter", "squash",
                "zucchini", "courgette", "aubergine", "broccoli", "blomkål", "spidskål",
                "spinat", "kål", "hvidkål", "grønkål", "rosenkål", "selleri", "knoldselleri",
                "champignon", "champignoner", "svampe", "persille", "bladpersille",
                "koriander", "basilikum", "dild", "porrer", "porre", "rødbeder",
                "majskolber", "asparges", "artiskokker", "hvidløg", "chili", "chilipeber",
                "ingefær", "ærter", "bønner", "timian", "rosmarin", "mynte");

        // FRUGT (Fruit)
        addKeywords(Category.FRUGT, "frugt", "æble", "æbler", "banan", "bananer",
                "appelsin", "appelsiner", "citron", "citroner", "lime", "pære", "pærer",
                "druer", "vindruer", "jordbær", "blåbær", "hindbær", "brombær", "melon",
                "vandmelon", "honningmelon", "ananas", "mango", "kiwi", "fersken",
                "ferskner", "abrikos", "abrikoser", "nektarin", "blommer", "kirsebær",
                "klementiner", "mandariner", "granatæble", "passionsfrugt", "avocado");

        // KOED (Meat)
        addKeywords(Category.KOED, "kød", "hakket oksekød", "oksekød", "hakket svinekød",
                "svinekød", "hakket kylling", "hakket kalvekød", "kylling", "hel kylling",
                "kyllingebryst", "kyllingebrystfilet", "kyllingelår", "kyllingeinderlår",
                "steg", "kalkun", "kalkunbryst", "pølse", "pølser", "hamburger",
                "hakkekød", "bøf", "koteletter", "svinekoteletter", "nakkekoteletter",
                "medister", "medisterpølse", "and", "gris", "kalv", "kalvekød", "mørbrad",
                "svinemørbrad", "mørksejfilet", "oksefilet", "inderlår", "culotte",
                "entrecote", "ribben", "flæsk", "bacon", "lammekød", "lammekotelet", "fiskefrikadeller");

        // PAALAEG (Deli/Cold cuts)
        addKeywords(Category.PAALAEG, "pålæg", "skinke", "hamburgerryg", "tulip pålæg",
                "leverpostej", "spegepølse", "kartoffelspegepølse", "rullepølse",
                "3 stjernet pålæg", "hønsesalat", "kyllingepålæg", "tunsalat", "tun",
                "hummus", "madpandekager", "pizzadej", "pizzafyld", "pepperoni",
                "cocktailpølser", "salami", "chorizo", "laks", "røget laks", "makrel",
                "røget makrel", "sild", "rejer", "kaviar");

        // MEJERI (Dairy)
        addKeywords(Category.MEJERI, "mejeri", "mejeriprodukter", "minimælk", "mælk",
                "sødmælk", "letmælk", "skummetmælk", "ost", "mammen", "smør", "smørbar",
                "lurpak", "kærgården", "margarine", "yoghurt", "græsk yoghurt", "skyr",
                "fløde", "piskefløde", "creme fraiche", "æg", "æggeblommer", "hytteost",
                "cottage cheese", "ricotta", "mascarpone", "mozzarella", "parmesan",
                "feta", "brie", "cheddar", "danbo", "havarti", "maribo", "rygeost",
                "tykmælk", "kærnemælk");

        // FROST (Frozen)
        addKeywords(Category.FROST, "frost", "frostpizza", "pizza", "is", "modena",
                "frosne", "frosne grøntsager", "frosne bær", "frosne hindbær",
                "frosne jordbær", "frosne blåbær", "lasagne", "pomfritter", "pommes frites",
                "isterninger", "isvafler", "ispinde", "suppe", "fiskefileter");

        // TOERSTOF (Dry goods/Pantry)
        addKeywords(Category.TOERSTOF, "tørvarer", "pasta", "spaghetti", "penne", "fusilli",
                "macaroni", "tagliatelle", "lasagneplader", "ris", "basmatiris", "jasminris",
                "parboiled ris", "risotto", "mel", "hvedemel", "rugmel", "sukker", "flormelis",
                "havregryn", "musli", "müsli", "cornflakes", "ketchup", "mayo", "mayonnaise",
                "remoulade", "olie", "olivenolie", "solsikkeolie", "rapsolie", "madolie",
                "sesamolie", "eddike", "balsamico", "hvidvinseddike", "salt", "peber",
                "krydderi", "hønsebouillon", "bouillon", "oksebouillon", "grøntsagsbouillon",
                "dåse", "bønner", "kidneybønner", "sorte bønner", "hvide bønner", "kikærter",
                "linser", "konserves", "majs", "hakkede tomater", "flåede tomater",
                "tomatpuré", "pizzasovs", "tomatsauce", "passata", "kokosmælk", "honning",
                "syltetøj", "marmelade", "nutella", "pålægschokolade", "peanutbutter",
                "gær", "bagepulver", "natron", "vaniljesukker", "vanilje", "kanel",
                "kardemomme", "karry", "paprika", "spidskommen", "gurkemeje", "oregano",
                "laurbærblade", "sojasauce", "fiskesauce", "worcestersauce", "tabasco",
                "sriracha", "sirup", "ahornsirup", "senep", "dijonsenep", "oliven", "kapers",
                "artiskok", "sardiner", "ananas", "champignons");

        // DRIKKELSE (Beverages)
        addKeywords(Category.DRIKKELSE, "drikkevarer", "drinks", "vand", "mineralvand",
                "citronvand", "juice", "appelsinjuice", "æblejuice", "sodavand",
                "dåsesodavand", "soda", "cola", "pepsi", "øl", "abrikosdrik", "vin",
                "rødvin", "hvidvin", "rosévin", "champagne", "saftevand", "kaffe",
                "kaffebønner", "te", "mathilde", "kakao", "smoothie", "energidrik",
                "spiritus");

        // SNACKS (Snacks)
        addKeywords(Category.SNACKS, "snacks", "chips", "nødder", "nuts", "mandler",
                "cashewnødder", "peanuts", "hasselnødder", "valnødder", "chokolade",
                "mørk chokolade", "choko", "slik", "roulade", "popcorn", "kiks", "cookies",
                "småkager", "vingummi", "madpakkesnacks", "pizzastænger", "knækbrød",
                "crackers", "lakrids", "bolsjer", "tyggegummi");

        // Sort keywords by length (longest first) to prioritize specific matches
        SORTED_KEYWORDS.addAll(KEYWORD_MAP.keySet());
        SORTED_KEYWORDS.sort((s1, s2) -> {
            return Integer.compare(s2.length(), s1.length()); // Descending order
        });
    }

    private static void addKeywords(Category category, String... keywords) {
        for (String keyword : keywords) {
            KEYWORD_MAP.put(keyword.toLowerCase(), category);
        }
    }

    /**
     * Predict category based on item name
     * @param itemName The name of the grocery item
     * @return Predicted category, or DIVERSE if no match found
     */
    public static Category predictCategory(String itemName) {
        if (itemName == null || itemName.trim().isEmpty()) {
            return Category.DIVERSE;
        }

        String lowerName = itemName.toLowerCase().trim();

        // Check keywords in order of length (longest first)
        // This ensures "pålæg" matches before "æg", "svin" before "vin", etc.
        for (String keyword : SORTED_KEYWORDS) {
            if (lowerName.contains(keyword)) {
                return KEYWORD_MAP.get(keyword);
            }
        }

        // No match found, return default
        return Category.DIVERSE;
    }
}