package in.srain.cube.sample.ui.views;

import java.util.ArrayList;

public class StoreHousePath {
    private static final float[][] LETTERS = new float[][]{
            new float[]{
                    // A
                    24, 0, 1, 22,
                    1, 22, 1, 72,
                    24, 0, 47, 22,
                    47, 22, 47, 72,
                    1, 48, 47, 48
            },

            new float[]{
                    // B
                    0, 0, 0, 72,
                    0, 0, 37, 0,
                    37, 0, 47, 11,
                    47, 11, 47, 26,
                    47, 26, 38, 36,
                    38, 36, 0, 36,
                    38, 36, 47, 46,
                    47, 46, 47, 61,
                    47, 61, 38, 71,
                    37, 72, 0, 72,
            },

            new float[]{
                    // C
                    47, 0, 0, 0,
                    0, 0, 0, 72,
                    0, 72, 47, 72,
            },

            new float[]{
                    // D
                    0, 0, 0, 72,
                    0, 0, 24, 0,
                    24, 0, 47, 22,
                    47, 22, 47, 48,
                    47, 48, 23, 72,
                    23, 72, 0, 72,
            },

            new float[]{
                    // E
                    0, 0, 0, 72,
                    0, 0, 47, 0,
                    0, 36, 37, 36,
                    0, 72, 47, 72,
            },

            new float[]{
                    // F
                    0, 0, 0, 72,
                    0, 0, 47, 0,
                    0, 36, 37, 36,
            },

            new float[]{
                    // G
                    47, 23, 47, 0,
                    47, 0, 0, 0,
                    0, 0, 0, 72,
                    0, 72, 47, 72,
                    47, 72, 47, 48,
                    47, 48, 24, 48,
            },

            new float[]{
                    // H
                    0, 0, 0, 72,
                    0, 36, 47, 36,
                    47, 0, 47, 72,
            },

            new float[]{
                    // I
                    0, 0, 47, 0,
                    24, 0, 24, 72,
                    0, 72, 47, 72,
            },

            new float[]{
                    // J
                    47, 0, 47, 72,
                    47, 72, 24, 72,
                    24, 72, 0, 48,
            },

            new float[]{
                    // K
                    0, 0, 0, 72,
                    47, 0, 3, 33,
                    3, 38, 47, 72,
            },

            new float[]{
                    // L
                    0, 0, 0, 72,
                    0, 72, 47, 72,
            },

            new float[]{
                    // M
                    0, 0, 0, 72,
                    0, 0, 24, 23,
                    24, 23, 47, 0,
                    47, 0, 47, 72,
            },

            new float[]{
                    // N
                    0, 0, 0, 72,
                    0, 0, 47, 72,
                    47, 72, 47, 0,
            },

            new float[]{
                    // O
                    0, 0, 0, 72,
                    0, 72, 47, 72,
                    47, 72, 47, 0,
                    47, 0, 0, 0,
            },

            new float[]{
                    // P
                    0, 0, 0, 72,
                    0, 0, 47, 0,
                    47, 0, 47, 36,
                    47, 36, 0, 36,
            },

            new float[]{
                    // Q
                    0, 0, 0, 72,
                    0, 72, 23, 72,
                    23, 72, 47, 48,
                    47, 48, 47, 0,
                    47, 0, 0, 0,
                    24, 28, 47, 71,
            },

            new float[]{
                    // R
                    0, 0, 0, 72,
                    0, 0, 47, 0,
                    47, 0, 47, 36,
                    47, 36, 0, 36,
                    0, 37, 47, 72,
            },

            new float[]{
                    // S
                    47, 0, 0, 0,
                    0, 0, 0, 36,
                    0, 36, 47, 36,
                    47, 36, 47, 72,
                    47, 72, 0, 72,
            },

            new float[]{
                    // T
                    0, 0, 47, 0,
                    24, 0, 24, 72,
            },

            new float[]{
                    // U
                    0, 0, 0, 72,
                    0, 72, 47, 72,
                    47, 72, 47, 0,
            },

            new float[]{
                    // V
                    0, 0, 24, 72,
                    24, 72, 47, 0,
            },

            new float[]{
                    // W
                    0, 0, 0, 72,
                    0, 72, 24, 49,
                    24, 49, 47, 72,
                    47, 72, 47, 0
            },

            new float[]{
                    // X
                    0, 0, 47, 72,
                    47, 0, 0, 72
            },

            new float[]{
                    // Y
                    0, 0, 24, 23,
                    47, 0, 24, 23,
                    24, 23, 24, 72
            },

            new float[]{
                    // Z
                    0, 0, 47, 0,
                    47, 0, 0, 72,
                    0, 72, 47, 72
            },
    };


    public static ArrayList<float[]> getPath(String str) {
        return getPath(str, 1, 11);
    }

    public static ArrayList<float[]> getPath(String str, float scale, int gapBetweenLetter) {
        str = str.toUpperCase();
        ArrayList<float[]> list = new ArrayList<float[]>();
        float offsetForWidth = 0;
        for (int i = 0; i < str.length(); i++) {
            int pos = str.charAt(i) - 65;
            float[] points = LETTERS[pos];
            int pointCount = points.length / 4;
            float letterWidth = 0;

            // for each point
            for (int j = 0; j < pointCount; j++) {
                float[] line = new float[4];
                for (int k = 0; k < 4; k++) {
                    float l = points[j * 4 + k];
                    // x
                    if (k % 2 == 0) {
                        if (l > letterWidth) {
                            letterWidth = l;
                        }
                        line[k] = (l + offsetForWidth) * scale;
                    }
                    // y
                    else {
                        line[k] = l * scale;
                    }
                }
                list.add(line);
            }
            offsetForWidth += letterWidth + gapBetweenLetter;
        }
        return list;
    }
}
