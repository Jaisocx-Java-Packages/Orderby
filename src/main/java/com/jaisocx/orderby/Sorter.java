package com.jaisocx.orderby;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;

import com.jaisocx.orderby.dto.SortingStore;
import com.jaisocx.orderby.dto.SortingStore.SortedValue;
import com.jaisocx.orderby.dto.SortingStore.SortedValueShort;
import com.jaisocx.orderby.helpers.Comparator;
import com.jaisocx.orderby.helpers.SortedListSearcher;
import com.jaisocx.orderby.helpers.SortedListSearcher.SearchMode;
import com.jaisocx.orderby.helpers.TransformerToComparable;
import com.jaisocx.orderby.helpers.Comparator.ComparationResult;

import java.util.Arrays;

public class Sorter {
    public static final Charset CHARSET = StandardCharsets.UTF_8;
    public boolean toDebug = false;
    protected Charset charset = CHARSET;
    public long COUNTER = 0;

    public enum SortingDirection {
        NEXT_BIGGER(1),
        NEXT_LESS(2);

        private long value; 

        SortingDirection(long value) {
            this.value = value;
        }

        public long getValue() {
            return this.value;
        }
    }

    protected final SortingDirection SORTING_DIRECTION_DEFAULT = SortingDirection.NEXT_BIGGER;

    protected SortingStore store = null;

    public Sorter() {
    }

    public SortingStore getStore() {
        return store;
    }

    public byte[][] sort(String[] stringsArrayToSort, Charset stringsGetBytesCharset) {
        this.COUNTER = 0;

        if (stringsGetBytesCharset == null) {
            this.charset = CHARSET;
        }

        String[] copy = Arrays.copyOfRange(stringsArrayToSort, 0, stringsArrayToSort.length);
        long startTimeJavaCore = System.nanoTime();
        Arrays.sort(copy);
        long finishTimeJavaCore = System.nanoTime();

        long startTimeJaisocx1 = System.nanoTime();
        this.putStringsArrayToStore(stringsArrayToSort);
        long finishTimeJaisocx1 = System.nanoTime();
        long startTimeJaisocx = System.nanoTime();
        this.sort4();
        this.fillSortedValues();
        long finishTimeJaisocx = System.nanoTime();

        long jsxResult1 = (finishTimeJaisocx1 - startTimeJaisocx1);
        long jsxResult = (finishTimeJaisocx - startTimeJaisocx);
        long javaCoreResult = (finishTimeJavaCore - startTimeJavaCore);

        System.out.println("===================");
        System.out.println("Jaisocx sort indx: " + jsxResult1);
        System.out.println("Jaisocx sort:      " + jsxResult);
        System.out.println("Jaisocx sort total:" + (finishTimeJaisocx - startTimeJaisocx1));
        System.out.println("=================");
        System.out.println("Java Core sort:    " + javaCoreResult);
        System.out.println("=================");
        System.out.println("Diff: jsx - java:  " + (jsxResult1 + jsxResult - javaCoreResult));

        return this.store.sortedValues;
    }

    public void printSortedResults(
        PrintStream out, 
        byte[][] sortedByteArrays, 
        long limit, 
        long offset,
        SortingDirection sortingDirection
    ) {
        if (offset >= sortedByteArrays.length) {
            throw new ArrayIndexOutOfBoundsException();
        }

        String sortedString = new String();

        if (sortingDirection == SortingDirection.NEXT_BIGGER) {
            int counter = 0;
            for (byte[] sortedItem : sortedByteArrays) {
                if (counter < offset) {
                    counter++;
                    continue;
                }
                sortedString = (sortedItem == null) ? "[NULL VALUE]" : new String(sortedItem, this.charset);
                out.println(sortedString);
                counter++;
                if (counter > (offset + limit)) {
                    break;
                }
            }
        } else if (sortingDirection == SortingDirection.NEXT_LESS) {
            int positionInArray = 0;
            int itemsCount = sortedByteArrays.length;
            byte[] sortedItem = new byte[0];
            for (positionInArray = (itemsCount - 1); positionInArray > (-1); positionInArray--) {
                sortedItem = sortedByteArrays[positionInArray];
                sortedString = (sortedItem == null) ? "[NULL VALUE]" : new String(sortedItem, this.charset);
                out.println(sortedString);
            }
        }

        System.out.println("COUNTER ITEMS: " + this.store.sourceValues.length);
        System.out.println("COUNTER LOOPS ITERATIONS: " + this.COUNTER);
    }

