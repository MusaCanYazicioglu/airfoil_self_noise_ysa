package airfoil_self_noise_ysa;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

public class airfoil_self_noise_ysa{
    
    static double[] errorlar;

    public static void main(String[] args) throws FileNotFoundException {
        // TODO code application logic here
        Scanner in = new Scanner(System.in);

        int araKatmanNoron;
        int secenek = 0;
        double momentum;
        double lr;
        double hata;
        int epoch;
        YSA ysa = null;
        List<Integer> araKatmanNoronlar = new ArrayList<>();

        do {
            System.out.println("1.Egitim");
            System.out.println("2.Test");
            System.out.println("3.Tekli Test");
            System.out.println("4.Cikis");
            System.out.println("Secenek:");
            secenek = in.nextInt();
            switch (secenek) {
                case 1:
                    System.out.println("Arakatman sayisi");
                    araKatmanNoron = in.nextInt();
                    for (int i = 0; i < araKatmanNoron; i++) {
                        System.out.println(i + 1 + ". katman noron sayisi :");
                        araKatmanNoronlar.add(in.nextInt());
                    }
                    System.out.println("Momemntum");
                    momentum = in.nextDouble();
                    System.out.println("Ogrenme katsayisi");
                    lr = in.nextDouble();
                    System.out.println("Min Hata");
                    hata = in.nextDouble();
                    System.out.println("Epoch");
                    epoch = in.nextInt();
                    ysa = new YSA(araKatmanNoronlar, momentum, lr, hata, epoch);
                    ysa.Egit();
                    errorlar = YSA.error;
                    System.out.println("Egitim Tamamlandi");
                    
                    break;
                case 2:
                    if (ysa == null) {
                        System.out.println("Egitim yapilmadi");
                    } else {
                        System.out.println("Egitimdeki hata degeri: " + ysa.getHata());
                        System.out.println("Test hata degeri: " + ysa.Test());
                    }
                    break;
                case 3:
                    ysa= new YSA();
                    double[] input = new double[5];
                    System.out.print("Frekans(200-20000):");
                    input[0] = in.nextDouble();
                    System.out.print("Saldırı açısı(0-22.2):");
                    input[1] = in.nextDouble();
                    System.out.print("Kord Uzunluğu(0.0254-0.3048):");
                    input[2] = in.nextDouble();
                    System.out.print("Serbest akış hızı(31.7-71.3):");
                    input[3] = in.nextDouble();
                    System.out.print("Ölçeklendirilmiş ses basıncı seviyesi(0.000400682-0.0584113):");
                    input[4] = in.nextDouble();

                    System.out.println("Sonuc :" + ysa.tekTest(input));
                    ysa=null;
                    break;
            }
        } while (secenek != 4);
    }
}
