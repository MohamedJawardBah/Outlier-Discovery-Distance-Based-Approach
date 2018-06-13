/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package outlierdetection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 * @author Nadian
 */
public class OutlierDetection {

    /**
     * @param args the command line arguments
     */
    private static NumberFormat formatter = new DecimalFormat("#0.00"); 
    private static Scanner scanner = new Scanner(System.in);
    private static final String TRAINING_DATA_FILES = "E:\\Users\\Nadian\\Documents\\NetBeansProjects\\OutlierDetection\\src\\resources\\outlierdataset.txt";

    private static ArrayList<Point> dataSet = new ArrayList<>();
    private static ArrayList<Point> trainingPoints = new ArrayList<>();
    //kebutuhan clustering
    private static ArrayList<Point> centroid = new ArrayList<>();
    static int sizeData=0;
    static int nIdx=0;
    private static double[][] cArrayDistances = new double[sizeData][sizeData]; //prepare matriks jarak

        
    private static final int k = 2; //kluster yang dinginkan di tahap 1. clustering
    
    public static void main(String[] args) {
        trainingPoints = setData(TRAINING_DATA_FILES);
        nIdx = trainingPoints.get(0).getmIndex().length; //get jumlah feature/index
        System.out.println("jumlah index: "+nIdx);
        
        //---------------------- 1. clustering, define centroid of each cluster (hierarchical clustering)
        centroid = setData(TRAINING_DATA_FILES);
        initLabelCentroid(trainingPoints);
        initLabelCentroid(centroid);
        sizeData = trainingPoints.size(); //prepare matriks of jarak 
        cArrayDistances = new double[sizeData][sizeData];
        //-- additional : EQUIPMENTS CHECKS
        //mencetak  inisial centroid didapat
        equipCheckClustering();
        while(centroid.size()>k){
            //DO THE HIRARCHICAL CLUSTERING ALGORITHM
            doHirarCluster();
        }
        System.out.println("-------------------- 1. CLUSTERING is done");
        System.out.println("=> trainingPoints CLUSTERING RESULT IS LOADED");
        printPointData(trainingPoints);
        System.out.println("=> centroid OF CLUSTERS IS LOADED");
        printPointData(centroid);
        System.out.println("---------------------------------------------------------------");
        
        //---------------------- 2. labelisasi (jarak centroid ke ground)
        HashMap<String,Integer> labeling = new HashMap<String,Integer>();  //hasmap untuk simpan label baik -> cluster sekian, buruk -> cluster sekian
        float distGroundToCent[] = new float[k]; //variabel untuk menyimpan jarak centroid kluster x dengan ground (sesuai case, ground=0,0)
        Point ground;                           //preparing for ground point
        ground = getGround();
        //mengisi value distance ground to each centroid
        for(int i=0; i<k ; i++){
            distGroundToCent[i] = getDistPoint2Point(centroid.get(i),ground);
            System.out.println("distance kluster to ground: "+i+" : "+ distGroundToCent[i]);
        }
        labeling = labelize(distGroundToCent);          //add hashmap label "GOOD" -> which cluster
        System.out.println("-------------------- 2. LABELING is done");
        System.out.println(Arrays.asList(labeling));    //checking label
        System.out.println("---------------------------------------------------------------");
        
        //----------------------- 3. hitung jarak rata2 member kluster 1 dengan centroid 1,dst
        float[]  meanJarakMember = getMeanJarakMember(trainingPoints, centroid);
        System.out.println("-------------------- 3. MEAN JARAK CENTROID to its MEMBER are calculated");
        printArrayFloat(meanJarakMember);
        System.out.println(" ");
        System.out.println("---------------------------------------------------------------");
        
        //----------------------- 4. define jari2. r = alpha* jarak rata2 (dari centroid tetanggaan yang tidak terlingkup adalah outlier)
        System.out.print("\n4. (Alpha > 1) Tentukan Alpha : ");
        float jari2[] = new float[k];
        float alpha = scanner.nextFloat();
        for(int i=0 ; i<k ; i++){
            jari2[i] = alpha * meanJarakMember[i];
        }
        System.out.println("-------------------- 4. THRESHOLD(JARI2) are calculated");
        printArrayFloat(jari2);
        System.out.println(" ");
        System.out.println("---------------------------------------------------------------");
        
        //----------------------- 5. output=>outlier ditemukan (ArrayList<Point> outliers)
        discoverOutlier(trainingPoints,jari2, centroid);
        System.out.print("\n-------------------- 5. Hasil Outlier Yang Ditemukan: \n");
        printPointData(trainingPoints);
        System.out.println("---------------------------------------------------------------");
        
        //6. sort A B C (A=member kluster terdekat, B=Outlier, C=Outlier) -> tentukan mana outlier yang lebih dekat dari A
        //6. sort A B C (A=member kluster terjauh, B=Outlier, C=Outlier) -> tentukan mana outlier yang lebih jauh dari A

    }
    public static ArrayList<Point> setData(String filePath) {
        ArrayList<String[]> resultList = new ArrayList<>(); // List untuk menampung sementara isi file
        ArrayList<Point> dataPoints = new ArrayList<>();
        try {
            File file = new File(filePath); // Membuat object file dari file yang diinginkan
            BufferedReader br = new BufferedReader(new FileReader(file)); // Membuat object untuk membaca file
            String str = "";
            /*
             * Looping untuk membaca keseluruhan isi file
             */
            while (str != null) {
                str = br.readLine(); // Mengambil setiap baris file
                if (str != null) // Jika baris ada
                {    resultList.add(str.split(",")); // Memisahkan setiap kata menurut tab ("   ")
                    
                }
            }
        } catch (FileNotFoundException e) { // Jika file tidak ditemukan
            System.out.println("File tidak ditemukan");
        } catch (IOException e) { // Jika terjadi kesalahan saat membaca file
            System.out.println("Kesalahan saat membaca file");
        } finally {
            if (resultList.size() > 0) { // Jika file tidak kosong
                
//                for(int k=0 ; k<resultList.size(); k++){
//                    index[k] = resultList.get(k);
//                }
                for (String[] strings : resultList) {
                    float index[] = new float[resultList.get(0).length];
                    Point point = new Point();
                    float label=0;
                    for(int k=0 ; k<strings.length; k++){
//                        if(k==(strings.length-1)){
//                            label = Float.parseFloat(strings[k]);
//                            System.out.println("label : "+label);
//                        }else{
//                            System.out.println("str :"+strings[k]);
                            index[k] = Float.parseFloat(strings[k]);
//                            System.out.println(+index[k]);
//                        }
                    }
                    point = new Point(index);
                    dataPoints.add(new Point(index));
                }
            }
        }
        return dataPoints; // Mengembalikan data
    }

