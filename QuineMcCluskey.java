import java.util.LinkedList;
import java.util.Arrays;
import java.util.Collections;

/**
 * A class to handle processing the Quine-McCluskey Algorithm
 */
public class QuineMcCluskey {

    /**
     * An object to hold information about a minterm when using the Quine-McCluskey Algorithm
     */
    public class Minterm implements Comparable<Minterm> {

        // Instance Fields

        private int[] values;
        private String value;
        private boolean used;

        // Constructor

        /**
        * Creates a new Minterm object
        *
        * @param values The values this Minterm covers
        * @param value The bit value for this Minterm
        */
        public Minterm(int[] values, String value) {
            this.values = values;
            this.value = value;
            this.used = false;

            // Sort the values in ascending order (bubble sort is okay in this case)
            for (int i = 0; i < values.length - 1; i++)
                for (int j = i + 1; j < values.length; j++)
                    if (values[j] < values[i]) {
                        int temp = values[j];
                        values[j] = values[i];
                        values[i] = temp;
                    }
        }

        /**
        * Returns a String representation of the Minterm.
        */
        public String toString() {
            String value = "";
            for (int i = 0; i < this.values.length; i++) {
                value += this.values[i];
                if (i < this.values.length - 1)
                    value += ", ";
            }
            
            return String.format(
                "m(%s) = %s",
                value, this.value
            );
        }

        /**
        * Determines if this Minterm object is equal to another object.
        */
        public boolean equals(Object object) {
            if (! (object instanceof Minterm))
                return false;
            
            Minterm minterm = (Minterm) object;

            return (
                Arrays.equals(minterm.values, this.values) &&
                this.value.equals(minterm.value)
            );
        }

        public int compareTo(Minterm minterm) {

            // Lengths of values are the same
            if (this.values.length == minterm.values.length) {

                // Compare by each value
                for (int i = 0; i < this.values.length; i++)
                    if (this.values[i] < minterm.values[i])
                        return -1;
                    else if (this.values[i] > minterm.values[i])
                        return 0;
                
                // Minterm is the same; Compare value
                return this.value.compareTo(minterm.value);
            }

            return this.values.length - minterm.values.length;

        }

        // Getters

        /**
        * Returns the values in this Minterm.
        *
        * @returns int[]
        */
        public int[] getValues() {
            return values;
        }

        /**
        * Returns the value of this Minterm.
        *
        * @returns String
        */
        public String getValue() {
            return value;
        }

        /**
        * Returns whether or not this Minterm has been used.
        *
        * @returns boolean
        */
        public boolean used() {
            return used;
        }

        // Setters

        /**
        * Labels this Minterm as "used"
        */
        public void use() {
            used = true;
        }

        // Other Methods

        /**
        * Combines 2 Minterms together if they can be combined
        *
        * @returns Minterm
        */
        public Minterm combine(Minterm minterm) {

            // Check if the value is the same; If so, do nothing
            if (this.value.equals(minterm.value))
                return null;
            
            // Check if the values are the same; If so, do nothing
            if (Arrays.equals(this.values, minterm.values))
                return null;
            
            // Keep track of the difference between the minterms
            int diff = 0;
            String result = "";

            // Iterate through the bits in this Minterm's value
            for (int i = 0; i < this.value.length(); i++) {

                // Check if the current bit value differs from the minterm's bit value
                if (this.value.charAt(i) != minterm.value.charAt(i)) {
                    diff += 1;
                    result += "-";
                }

                // There is not a difference
                else
                    result += this.value.charAt(i);
                
                // The difference has exceeded 1
                if (diff > 1)
                    return null;
            }

            // Combine the values of this Minterm with the values of minterm
            int[] newValues = new int[this.values.length + minterm.values.length];
            for (int i = 0; i < this.values.length; i++)
                newValues[i] = this.values[i];
            for (int i = 0; i < minterm.values.length; i++)
                newValues[i + this.values.length] = minterm.values[i];

            return new Minterm(newValues, result);
        }
    }

    // Instance Fields

    private String[] variables;
    private int[] values;
    private String function;

    // Constructor

    /**
     * Creates a new QM object to process the Quine-McCluskey Algorithm
     */
    public QuineMcCluskey(String[] variables, int[] values) {
        this.variables = variables;
        this.values = values;
        this.function = getFunction(false);
    }

    // Helper Methods

    /**
     * Returns the binary representation of the specified value.
     *
     * @param value The int value to turn into a binary digit.
     * @returns String
     */
    private String getBits(int value) {

        // Convert the value into a binary number
        // Then add extra 0's to the beginning to match how many variables there are
        String result = Integer.toBinaryString(value);
        for (int i = result.length(); i < this.variables.length; i++)
            result = "0" + result;
        return result;

    }

