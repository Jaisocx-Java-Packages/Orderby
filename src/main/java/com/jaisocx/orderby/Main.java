package com.jaisocx.orderby;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import javax.json.JsonValue;

import com.jaisocx.orderby.helpers.SortedListSearcher;
import com.jaisocx.orderby.helpers.TransformerToComparable;
import com.jaisocx.orderby.helpers.SortedListSearcher.SearchMode;

class Main {
    public static final int LIMIT = 100_000;
    public static final int LIMIT_PRINT = 200;
    public static void main(String[] args) throws Exception {
        String[] unsortedStrings = new String[0];
        //unsortedStrings = readJson();
        unsortedStrings = readCsv();

        Sorter sorter = new Sorter();
        sorter.toDebug = false;
        byte[][] sorted = sorter.sort(unsortedStrings, null);
        sorter.printSortedResults(System.out, sorted, Math.min(LIMIT, LIMIT_PRINT), 5_000, Sorter.SortingDirection.NEXT_BIGGER);

        SortedListSearcher sortedListSearcher = new SortedListSearcher();
        String valueToSearch = "Pellentesque malesuada nulla a mi.";
        valueToSearch = "Battery Electric Vehicle (BEV)";

        long startSearch1 = System.nanoTime();
        long[] comparableToSearch = TransformerToComparable.transformByteArrayToComparable(valueToSearch.getBytes(sorter.charset));

        long startSearch2 = System.nanoTime();
        /*long index = sortedListSearcher.findInSorted (
            SearchMode.FIND_NEXT_BIGGER, 
            comparableToSearch, 
            sorter.getStore().sortedComparableLongArrays, 
            0, 
            (sorter.getStore().sortedComparableLongArrays.length)
        );*/
        long index2 = sortedListSearcher.findWithSortPointers (
            SearchMode.FIND_NEXT_BIGGER, 
            comparableToSearch, 
            sorter.getStore().comparableValues, 
            sorter.getStore().unorderedPositionsReverseBySortPosition,
            0, 
            (sorter.getStore().comparableValues.length)
        );
        /*System.out.println("2 searches equal: " + (index == index2));*/

        long finishSearch = System.nanoTime();
        if (index2 < 0) {
            System.out.println("LOOKUP ERR: " + index2);
            return;
        }

        //String valueFound = new String(sorter.getStore().sortedValues[(int)index], sorter.charset);
        String valueFound2 = new String(sorter.getStore().sortedValues[(int)index2], sorter.charset);

        //System.out.println("INDEX FOUND:          " + index);
        System.out.println("INDEX2 FOUND:         " + index2);
        System.out.println("String To Search:     " + valueToSearch);
        //System.out.println("String found:         " + valueFound);
        System.out.println("String found2:        " + valueFound2);
        System.out.println("COMPARATIONS_COUNT:   " + sortedListSearcher.COMPARATIONS_COUNT);
        System.out.println("\r\nNANOS:");
        System.out.println("Search with transform String to long[]: " + (finishSearch - startSearch1));
        System.out.println("Transform call:                         " + (startSearch2 - startSearch1));
        System.out.println("Search method call:                     " + (finishSearch - startSearch2));
        System.out.println("");
        System.out.println("");
    }

    private static String[] readJson() throws IOException {
        String jsonPath = "/Users/illiapolianskyi/Projects/sorting/src/main/resources/unorderedJsonArrayOfStrings.json";
        FileInputStream fileInputStream = new FileInputStream(jsonPath);
        //byte[] jsonContents = fileInputStream.readAllBytes();
        //String jsonContentsString = new String(jsonContents, StandardCharsets.UTF_8);

        JsonReader jsonReader = Json.createReader(fileInputStream);
        JsonArray jsonArray = jsonReader.readArray();
        jsonReader.close();
        fileInputStream.close();

        String[] unsortedStrings = new String[jsonArray.size()];
        int positionInArray = 0;
        for (JsonValue value : jsonArray) {
            unsortedStrings[positionInArray] = value.toString().replaceAll("\"", "");
            positionInArray++;
        }
        return unsortedStrings;
    }

    private static String[] readCsv() {
        String[] result = new String[0];

        String path = "/Users/illiapolianskyi/Projects/sorting/src/main/resources/Electric_Vehicle_Population_Data.csv";
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        byte[] contents = new byte[0];
        try {
            contents = fileInputStream.readAllBytes();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            fileInputStream.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String contentsString = new String(contents, StandardCharsets.US_ASCII);
        String[] lines = contentsString.split("\n");
        List<String> list = new ArrayList<>();
        int counter2 = 0;
        int limit = LIMIT;
        for (String line : lines) {
            line = line.trim();
            String[] cols = line.split(",");
            for (String word : cols) {
                list.add(word);
                counter2++;
                if (counter2 > limit) {
                    break;
                }
            }
            if (counter2 > limit) {
                break;
            }
        }

        Object[] objs = list.toArray();
        result = new String[counter2];
        int counter = 0;
        String s = new String();
        for (Object o : objs) {
            s = o.toString().trim();
            if (s.length() < 5) {
                continue;
            }
            result[counter] = o.toString();
            counter++;
        }

        result = java.util.Arrays.copyOfRange(result, 0, counter);

        return result;
    }
}
