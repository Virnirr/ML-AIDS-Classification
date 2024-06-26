import java.util.*;

public class DecisionTree {

    public int[][] matrix;
    public TreeNode decisionTree = new TreeNode(-1, -1, new ArrayList<TreeNode>(), -1);
//    private final int LABEL_COLUMN = 22;
    public DecisionTree(int[][] matrix) {
        this.matrix = matrix;
    }
    private int findFrequency(int attribute, int value, ArrayList<Integer> rows) {
        // Examines only the specified rows of the array.
        // It returns the number of rows in which the element at position attribute
        // (a number between 0 and 4) is equal to value.

        int total_freq = 0;
        for (Integer rowIndex : rows) {
            if (this.matrix[rowIndex][attribute] == value) {
                total_freq++;
            }
        }
        return total_freq;
    }
    private int findLabelCol() {
        return this.matrix[0].length - 1;
    }
    private HashSet<Integer> findDifferentValues(int attribute, ArrayList<Integer> rows) {
        //Examines only the specified rows of the array. It returns a HashSet
        // of the different values for the specified attribute.
        HashSet<Integer> unqiue_values_from_row = new HashSet<Integer>();
        for (Integer rowIndex : rows) {
            unqiue_values_from_row.add(this.matrix[rowIndex][attribute]);
        }
        return unqiue_values_from_row;
    }

    private ArrayList<Integer> findRows(int attribute, int value, ArrayList<Integer> rows) {
        //Examines only the specified rows of the array.
        // Returns an ArrayList of the rows where the
        // value for the attribute is equal to value.
        ArrayList<Integer> rowSameAsValue = new ArrayList<>();
        for (Integer rowIndex : rows) {
            // add rowIndex to arraylist if the same as value
            if (this.matrix[rowIndex][attribute] == value) {
                rowSameAsValue.add(rowIndex);
            }
        }
        return rowSameAsValue;
    }

    private double log2(double number) {
        return (Math.log(number) / Math.log(2));
    }

    private double findEntropy(ArrayList<Integer> rows) {
        //finds the entropy of the dataset that consists of the specified rows.
        HashSet<Integer> unqiue_values_from_row = findDifferentValues(findLabelCol(), rows);
        double entropy = 0;

        // find total entropy in specified rows
        for (Integer val : unqiue_values_from_row) {
            double pr_c = (double) findFrequency(findLabelCol(), val, rows) / rows.size();
            double entropy_temp = (-1) * pr_c * log2(pr_c);
            entropy += entropy_temp;
        }
        return entropy;
    }

    private double findEntropy(int attribute, ArrayList<Integer> rows) {
        //finds the entropy of the dataset that consists of the specified
        // rows after it is partitioned on the attribute.
        HashSet<Integer> unqiue_values_from_row = findDifferentValues(attribute, rows);
        double total_entropy = 0.0;
        for (Integer val : unqiue_values_from_row) {
            // get all the rows that contains that specific value
            ArrayList<Integer> rowContainingVal = findRows(attribute, val, rows);
            double freq_of_val = findFrequency(attribute, val, rows);

            // sum to total_entropy Sum (D_A / D) * entropy(D_A)
            total_entropy += ((freq_of_val / rows.size()) * findEntropy(rowContainingVal));
        }
        return total_entropy;
    }

    private double findGain(int attribute, ArrayList<Integer> rows) {
        // finds the information gain of partitioning on the attribute.
        // Considers only the specified rows.
        double entropyD = findEntropy(rows);
        double entropyD_A = findEntropy(attribute, rows);

        return entropyD - entropyD_A;
    }

    public double computeIGR(int attribute, ArrayList<Integer> rows) {
        // returns the Information Gain Ratio, where we only look at the
        // data defined by the set of rows and we consider splitting on attribute.
        HashSet<Integer> unqiue_values_from_row = findDifferentValues(attribute, rows);
        double gain = findGain(attribute, rows);
        double sum_entropy = 0.0;
        // split into multiple
        for (Integer val : unqiue_values_from_row) {
            double ratio = (double) findFrequency(attribute, val, rows) / rows.size();
            sum_entropy -= (ratio * log2(ratio)); // make sure to find the entropy
        }

        if (sum_entropy == 0) {
            return 0;
        }
        return gain / sum_entropy;
    }

