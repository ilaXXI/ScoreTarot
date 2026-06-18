package com.example.scoretarot;

import android.graphics.Color;
import java.util.Random;

public class TarotUtils {
    
    public static int getPlaceholderColor(String name) {
        if (name == null || name.isEmpty()) return Color.GRAY;
        Random r = new Random(name.hashCode());
        return Color.rgb(r.nextInt(128) + 64, r.nextInt(128) + 64, r.nextInt(128) + 64);
    }

    public static int calculateContractThreshold(int bouts) {
        switch (bouts) {
            case 3: return 36;
            case 2: return 41;
            case 1: return 51;
            default: return 56;
        }
    }

    public static int getContractCoefficient(String contract) {
        switch (contract) {
            case "Garde": return 2;
            case "Garde Sans": return 4;
            case "Garde Contre": return 6;
            default: return 1;
        }
    }
}
