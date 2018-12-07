/* 
 * Lattice.java
 *
 * Defines a new "Lattice" type, which is a directed acyclic graph that
 * compactly represents a very large space of speech recognition hypotheses
 *
 * Note that the Lattice type is immutable: after the fields are initialized
 * in the constructor, they cannot be modified.
 *
 * 
 * I.C. & I.M
 *
 */
import java.io.File;
import java.util.List;
import java.util.Stack;
import java.util.Arrays;
import java.util.Scanner;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.ArrayDeque;
import java.util.Collections;
import java.text.NumberFormat;
import java.util.PriorityQueue;
import java.io.FileNotFoundException;

public class Lattice {
    private String utteranceID;       // A unique ID for the sentence
    private int startIdx, endIdx;     // Indices of the special start and end tokens
    private int numNodes, numEdges;   // The number of nodes and edges, respectively
    private Edge[][] adjMatrix;       // Adjacency matrix representing the lattice
                                      //   Two dimensional array of Edge objects
                                      //   adjMatrix[i][j] == null means no edge (i,j)
    private double[] nodeTimes;       // Stores the timestamp for each node
    private int[] inDegrees;
    
    

    
    // Constructor

    // Lattice
    // Preconditions:
    //     - latticeFilename contains the path of a valid lattice file
    // Post-conditions
    //     - Field id is set to the lattice's ID
    //     - Field startIdx contains the node number for the start node
    //     - Field endIdx contains the node number for the end node
    //     - Field numNodes contains the number of nodes in the lattice
    //     - Field numEdges contains the number of edges in the lattice
    //     - Field adjMatrix encodes the edges in the lattice:
    //        If an edge exists from node i to node j, adjMatrix[i][j] contains
    //        the address of an Edge object, which itself contains
    //           1) The edge's label (word)
    //           2) The edge's acoustic model score (amScore)
    //           3) The edge's language model score (lmScore)
    //        If no edge exists from node i to node j, adjMatrix[i][j] == null
    //     - Field nodeTimes is allocated and populated with the timestamps for each node
    // Notes:
    //     - If you encounter a FileNotFoundException, print to standard error
    //         "Error: Unable to open file " + latticeFilename
    //       and exit with status (return code) 1
    //     - If you encounter a NoSuchElementException, print to standard error
    //         "Error: Not able to parse file " + latticeFilename
    //       and exit with status (return code) 2
    public Lattice(String latticeFilename) {
                
        Scanner inFile;
        File latticeFile = new File(latticeFilename);

         try{
             inFile = new Scanner (new File (latticeFilename));            
             getMatrix(inFile);
             
                                                          
         }catch(FileNotFoundException e){
             System.out.println("Error: 1 " + latticeFilename + " not found.");
             System.exit(1);
         }
        return;
    }
    
        
    private void getMatrix(Scanner input){
        String data [];
        
        for(int i =0; i < 5; i++){
            data = input.nextLine().split(" ");
            if(data[0].equals("id"))
                this.utteranceID = data[1];
            if(data[0].equals("start"))
                this.startIdx = Integer.parseInt(data[1]);
            if(data[0].equals("end"))
                this.endIdx = Integer.parseInt(data[1]);                
            if(data[0].equals("numNodes"))
                this.numNodes = Integer.parseInt(data[1]);
            if(data[0].equals("numEdges"))
                this.numEdges = Integer.parseInt(data[1]);                
        }
        
        this.adjMatrix = new Edge [this.numNodes][this.numNodes];
        this.nodeTimes = new double [this.numNodes];
        this.inDegrees = new int [this.numNodes];
        
        for(int n =0; n< this.numNodes ; n++){
             data = input.nextLine().split(" ");
            this.nodeTimes[Integer.parseInt(data[1])] = Double.parseDouble(data[2]); 
        }
        
        while(input.hasNextLine()){
            data = input.nextLine().split(" ");            
            this.adjMatrix[Integer.parseInt(data[1])][Integer.parseInt(data[2])] 
                = new Edge(data[3], Integer.parseInt(data[4]), Integer.parseInt(data[5]));
            
            this.inDegrees[Integer.parseInt(data[2])]++;
        }
             
        input.close();
    }
    
        
    
    // Accessors 

    // getUtteranceID
    // Pre-conditions:
    //    - None
    // Post-conditions:
    //    - Returns the utterance ID
    public String getUtteranceID() {
        return this.utteranceID;
    }

    // getNumNodes
    // Pre-conditions:
    //    - None
    // Post-conditions:
    //    - Returns the number of nodes in the lattice
    public int getNumNodes() {
        return this.numNodes;
    }