    protected void putStringsArrayToStore(String[] stringsArray) {
        this.store = new SortingStore();

        int sortingArrayLength = stringsArray.length;
        this.store.init(sortingArrayLength, this.toDebug);

        byte[] sortingItemBytes = new byte[0];
        long[] comparableValue = new long[0];
        int counter = 0;
        for (String sortingArrayItem : stringsArray) {
            sortingItemBytes = sortingArrayItem.getBytes(this.charset);
            comparableValue = TransformerToComparable.transformByteArrayToComparable(
                sortingItemBytes
            );
            this.store.sourceValues[counter] = sortingItemBytes;
            this.store.comparableValues[counter] = comparableValue;
            counter++;
        }

        if (this.toDebug) {
            counter = 0;
            for (String sortingrrayItem : stringsArray) {
                this.store.sourceStringsArray[counter] = sortingrrayItem;
                counter++;
            }
        }
    }

    protected void fillSortedValues() {
        byte[] sourceValue = new byte[0];
        long loopFinishValue = this.store.sourceValues.length;
        long sortNumber = 0;
        long sourceIndexOfItemBySortPosition = 0;
        for (sortNumber = 0; sortNumber < loopFinishValue; sortNumber++) {
            sourceIndexOfItemBySortPosition = this.store.unorderedPositionsReverseBySortPosition[(int)sortNumber];
            sourceValue = this.store.sourceValues[(int)sourceIndexOfItemBySortPosition];
            this.store.sortedValues[(int)sortNumber] = sourceValue;
            if (this.toDebug == true) {
                this.store.sortedStringsArray[(int)sortNumber] = this.store.sourceStringsArray[(int)sourceIndexOfItemBySortPosition];
                this.store.sortedComparableLongArrays[(int)sortNumber] = this.store.comparableValues[(int)sourceIndexOfItemBySortPosition];
            }
        }
    }

