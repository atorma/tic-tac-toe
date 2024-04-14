package org.atorma.tictactoe.game.player.mcts;

public class UCT {

    /**
     * Computes the exploration bonus according to Upper Confidence Bounds Applied to Trees.
     *
     * @param moveNode
     * @param uctConstant
     * @return
     */
    public static double getUCTBonus(MoveNode moveNode, double uctConstant) {
        if (moveNode.getParent() == null) { // root node
            return Double.POSITIVE_INFINITY;
        }

        double t = moveNode.getParent().getNumPlays();
        double n = moveNode.getNumPlays();

        double score;
        if (n == 0) {
            score = Double.POSITIVE_INFINITY;
        } else {
            score = uctConstant*Math.sqrt(2*Math.log(t)/n);
        }

        return score;
    }
}
