package com.jaisocx.orderby.dto;


public class SortingStore {
    public String[] sourceStringsArray;
    public String[] sortedStringsArray;
    public byte[][] sourceValues;
    public byte[][] sortedValues;
    public long[][] comparableValues;
    public SortedValue[] sortedComparableValues;
    public SortedValueShort[] sortedComparableShortValues;
    public long[][] sortedComparableLongArrays;
    public long[] sortPositions;
    public long[] unorderedPositionsReverseBySortPosition;

    public record SortedValue(
        String sourceValue, 
        byte[] sourceByteArray, 
        long[] comparableValue, 
        long positionInSourceArray
    ) {
    }    

    public record SortedValueShort(
        long[] comparableValue, 
        long positionInSourceArray
    ) {
    }    
    
    public void init(int sortingArrayItemsCount, boolean toDebug) {
        this.sourceValues = new byte[sortingArrayItemsCount][];
        this.sortedValues = new byte[sortingArrayItemsCount][];
        this.comparableValues = new long[sortingArrayItemsCount][];
        this.sortPositions = new long[sortingArrayItemsCount];
        this.unorderedPositionsReverseBySortPosition = new long[sortingArrayItemsCount];

        if (toDebug) {
            this.sourceStringsArray = new String[sortingArrayItemsCount];
            this.sortedStringsArray = new String[sortingArrayItemsCount];
            this.sortedComparableValues = new SortedValue[sortingArrayItemsCount];
            this.sortedComparableShortValues = new SortedValueShort[sortingArrayItemsCount];
            this.sortedComparableLongArrays = new long[sortingArrayItemsCount][];
        }
    }
}