    protected void sortForDebug() {

        long[] itemCurr = new long[0];
        long[] itemNext = new long[0];

        long itemsToSortCount = this.store.sourceValues.length;
        long sortingLoopFinishValue = itemsToSortCount - 1;

        ComparationResult comparationResult = ComparationResult.EQUAL_BOTH;

        int currentPositionInArray = 0;
        int nextPositionInArray = 0;

        this.store.sortPositions[0] = 0;
        this.store.sortedComparableValues[0] = new SortedValue(
            this.store.sourceStringsArray[0], 
            this.store.sourceValues[0], 
            this.store.comparableValues[0], 
            0
        );
        
        int nextMaxSortedPosition = 1;

        String curr;
        String next;
        String back;
        String rewrite;

        SortedValue sortedValue = null;
        for (currentPositionInArray = 0; currentPositionInArray < sortingLoopFinishValue; currentPositionInArray++) {
            /*String CHECK = this.sortedToTextLines(currentPositionInArray+1);
            System.out.println(CHECK);
            System.out.println("---------------------------------------");
            */
            nextPositionInArray = currentPositionInArray + 1;

            itemCurr = this.store.comparableValues[currentPositionInArray];
            if (currentPositionInArray == 0) {
                itemCurr = this.store.comparableValues[currentPositionInArray];
            } else {
                itemCurr = this.store.sortedComparableValues[currentPositionInArray].comparableValue();
            }

            itemNext = this.store.comparableValues[nextPositionInArray];

            curr = (currentPositionInArray == 0) ? this.store.sourceStringsArray[currentPositionInArray] : this.store.sortedComparableValues[currentPositionInArray].sourceValue();
            next = this.store.sourceStringsArray[nextPositionInArray];

            comparationResult = Comparator.compare(itemCurr, itemNext);

            // if both equal, assign to next item the pos nextMaxItemPos, inc
            if (comparationResult == ComparationResult.EQUAL_BOTH) {
                this.store.sortPositions[nextPositionInArray] = nextPositionInArray;
                this.store.sortedComparableValues[nextPositionInArray] = new SortedValue(
                    this.store.sourceStringsArray[nextPositionInArray], 
                    this.store.sourceValues[nextPositionInArray], 
                    itemNext, 
                    nextPositionInArray
                );
                //nextMaxSortedPosition++;
                continue;
            }

            // if current less than next, just put sorted position of next item to increased max val, and continue
            else if (comparationResult == ComparationResult.FIRST_LESS) {
                this.store.sortPositions[nextPositionInArray] = nextPositionInArray;
                this.store.sortedComparableValues[nextPositionInArray] = new SortedValue(
                    this.store.sourceStringsArray[nextPositionInArray], 
                    this.store.sourceValues[nextPositionInArray], 
                    itemNext, 
                    nextPositionInArray
                );
                //nextMaxSortedPosition++;
                continue;
            }

            /* if next less than current, 
                loop back direction til next bigger
                if next bigger, get compared value sortedPosition, inc, assign
                loop default direction til current inclusive, inc each
            */
            else if (comparationResult == ComparationResult.FIRST_BIGGER) {
                ComparationResult backLoopComparationResult = null;
                int backLoopPositionInSortedArray = currentPositionInArray;
                SortedValue backLoopValue = null;
                long positionOfbackLoopItemInSourceArray = (-1);
                for (
                    backLoopPositionInSortedArray = (currentPositionInArray); 
                    backLoopPositionInSortedArray > (-1); 
                    backLoopPositionInSortedArray--
                ) {
                    backLoopValue = this.store.sortedComparableValues[backLoopPositionInSortedArray];
                    positionOfbackLoopItemInSourceArray = backLoopValue.positionInSourceArray();
                    back = backLoopValue.sourceValue();

                    backLoopComparationResult = Comparator.compare(backLoopValue.comparableValue(), itemNext);
                    if (backLoopComparationResult == ComparationResult.FIRST_BIGGER) {
                        this.store.sortPositions[(int) backLoopValue.positionInSourceArray()] = backLoopPositionInSortedArray + 1;
                        this.store.sortedComparableValues[(int)(backLoopPositionInSortedArray + 1)] = new SortedValue(
                            this.store.sourceStringsArray[(int) positionOfbackLoopItemInSourceArray], 
                            this.store.sourceValues[(int) positionOfbackLoopItemInSourceArray], 
                            this.store.comparableValues[(int) positionOfbackLoopItemInSourceArray], 
                            (positionOfbackLoopItemInSourceArray)
                        );
                        continue;
                    }

                    break;
                }

                if (backLoopPositionInSortedArray == (-1)) {
                    backLoopPositionInSortedArray = 0;
                }

                if (backLoopComparationResult == ComparationResult.FIRST_BIGGER) {
                    backLoopPositionInSortedArray = 0;
                } else if (backLoopComparationResult != null && backLoopComparationResult != ComparationResult.FIRST_BIGGER) {
                    backLoopPositionInSortedArray++;
                }

                this.store.sortPositions[nextPositionInArray] = backLoopPositionInSortedArray;
                this.store.sortedComparableValues[(int) backLoopPositionInSortedArray] = new SortedValue(
                    this.store.sourceStringsArray[nextPositionInArray], 
                    this.store.sourceValues[nextPositionInArray], 
                    itemNext, 
                    nextPositionInArray
                );

                //nextMaxSortedPosition++;
                continue;
            }
        }
    }

