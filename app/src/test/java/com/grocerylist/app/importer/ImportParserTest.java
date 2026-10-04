package com.grocerylist.app.importer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.grocerylist.app.models.ListCategory;

import org.junit.Test;

import java.math.BigDecimal;

/** Mirrors the examples on the browser test page (importtest.html). */
public class ImportParserTest {

    private static ImportResult parse(String text) {
        return ImportParser.parse(text);
    }

    private static boolean hasErrorContaining(ImportResult result, String fragment) {
        for (String error : result.getErrors()) {
            if (error.contains(fragment)) {
                return true;
            }
        }
        return false;
    }

    // ===== VALID INPUT =====

    @Test
    public void validPayload_parsesAllFields() {
        ImportResult result = parse("{\"version\":1,\"lists\":[{\"name\":\"Weekend\",\"store\":\"REMA\",\"items\":["
                + "{\"name\":\"mælk\",\"quantity\":2},"
                + "{\"name\":\"franskbrød\"},"
                + "{\"name\":\"hakket oksekød\",\"quantity\":500,\"unit\":\"g\",\"price\":40,\"onOffer\":true},"
                + "{\"name\":\"kaffe\",\"note\":\"økologisk\"}]}]}");

        assertTrue(result.getErrors().toString(), result.isValid());
        assertEquals(1, result.getLists().size());
        ImportList list = result.getLists().get(0);
        assertEquals("Weekend", list.getName());
        assertEquals(ListCategory.REMA, list.getStore());
        assertEquals(4, list.getItems().size());

        ImportItem milk = list.getItems().get(0);
        assertEquals(0, milk.getQuantity().compareTo(new BigDecimal(2)));
        assertNull(milk.getUnit());

        ImportItem bread = list.getItems().get(1);
        assertEquals(0, bread.getQuantity().compareTo(BigDecimal.ONE));

        ImportItem meat = list.getItems().get(2);
        assertEquals("g", meat.getUnit());
        assertEquals(0, meat.getPrice().compareTo(new BigDecimal(40)));
        assertTrue(meat.isOnOffer());

        assertEquals("økologisk", list.getItems().get(3).getNote());
    }

    @Test
    public void listWithoutNameOrStore_isValid() {
        ImportResult result = parse("{\"version\":1,\"lists\":[{\"items\":[{\"name\":\"æg\"}]}]}");
        assertTrue(result.isValid());
        assertNull(result.getLists().get(0).getName());
        assertNull(result.getLists().get(0).getStore());
    }

    @Test
    public void namesAreTrimmedAndWhitespaceCollapsed() {
        ImportResult result = parse("{\"version\":1,\"lists\":[{\"items\":[{\"name\":\"  hakket   oksekød \"}]}]}");
        assertEquals("hakket oksekød", result.getLists().get(0).getItems().get(0).getName());
    }

    // ===== CLEAN-UP OF SHARED TEXT =====

    @Test
    public void proseAndCodeFenceAroundJson_isIgnored() {
        ImportResult result = parse("Her er din liste:\n\n```json\n"
                + "{\"version\":1,\"lists\":[{\"items\":[{\"name\":\"havregryn\"}]}]}\n```\n\nGod fornøjelse!");
        assertTrue(result.getErrors().toString(), result.isValid());
    }

    @Test
    public void nestedCodeFences_areRemoved() {
        ImportResult result = parse("```json\n```json\n"
                + "{\"version\":1,\"lists\":[{\"items\":[{\"name\":\"mælk\",\"quantity\":2,\"unit\":\"l\"},"
                + "{\"name\":\"rugbrød\"},{\"name\":\"boller\"}]}]}\n```\n```");
        assertTrue(result.getErrors().toString(), result.isValid());
        assertEquals(3, result.getItemCount());
    }

    @Test
    public void braceInsideString_doesNotEndObject() {
        ImportResult result = parse("{\"version\":1,\"lists\":[{\"items\":[{\"name\":\"chips\",\"note\":\"store } poser\"}]}]}");
        assertTrue(result.getErrors().toString(), result.isValid());
        assertEquals("store } poser", result.getLists().get(0).getItems().get(0).getNote());
    }