    // Grouping Methods

    /**
     * Creates the initial grouping for the bits from the values
     * given to the Quine-McCluskey Algorithm.
     *
     * @returns Minterm[][]
     */
    private Minterm[][] initialGroup() {

        // Keep track of groups by an array of linked lists
        LinkedList<Minterm>[] groups = new LinkedList[this.variables.length + 1];
        for (int i = 0; i < groups.length; i++)
            groups[i] = new LinkedList<Minterm>();

        // Iterate through values
        for (int value: this.values) {

            // Count number of 1's in value's bit equivalent
            int count = 0;
            String bits = getBits(value);
            for (int i = 0; i < bits.length(); i++)
                if (bits.charAt(i) == '1')
                    count += 1;
            
            // Add count to proper group
            groups[count].add(new Minterm(new int[] {value}, bits));
        }

        // Turn the groups into 2-dimensional array
        Minterm[][] groupsArray = new Minterm[groups.length][];
        for (int i = 0; i < groups.length; i++) {
            groupsArray[i] = new Minterm[groups[i].size()];
            for (int j = 0; j < groups[i].size(); j++)
                groupsArray[i][j] = groups[i].get(j);
        }

        return groupsArray;

    }

    /**
     * Creates a power set of all valid prime implicants that covers the rest of an expression.
     * This is used after the essential prime implicants have been found.
     *
     * @param values An array of int's that the power set must cover.
     * @param primeImplicants An array of Minterms for the prime implicants to check for.
     * @returns Minterm[]
     */
    private Minterm[] powerSet(int[] values, Minterm[] primeImplicants) {

        if (primeImplicants.length == 0)
            return new Minterm[] {};

        // Get the power set of all the prime implicants
        LinkedList<LinkedList<Minterm>> powerset = new LinkedList<>();

        // Iterate through the decimal values from 1 to 2 ** size - 1
        for (int i = 1; i < (int)(Math.pow(2, primeImplicants.length)); i++) {
            LinkedList<Minterm> currentset = new LinkedList<>();
            
            // Get the binary value of the decimal value
            String binValue = Integer.toBinaryString(i);
            for (int j = binValue.length(); j < primeImplicants.length; j++)
                binValue = "0" + binValue;
            
            // Find which indexes have 1 in the binValue string
            for (int index = 0; index < binValue.length(); index++)
                if (binValue.charAt(index) == '1')
                    currentset.add(primeImplicants[index]);
            powerset.add(currentset);
        }

        // Remove all subsets that do not cover the rest of the implicants
        LinkedList<Integer> valuesLinkedList = new LinkedList<>();
        for (int i = 0; i < values.length; i++)
            valuesLinkedList.add(values[i]);

        LinkedList<LinkedList<Minterm>> newPowerset = new LinkedList<>();
        for (LinkedList<Minterm> subset: powerset) {

            // Get all the values the set covers
            LinkedList<Integer> tempValues = new LinkedList<>();
            for (Minterm implicant: subset)
                for (int value: implicant.getValues())
                    if (!tempValues.contains(value) && valuesLinkedList.contains(value))
                        tempValues.add(value);
            
            // Turn the LinkedList into an array
            int[] tempValuesArray = new int[tempValues.size()];
            for (int i = 0; i < tempValues.size(); i++)
                tempValuesArray[i] = tempValues.get(i);
            
            // Sort tempValuesArray
            for (int i = 0; i < tempValuesArray.length; i++)
                for (int j = i; j < tempValuesArray.length; j++)
                    if (tempValuesArray[j] < tempValuesArray[i]) {
                        int temp = tempValuesArray[i];
                        tempValuesArray[i] = tempValuesArray[j];
                        tempValuesArray[j] = temp;
                    }

            // Check if this subset covers the rest of the values
            if (Arrays.equals(tempValuesArray, values))
                newPowerset.add(subset);
        }
        powerset = newPowerset;

        // Find the minimum amount of implicants that can cover the expression
        LinkedList<Minterm> minSet = powerset.get(0);
        for (LinkedList<Minterm> subset: powerset)
            if (subset.size() < minSet.size())
                minSet = subset;
        
        // Turn the minSet into an array
        Minterm[] minSetArray = new Minterm[minSet.size()];
        for (int i = 0; i < minSet.size(); i++)
            minSetArray[i] = minSet.get(i);
        
        return minSetArray;
    }

    // Compare Methods

    /**
     * Returns an array of all the prime implicants for the expression.
     *
     * @returns Minterm[]
     */
    private Minterm[] getPrimeImplicants() {
        return getPrimeImplicants(initialGroup());
    }