    private static Point getCentroid(int i, ArrayList<Point> mDataSet) {
        Point mCentro = new Point();
        int nDataCentro = 0;
        int nIdx = mDataSet.get(0).getmIndex().length;
        float[] idx = new float[nIdx];
        idx = initFloatNol1D(idx);
        
        for(Point data: mDataSet){
            if(data.getmLabel()==(i+1)){
                for(int j=0; j<nIdx ; j++){
                    idx[j] += data.getmIndex()[j];  //sum all of feature data that have same label
                }
                nDataCentro++;                      //sum of data
            }
        }
        for(int j=0; j<nIdx ; j++){
            idx[j] = idx[j]/nDataCentro;            
        }
        mCentro.setmIndex(idx);
        return mCentro;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public static int[] initNol1D(int[] array) {
        int result[] = new int[array.length];
        for(int j=0; j<array.length;j++) //initial
            result[j] = 0;
        
        return result;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public static float[] initFloatNol1D(float[] array) {
        float result[] = new float[array.length];
        for(int j=0; j<array.length;j++) //initial
            result[j] = 0;
        
        return result;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static float getDistPoint2Point(Point mPoint1, Point mPoint2) {
        float result;
        int nIdx = mPoint1.getmIndex().length;
        float temp = 0;
        float temSum =0;
        float[] index1 = mPoint1.getmIndex();
        float[] index2 = mPoint2.getmIndex();
        for(int i=0 ; i<nIdx ; i++){
            temp = index1[i] - index2[i];
            temSum += Math.pow(temp, 2);
        }
        temp = (float)Math.sqrt(temSum);
        result = temp;
        return result;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static void printPointData(ArrayList<Point> Points) {
        int counter=0;
        for (Point point : Points) {        //print datatraining
            float[] tempIndex = new float[point.getmIndex().length];
            tempIndex = point.getmIndex();
            if(point.getMemberStatus()!=null){
//                System.out.println(tempIndex[0]);
             System.out.println(+counter+"---> "+  Arrays.toString(tempIndex) + " label: "+ point.getmLabel() + " status : " + point.getMemberStatus());
               
            }else
                System.out.println(+counter+"---> "+  Arrays.toString(tempIndex) + " label: "+ point.getmLabel());
            counter++;
        }
        System.out.println(" ");
    }

    private static void initLabelCentroid(ArrayList<Point> Points) {
        for(int i=0 ; i < Points.size() ; i++){
            Points.get(i).setmLabel(i);
        }
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static void makeCentArrayDistances() {
        cArrayDistances = new double[centroid.size()][centroid.size()];
        Point dist = new Point();
        System.out.println("=> (Matriks) Array of Distance: ");
        for(int i=0; i<centroid.size() ; i++){
            float[] iIndex = centroid.get(i).getmIndex();
//            for(int j=0; j<(i+1) ; j++){
            for(int j=0; j<(centroid.size()) ; j++){
                float[] tempIndex = new float[nIdx];
                float[] jIndex = centroid.get(j).getmIndex();
                double tempcArrayDistance =0;
                for(int k=0; k<nIdx ;k++){
                    //System.out.println(iIndex[k]+" - "+ jIndex[k]);
                    tempIndex[k] = iIndex[k]-jIndex[k];
                    //System.out.print(" "+tempIndex[k]);
                    tempcArrayDistance += Math.pow(tempIndex[k],2);
                }
                //System.out.println(" ");
                dist.setmIndex(tempIndex);
                //System.out.println(tempcArrayDistance);
                if(i==j)
                    cArrayDistances[i][j] = 999;
                else
                    cArrayDistances[i][j] = Math.sqrt(tempcArrayDistance);
                System.out.print(formatter.format(cArrayDistances[i][j]) + " ");
            }
            System.out.println(" ");
        }
        System.out.println("4. Matriks Jarak dari "+centroid.size()+" centroid LOADED");
    }

    private static double getMinValueOf2DDouble(double[][] Array) {
        Stream<double[]> temp = Stream.of(Array);
        DoubleStream Stream = temp.flatMapToDouble(x -> Arrays.stream(x)); // Cant print Stream<int[]> directly, convert / flat it to IntStream 
        double mMinDistance = Stream.min().getAsDouble();
        return mMinDistance;
    }
    
    private static int[] getIndexOf2D(double[][] array2D, double value) {
        int[] var = new int[2];
        for(int i=0; i<array2D.length ; i++){
            //System.out.println(array2D.length);
//            for(int j=0; j < (i+1) ; j++){
            for(int j=0; j < (array2D[0].length) ; j++){
                if(array2D[i][j] == value){
                    var[0]=i;
                    var[1]=j;
                    break;
                }
            }
        }
        return var;
    }
    
    private static ArrayList<Point> doMergeCluster(ArrayList<Point> mCentroid, int[] varMerge) {
        IntStream stream = Stream.of(varMerge).flatMapToInt(x -> Arrays.stream(x));
            int min = stream.min().getAsInt();                                  //getting min value of clusterthat will be merged
        IntStream stream2 = Stream.of(varMerge).flatMapToInt(x -> Arrays.stream(x));
            int max = stream2.max().getAsInt();                                 //getting min value of clusterthat will be 

            float[] tempIndex = new float[nIdx];
            Point newMergedPoint = new Point(); //preparing new point(new centroid) of merged cluster
            tempIndex = getCentroidMerged(varMerge);
            
            newMergedPoint = new Point(tempIndex);
        //merge max indexed value into min indexed value
            //System.out.println("BEFORE");
            //System.out.println("=> min index: "+ min +" "+ Arrays.toString(mCentroid.get(min).getmIndex()));
            //System.out.println("=> max index: "+ max +" "+ Arrays.toString(mCentroid.get(max).getmIndex()));
            mCentroid.set(min, newMergedPoint);
            mCentroid.remove(max);
            //System.out.println("BEFORE");
            //System.out.println("=> min index: "+ min +" "+ Arrays.toString(mCentroid.get(min).getmIndex()));
            //System.out.println("=> max index: "+ max +" "+ Arrays.toString(mCentroid.get(max).getmIndex()));
            return mCentroid;
    }
    
    private static ArrayList<Point> rearrangeLabelCentroid(ArrayList<Point> points, int[] var) {
        int max = Arrays.stream(var).max().getAsInt();
        int min = Arrays.stream(var).min().getAsInt();  
        //System.out.println("BEFORE");
        //System.out.println("=>label min: "+ points.get(min).getmLabel());
        //System.out.println("=>label max: "+ points.get(max).getmLabel());
        for(int i=0 ; i < points.size() ; i++){
            if(points.get(i).getmLabel() == max){ //label max dimerge menjadi label min
                points.get(i).setmLabel(min);
            }else if(points.get(i).getmLabel() > max && (min != max )){ //label di atas max, dikurang 1, mengikuti jumlah centroid now
                float tempLabel = points.get(i).getmLabel();
                points.get(i).setmLabel(tempLabel-1);
            }
        }
        //System.out.println("AFTER");
        //System.out.println("=>label min: "+ points.get(min).getmLabel());
        //System.out.println("=>label max: "+ points.get(max).getmLabel());
         return points;        
    }

    private static void doHirarCluster() {
        //---------------4. hitung jarak, buat matriksnya [matriks jarak]
            makeCentArrayDistances(); 

            //---------------5. cari angka minimum dari matriks jarak
            double minDistance = getMinValueOf2DDouble(cArrayDistances);
            System.out.println("5. Tentukan Jarak minimum: " +minDistance);

            //---------------6. cari index dari angka minimum matriks jarak
            int[] indexOfMinDistance = getIndexOf2D(cArrayDistances, minDistance); //3. mencari index (2 kluster terdekat) dari nilai minimum jarak centroid
            System.out.println("=> index Merge (Cluster that will be merged) : " + indexOfMinDistance[0] +"  --and-- "+indexOfMinDistance[1]);

            //---------------7. Gabungkan 2 Kluster yang terdekat
            centroid = doMergeCluster(centroid, indexOfMinDistance);
            //---------------8. Update data set dengan label kluster yang baru
            trainingPoints = rearrangeLabelCentroid(trainingPoints, indexOfMinDistance);
            //---------------9. Cetak Data dengan LABEL updated
            System.out.println("=> Data Training IRIS after arrange :");
            printPointData(trainingPoints);
            //printPointDataLabelOnly(trainingPoints); //HASIL PELABELAN ONLY
            
            System.out.println("\n==============="+centroid.size()+"===============\n\n");
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private static float[] getMeanJarakMember(ArrayList<Point> mTrainingPoints, ArrayList<Point> mCentroid) {
        int k = mCentroid.size();
        float distance[] = new float[mTrainingPoints.size()];
        float result[] = new float[k];  //saving mean jarak members of x cluster
        float counter[] = new float[k]; //saving n Member kluster x
        result = initFloatNol1D(result);
        counter = initFloatNol1D(counter);
        for(Point poin:mTrainingPoints){
            for(int i=0 ; i<k ; i++){
                float label = poin.getmLabel();
                if(label == i){
                    counter[i]+=1;
                    poin.setDistToCentroid(getDistPoint2Point(poin, centroid.get(i)));
                    System.out.println("distance to cent kluster "+i+" : "+poin.getDistToCentroid());
                    result[i] += getDistPoint2Point(poin, centroid.get(i));
                    }
            }
        }
        
        for(int j=0 ; j<k ; j++){
            result[j]=result[j]/counter[j];
//            System.out.println(result[j] +" "+ counter[j]);
        }
        return result;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static void printArrayFloat(float[] mArr) {
        System.out.print("[{");
        for(int i=0; i<mArr.length; i++){
            System.out.print(mArr[i]);
            if(i!=mArr.length-1)
                System.out.print(",");
        }
        System.out.print("}]");
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

//    mengembalikan hashmap pasangan label dan kluster yang berkaitan dengan label tsb
    private static HashMap<String, Integer> labelize(float[] mDistGroundToCent) {
        int mKluster = mDistGroundToCent.length;
        HashMap<String,Integer> mLabeling = new HashMap<String,Integer>();
        if(mKluster==2){
            if(mDistGroundToCent[0]<mDistGroundToCent[1]){
                mLabeling.put("JELEK",0);
                mLabeling.put("BAIK",1);
                
            }else{
                mLabeling.put("BAIK",0);
                mLabeling.put("JELEK",1);
            }       
        }
        return mLabeling;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static void equipCheckClustering() {
        System.out.println("=> trainingPoints Labeled LOADED");
        printPointData(trainingPoints);
        System.out.println("------------------------ jumlah trainingPoints: " + trainingPoints.size());
        System.out.println("=> centroid LOADED");
        printPointData(centroid);
        System.out.println("------------------------ jumlah centroid: " + centroid.size());
        System.out.println("sizedata :" +sizeData);
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static Point getGround() {
        Point mGround;
        float nol[] = new float[nIdx];
        nol = initFloatNol1D(nol);
        mGround = new Point(nol);
        return mGround;
    }

    private static void discoverOutlier(ArrayList<Point> mTrainingPoints, float[] jari2, ArrayList<Point> mCentroid) {
        for(Point pt:mTrainingPoints){
            int kluster = (int)pt.getmLabel();
            float jarakMember = getDistPoint2Point(pt,mCentroid.get(kluster));
            if(jarakMember>jari2[kluster]){
                pt.setMemberStatus("OUTLIER");
            }else
                pt.setMemberStatus("NORMAL");
        }
    }

    private static float[] getCentroidMerged(int[] varMerge) {
        float[] result = new float[trainingPoints.get(0).getmIndex().length];
        float[] tempSum = new float[trainingPoints.get(0).getmIndex().length];
        int counter=0;
            for(Point pt: trainingPoints){
                float[] tempIndex = pt.getmIndex();
                if(pt.getmLabel()==varMerge[0]||pt.getmLabel()==varMerge[1]){
                    counter++;
                    for(int i=0 ; i< tempIndex.length ; i++){
                        tempSum[i] += tempIndex[i];
                        result[i] = tempSum[i]/counter;
                    } 
                }
            }
        return result;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
