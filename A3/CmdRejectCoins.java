import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class CmdRejectCoins implements Command {
    @Override
    public String execute(VendingMachine v, String[] cmdParts) {
        Map<Integer, Integer> coinsSnapshot = v.getInsertedCoins();
        int total = v.getTotalInsertedCoins();
        if (total == 0 || coinsSnapshot == null || coinsSnapshot.isEmpty()) {
            v.rejectCoins();
            return "Rejected no coin!";
        }

        List<Integer> coins = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : coinsSnapshot.entrySet()) {
            int denomination = entry.getKey();
            int count = entry.getValue();
            for (int i = 0; i < count; i++) {
                coins.add(denomination);
            }
        }

        Collections.sort(coins, Collections.reverseOrder());

        StringJoiner joiner = new StringJoiner(", ");
        for (int coin : coins) {
            joiner.add("$" + coin);
        }

        v.rejectCoins();

        return "Rejected " + joiner.toString() + ". $" + total + " in total.";
    }
}