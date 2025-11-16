import java.io.*;
import java.util.*;

public class Question2 {
    static int x = 0;
    static int switchCost = 0;
    static int[] dpTunMun;
    static int[] dpCastlePeak ;

    public static int minimumTime(BufferedReader reader) throws IOException {
        x = Integer.parseInt(reader.readLine());
        switchCost = Integer.parseInt(reader.readLine());
        String[] tunMunStr = reader.readLine().trim().split("\\s+");
        dpTunMun = new int[tunMunStr.length];  // Remove "int[]" here
        for (int i = 0; i < tunMunStr.length; i++) {
            dpTunMun[i] = Integer.parseInt(tunMunStr[i]);
        }
        String[] castlePeakStr = reader.readLine().trim().split("\\s+");
        dpCastlePeak = new int[castlePeakStr.length];  // Remove "int[]" here
        for (int i = 0; i < castlePeakStr.length; i++) {
            dpCastlePeak[i] = Integer.parseInt(castlePeakStr[i]);
        }
        int[] dp1 = new int[dpTunMun.length + 1];
        int[] dp2 = new int[dpCastlePeak.length + 1];

        dp1[0] = dpTunMun[0];
        dp2[0] = dpCastlePeak[0];
        for (int i = 1; i <= x; i++) {
            // for tuen mun road
            int stayOnTuenMun = dp1[i-1] + dpTunMun[i];
            int switchToTuenMun = dp2[i-1] + switchCost + dpTunMun[i];
            dp1[i] = Math.min(stayOnTuenMun, switchToTuenMun);
            // For Castle Peak Road
            int stayOnCastlePeak = dp2[i-1] + dpCastlePeak[i];
            int switchToCastlePeak = dp1[i-1] + switchCost + dpCastlePeak[i];
            dp2[i] = Math.min(stayOnCastlePeak, switchToCastlePeak);
        }


        return Math.min(dp1[x], dp2[x]);
    }

	public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        int result = minimumTime(reader);
        System.out.println("The minimum time needed is " + result + ".");
	}
}