    // getNumEdges
    // Pre-conditions:
    //    - None
    // Post-conditions:
    //    - Returns the number of edges in the lattice
    public int getNumEdges() {
        return this.numEdges;
    }

    // toString
    // Pre-conditions:
    //    - None
    // Post-conditions:
    //    - Constructs and returns a string describing the lattice in the same
    //      format as the input files.  Nodes should be sorted ascending by node 
    //      index, edges should be sorted primarily by start node index, and 
    //      secondarily by end node index 
    // Notes:
    //    - Do not store the input string verbatim: reconstruct it on they fly
    //      from the class's fields
    //    - toString simply returns a string, it should not print anything itself
    // Hints:
    //    - You can use the String.format method to print a floating point value 
    //      to two decimal places
    //    - A StringBuilder is asymptotically more efficient for accumulating a
    //      String than repeated concatenation
    public String toString() {
        
        String timeString = "";
        StringBuilder sBuilder = new StringBuilder();
        
        sBuilder.append("id " + this.utteranceID +" start " + this.startIdx +" end "+ endIdx + " numNodes " + this.numNodes 
                + " numEdges " + this.numEdges + " ");
    
        
        for(int i= 0; i< this.numNodes; i++)
            sBuilder.append("node " + i + " "+ timeString.format("%.2f", (double)this.nodeTimes[i]) + " ");        
        
        for(int i= 0; i< this.numNodes; i++){
            for(int j=0; j< this.numNodes; j++)
                if(this.adjMatrix[i][j] != null){
                    sBuilder.append("edge " +i + " " + j+" " +this.adjMatrix[i][j].getLabel() + " " + 
                    this.adjMatrix[i][j].getAmScore() + " " +this.adjMatrix[i][j].getLmScore() + " ");
                }
        }
        return sBuilder.toString();
    }

    // decode
    // Pre-conditions:
    //    - lmScale specifies how much lmScore should be weighted
    //        the overall weight for an edge is amScore + lmScale * lmScore
    // Post-conditions:
    //    - A new Hypothesis object is returned that contains the shortest path
    //      (aka most probable path) from the startIdx to the endIdx
    // Hints:
    //    - You can create a new empty Hypothesis object and then
    //      repeatedly call Hypothesis's addWord method to add the words and 
    //      weights, but this needs to be done in order (first to last word)
    //      Backtracking will give you words in reverse order.
    //    - java.lang.Double.POSITIVE_INFINITY represents positive infinity
    // Notes:
    //    - It is okay if this algorithm has time complexity O(V^2)
    public Hypothesis decode(double lmScale) {                
        double posInfinity = java.lang.Double.POSITIVE_INFINITY;
        double costs[] = new double [this.numNodes];        
        int predecessor [] = new int [this.numNodes];
        Hypothesis p = new Hypothesis();
        double edgeWeight =0.0;                
        int n=0;
        
        for(int i= 0; i < this.numNodes; i++){
            costs[i] = posInfinity;        
            predecessor[i]=-1;
        }
        
        predecessor[this.startIdx]=0;
        costs[this.startIdx] = 0.0;
        
        int topSortNodes [] = topologicalSort();
        int topSortNodesLength = topSortNodes.length;
        
        //for each vertex n in topSortNodes
        for(int j =0; j < topSortNodesLength; j++){
            n = topSortNodes[j];
            //for each vetrex i that n is adjacent to 
            for(int i =0; i < this.numNodes; i++){
                if(this.adjMatrix[i][n] != null){
                        edgeWeight = this.adjMatrix[i][n].getCombinedScore(lmScale);
                        if( ( edgeWeight + costs[i]) <= costs[n]){                   
                            costs[n] = edgeWeight + costs[i];                    
                            predecessor[n]=i;
                        }
                }
            }
        }
        int u, v;
        int t [] =  BackTrack(predecessor);            
        for(int i =0; i < t.length-1; i++){
            u = t[i];
            v= t[i+1];
            if(this.adjMatrix[u][v] != null)
                p.addWord(this.adjMatrix[u][v].getLabel(), this.adjMatrix[u][v].getCombinedScore(lmScale));
            
        }
        return p;
    }