    protected void sort2() {
        long[] itemCurr = new long[0];
        long[] itemNext = new long[0];

        long itemsToSortCount = this.store.sourceValues.length;
        long sortingLoopFinishValue = itemsToSortCount - 1;

        ComparationResult comparationResult = ComparationResult.EQUAL_BOTH;

        int currentPositionInArray = 0;
        int nextPositionInArray = 0;

        this.store.sortPositions[0] = 0;
        this.store.sortedComparableShortValues[0] = new SortedValueShort(
            this.store.comparableValues[0], 
            0
        );
        
        for (currentPositionInArray = 0; currentPositionInArray < sortingLoopFinishValue; currentPositionInArray++) {
            this.COUNTER++;

            nextPositionInArray = currentPositionInArray + 1;

            if (currentPositionInArray == 0) {
                itemCurr = this.store.comparableValues[currentPositionInArray];
            } else {
                itemCurr = this.store.sortedComparableShortValues[currentPositionInArray].comparableValue();
            }

            itemNext = this.store.comparableValues[nextPositionInArray];

            comparationResult = Comparator.compare(itemCurr, itemNext);

            // if both equal, assign to next item the pos nextMaxItemPos, inc
            if (comparationResult == ComparationResult.EQUAL_BOTH) {
                this.store.sortPositions[nextPositionInArray] = nextPositionInArray;
                this.store.sortedComparableShortValues[nextPositionInArray] = new SortedValueShort(
                    itemNext, 
                    nextPositionInArray
                );
                continue;
            }

            // if current less than next, just put sorted position of next item to increased max val, and continue
            else if (comparationResult == ComparationResult.FIRST_LESS) {
                this.store.sortPositions[nextPositionInArray] = nextPositionInArray;
                this.store.sortedComparableShortValues[nextPositionInArray] = new SortedValueShort(
                    itemNext, 
                    nextPositionInArray
                );
                continue;
            }

            /* if next less than current, 
                loop back direction til next bigger
                if next bigger, get compared value sortedPosition, inc, assign
                loop default direction til current inclusive, inc each
            */
            else if (comparationResult == ComparationResult.FIRST_BIGGER) {
                ComparationResult backLoopComparationResult = null;
                int backLoopPositionInSortedArray = currentPositionInArray;
                SortedValueShort backLoopValue = null;
                long positionOfbackLoopItemInSourceArray = (-1);
                for (
                    backLoopPositionInSortedArray = (currentPositionInArray); 
                    backLoopPositionInSortedArray > (-1); 
                    backLoopPositionInSortedArray--
                ) {
                    this.COUNTER++;

                    backLoopValue = this.store.sortedComparableShortValues[backLoopPositionInSortedArray];
                    positionOfbackLoopItemInSourceArray = backLoopValue.positionInSourceArray();

                    backLoopComparationResult = Comparator.compare(backLoopValue.comparableValue(), itemNext);
                    if (backLoopComparationResult == ComparationResult.FIRST_BIGGER) {
                        this.store.sortPositions[(int)positionOfbackLoopItemInSourceArray] = backLoopPositionInSortedArray + 1;
                        this.store.sortedComparableShortValues[(int)(backLoopPositionInSortedArray + 1)] = new SortedValueShort(
                            this.store.comparableValues[(int) positionOfbackLoopItemInSourceArray], 
                            (positionOfbackLoopItemInSourceArray)
                        );
                        continue;
                    }

                    break;
                }

                if (backLoopPositionInSortedArray == (-1)) {
                    backLoopPositionInSortedArray = 0;
                }

                if (backLoopComparationResult == ComparationResult.FIRST_BIGGER) {
                    backLoopPositionInSortedArray = 0;
                } else if (backLoopComparationResult != null && backLoopComparationResult != ComparationResult.FIRST_BIGGER) {
                    backLoopPositionInSortedArray++;
                }

                this.store.sortPositions[nextPositionInArray] = backLoopPositionInSortedArray;
                this.store.sortedComparableShortValues[(int) backLoopPositionInSortedArray] = new SortedValueShort(
                    itemNext, 
                    nextPositionInArray
                );

                continue;
            }
        }
    }

    protected void fillSortedItem(int sortedArrayPointer, int sourceArrayPointer) {
        this.store.sortedComparableLongArrays[sortedArrayPointer] = this.store.comparableValues[sourceArrayPointer];
        this.store.unorderedPositionsReverseBySortPosition[sortedArrayPointer] = sourceArrayPointer;
    }

