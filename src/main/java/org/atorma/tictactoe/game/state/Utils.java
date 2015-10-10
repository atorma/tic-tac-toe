package org.atorma.tictactoe.game.state;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class Utils {

    public static Random random = new Random();

    public static Double round(Double num, int decimals) {
        if (num == null) {
            return null;
        }
        double mult = Math.pow(10, decimals);
        return Math.round(num * mult)/mult;
    }

    public static <T> T pickRandom(List<T> elements) {
        return elements.get(random.nextInt(elements.size()));
    }

    /**
     * Finds all the elements having maximum value by given scoring function.
     * For constant score calculation cost, this operation has cost O(n)
     * where n is elements collection size (compare with O(nlog(n)) for sorting).
     */
    public static <T> List<T> max(Collection<T> elements, ScoringFunction<T> scoringFunction) {
        List<T> candidates = new ArrayList<>();
        double bestScore = Double.NEGATIVE_INFINITY;
        for (T element : elements) {
            double score = scoringFunction.getScore(element);
            if (score > bestScore) {
                candidates.clear();
                bestScore = score;
                candidates.add(element);
            } else if (score == bestScore) {
                candidates.add(element);
            }
        }
        return candidates;
    }

    public interface ScoringFunction<T> {
        double getScore(T element);
    }
}