    public int findMostCommonValue(ArrayList<Integer> rows) {
        // returns the most common category for the dataset that is
        // the defined by the specified rows.

        // basically return the most common label
        HashSet<Integer> categoryValues = findDifferentValues(findLabelCol(), rows);
        int mostFrequentCategory = 0;
        int mostFrequencyAmount = -1;
        for (Integer category : categoryValues) {
            int frequency = findFrequency(findLabelCol(), category, rows);
            if (frequency > mostFrequencyAmount) {
                mostFrequentCategory = category;
                mostFrequencyAmount = frequency;
            }
        }
        return mostFrequentCategory;
    }
    public HashMap<Integer, ArrayList<Integer>> split(int attribute, ArrayList<Integer> rows) {
        //Splits the dataset that is defined by rows on the attribute.
        // Each element of the HashMap that is returned contains the value for
        // the attribute and an ArrayList of rows that have this value.

        HashSet<Integer> unqiue_values_from_row = findDifferentValues(attribute, rows);
        HashMap<Integer, ArrayList<Integer>> valToRowMapping = new HashMap<>();

        // create mapping for each unique value in the attribute
        for (Integer val : unqiue_values_from_row) {
            valToRowMapping.put(val, findRows(attribute, val, rows));
        }
        return valToRowMapping;
    }

    public TreeNode getDecisionTree() {
        return this.decisionTree;
    }

    public int predictLabel(ArrayList<Integer> featureValues) {
        // return either 0 or 1 for label
        TreeNode currNode = this.decisionTree;
        int check_forever = 0;
        while (currNode.getPaths() != null) {
            // find the current path and go through the path iteratively
            for (TreeNode node : currNode.getPaths()) {
                if (node.is_leaf()) {
                    currNode = node;
                    break;
                }
                if (featureValues.get(node.getFeatureIdx()) ==  node.getFeatureVal()) {
                    currNode = node;
                    break;
                }
            }
            check_forever++;
            if (check_forever > 1000) {
                return -1;
            }
        }
        return currNode.getLabel();
    }

    public void printDecisionTree(int[][] data,
                                  ArrayList<Integer> attributes,
                                  ArrayList<Integer> rows,
                                  int level,
                                  double currentIGR, TreeNode parent) {
        // recursively prints the decision tree.
        // base case return if either attribute or rows is empty
        if (attributes.size() == 0 || rows.size() == 0) {
            int mostCommonLabel = this.findMostCommonValue(rows);
//            System.out.println("\t".repeat(level) + "value = " + mostCommonLabel);
            // add leaf node to parent node as final path
            parent.addTreePath(new TreeNode(-1, -1, null, mostCommonLabel));
            return;
        }

        HashMap<Integer, Double> attributeIGR = new HashMap<>();

        for (Integer attr : attributes) {
            attributeIGR.put(attr, this.computeIGR(attr, rows));
        }

        // get the attribute with highest IGR from hashmap
        int attributeWithHighestIGR = -1;
        double highestIGR = -1;
        for (Map.Entry<Integer, Double> entry : attributeIGR.entrySet()) {
            int attr = entry.getKey();
            double igr = entry.getValue();
            if (igr > highestIGR) {
                attributeWithHighestIGR = attr;
                highestIGR = igr;
            }
        }

//        if (highestIGR <= 0.02) {
//            int mostCommonLabel = this.findMostCommonValue(rows);
//            System.out.println("\t".repeat(level) + "value = " + mostCommonLabel);
//            parent.addTreePath(new TreeNode(-1, -1, null, mostCommonLabel));
//            return;
//        }

        // contains the attribute values --> list of rows with attribute value
        HashMap<Integer, ArrayList<Integer>> splitAttribute =
                this.split(attributeWithHighestIGR, rows);

        // remove attribute from attributes arraylist
        attributes.remove(Integer.valueOf(attributeWithHighestIGR));
        for (Map.Entry<Integer, ArrayList<Integer>> entry : splitAttribute.entrySet()) {
            int valInAttribute = entry.getKey();

            ArrayList<Integer> rowsAssocWithAttrVal = entry.getValue();
            // get the IGR of current value to recurse on; note: base case will result in printing value if <= 0.01
            double currValIGR = this.computeIGR(attributeWithHighestIGR, rows);
//            System.out.println("\t".repeat(level) + "When attribute " +
//                    (attributeWithHighestIGR + 1) +
//                    " has value " + valInAttribute);

            TreeNode childNode = new TreeNode(attributeWithHighestIGR, valInAttribute, new ArrayList<>(), -1);

            // recursive call on childNode and build the tree
            printDecisionTree(data, attributes, rowsAssocWithAttrVal, level+1, currValIGR, childNode);
            parent.addTreePath(childNode); // add the child node path
        }
    }
}

