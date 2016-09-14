package com.navinfo.mapspotter.foundation.algorithm.string;

/**
 * 计算字符串的Levenshtein距离及相似度
 * Created by SongHuiXing on 2016/1/6.
 * @link https://en.wikipedia.org/wiki/Levenshtein_distance
 */
public class Levenshtein {
    private static int maxStringLength = (int)Math.pow(2, 31);

    /**
     * 计算字符串的相似度
     * @param sNew
     * @param sOld
     * @return 0~1, 1--完全相同, 0--完全不同
     */
    public static float similarity(String sNew, String sOld){
        float maxLen = Math.max(sNew.length(), sOld.length());

        return (maxLen - iLD(sNew, sOld)) / maxLen;
    }

    /**
     * 计算两个字符串的Levenshtein距离
     * 原地小内存版本
     * @param sNew
     * @param sOld
     * @link http://www.codeproject.com/Articles/13525/Fast-memory-efficient-Levenshtein-algorithm
     * @return
     */
    public static int iLD(String sNew, String sOld){
        int rowLen = sNew.length();
        int colLen = sOld.length();

        if(Math.max(rowLen, colLen) > maxStringLength) {
            throw new IllegalArgumentException("\nMaximum string length in Levenshtein.iLD is " +
                    maxStringLength +
                    ".\nYours is " + Math.max(rowLen, colLen) + ".");
        }

        if(0 == rowLen)
            return colLen;

        if(0 == colLen)
            return rowLen;

        /// Create the two vectors
        int[] v0 = new int[rowLen + 1];
        int[] v1 = new int[rowLen + 1];
        int[] vTmp;

        /// Initialize the first vector
        for (int RowIdx = 1; RowIdx <= rowLen; RowIdx++)
        {
            v0[RowIdx] = RowIdx;
        }

        char[] charRow = sNew.toCharArray();
        char[] charCol = sOld.toCharArray();

        int cost;
        /// Fore each column
        for (int ColIdx = 1; ColIdx <= colLen; ColIdx++)
        {
            /// Set the 0'th element to the column number
            v1[0] = ColIdx;

            char Col_j = charCol[ColIdx - 1];

            /// Fore each row
            for (int RowIdx = 1; RowIdx <= rowLen; RowIdx++)
            {
                char Row_i = charRow[RowIdx - 1];

                if (Row_i == Col_j)
                {
                    cost = 0;
                }
                else
                {
                    cost = 1;
                }

                /// Find minimum
                v1[RowIdx] = Minimum(v0[RowIdx] + 1,
                                    v1[RowIdx - 1] + 1,
                                    v0[RowIdx - 1] + cost);
            }

            /// Swap the vectors
            vTmp = v0;
            v0 = v1;
            v1 = vTmp;
        }

        return v0[rowLen];
    }

    /**
     * 计算两个字符串的Levenshtein距离
     * @param sNew
     * @param sOld
     * @return
     */
    public static int LD(String sNew, String sOld) {
        int sNewLen = sNew.length();  // length of sNew
        int sOldLen = sOld.length();  // length of sOld

        /// Test string length
        if (Math.max(sNewLen, sOldLen) > maxStringLength)
            throw (new IllegalArgumentException("\nMaximum string length in Levenshtein.LD is " + maxStringLength +
                                                ".\nYours is " + Math.max(sNewLen, sOldLen) + "."));

        if (sNewLen == 0)
            return sOldLen;

        if (sOldLen == 0)
            return sNewLen;

        //initial matrix
        int[][] matrix = new int[sNewLen + 1][sOldLen + 1];
        for (int sNewIdx = 0; sNewIdx <= sNewLen; sNewIdx++) {
            matrix[sNewIdx][0] = sNewIdx;
        }
        for (int sOldIdx = 0; sOldIdx <= sOldLen; sOldIdx++) {
            matrix[0][sOldIdx] = sOldIdx;
        }

        char sNew_i; // ith character of sNew
        char sOld_j; // jth character of sOld
        int cost;
        char[] sNewChars = sNew.toCharArray();
        char[] sOldChars = sOld.toCharArray();
        for (int sNewIdx = 1; sNewIdx <= sNewLen; sNewIdx++)
        {
            sNew_i = sNewChars[sNewIdx - 1];

            for (int sOldIdx = 1; sOldIdx <= sOldLen; sOldIdx++)
            {
                sOld_j = sOldChars[sOldIdx - 1];

                if (sNew_i == sOld_j)
                {
                    cost = 0;
                }
                else
                {
                    cost = 1;
                }

                matrix[sNewIdx][sOldIdx] = Minimum(matrix[sNewIdx - 1][sOldIdx] + 1,
                                                    matrix[sNewIdx][sOldIdx - 1] + 1,
                                                    matrix[sNewIdx - 1][sOldIdx - 1] + cost);

            }
        }

        return matrix[sNewLen][sOldLen];
    }

    /**
     *  查找最小的数
     */
    private static int Minimum(int a, int b, int c) {
        int mi = a < b ? a : b;

        if (c < mi)
        {
            mi = c;
        }

        return mi;
    }
}
