package com.jaisocx.orderby.helpers;



public class Comparator {

    public enum ComparationResult {
        FIRST_BIGGER(3),
        EQUAL_BOTH(2),
        FIRST_LESS(1);

        private long value;

        ComparationResult(long value) {
            this.value = value;
        }

        public long getValue() {
            return this.value;
        }
    }

    public static boolean tellNotEqual (
        long[] a, 
        long[] b
    ) {
        if ( a.length != b.length ) {
            return true;

        } else if ( a[0] != b[0] ) {
            return true;

        } else {
            ComparationResult comparationResult = Comparator.compare( a, b );
            return (comparationResult != ComparationResult.EQUAL_BOTH);
        }
    }

    public static ComparationResult compare (
        long[] a, 
        long[] b
    ) {
        ComparationResult comparationResult = ComparationResult.EQUAL_BOTH;

        if (a == null && b == null) {
            return ComparationResult.EQUAL_BOTH;
        } else if (a == null) {
            return ComparationResult.FIRST_LESS;
        } else if (b == null) {
            return ComparationResult.FIRST_BIGGER;
        }

        long otherItemValue = 0;
        int firstItemValueLength = a.length;
        int otherItemValueLength = b.length;
        int counter = 0;
        for (long firstItemValue : a) {
            otherItemValue = b[counter];

            if (firstItemValue > otherItemValue) {
                comparationResult = ComparationResult.FIRST_BIGGER;
                break;
            } else if (firstItemValue < otherItemValue) {
                comparationResult = ComparationResult.FIRST_LESS;
                break;
            }

            counter++;
            if (counter >= otherItemValueLength) {
                break;
            }
        }

        // comparison result still equality, but first item is longer, so first item is bigger
        if ((comparationResult == ComparationResult.EQUAL_BOTH) && (firstItemValueLength > otherItemValueLength)) {
            return ComparationResult.FIRST_BIGGER;
        } else if ((comparationResult == ComparationResult.EQUAL_BOTH) && (firstItemValueLength < otherItemValueLength)) {
            return ComparationResult.FIRST_LESS;
        }

        return comparationResult;
    }
}
