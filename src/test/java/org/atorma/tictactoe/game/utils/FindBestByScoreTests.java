package org.atorma.tictactoe.game.utils;

import org.atorma.tictactoe.game.state.Utils;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FindBestByScoreTests {


    @Test
    public void find_max_by_negative_value() {
        List<Element> elementList = Arrays.asList(
                new Element(3),
                new Element(-1),
                new Element(0),
                new Element(3),
                new Element(-1),
                new Element(2)
        );
        List<Element> maxList = Utils.max(elementList, new Utils.ScoringFunction<Element>() {
            public double getScore(Element element) {
                return -1 * element.getValue();
            }
        });
        assertEquals(2, maxList.size());
        for (Element e : maxList) {
            assertEquals(-1, e.getValue());
        }
    }


    private static class Element {
        private final int value;

        public Element(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