    //BackTrack
    //Pre-conditions:
    //  -p[] predecessors of node at index i where i is in the range of 0-p.length
    //Post-condtions:
    //  -returns a path of node indices from this.startIdx to this.endIdx
    private int [] BackTrack(int p[]){
        int node = this.endIdx;
        
    
        Stack<Integer> path = new Stack<Integer>();
        while(node != this.startIdx){
            path.push(node);
            node = p[node];
        }
               
        int [] k = new int [path.size()];

        int m=0;
        while( !path.isEmpty()){
            k[m] = path.pop();            
            m++;
        }
        
        return k;                
    }
        
            
    // topologicalSort
    // Pre-conditions:
    //    - None
    // Post-conditions:
    //    - A new int[] is returned with a topological sort of the nodes
    //      For example, the 0'th element of the returned array has no 
    //      incoming edges.  More generally, the node in the i'th element 
    //      has no incoming edges from nodes in the i+1'th or later elements
    public int[] topologicalSort() {
        ArrayList<Integer> result = new ArrayList<Integer>();       
        ArrayList<Integer> S = new ArrayList<Integer>();       
        int sum = 0;
        int n;
        
        int [] tempInDegrees= new int [this.numNodes];
        S.add(this.startIdx);             

        for(int i= 0; i< this.numNodes; i++)
            tempInDegrees[i] = this.inDegrees[i];
        
        while( !S.isEmpty()){
            n= S.remove(0);
            result.add(n);            
            for(int j =0; j< this.numNodes; j++){
                if(this.adjMatrix[n][j]!=null){
                    tempInDegrees[j]--;
                    if(tempInDegrees[j]==0)
                        S.add(j);                                                                   
                }
            }
        }        

        int resultSize= result.size();
        int a [] = new int [resultSize];
        for(int l=0; l< resultSize; l++)
            a[l] = result.get(l);
                                
        for(int m =0; m< this.numNodes; m++)
            sum+= tempInDegrees[m];
            
        if(sum> 0){
            System.out.println("graph has cycles, exitiing");
            System.exit(3);
        }
        
        return a;
    }
        

    // countAllPaths
    // Pre-conditions:
    //    - None
    // Post-conditions:
    //    - Returns the total number of distinct paths from startIdx to endIdx
    //       (do not count other subpaths)
    // Hints:
    //    - The straightforward recursive traversal is prohibitively slow
    //    - This can be solved efficiently using something similar to the 
    //        shortest path algorithm used in decode
    //        Instead of min'ing scores over the incoming edges, you'll want to 
    //        do some other operation...

    public java.math.BigInteger countAllPaths(){
        BigInteger countNodePaths [] = new BigInteger[ this.numNodes];
        int topSortNodes [] = topologicalSort();
        int topSortLimit = topSortNodes.length;
        int paths=0;
        int n=0;
        
        for(int l=0; l< this.numNodes; l++)
            countNodePaths[l] = BigInteger.ZERO;
                    
        countNodePaths[this.startIdx] = BigInteger.ONE;        
        
        for(int k=0; k< topSortLimit; k++){
                n= topSortNodes[k];
                for(int i=0; i< this.numNodes; i++){
                        if(this.adjMatrix[i][n] != null)                                                                               
                            countNodePaths[n] = countNodePaths[n].add(countNodePaths[i]);                        
                }
       }        
       return countNodePaths[this.endIdx];
    }
    
    
    // getLatticeDensity
    // Pre-conditions:
    //    - None
    // Post-conditions:
    //    - Returns the lattice density, which is defined to be:
    //      (# of non -silence- words in lattice) / (# seconds from start to end index)
	//      Note that multiwords (e.g. to_the) count as a single non-silence word
    public double getLatticeDensity() {        
        int silenceCount=0;
        
        for(int i=0; i< this.numNodes; i++){
            for(int j=0; j< this.numNodes; j++){
                if(this.adjMatrix[i][j] != null){
                    if( !this.adjMatrix[i][j].getLabel().equals("-silence-"))
                        silenceCount++;
                }                
            }            
        }
        return (double)silenceCount /(nodeTimes[this.endIdx] - nodeTimes[this.startIdx]);
    }

    // writeAsDot - write lattice in dot format
    // Pre-conditions:
    //    - dotFilename is the name of the intended output file
    // Post-conditions:
    //    - The lattice is written in the specified dot format to dotFilename
    // Notes:
    //    - See the assignment description for the exact for        //System.out.println(a);matting to use
    //    - For context on the dot format, see    
    //        - http://en.wikipedia.org/wiki/DOT_%28graph_description_language%29
    //        - http://www.graphviz.org/pdf/dotguide.pdf
    public void writeAsDot(String dotFilename) {        
        File dotFile;
        
        if( dotFilename != null){                    
            try{
                dotFile = new File(dotFilename);
                PrintWriter printer = new PrintWriter(dotFile); 
                String dotString = this.toString();
                
                printer.write("digraph g { " + "\n   rankdir=\"LR\"\n");
                for(int i=0; i<this.numNodes; i++){                    
                    for(int j=0; j<this.numNodes; j++){
                        if(this.adjMatrix[i][j] != null)
                            printer.write("   " + i + " -> " + j + " [label = \"" + this.adjMatrix[i][j].getLabel()+ "\"]\n" );
                    }
                }                
                printer.write("}");
                printer.close();                        
            }catch(FileNotFoundException e){        
                System.out.println("Error: Unable to open file " + dotFilename);
                System.exit(3);                
            }
        }
        
        return;
    }

