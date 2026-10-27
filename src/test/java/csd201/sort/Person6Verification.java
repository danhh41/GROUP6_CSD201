package csd201.sort;
import java.util.*;
public class Person6Verification {
  public static void main(String[] args) {
    Random r = new Random(20261027L);
    String[] names = {"pdq", "noA", "noB", "noA_noB"};
    int cases = 0;
    for (String name : names) {
      boolean a = !name.equals("noA") && !name.equals("noA_noB");
      boolean b = !name.equals("noB") && !name.equals("noA_noB");
      for (int n = 0; n <= 200; n++) {
        for (int t = 0; t < 1000; t++) {
          int[] arr = new int[n];
          for (int j=0;j<n;j++) arr[j]=r.nextInt(t%5==0 ? 2 : 10000);
          int[] expected = arr.clone(); Arrays.sort(expected);
          new PdqSort(false,a,b).run(arr,0,n);
          if (!Arrays.equals(arr,expected)) throw new AssertionError(name+" n="+n+" t="+t);
          cases++;
        }
      }
      for (int n : new int[] {100000}) {
        for (int mode=0;mode<3;mode++) {
          int[] arr=new int[n];
          for(int j=0;j<n;j++) arr[j]= mode==0?7:mode==1?j:n-j;
          int[] expected=arr.clone();Arrays.sort(expected);
          new PdqSort(false,a,b).run(arr,0,n);
          if(!Arrays.equals(arr,expected)) throw new AssertionError(name+" large="+mode);
          cases++;
        }
      }
      System.out.println(name+" PASS");
    }
    System.out.println("TOTAL PASS="+cases);
  }
}
