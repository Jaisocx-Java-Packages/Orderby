package com.jaisocx.orderby.helpers;

import com.jaisocx.orderby.helpers.Comparator.ComparationResult;

public class SortedListSearcher {
    public static final long RESULT_NOT_FOUND = (-1);
    public int LOOKUP_VIA_ITERATIONS_TIL_ITEMS_COUNT = 10;
    public long COMPARATIONS_COUNT = 0;

    public enum SearchMode {
        FIND_EXACT,
        FIND_NEXT_BIGGER;
    }

    protected ComparationResult compare(long[] a, long[] b) {
        this.COMPARATIONS_COUNT++;
        return Comparator.compare(a, b);
    }

    /**
     * only sorted arrays supported
     * @param sortedComparablesArray
     * @param searchMode
     * @return the matching array item position in the array
     */
    public long findInSorted (
        SearchMode searchMode,
        long[] itemToSearch,
        long[][] sortedComparablesArray,
        long from,
        long to
    ) {
        long result = RESULT_NOT_FOUND;

        if (sortedComparablesArray.length > this.LOOKUP_VIA_ITERATIONS_TIL_ITEMS_COUNT) {
            try {
                result = this.findInSortedByJumps(searchMode, itemToSearch, sortedComparablesArray, from, to);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            result = this.findInSortedByLoop(searchMode, itemToSearch, sortedComparablesArray, from, to);
        }

        return result;
    }

    public long findWithSortPointers (
        SearchMode searchMode,
        long[] itemToSearch,
        long[][] unsortedComparablesArray,
        long[] reverseLookupTable,
        long from,
        long to
    ) {
        long result = RESULT_NOT_FOUND;

        if (unsortedComparablesArray.length > this.LOOKUP_VIA_ITERATIONS_TIL_ITEMS_COUNT) {
            try {
                result = this.findInUnsortedByJumps(searchMode, itemToSearch, unsortedComparablesArray, reverseLookupTable, from, to);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            result = this.findInUnsortedByLoop(searchMode, itemToSearch, unsortedComparablesArray, reverseLookupTable, from, to);
        }

        return result;
    }

    private boolean verifyItemValueIsMoreThanSmallest (
        long[] itemToSearch,
        long[][] sortedComparablesArray,
        long from,
        long to
    ) {
        boolean result = true;
        long[] itemFirst = sortedComparablesArray[(int)from];
        ComparationResult comparationResult = this.compare(itemFirst, itemToSearch);
        if (comparationResult == ComparationResult.FIRST_BIGGER) {
            result = false;
        }
        
        return result;
    }

    private boolean verifyItemValueIsLessThanLargest (
        long[] itemToSearch,
        long[][] sortedComparablesArray,
        long from,
        long to
    ) {
        boolean result = true;

        long[] itemLast = sortedComparablesArray[(int)to-1];
        ComparationResult comparationResult = this.compare(itemLast, itemToSearch);
        if (comparationResult == ComparationResult.FIRST_LESS) {
            result = false;
        }

        return result;
    }

    private boolean verifyItemValueIsInRange (
        SearchMode searchMode,
        long[] itemToSearch,
        long[][] sortedComparablesArray,
        long from,
        long to
    ) {
        boolean result = true;

        result = this.verifyItemValueIsLessThanLargest(itemToSearch, sortedComparablesArray, from, to);
        if (result == false) {
            return result;
        }

        if (searchMode == SearchMode.FIND_EXACT) {
            result = this.verifyItemValueIsMoreThanSmallest(itemToSearch, sortedComparablesArray, from, to);
        }
        
        return result;
    }

    private long findInSortedByLoop (
        SearchMode searchMode,
        long[] itemToSearch,
        long[][] sortedComparablesArray,
        long from,
        long to
    ) {
        if (from < 0) {
            throw new ArrayIndexOutOfBoundsException();
        }

        long result = RESULT_NOT_FOUND;

        ComparationResult comparationResult = ComparationResult.EQUAL_BOTH;

        long itemsNumber = sortedComparablesArray.length;
        long loopFinishValue = to;
        if (loopFinishValue >= itemsNumber) {
            throw new ArrayIndexOutOfBoundsException();
        }

        long itemPosition = 0;
        long itemNextPosition = 0;

        boolean verifyInRangeResult = this.verifyItemValueIsInRange(searchMode, itemToSearch, sortedComparablesArray, from, to);
        if (verifyInRangeResult == Boolean.FALSE) {
            return RESULT_NOT_FOUND;
        }

        ComparationResult comparationResultSuccessToCheck = ComparationResult.EQUAL_BOTH;
        if (searchMode == SearchMode.FIND_EXACT) {
            comparationResultSuccessToCheck = ComparationResult.EQUAL_BOTH;
        } else if (searchMode == SearchMode.FIND_NEXT_BIGGER) {
            comparationResultSuccessToCheck = ComparationResult.FIRST_LESS;
        }

        long[] itemCurr = new long[0];
        long[] itemNext = new long[0];
        for (itemPosition = from; itemPosition < loopFinishValue; itemPosition++) {
            itemNextPosition = itemPosition + 1;
            itemCurr = sortedComparablesArray[(int)itemPosition];
            itemNext = sortedComparablesArray[(int)itemNextPosition];

            comparationResult = this.compare(itemCurr, itemNext);
            if (comparationResult == comparationResultSuccessToCheck) {
                result = itemPosition;
                break;
            }
        }

        return result;
    }

    private long findInUnsortedByLoop (
        SearchMode searchMode,
        long[] itemToSearch,
        long[][] unsortedComparablesArray,
        long[] reverseLookupTable,
        long from,
        long to
    ) {
        if (from < 0) {
            throw new ArrayIndexOutOfBoundsException();
        }

        long result = RESULT_NOT_FOUND;

        ComparationResult comparationResult = ComparationResult.EQUAL_BOTH;

        long itemsNumber = unsortedComparablesArray.length;
        long loopFinishValue = to;
        if (loopFinishValue >= itemsNumber) {
            throw new ArrayIndexOutOfBoundsException();
        }

        long itemPosition = 0;
        long itemNextPosition = 0;

        long indexFrom = reverseLookupTable[(int)from];
        long indexTo = reverseLookupTable[(int)(to - 1)];

        long[][] valuesToValidate = new long[2][];
        valuesToValidate[0] = unsortedComparablesArray[(int)indexFrom];
        valuesToValidate[1] = unsortedComparablesArray[(int)indexTo];

        boolean verifyInRangeResult = this.verifyItemValueIsInRange(searchMode, itemToSearch, valuesToValidate, 0, 2);
        if (verifyInRangeResult == Boolean.FALSE) {
            return RESULT_NOT_FOUND;
        }

        ComparationResult comparationResultSuccessToCheck = ComparationResult.EQUAL_BOTH;
        if (searchMode == SearchMode.FIND_EXACT) {
            comparationResultSuccessToCheck = ComparationResult.EQUAL_BOTH;
        } else if (searchMode == SearchMode.FIND_NEXT_BIGGER) {
            comparationResultSuccessToCheck = ComparationResult.FIRST_LESS;
        }

        long[] itemCurr = new long[0];
        long[] itemNext = new long[0];

        long indexCurr = 0;
        long indexNext = 0;
        for (itemPosition = from; itemPosition < loopFinishValue; itemPosition++) {
            itemNextPosition = itemPosition + 1;

            indexCurr = reverseLookupTable[(int)itemPosition];
            indexNext = reverseLookupTable[(int)itemNextPosition];

            itemCurr = unsortedComparablesArray[(int)indexCurr];
            itemNext = unsortedComparablesArray[(int)indexNext];

            comparationResult = this.compare(itemCurr, itemNext);
            if (comparationResult == comparationResultSuccessToCheck) {
                result = itemPosition;
                break;
            }
        }

        return result;
    }

    private long findInSortedByJumps (
        SearchMode searchMode,
        long[] itemToSearch,
        long[][] sortedComparablesArray,
        long from,
        long to
    ) throws Exception {
        if (from < 0) {
            throw new ArrayIndexOutOfBoundsException();
        }

        long result = RESULT_NOT_FOUND;

        long itemsNumber = sortedComparablesArray.length;

        // validating array pointers
        if (to > itemsNumber) {
            throw new ArrayIndexOutOfBoundsException();
        }

        // verifying outer bounds of array
        boolean verifyInRangeResult = this.verifyItemValueIsInRange(searchMode, itemToSearch, sortedComparablesArray, from, to);
        if (verifyInRangeResult == Boolean.FALSE) {
            return RESULT_NOT_FOUND;
        }

        // calling recursive jumpingg to middle and comparing 3 values
        long[] jumpsResult = this.compareAndGetNewRange(searchMode, itemToSearch, sortedComparablesArray, from, to);

        // here means default, error or result value
        if (jumpsResult.length == 1) {
            long jumpsVal = jumpsResult[0];
            if (jumpsVal == (-2)) {
                throw new Exception("PLEASE DEBUG, RETVAL (-2) DEFULT, DID NOT SEARCH!!!");
            } else if (jumpsVal == (-1)) {
                result = RESULT_NOT_FOUND;
            } else {
                result = jumpsVal;
            }
        } else if (jumpsResult.length == 2) {
            throw new Exception("PLEASE DEBUG, RETVAL TO PROCESS FURTHERR, RECURSION DID NOT FINISH!!!");
        } else {
            throw new Exception("UNEXPECTED RETVAL!!!");
        }

        return result;
    }

    private long findInUnsortedByJumps (
        SearchMode searchMode,
        long[] itemToSearch,
        long[][] unsortedComparablesArray,
        long[] reverseLookupTable,
        long from,
        long to
    ) throws Exception {
        if (from < 0) {
            throw new ArrayIndexOutOfBoundsException();
        }

        long result = RESULT_NOT_FOUND;

        long itemsNumber = unsortedComparablesArray.length;

        // validating array pointers
        if (to > itemsNumber) {
            throw new ArrayIndexOutOfBoundsException();
        }

        long indexFrom = reverseLookupTable[(int)from];
        long indexTo = reverseLookupTable[(int)(to - 1)];

        long[][] valuesToValidate = new long[2][];
        valuesToValidate[0] = unsortedComparablesArray[(int)indexFrom];
        valuesToValidate[1] = unsortedComparablesArray[(int)indexTo];

        // verifying outer bounds of array
        boolean verifyInRangeResult = this.verifyItemValueIsInRange(searchMode, itemToSearch, valuesToValidate, 0, 2);
        if (verifyInRangeResult == Boolean.FALSE) {
            return RESULT_NOT_FOUND;
        }

        // calling recursive jumpingg to middle and comparing 3 values
        long[] jumpsResult = this.compareAndGetNewRangeWithSortPointers(searchMode, itemToSearch, unsortedComparablesArray, reverseLookupTable, from, to);

        // here means default, error or result value
        if (jumpsResult.length == 1) {
            long jumpsVal = jumpsResult[0];
            if (jumpsVal == (-2)) {
                throw new Exception("PLEASE DEBUG, RETVAL (-2) DEFULT, DID NOT SEARCH!!!");
            } else if (jumpsVal == (-1)) {
                result = RESULT_NOT_FOUND;
            } else {
                result = jumpsVal;
            }
        } else if (jumpsResult.length == 2) {
            throw new Exception("PLEASE DEBUG, RETVAL TO PROCESS FURTHERR, RECURSION DID NOT FINISH!!!");
        } else {
            throw new Exception("UNEXPECTED RETVAL!!!");
        }

        return result;
    }

    /**
     * 
     * @param searchMode
     * @param itemToSearch
     * @param sortedComparablesArray
     * @param from
     * @param to
     * @return long[from, to] for new jump, if a value matched, returns long[arrayIndex], if not found, long[-1], if nothingg done, default long[-2]
     */
    private long[] compareAndGetNewRange(
        SearchMode searchMode,
        long[] itemToSearch,
        long[][] sortedComparablesArray,
        long from,
        long to        
    ) {
        long[] result = new long[]{(-2)};

        long itemsNumberInSpan = (to - 1 - from);
        if (itemsNumberInSpan < this.LOOKUP_VIA_ITERATIONS_TIL_ITEMS_COUNT) {
            long findViaLoopValue = this.findInSortedByLoop(searchMode, itemToSearch, sortedComparablesArray, from, to);
            result = new long[]{findViaLoopValue};
            return result;
        }

        long itemsNumberInSpanHalf = itemsNumberInSpan >> 1;
        long middle = from + itemsNumberInSpanHalf;

        long[] lastValue = sortedComparablesArray[(int)(to - 1)];
        long[] middleValue = sortedComparablesArray[(int)middle];
        long[] firstValue = sortedComparablesArray[(int)from];

        ComparationResult comparationResultLast = this.compare(lastValue, itemToSearch);
        ComparationResult comparationResultMiddle = this.compare(middleValue, itemToSearch);
        ComparationResult comparationResultFirst = this.compare(firstValue, itemToSearch);

        if (comparationResultLast == ComparationResult.EQUAL_BOTH) {
            result = new long[]{to - 1};
            return result;
        }

        // searchItemValue between middle and last
        if ((comparationResultMiddle == ComparationResult.FIRST_LESS) && (comparationResultLast == ComparationResult.FIRST_BIGGER)) {
            result = this.compareAndGetNewRange(searchMode, itemToSearch, sortedComparablesArray, (middle + 1), to);
            return result;
        }

        // searchItem equal to middle and less than last
        if (comparationResultMiddle == ComparationResult.EQUAL_BOTH) {
            if (searchMode == SearchMode.FIND_EXACT) {
                result = new long[]{middle};
                return result;
            } else {
                result = this.compareAndGetNewRange(searchMode, itemToSearch, sortedComparablesArray, (middle + 1), to);
                return result;
            }
        }

        // search item between first and middle
        if ((comparationResultFirst == ComparationResult.FIRST_LESS) && (comparationResultMiddle == ComparationResult.FIRST_BIGGER)) {
            result = this.compareAndGetNewRange(searchMode, itemToSearch, sortedComparablesArray, (from + 1), middle);
            return result;
        }

        // equal tto first valuee, and less than the middle value
        if (comparationResultFirst == ComparationResult.EQUAL_BOTH) {
            if (searchMode == SearchMode.FIND_EXACT) {
                result = new long[]{from};
                return result;
            } else {
                result = this.compareAndGetNewRange(searchMode, itemToSearch, sortedComparablesArray, (from + 1), middle);
                return result;
            }
        }

        if (comparationResultFirst == ComparationResult.FIRST_BIGGER) {
            if (searchMode == SearchMode.FIND_EXACT) {
                result = new long[]{(-1)};
                return result;
            } else {
                result = new long[]{from};
                return result;
            }
        }

        return result;
    }

    private long[] compareAndGetNewRangeWithSortPointers (
        SearchMode searchMode,
        long[] itemToSearch,
        long[][] unsortedComparablesArray,
        long[] reverseLookupTable,
        long from,
        long to        
    ) {
        long[] result = new long[]{(-2)};

        long itemsNumberInSpan = (to - 1 - from);
        if (itemsNumberInSpan < this.LOOKUP_VIA_ITERATIONS_TIL_ITEMS_COUNT) {
            long findViaLoopValue = this.findInUnsortedByLoop(searchMode, itemToSearch, unsortedComparablesArray, reverseLookupTable, from, to);
            result = new long[]{findViaLoopValue};
            return result;
        }

        long itemsNumberInSpanHalf = itemsNumberInSpan >> 1;
        long middle = from + itemsNumberInSpanHalf;

        long lastIndex = reverseLookupTable[(int)(to - 1)];
        long middleIndex = reverseLookupTable[(int)middle];
        long firstIndex = reverseLookupTable[(int)from];

        long[] lastValue = unsortedComparablesArray[(int)lastIndex];
        long[] middleValue = unsortedComparablesArray[(int)middleIndex];
        long[] firstValue = unsortedComparablesArray[(int)firstIndex];

        ComparationResult comparationResultLast = this.compare(lastValue, itemToSearch);
        ComparationResult comparationResultMiddle = this.compare(middleValue, itemToSearch);
        ComparationResult comparationResultFirst = this.compare(firstValue, itemToSearch);

        if (comparationResultLast == ComparationResult.EQUAL_BOTH) {
            result = new long[]{to - 1};
            return result;
        }

        // searchItemValue between middle and last
        if ((comparationResultMiddle == ComparationResult.FIRST_LESS) && (comparationResultLast == ComparationResult.FIRST_BIGGER)) {
            result = this.compareAndGetNewRangeWithSortPointers(searchMode, itemToSearch, unsortedComparablesArray, reverseLookupTable, (middle + 1), to);
            return result;
        }

        // searchItem equal to middle and less than last
        if (comparationResultMiddle == ComparationResult.EQUAL_BOTH) {
            if (searchMode == SearchMode.FIND_EXACT) {
                result = new long[]{middle};
                return result;
            } else {
                result = this.compareAndGetNewRangeWithSortPointers(searchMode, itemToSearch, unsortedComparablesArray, reverseLookupTable, (middle + 1), to);
                return result;
            }
        }

        // search item between first and middle
        if ((comparationResultFirst == ComparationResult.FIRST_LESS) && (comparationResultMiddle == ComparationResult.FIRST_BIGGER)) {
            result = this.compareAndGetNewRangeWithSortPointers(searchMode, itemToSearch, unsortedComparablesArray, reverseLookupTable, (from + 1), middle);
            return result;
        }

        // equal tto first valuee, and less than the middle value
        if (comparationResultFirst == ComparationResult.EQUAL_BOTH) {
            if (searchMode == SearchMode.FIND_EXACT) {
                result = new long[]{from};
                return result;
            } else {
                result = this.compareAndGetNewRangeWithSortPointers(searchMode, itemToSearch, unsortedComparablesArray, reverseLookupTable, (from + 1), middle);
                return result;
            }
        }

        if (comparationResultFirst == ComparationResult.FIRST_BIGGER) {
            if (searchMode == SearchMode.FIND_EXACT) {
                result = new long[]{(-1)};
                return result;
            } else {
                result = new long[]{from};
                return result;
            }
        }

        return result;
    }    
}
