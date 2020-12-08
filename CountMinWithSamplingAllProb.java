//CountMin
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;  
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.io.FileWriter;

public class CountMinWithSamplingAllProb {
    public static int[][] mat;
    public static int[] hashes;
    public static Pair[] flowArray;
    public static Triplet[] tripletArray;
    public static double Probablity = 0.01;
    public static int MAX_RAND = Integer.MAX_VALUE;
    public static int threshold = (int)(Probablity * MAX_RAND);
    public static int dropped = 0;
    public static int totalCountOfPack=0;
    public static int conterSize=0;
    
    // output file
    public static FileWriter fw;
    public static String outfile = "filename.txt";
    
    public static void main(String[] args) {
        //Scanner sc = new Scanner(System.in);
        //System.out.println("Enter value of k");
        //int counterK = 3;//sc.nextInt();//3;
        //System.out.println("Enter value of w");
        //conterSize = 3000;//sc.nextInt(); //3000;
        try {
            File myObj = new File(outfile);
            if (myObj.createNewFile()) {
              System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File already exists.");
            }
            fw = new FileWriter(outfile);
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        

        while(Probablity<1.0){
        int counterK = 3;//sc.nextInt();//3;
        dropped = 0;
        totalCountOfPack = 0;
        //System.out.println("Enter value of w");
        conterSize = 3000;//sc.nextInt(); //3000;
        mat = new int[counterK][conterSize];
        hashes = fillHashArray(counterK);
        threshold = (int)(Probablity * MAX_RAND);
        //for(int i =0;i<hashes.length;i++)
            //System.out.println(hashes[i]);

        int flowCount = readSizeFromFile();
        flowArray = new Pair[flowCount];
        tripletArray = new Triplet[flowCount];
        readFromFile();
        startRecording();
        double averageError = getAverageError();
        Arrays.sort(tripletArray,(a,b) -> b.getEstimate()-a.getEstimate());
        writeToFile(Probablity,averageError);
        //System.out.println("Successfully wrote to the file.");
        //System.out.println("Average Error = " + averageError);
        System.out.println("drpped  = " + dropped);
        //System.out.println("totalCountOfPack  = " + totalCountOfPack);
        Probablity += 0.01;
        }
        try {
            fw.close();
        }
        catch (IOException e) {
          System.out.println("An error occurred.");
          e.printStackTrace();
        }
        
        //for(int i =0;i<hashes.length;i++)
            //System.out.println(hashes[i]);
        //printTop100Values();
    }
    
    public static void readFromFile() {
        try {
            File myObj = new File("project3input.txt");
            Scanner myReader = new Scanner(myObj);
            int k =0;
            myReader.nextLine();
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                Pair p = getPairFromline(data);
                flowArray[k++] = p;
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public static int readSizeFromFile() {
        try {
            File myObj = new File("project3input.txt");
            Scanner myReader = new Scanner(myObj);
            String data = myReader.nextLine();
            myReader.close();
            return Integer.parseInt(data);
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
            return 0;
        }
    }

    public static int[] fillHashArray(int numberOfHash) {
        int newRand;
        int[] hashes = new int[numberOfHash];
        hashes[0] = 387373311;
        hashes[1] = 1611730403;
        hashes[2] = 1337130151;
        // Random rand = new Random();
        // HashSet<Integer> set = new HashSet<>();
        /*for(int i = 0; i<numberOfHash;i++)
        {

            while(true)
            {
                newRand = rand.nextInt((Integer.MAX_VALUE - 1) + 1);
                //newRand = new Random().nextInt((numberOfHash - 1) + 1) + 1;
                if(!set.contains(newRand))
                {
                    set.add(newRand);
                    break;
                }

            }
            hashes[i] = newRand;
        }*/
        return hashes;
    }

    public static void recordInCounter(String flowId, String packetCount)  {
        int packCount = Integer.parseInt(packetCount);
        int newRand;
        totalCountOfPack += packCount;
        for(int currCount = 0; currCount<packCount; currCount++){
            Random rand = new Random();
            newRand = rand.nextInt((Integer.MAX_VALUE - 1) + 1); 
            //System.out.println(newRand + " "+threshold);
            if(newRand<(Probablity*MAX_RAND))
            {
                //System.out.println(newRand + " "+threshold);
                for(int i =0; i<mat.length;i++) {
                int index = getHash(flowId,i);
                mat[i][index] += 1;
                }
            }
            else {
                dropped++;
            }    
        }
    }

    public static int getHash(String flowId, int i) {
        int flowIDHash = Math.abs(flowId.hashCode());
        int index = (hashes[i] ^ flowIDHash)% conterSize;
        return index;
    }

    public static void startRecording()    {
        for(int i =0;i<flowArray.length;i++) {
            recordInCounter(flowArray[i].getFlowId(),flowArray[i].getPacketCount());
        }
    }

    public static double getAverageError() {
        int totalAverageSumValue = 0;
        for(int i =0;i<flowArray.length;i++)   {
            String flowId = flowArray[i].getFlowId();
            int estimatedValue = getEstimatedValue(flowId);
            //System.out.println(estimatedValue);
            Triplet t = new Triplet(flowArray[i],estimatedValue);
            tripletArray[i] = t;
            totalAverageSumValue += Math.abs(estimatedValue - Integer.parseInt(flowArray[i].getPacketCount()));
        }
        return (totalAverageSumValue*1.0)/flowArray.length;
        //return (totalAverageSumValue*1.0);
    }

    public static int getEstimatedValue(String flowId) {
        int minPacketCount = Integer.MAX_VALUE;
        for(int i =0;i<hashes.length;i++)   {
            int index = getHash(flowId,i);
            minPacketCount = Math.min(minPacketCount,mat[i][index]);
        }
        return (int)(minPacketCount*1.0*(1.0/Probablity));
    }

    public static void printTop100Values()    {
        System.out.println("IP Address \t Actual Value \t Estimated Value");
        for(int i =0;i<100;i++) {
            String flowID = tripletArray[i].getFlowId();
            int actualValue = Integer.parseInt(tripletArray[i].getPacketCount());
            int estimatedValue = tripletArray[i].getEstimate();
            System.out.println(flowID+ "\t\t " + actualValue + "\t\t " + estimatedValue);
        }
    }

    public static Pair getPairFromline(String line)    {
        String[] str = line.split(" ");
        int flag = 0;
        String flowId = "";
        String packetCount = "";
        for(String si : str) {
            if(!si.trim().isEmpty() && flag == 0) {
                flowId = si;flag =1;
            }
            else if(!si.trim().isEmpty() && flag == 1)
                packetCount = si;
        }
      return new Pair(flowId,packetCount);
    }


public static void writeToFile(double Probablity, double averageError){
    String val1 = String.valueOf(Probablity);
    String val2 = String.valueOf(averageError);
    try{
        fw.write(val1 + "," + val2+"\n");
    }
    catch (IOException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
    
}
}
class Pair {

    private String flowId;
    private String packetCount;
    public Pair(){}
    public Pair(String flowId, String packetCount)   {
        this.flowId = flowId;
        this.packetCount = packetCount;
    }

    public String getFlowId()  {
        return this.flowId;
    }

    public String getPacketCount()  {
        return this.packetCount;
    }

    public void setPair(Pair pair) {
        this.flowId = pair.getFlowId();
        this.packetCount = pair.getPacketCount();
    }


}

class Triplet extends Pair {
    private int estimate;

    public Triplet(Pair pair, int estimate)    {
        super();
        setPair(pair);
        this.estimate = estimate;
    }

    public int getEstimate() {
        return this.estimate;
    }

}
