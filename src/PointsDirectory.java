import java.io.*;
import java.net.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.time.Instant;

// Class type to hold each transaction
// Holds Payer Name, Payer Points, and Timestamp of Points age
class Transaction {
    public String payer; 
    public int points;
    public Instant timestamp;
    public Transaction(String payer_, int points_, Instant timestamp_) {payer = payer_; points = points_; timestamp = timestamp_;}
    public String toString() {return payer + " " + points + " " + timestamp;}
}

// Compares two Transaction objects and returns the earlier date for heap sorting
class TransactionComparator implements Comparator<Transaction>{
    public int compare(Transaction a, Transaction b) {
        return a.timestamp.compareTo(b.timestamp);
    }
}

public class PointsDirectory {
    // Private member variable to maintain a Priority Queue of Transactions based on date
    private PriorityQueue<Transaction> transactionPQ;
    // Private member variable to maintain a map for each Payer and their respective Point totals
    private HashMap<String, Integer> transactionMap;

    public PointsDirectory(String file) throws FileNotFoundException {
        transactionPQ = new PriorityQueue<>(new TransactionComparator());
        transactionMap = new HashMap<>();
        populateHeapMap(file);
    }
    // Private constructor function reads CSV file, parses values, and adds to Map and PQ
    private void populateHeapMap(String file) throws FileNotFoundException {
        // Reads CSV file from current directory
        URL path = PointsDirectory.class.getResource(file);
        Scanner CSVscanner = new Scanner(new File(path.getFile()));
        CSVscanner.nextLine();

        // Parses each line, converting values into Transaction objects to hold each transaction
        while(CSVscanner.hasNextLine()){
            String line = CSVscanner.nextLine();
            String[] transaction = line.split(",");

            Instant timestamp = Instant.parse(transaction[2].substring(1, transaction[2].length()-1));

            Transaction currTransaction = new Transaction(transaction[0], Integer.parseInt(transaction[1]), timestamp);
            
            // Add transactions to PQ and Map if points are earned
            if (currTransaction.points >= 0) {
                transactionPQ.add(currTransaction);
                transactionMap.put(currTransaction.payer, currTransaction.points + transactionMap.getOrDefault(currTransaction.payer, 0));
            }
            // If points from transaction is negative, spend the points
            else {
                spendPoints(currTransaction.points*-1);
            }
        }
        CSVscanner.close();
    }
    // Takes a point amount and subtracts it from the payers first in the Priority Queue
    public void spendPoints(int spendingPoints) {
        while (spendingPoints > 0 && !transactionPQ.isEmpty()) {
            int currSpendingPoints = spendingPoints;
            spendingPoints -= transactionPQ.peek().points;

            // SpendingPoints still exist after removing first payer from PQ
            if (spendingPoints >= 0) {
                Transaction removed = transactionPQ.poll();
                transactionMap.put(removed.payer, transactionMap.get(removed.payer) - removed.points);
            }
            // First payer from PQ uses all spendingPoints and still exists
            else {
                Transaction oldestTransaction = transactionPQ.peek();
                transactionMap.put(oldestTransaction.payer, transactionMap.get(oldestTransaction.payer) - currSpendingPoints);
            }
        }
    }
    // Outputs each payer and the point balances
    public void getPayerBalances() {
        transactionMap.forEach((k,v) -> System.out.println(k + ": " + v));
    }
    public void printHeap() {
        System.out.println(transactionPQ);
    }

    public static void main(String[] args) throws Exception {
        PointsDirectory transactions = new PointsDirectory("transactions.csv");
        //transactions.getPayerBalances();
        //transactions.printHeap();
        if (args.length != 0)
            transactions.spendPoints(Integer.parseInt(args[0]));
        //System.out.println();
        transactions.getPayerBalances();
    }
}