    protected void sort3() {
        long[][] comparableValues = this.store.comparableValues;
        long[][] sortedComparableLongArrays = this.store.sortedComparableLongArrays;
        long[] sourceValuesSortedPositions = this.store.sortPositions;
        long[] sortedValuesSourcePositions = this.store.unorderedPositionsReverseBySortPosition;
    
        long[] itemCurr = new long[0];
        long[] itemNext = new long[0];

        long itemsToSortCount = this.store.sourceValues.length;
        long sortingLoopFinishValue = itemsToSortCount - 1;

        ComparationResult comparationResult = ComparationResult.EQUAL_BOTH;

        int currentPositionInArray = 0;
        int nextPositionInArray = 0;

        sourceValuesSortedPositions[0] = 0;
        this.fillSortedItem(0, 0);

        for (currentPositionInArray = 0; currentPositionInArray < sortingLoopFinishValue; currentPositionInArray++) {
            this.COUNTER++;

            nextPositionInArray = currentPositionInArray + 1;

            if (currentPositionInArray == 0) {
                itemCurr = comparableValues[currentPositionInArray];
            } else {
                itemCurr = sortedComparableLongArrays[currentPositionInArray];
            }

            itemNext = comparableValues[nextPositionInArray];

            comparationResult = Comparator.compare(itemCurr, itemNext);

            // if both equal, assign to next item the pos nextMaxItemPos, inc
            if (comparationResult == ComparationResult.EQUAL_BOTH) {
                sourceValuesSortedPositions[nextPositionInArray] = nextPositionInArray;
                this.fillSortedItem(nextPositionInArray, nextPositionInArray);
                continue;
            }

            // if current less than next, just put sorted position of next item to increased max val, and continue
            else if (comparationResult == ComparationResult.FIRST_LESS) {
                sourceValuesSortedPositions[nextPositionInArray] = nextPositionInArray;
                this.fillSortedItem(nextPositionInArray, nextPositionInArray);
                continue;
            }

            /* if next less than current, 
                loop back direction til next bigger
                if next bigger, get compared value sortedPosition, inc, assign
                loop default direction til current inclusive, inc each
            */
            else if (comparationResult == ComparationResult.FIRST_BIGGER) {
                ComparationResult backLoopComparationResult = null;
                int backLoopPositionInSortedArray = currentPositionInArray;
                long positionOfbackLoopItemInSourceArray = (-1);
                long[] comparableValue = new long[0];
                for (
                    backLoopPositionInSortedArray = (currentPositionInArray); 
                    backLoopPositionInSortedArray > (-1); 
                    backLoopPositionInSortedArray--
                ) {
                    this.COUNTER++;

                    comparableValue = sortedComparableLongArrays[backLoopPositionInSortedArray];
                    positionOfbackLoopItemInSourceArray = sortedValuesSourcePositions[backLoopPositionInSortedArray];

                    backLoopComparationResult = Comparator.compare(comparableValue, itemNext);
                    if (backLoopComparationResult == ComparationResult.FIRST_BIGGER) {
                        sourceValuesSortedPositions[(int)positionOfbackLoopItemInSourceArray] = backLoopPositionInSortedArray + 1;
                        this.fillSortedItem((int)(backLoopPositionInSortedArray + 1), (int)positionOfbackLoopItemInSourceArray);

                        continue;
                    }

                    break;
                }

                if (backLoopPositionInSortedArray == (-1)) {
                    backLoopPositionInSortedArray = 0;
                }

                if (backLoopComparationResult == ComparationResult.FIRST_BIGGER) {
                    backLoopPositionInSortedArray = 0;
                } else if (backLoopComparationResult != null && backLoopComparationResult != ComparationResult.FIRST_BIGGER) {
                    backLoopPositionInSortedArray++;
                }

                sourceValuesSortedPositions[nextPositionInArray] = backLoopPositionInSortedArray;
                this.fillSortedItem((int) backLoopPositionInSortedArray, nextPositionInArray);

                continue;
            }
        }
    }