    @Test
    public void emptyText_isRejected() {
        assertFalse(parse("   ").isValid());
        assertFalse(parse(null).isValid());
    }

    @Test
    public void proseOnly_isRejected() {
        ImportResult result = parse("Jeg skal bruge mælk, æg og brød.");
        assertFalse(result.isValid());
        assertTrue(hasErrorContaining(result, "JSON-objekt"));
    }

    @Test
    public void unclosedJson_isRejected() {
        ImportResult result = parse("{\"version\":1,\"lists\":[{\"items\":[{\"name\":\"mælk\"}]}");
        assertFalse(result.isValid());
        assertTrue(hasErrorContaining(result, "ikke afsluttet"));
    }

    @Test
    public void lenientJson_isRejected() {
        ImportResult result = parse("{version:1,'lists':[{\"items\":[{\"name\":\"mælk\"}]}]}");
        assertFalse(result.isValid());
    }

    @Test
    public void tooLongText_isRejected() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ImportTextExtractor.MAX_INPUT_CHARS + 1; i++) {
            sb.append('x');
        }
        assertTrue(hasErrorContaining(parse(sb.toString()), "for lang"));
    }

    // ===== DUPLICATES =====

    @Test
    public void duplicatesWithSameDetails_areMerged() {
        ImportResult result = parse("{\"version\":1,\"lists\":[{\"items\":["
                + "{\"name\":\"mælk\"},{\"name\":\"æg\"},{\"name\":\"Mælk\",\"quantity\":2}]}]}");
        assertTrue(result.isValid());
        assertEquals(2, result.getItemCount());
        assertEquals(0, result.getLists().get(0).getItems().get(0).getQuantity().compareTo(new BigDecimal(3)));
        assertEquals(1, result.getWarnings().size());
    }

    @Test
    public void duplicatesWithDifferentPrice_areKeptSeparate() {
        ImportResult result = parse("{\"version\":1,\"lists\":[{\"items\":["
                + "{\"name\":\"kaffe\",\"price\":35},{\"name\":\"kaffe\",\"price\":29.95,\"onOffer\":true}]}]}");
        assertTrue(result.isValid());
        assertEquals(2, result.getItemCount());
        assertTrue(result.getWarnings().get(0).contains("Begge linjer"));
    }

    @Test
    public void sameNameDifferentUnit_isNotMerged() {
        ImportResult result = parse("{\"version\":1,\"lists\":[{\"items\":["
                + "{\"name\":\"mælk\",\"quantity\":2},{\"name\":\"mælk\",\"quantity\":2,\"unit\":\"l\"}]}]}");
        assertTrue(result.isValid());
        assertEquals(2, result.getItemCount());
        assertTrue(result.getWarnings().isEmpty());
    }

    @Test
    public void mergeExceedingPieceLimit_isRejected() {
        ImportResult result = parse("{\"version\":1,\"lists\":[{\"items\":["
                + "{\"name\":\"æg\",\"quantity\":60},{\"name\":\"æg\",\"quantity\":60}]}]}");
        assertFalse(result.isValid());
        assertTrue(hasErrorContaining(result, "efter sammenlægning"));
    }

    // ===== STRUCTURE AND LIMITS =====

    @Test
    public void tooManyItems_isRejected() {
        StringBuilder items = new StringBuilder();
        for (int i = 1; i <= 51; i++) {
            if (i > 1) {
                items.append(',');
            }
            items.append("{\"name\":\"vare ").append(i).append("\"}");
        }
        ImportResult result = parse("{\"version\":1,\"lists\":[{\"items\":[" + items + "]}]}");
        assertFalse(result.isValid());
        assertTrue(hasErrorContaining(result, "51 varer"));
    }

    @Test
    public void unknownFields_areRejected() {
        ImportResult result = parse("{\"version\":1,\"lists\":[{\"name\":\"Fest\",\"createIfMissing\":true,\"items\":["
                + "{\"name\":\"chips\",\"category\":\"SNACKS\"}]}]}");
        assertFalse(result.isValid());
        assertTrue(hasErrorContaining(result, "lists[0].createIfMissing"));
        assertTrue(hasErrorContaining(result, "lists[0].items[0].category"));
    }

    @Test
    public void wrongVersion_isRejected() {
        assertTrue(hasErrorContaining(parse("{\"version\":2,\"lists\":[{\"items\":[{\"name\":\"æg\"}]}]}"), "version"));
        assertTrue(hasErrorContaining(parse("{\"lists\":[{\"items\":[{\"name\":\"æg\"}]}]}"), "version mangler"));
    }

    @Test
    public void unknownStore_isRejected() {
        ImportResult result = parse("{\"version\":1,\"lists\":[{\"store\":\"NETTO\",\"items\":[{\"name\":\"æg\"}]}]}");
        assertFalse(result.isValid());
        assertTrue(hasErrorContaining(result, "store"));
    }

    @Test
    public void emptyItems_isRejected() {
        assertTrue(hasErrorContaining(parse("{\"version\":1,\"lists\":[{\"items\":[]}]}"), "mindst én vare"));
    }

    // ===== QUANTITY, UNIT, PRICE, OFFER =====

    @Test
    public void badQuantities_areRejected() {
        ImportResult result = parse("{\"version\":1,\"lists\":[{\"items\":["
                + "{\"name\":\"agurker\",\"quantity\":0},"
                + "{\"name\":\"løg\",\"quantity\":\"2\"},"
                + "{\"name\":\"kartofler\",\"quantity\":1.5}]}]}");
        assertFalse(result.isValid());
        assertEquals(3, result.getErrors().size());
    }

    @Test
    public void decimalAmountWithUnit_isAccepted() {
        ImportResult result = parse("{\"version\":1,\"lists\":[{\"items\":[{\"name\":\"kartofler\",\"quantity\":0.5,\"unit\":\"kg\"}]}]}");
        assertTrue(result.getErrors().toString(), result.isValid());
    }

    @Test
    public void unitProblems_areRejected() {
        ImportResult result = parse("{\"version\":1,\"lists\":[{\"items\":["
                + "{\"name\":\"ost\",\"unit\":\"pakke\",\"quantity\":1},"
                + "{\"name\":\"mel\",\"unit\":\"kg\"}]}]}");
        assertFalse(result.isValid());
        assertTrue(hasErrorContaining(result, "items[0].unit"));
        assertTrue(hasErrorContaining(result, "items[1].quantity mangler"));
    }

    @Test
    public void priceProblems_areRejected() {
        ImportResult result = parse("{\"version\":1,\"lists\":[{\"items\":["
                + "{\"name\":\"smør\",\"price\":12.345},"
                + "{\"name\":\"rugbrød\",\"price\":\"25 kr\"},"
                + "{\"name\":\"æbler\",\"onOffer\":\"ja\"}]}]}");
        assertFalse(result.isValid());
        assertTrue(hasErrorContaining(result, "2 decimaler"));
        assertTrue(hasErrorContaining(result, "items[1].price"));
        assertTrue(hasErrorContaining(result, "items[2].onOffer"));
    }

    // ===== MAPPING TO THE APP'S FORMAT =====

    @Test
    public void numbersAreFormattedInDanish() {
        assertEquals("2", ImportItemMapper.formatNumber(new BigDecimal("2")));
        assertEquals("2", ImportItemMapper.formatNumber(new BigDecimal("2.0")));
        assertEquals("0,5", ImportItemMapper.formatNumber(new BigDecimal("0.5")));
        assertEquals("500", ImportItemMapper.formatNumber(new BigDecimal("500")));
        assertEquals("35", ImportItemMapper.formatPrice(new BigDecimal("35")));
        assertEquals("34,95", ImportItemMapper.formatPrice(new BigDecimal("34.95")));
        assertEquals("29,50", ImportItemMapper.formatPrice(new BigDecimal("29.5")));
    }

    @Test
    public void unitsAreMappedToAppUnits() {
        assertEquals("stk", ImportItemMapper.mapUnit(null));
        assertEquals("L", ImportItemMapper.mapUnit("l"));
        assertEquals("g", ImportItemMapper.mapUnit("g"));
        assertEquals("kg", ImportItemMapper.mapUnit("kg"));
        assertEquals("ml", ImportItemMapper.mapUnit("ml"));
    }
}
