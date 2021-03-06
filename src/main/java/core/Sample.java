package core;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by ahmetu on 28.09.2016.
 */
public class Sample {

    private String inTrie;
    private double similarityScore;
    private double poissonScore;
    private String word;
    private String segmentation;
    private double presenceScore;
    private double lenghtPrior;
    private boolean isCalculated;

    public String toString() {
        return word + " " + segmentation;
    }

    public boolean isCalculated() {
        return isCalculated;
    }

    public void setCalculated(boolean calculated) {
        isCalculated = calculated;
    }

    public double getPresenceScore() {
        return presenceScore;
    }

    public void setPresenceScore(double presenceScore) {
        this.presenceScore = presenceScore;
    }

    public String getInTrie() {
        return inTrie;
    }

    public void setInTrie(String inTrie) {
        this.inTrie = inTrie;
    }

    public double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(double similarityScore) {
        this.similarityScore = similarityScore;
    }

    public double getPoissonScore() {
        return poissonScore;
    }

    public void setPoissonScore(double poissonScore) {
        this.poissonScore = poissonScore;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public double getLenghtPrior() {
        return lenghtPrior;
    }

    public void update(String segmentation, double poissonScore, double similarityScore, double presenceScore, double lenghtPrior) {
        this.segmentation = segmentation;
        this.poissonScore = poissonScore;
        this.similarityScore = similarityScore;
        this.presenceScore = presenceScore;
        this.lenghtPrior = lenghtPrior;
    }

    public String getSegmentation() {
        return segmentation;
    }

    public String getWord() {
        return word;
    }

    public void setSegmentation(String segmentation) {
        this.segmentation = segmentation;
    }

    public Sample(String word, String segmentation, String inTrie) {
        this.word = word;
        this.segmentation = segmentation;
        this.inTrie = inTrie;
        this.isCalculated = false;

        /*
        ArrayList<String> segments = Operations.getSegments(segmentation);
        this.poissonScore = calculatePoisson(segments);
        this.similarityScore = calculateSimilarity(segments);
        this.presenceScore = calculatePresenceScore(segments);
        */
    }

    public ArrayList<Double> calculateScores(String segmentation, boolean[] features) {
        //0:poisson, 1:similarity, 2:presence
        ArrayList<Double> scores = new ArrayList<>();
        boolean poisson_f = features[0];
        boolean poisson_b = features[1];
        boolean sim = features[2];
        boolean presence = features[3];
        boolean length = features[4];

        ArrayList<String> segments = Operations.getSegments(segmentation);
        ArrayList<String> segmentsForRecursive = new ArrayList<>(segments);

        if (segmentsForRecursive.size() > 1) {
            segmentsForRecursive.remove(segmentsForRecursive.size() - 1);
        }

        double poissonScore_f = 0;
        if (poisson_f)
            poissonScore_f = calculatePoisson_f(segmentsForRecursive);

        double poissonScore_b = 0;
        if (poisson_b)
            poissonScore_b = calculatePoisson_b(segmentsForRecursive);

        double similarityScore = 0;
        if (sim)
            similarityScore = calculateSimilarityWithHashMap(segmentation);

        double presenceScore = 0;
        if (presence)
            presenceScore = calculatePresenceScore(segmentsForRecursive);

        double lengthScore = 0;
        if (length)
            lengthScore = calculateLenghtScore(segmentation);

        scores.add(poissonScore_f);
        scores.add(poissonScore_b);
        scores.add(similarityScore);
        scores.add(presenceScore);
        scores.add(lengthScore);
        return scores;
    }

    private double calculateLenghtScore(String segmentation) {
        double lenghtScore = 0;
        StringTokenizer tokenizer = new StringTokenizer(segmentation, "+");
        int length = 0;
        int c = 0;
        while (tokenizer.hasMoreTokens()) {
            length = length + tokenizer.nextToken().length();
            c++;
        }
        lenghtScore = Math.pow(0.037, length / c);

        return Math.log10(lenghtScore);

    }

    private double calculatePoisson_f(ArrayList<String> segments) {
        double totalPoisson = 0;
        for (String s : segments) {
            totalPoisson = totalPoisson + Math.log10(Operations.getPoissonScore(Initialization.getBranchTable_f().get(inTrie).get(s), Initialization.getLambda_f()));
        }
        return totalPoisson;
    }

    private double calculatePoisson_b(ArrayList<String> segments) {
        double totalPoisson = 0;

        for (String s : segments) {
            String suffix = new StringBuilder(this.getWord().substring(segments.get(0).length())).reverse().toString();
            if (!suffix.equalsIgnoreCase("")) {
                totalPoisson = totalPoisson + Math.log10(Operations.getPoissonScore(Initialization.getBranchTable_b().get(inTrie).get(suffix), Initialization.getLambda_b()));
            } else {
                totalPoisson = totalPoisson + Math.log10(Operations.getPoissonScore(1, Initialization.getLambda_b()));
            }
        }

        return totalPoisson;
    }

    private double calculateSimilarityWithHashMap(String segmentation) {

        if (!segmentation.contains("+"))
            return Initialization.getSimUnsegmented();
        else
            return Math.log10(Operations.getCosineScore(segmentation));
    }

    private double calculatePresenceScore(ArrayList<String> segments) {

        double presenceScore = 0;

        for (String s : segments) {
            //    System.out.println("presence segment: " + s);

            if (Initialization.getNewCorpus().containsKey(s)) {
                presenceScore = presenceScore + Math.log10(Initialization.getNewCorpus().get(s) / Initialization.getNewCorpusSize());
            } else {
                presenceScore = presenceScore + Math.log10(Initialization.getLaplaceCoefficient() / Initialization.getNewCorpusSize());
            }
        }

        return presenceScore;
    }
}
/*
    private double calculateSimilarity(ArrayList<String> segments) {
        if (segments.size() == 1) {
            return Constant.getSimUnsegmented();
        }
        double similarityScore = 0;

        String w1 = segments.get(0);
        String w2 = "";
        for (int i = 1; i < segments.size(); i++) {
            w2 = segments.get(i);
            double cosine = Operations.getCosineScore(w1, w2);
            //System.out.println("Cosine similarity between " + w1 + " " + w2 + " is " + cosine);
            similarityScore = similarityScore + Math.log10(cosine);
            w1 = w2;
        }
        return similarityScore;

    }
*/