    protected void sort4() {
        long COMPARED_FIRST_BIGGER = ComparationResult.FIRST_BIGGER.getValue();
        long COMPARED_EQUAL_BOTH = ComparationResult.EQUAL_BOTH.getValue();
        //long COMPARED_FIRST_LESS = ComparationResult.FIRST_LESS.getValue();

        long[][] comparableValues = this.store.comparableValues;
        long[] itemCurr = new long[0];
        long[] itemNext = new long[0];

        long[] sortPositions = this.store.sortPositions;
        long[] unorderedPositionsReverseBySortPosition = this.store.unorderedPositionsReverseBySortPosition;

        long sourceIndex = 0;
    
        long itemsToSortCount = this.store.sourceValues.length;
        long sortingLoopFinishValue = itemsToSortCount - 1;

        long comparationResult = COMPARED_EQUAL_BOTH;

        int currentPositionInArray = 0;
        int nextPositionInArray = 0;

        sortPositions[0] = 0;

        long backLoopComparationResult = COMPARED_FIRST_BIGGER;
        long[] comparableValue = new long[0];

        // @var: looping from the highest sorted position in array, comparing each value in NEXT_LESS order
        int positionSortedOfbackLoopItem = 0;
        int backLoopStartValue = currentPositionInArray; // (nextPositionInArray == itemsToSortCount) ? (nextPositionInArray - 1) : nextPositionInArray;
        int nextBackLoopSortPosition = 0;
        for (currentPositionInArray = 0; currentPositionInArray < sortingLoopFinishValue; currentPositionInArray++) {
            this.COUNTER++;

            nextPositionInArray = currentPositionInArray + 1;

            // the position in the source arrays by reverse lookup og the last appended bigggest value in the sorted list
            sourceIndex = unorderedPositionsReverseBySortPosition[currentPositionInArray];

            itemCurr = comparableValues[(int)sourceIndex];
            itemNext = comparableValues[nextPositionInArray];

            comparationResult = Comparator.compare(itemCurr, itemNext).getValue();

            // if both equal, assign to next item the pos nextMaxItemPos, inc
            // if current less than next, just put sorted position of next item to increased max val, and continue
            if (comparationResult != COMPARED_FIRST_BIGGER) {
                sortPositions[nextPositionInArray] = nextPositionInArray;
                unorderedPositionsReverseBySortPosition[nextPositionInArray] = nextPositionInArray;
                continue;
            }

            /* if next less than current, 
                loop back direction til next bigger
                if next bigger, get compared value sortedPosition, inc, assign
                loop default direction til current inclusive, inc each
            */
            else if (comparationResult == COMPARED_FIRST_BIGGER) {
                backLoopComparationResult = COMPARED_FIRST_BIGGER;
                backLoopStartValue = currentPositionInArray;
                for (
                    positionSortedOfbackLoopItem = backLoopStartValue; 
                    positionSortedOfbackLoopItem > (-1); 
                    positionSortedOfbackLoopItem--
                ) {
                    this.COUNTER++;

                    // @var: we should get the position in the UNORDERED SOURCE array, by it's sort position, to get the source comparable
                    sourceIndex = unorderedPositionsReverseBySortPosition[positionSortedOfbackLoopItem];

                    // @var: the comparable value of the next less, sorted from top, 
                    comparableValue = comparableValues[(int)sourceIndex];
    
                    // compare 
                    backLoopComparationResult = Comparator.compare(comparableValue, itemNext).getValue();
                    if (backLoopComparationResult == COMPARED_FIRST_BIGGER) {
                        // incrementing sort position for this next less comparable item from top
                        nextBackLoopSortPosition = positionSortedOfbackLoopItem + 1;
                        sortPositions[(int)sourceIndex] = nextBackLoopSortPosition;
                        unorderedPositionsReverseBySortPosition[nextBackLoopSortPosition] = sourceIndex;
                        continue;
                    }

                    break;
                }

                // so we detect the dafault value, and the fact, that the back loop did not change it. so, set the first position = 0.
                if (positionSortedOfbackLoopItem == (-1)) {
                    nextBackLoopSortPosition = 0;
                }

                // this case means, theere was not a smallercomparable value found, so the new item is the smallest. first position = 0.
                if (backLoopComparationResult == COMPARED_FIRST_BIGGER) {
                    nextBackLoopSortPosition = 0;
                } 
                // in this case, we have detected less or equal comparable item in the sorted array, so for the new item we calculate the next sorted position, += 1. 
                else if (backLoopComparationResult != COMPARED_FIRST_BIGGER) {
                    nextBackLoopSortPosition = positionSortedOfbackLoopItem + 1;
                }

                // for the next Item, put it's new sorted position after all calculations
                sortPositions[nextPositionInArray] = nextBackLoopSortPosition;
                unorderedPositionsReverseBySortPosition[nextBackLoopSortPosition] = nextPositionInArray;

                continue;
            }
        }
    }

