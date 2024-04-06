package org.atorma.tictactoe.game;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ThreadLocalRandom;

public class Utils {

    public static Double round(Double num, int decimals) {
        if (num == null) {
            return null;
        }
        double mult = Math.pow(10, decimals);
        return Math.round(num * mult)/mult;
    }

    public static <T> T pickRandom(List<T> elements) {
        return elements.get(ThreadLocalRandom.current().nextInt(elements.size()));
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

    public static <E> ListIterator<E> reverse(ListIterator<E> listIterator) {
        return new ListIterator<E>() {
            @Override
            public boolean hasNext() {
                return listIterator.hasPrevious();
            }

            @Override
            public E next() {
                return listIterator.previous();
            }

            @Override
            public boolean hasPrevious() {
                return listIterator.hasNext();
            }

            @Override
            public E previous() {
                return listIterator.next();
            }

            @Override
            public int nextIndex() {
                return listIterator.previousIndex();
            }

            @Override
            public int previousIndex() {
                return listIterator.nextIndex();
            }

            @Override
            public void remove() {
                listIterator.remove();
            }

            @Override
            public void set(E e) {
                listIterator.set(e);
            }

            @Override
            public void add(E e) {
                listIterator.add(e);
            }
        };
    }

    public interface ScoringFunction<T> {
        double getScore(T element);
    }
}