    /**
     * Returns an array of all the prime implicants for the expression.
     *
     * @param groups A 2-dimensional array of minterms separated into groups
     * @returns Minterm[]
     */
    private Minterm[] getPrimeImplicants(Minterm[][] groups) {

        // If there is only 1 group, return all minterms in it
        if (groups.length == 1)
            return groups[0];
        
        // Try comparing the rest
        else {

            // Only run this if groups.length - 1 is greater than 0
            if (groups.length - 1 <= 0)
                return new Minterm[] {};
                
            LinkedList<Minterm> unused = new LinkedList<Minterm>();
            
            int[] comparisons = new int[(groups.length - 1 > 0)? groups.length - 1: 0];
            for (int i = 0; i < comparisons.length; i++)
                comparisons[i] = i;

            LinkedList<Minterm>[] newGroups = new LinkedList[comparisons.length];
            for (int i = 0; i < newGroups.length; i++)
                newGroups[i] = new LinkedList<Minterm>();

            for (int compare: comparisons) {
                Minterm[] group1 = groups[compare];
                Minterm[] group2 = groups[compare + 1];

                // Compare every term in group1 with every term in group2
                for (Minterm term1: group1)
                    for (Minterm term2: group2) {

                        // Try combining it
                        Minterm term3 = term1.combine(term2);

                        // Only add it to the new group if term3 is not null
                        //  term3 will only be null if term1 and term2 could not
                        //  be combined
                        if (term3 != null) {
                            term1.use();
                            term2.use();
                            if (! newGroups[compare].contains(term3))
                                newGroups[compare].add(term3);
                        }
                    }
            }

            // Turn the newGroups into a 2-dimensional array
            Minterm[][] newGroupsArray = new Minterm[newGroups.length][];
            for (int i = 0; i < newGroups.length; i++) {
                newGroupsArray[i] = new Minterm[newGroups[i].size()];
                for (int j = 0; j < newGroups[i].size(); j++)
                    newGroupsArray[i][j] = newGroups[i].get(j);
            }

            // Add unused minterms
            for (Minterm[] group: groups)
                for (Minterm term: group)
                    if (!term.used() && !unused.contains(term))
                        unused.add(term);
            
            // Add recursive call
            for (Minterm term: getPrimeImplicants(newGroupsArray))
                if (!term.used() && !unused.contains(term))
                    unused.add(term);
            
            // Turn the unused into an array
            Minterm[] unusedArray = new Minterm[unused.size()];
            for (int i = 0; i < unused.size(); i++)
                unusedArray[i] = unused.get(i);
                
            return unusedArray;
        }
    }

    // Solving Methods