    protected void sort5() {
        long COMPARED_FIRST_BIGGER = ComparationResult.FIRST_BIGGER.getValue();
        long COMPARED_EQUAL_BOTH = ComparationResult.EQUAL_BOTH.getValue();

        SortedListSearcher sortedListSearcher = new SortedListSearcher();

        long[][] comparableValues = this.store.comparableValues;
        long[] itemCurr = new long[0];
        long[] itemNext = new long[0];

        long[] sortPositions = this.store.sortPositions;
        long[] unorderedPositionsReverseBySortPosition = this.store.unorderedPositionsReverseBySortPosition;

        long sourceIndex = 0;
    
        long itemsToSortCount = this.store.sourceValues.length;
        long sortingLoopFinishValue = itemsToSortCount - 1;

        long comparationResult = COMPARED_EQUAL_BOTH;

        int currentPositionInArray = 0;
        int nextPositionInArray = 0;

        sortPositions[0] = 0;

        long backLoopComparationResult = COMPARED_FIRST_BIGGER;
        long[] comparableValue = new long[0];

        // @var: looping from the highest sorted position in array, comparing each value in NEXT_LESS order
        int positionSortedOfbackLoopItem = 0;
        int backLoopStartValue = currentPositionInArray; // (nextPositionInArray == itemsToSortCount) ? (nextPositionInArray - 1) : nextPositionInArray;
        int nextBackLoopSortPosition = 0;
        for (currentPositionInArray = 0; currentPositionInArray < sortingLoopFinishValue; currentPositionInArray++) {
            this.COUNTER++;

            nextPositionInArray = currentPositionInArray + 1;

            // the position in the source arrays by reverse lookup og the last appended bigggest value in the sorted list
            sourceIndex = unorderedPositionsReverseBySortPosition[currentPositionInArray];

            itemCurr = comparableValues[(int)sourceIndex];
            itemNext = comparableValues[nextPositionInArray];

            comparationResult = Comparator.compare(itemCurr, itemNext).getValue();

            // if both equal, assign to next item the pos nextMaxItemPos, inc
            // if current less than next, just put sorted position of next item to increased max val, and continue
            if (comparationResult != COMPARED_FIRST_BIGGER) {
                sortPositions[nextPositionInArray] = nextPositionInArray;
                unorderedPositionsReverseBySortPosition[nextPositionInArray] = nextPositionInArray;
                continue;
            }

            /* if next less than current, 
                loop back direction til next bigger
                if next bigger, get compared value sortedPosition, inc, assign
                loop default direction til current inclusive, inc each
            */
            else if (comparationResult == COMPARED_FIRST_BIGGER) {
                backLoopComparationResult = COMPARED_FIRST_BIGGER;
                backLoopStartValue = currentPositionInArray;

                long fastIndexSearchSortPointer = sortedListSearcher.findWithSortPointers (
                    SearchMode.FIND_NEXT_BIGGER, 
                    itemNext, 
                    comparableValues, 
                    unorderedPositionsReverseBySortPosition,
                    0, 
                    backLoopStartValue
                );

                // so we detect the dafault value, and the fact, that the back loop did not change it. so, set the first position = 0.
                if (fastIndexSearchSortPointer == (-1)) {
                    fastIndexSearchSortPointer = 0;
                }

                for (
                    positionSortedOfbackLoopItem = backLoopStartValue; 
                    positionSortedOfbackLoopItem >= fastIndexSearchSortPointer; 
                    positionSortedOfbackLoopItem--
                ) {
                    this.COUNTER++;

                    // @var: we should get the position in the UNORDERED SOURCE array, by it's sort position, to get the source comparable
                    sourceIndex = unorderedPositionsReverseBySortPosition[positionSortedOfbackLoopItem];

                    // incrementing sort position for this next less comparable item from top
                    nextBackLoopSortPosition = positionSortedOfbackLoopItem + 1;
                    sortPositions[(int)sourceIndex] = nextBackLoopSortPosition;
                    unorderedPositionsReverseBySortPosition[nextBackLoopSortPosition] = sourceIndex;
                }

                // for the next Item, put it's new sorted position after all calculations
                sortPositions[nextPositionInArray] = fastIndexSearchSortPointer;
                unorderedPositionsReverseBySortPosition[(int)fastIndexSearchSortPointer] = nextPositionInArray;
                
                continue;
            }
        }
    }

    private String sortedToJsonArray(int maxCount) {
        JsonArrayBuilder builder = Json.createArrayBuilder();
        
        int counter = 0;
        for (SortedValue value : this.store.sortedComparableValues) {
            builder.add(value.sourceValue());
            counter++;
            if (counter >= maxCount) {
                break;
            }
        }
        JsonArray array = builder.build();

        String retVal = array.toString();
        return retVal;
    }

    private String sortedToTextLines(int maxCount) {
        StringBuilder stringBuilder = new StringBuilder();
        int counter = 0;
        for (SortedValue value : this.store.sortedComparableValues) {
            stringBuilder.append(value.sourceValue()).append("\r\n");
            counter++;
            if (counter >= maxCount) {
                break;
            }
        }
        stringBuilder.trimToSize();

        String retVal = stringBuilder.toString();
        return retVal;
    }
}