    // saveAsFile - write in the simplified lattice format (same as input format)
    // Pre-conditions:
    //    - latticeOutputFilename is the name of the intended output file
    // Post-conditions:
    //    - The lattice's toString() representation is written to the output file
    // Note:
    //    - This output file should be in the same format as the input .lattice file
    public void saveAsFile(String latticeOutputFilename) {        
        File latticeOutputFile;
        
        if( latticeOutputFilename != null){            
            try{
                latticeOutputFile = new File(latticeOutputFilename);
                PrintWriter printer = new PrintWriter(latticeOutputFile); 
                String latticString = this.toString();
                Scanner Lattice1 = new Scanner(latticString);        
                
                printer.write(Lattice1.next() +" "+ Lattice1.next() + "\n");
                printer.write(Lattice1.next() +" "+ Lattice1.next() + "\n");
                printer.write(Lattice1.next() +" "+ Lattice1.next() + "\n");
                printer.write(Lattice1.next() +" "+ Lattice1.next() + "\n");
                printer.write(Lattice1.next() +" "+ Lattice1.next() + "\n");        
                        
                for(int i=0; i<this.numNodes; i++)
                    printer.write(Lattice1.next() +" "+ Lattice1.next() +" "+ Lattice1.next() + "\n");                
                
                for(int i=0; i<this.numEdges; i++)
                    printer.write(Lattice1.next() +" "+ Lattice1.next() +" "+ Lattice1.next() +" "+ Lattice1.next()+ " "+ Lattice1.next()+" "+          Lattice1.next() +"\n");                
                
                printer.close();                        
            }catch(FileNotFoundException e){
                System.out.println("Error: Unable to open file " + latticeOutputFilename);
                System.exit(3);                
            }
        }                        
        return;
    }

    // uniqueWordsAtTime - find all words at a certain point in time
    // Pre-conditions:
    //    - time is the time you want to query
    // Post-conditions:
    //    - A HashSet is returned containing all unique words that overlap 
    //      with the specified time
    //     (If the time is not within the time range of the lattice, the Hashset should be empty)
    public java.util.HashSet<String> uniqueWordsAtTime(double time) { 
        HashSet<String> wordSet = new HashSet<String>();
        
        for(int i=0; i< this.numNodes; i++){
            if(this.nodeTimes[i]==time) {
                for(int j =0; j<this.numNodes; j++){                    
                    if(this.nodeTimes[j]==time) {  
                        if(this.adjMatrix[i][j] != null)
                            wordSet.add(this.adjMatrix[i][j].getLabel());                                            
                    }
                }
            }
        }        
        return wordSet;
    }

    // printSortedHits - print in sorted order all times where a given token appears
    // Pre-conditions:
    //    - word is the word (or multiword) that you want to find in the lattice
    // Post-conditions:
    //    - The midpoint (halfway between start and end time) for each instance of word
    //      in the lattice is printed to two decimal places in sorted (ascending) order
    //      All times should be printed on the same line, separated by a single space character
    //      (If no instances appear, nothing is printed) 
    // Note:
    //    - java.util.Arrays.sort can be used to sort
    //    - PrintStream's format method can print numbers to two decimal places
    public void printSortedHits(String word) {        
        ArrayList<Double> occuranceArray = new ArrayList<Double>();
        
        for(int i =0; i< this.numNodes; i++){
            for(int j=0; j< this.numNodes; j++)
                if(this.adjMatrix[i][j] != null && this.adjMatrix[i][j].getLabel().equals(word))
                   occuranceArray.add((this.nodeTimes[j] + this.nodeTimes[i])/2.0 );                   
        }
        
        Collections.sort(occuranceArray);
        int occurArraLimit = occuranceArray.size();
        for(int i=0; i< occurArraLimit; i++){
            System.out.format("%2.2f", occuranceArray.get(i));
            System.out.print(" ");        
        }
        System.out.println();                
        return;
    }
}