    /**
     * Solves for the expression returning the minimal amount of prime implicants needed
     * to cover the expression.
     *
     * @returns Minterm[]
     */
    private Minterm[] solve() {

        // Get the prime implicants
        Minterm[] primeImplicants = getPrimeImplicants();

        // Keep track of values with only 1 implicant
        //  These are the essential prime implicants
        LinkedList<Minterm> essentialPrimeImplicants = new LinkedList<Minterm>();
        boolean[] valuesUsed = new boolean[this.values.length];
        for (int i = 0; i < this.values.length; i++)
            valuesUsed[i] = false;
        
        for (int i = 0; i < values.length; i++) {
            int value = values[i];

            // Count how many times the current minterm value is used
            int uses = 0;
            Minterm last = null;
            for (Minterm minterm: primeImplicants) {
                boolean found = false;
                for (int j = 0; j < minterm.getValues().length; j++)
                    if (value == minterm.getValues()[j]) {
                        found = true;
                        break;
                    }
                
                if (found) {
                    uses += 1;
                    last = minterm;
                }
            }
            
            // If there is only 1 use, this is an essential prime implicant
            if (uses == 1 && !essentialPrimeImplicants.contains(last)) {
                for (int lv = 0; lv < last.getValues().length; lv++)
                    for (int v = 0; v < values.length; v++)
                        if (last.getValues()[lv] == values[v]) {
                            valuesUsed[v] = true;
                            break;
                        }
                essentialPrimeImplicants.add(last);
            }
        }

        // Turn the essentialPrimeImplicants into an array
        Minterm[] essentialPrimeImplicantsArray = new Minterm[essentialPrimeImplicants.size()];
        for (int i = 0; i < essentialPrimeImplicants.size(); i++) {
            essentialPrimeImplicantsArray[i] = essentialPrimeImplicants.get(i);
        }

        // Check if all values were used
        boolean found = false;
        for (int i = 0; i < valuesUsed.length; i++)
            if (valuesUsed[i]) {
                found = true;
                break;
            }
        
        // If all values were used, return the essential prime implicants
        if (!found) {
            return essentialPrimeImplicantsArray;
        }

        // Keep track of prime implicants that cover as many values as possible
        LinkedList<Minterm> newPrimeImplicants = new LinkedList<>();
        for (int i = 0; i < primeImplicants.length; i++)
            if (!essentialPrimeImplicants.contains(primeImplicants[i]))
                newPrimeImplicants.add(primeImplicants[i]);
        
        // Turn the new prime implicants into an array
        primeImplicants = new Minterm[newPrimeImplicants.size()];
        for (int i = 0; i < newPrimeImplicants.size(); i++) {
            primeImplicants[i] = newPrimeImplicants.get(i);
        }
        
        // Check if there is only 1 implicant left (very rare but just in case)
        if (primeImplicants.length == 1) {
            Minterm[] finalResult = new Minterm[
                essentialPrimeImplicantsArray.length + primeImplicants.length
            ];
            for (int i = 0; i < essentialPrimeImplicantsArray.length; i++)
                finalResult[i] = essentialPrimeImplicantsArray[i];
            for (int i = 0; i < primeImplicants.length; i++)
                finalResult[essentialPrimeImplicantsArray.length + i] = primeImplicants[i];
            return finalResult;
        }

        // Create a power set from the remaining prime implicants and check which
        //  combination of prime implicants gets the simplest form
        LinkedList<Integer> valuesLeftLinkedList = new LinkedList<>();
        for (int i = 0; i < valuesUsed.length; i++)
            if (!valuesUsed[i])
                valuesLeftLinkedList.add(values[i]);

        // Turn the values left into an array
        int[] valuesLeft = new int[valuesLeftLinkedList.size()];
        for (int i = 0; i < valuesLeftLinkedList.size(); i++) {
            valuesLeft[i] = valuesLeftLinkedList.get(i);
        }
        
        // Get the power set
        Minterm[] powerset = powerSet(valuesLeft, primeImplicants);
        
        // Get the final result
        Minterm[] finalResult = new Minterm[
            essentialPrimeImplicantsArray.length + powerset.length
        ];

        for (int i = 0; i < essentialPrimeImplicantsArray.length; i++)
            finalResult[i] = essentialPrimeImplicantsArray[i];
        for (int i = 0; i < powerset.length; i++)
            finalResult[essentialPrimeImplicantsArray.length + i] = powerset[i];

        return finalResult;
    }

    /**
     * Returns the expression in a readable form.
     */
    public String getFunction() {
        return function;
    }

    /**
     * Returns the expression in a readable form.
     */
    private String getFunction(boolean saveVariable) {

        // Get the prime implicants and variables
        Minterm[] primeImplicants = solve();

        // Check if there are no prime implicants; Always False
        if (primeImplicants.length == 0)
            return "0";
        
        // Check if there is only 1 prime implicant
        else if (primeImplicants.length == 1) {

            // Now check if there are just as many hyphens (-) as there are variables
            int hyphens = 0;
            for (int i = 0; i < primeImplicants[0].getValue().length(); i++)
                if (primeImplicants[0].getValue().charAt(i) == '-')
                    hyphens += 1;
            
            if (hyphens == variables.length)
                return "1";
        }

        String result = "";

        // Iterate through the prime implicants
        for (int i = 0; i < primeImplicants.length; i++) {
            Minterm implicant = primeImplicants[i];

            // Determine if parentheses should be added to each minterm's expression
            int hyphens = 0;
            boolean addParenthesis = false;
            for (int j = 0; j < implicant.getValue().length(); j++)
                if (implicant.getValue().charAt(j) == '-')
                    hyphens += 1;
            if (hyphens < this.variables.length - 1)
                addParenthesis = true;
            
            // Add parenthesis if necessary
            if (addParenthesis)
                result += "(";

            // Iterate through all bits in the implicants value
            for (int j = 0; j < implicant.getValue().length(); j++) {
                String character = String.valueOf(implicant.getValue().charAt(j));
                if (character.equals("0"))
                    result += "NOT ";
                if (!character.equals("-"))
                    result += variables[j];
                
                // Make sure there are no more hyphens
                hyphens = 0;
                for (int k = j + 1; k < implicant.getValue().length(); k++)
                    if (implicant.getValue().charAt(k) == '-')
                        hyphens += 1;
                
                if ((hyphens < implicant.getValue().length() - j - 1) && !character.equals("-"))
                    result += " AND ";
            }

            // Add parenthesis if necessary
            if (addParenthesis)
                result += ")";

            // Combine minterm expressions with an OR statement
            if (i < primeImplicants.length - 1)
                result += " OR ";
        }

        return result;
    }

}