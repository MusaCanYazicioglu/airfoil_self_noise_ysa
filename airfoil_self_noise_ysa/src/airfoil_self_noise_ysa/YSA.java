package airfoil_self_noise_ysa;

import java.io.File;
import java.io.FileNotFoundException;
import static java.lang.System.in;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.neuroph.core.Layer;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.core.events.LearningEvent;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.MomentumBackpropagation;
import org.neuroph.util.NeuronProperties;
import org.neuroph.util.TransferFunctionType;

public class YSA {

    private static final File egitimDosya = new File(YSA.class.getResource("dataset.txt").getPath());
    private int inputCount = 5;
    private int outputCount = 1;
    List<Integer> araKatmanlar;
    private double[] maksimumlar;
    private double[] minimumlar;
    private double maksimumOutput;
    private double minimumOutput;
    private DataSet egitimVeriSeti;
    private int araKatmanNoronSayisi;
    private MomentumBackpropagation bp;
    private DataSet[] dataset;
    public static double[] error;

    public YSA(List<Integer> araKatmanNoron, double momentum, double lr, double hata, int epoch) throws FileNotFoundException {
        maksimumlar = new double[inputCount];
        minimumlar = new double[inputCount];
        for (int i = 0; i < inputCount; i++) {
            maksimumlar[i] = Double.MIN_VALUE;
            minimumlar[i] = Double.MAX_VALUE;
        }
        egitimVeriSetiMaksMin();
        egitimVeriSeti = EgitimVeriSeti();
        error = new double[epoch];
        bp = new MomentumBackpropagation();
        bp.setMomentum(momentum);
        bp.setLearningRate(lr);
        bp.setMaxError(hata); // hata degeri veya epoch degeri hangisi ilk biterse
        bp.setMaxIterations(epoch);
        araKatmanlar = araKatmanNoron;
        bp.addListener((LearningEvent event) -> {
            MomentumBackpropagation mbp = (MomentumBackpropagation) event.getSource();
            if (event.getEventType() != LearningEvent.Type.LEARNING_STOPPED) {
                error[mbp.getCurrentIteration() - 1] = mbp.getTotalNetworkError();
            }
        });
    }
    
    public YSA() throws FileNotFoundException
    {
        maksimumlar = new double[inputCount];
        minimumlar = new double[inputCount];
        for (int i = 0; i < inputCount; i++) {
            maksimumlar[i] = Double.MIN_VALUE;
            minimumlar[i] = Double.MAX_VALUE;
        }
        egitimVeriSetiMaksMin();
        egitimVeriSeti = EgitimVeriSeti();
    }

    public void Egit() {

        araKatmanlar.add(0, inputCount);
        araKatmanlar.add(outputCount);
        MultiLayerPerceptron nn = new MultiLayerPerceptron(araKatmanlar, TransferFunctionType.SIGMOID);
        nn.setLearningRule(bp);
        egitimVeriSeti.shuffle();
        int veriSetiCount = egitimVeriSeti.size();
        this.dataset = egitimVeriSeti.createTrainingAndTestSubsets(0.7, 0.3);
        nn.learn(dataset[0]);
        nn.save("model.nnet");
    }

    private double MSE(double[] beklenen, double[] cikti) {
        double satirToplamHata = 0;
        for (int i = 0; i < outputCount; i++) {
            satirToplamHata += Math.pow(beklenen[i] - cikti[i], 2);
        }
        return satirToplamHata / outputCount;
    }

    public String Sonuc(double[] outputs) {
        return DeMinMax(maksimumOutput, minimumOutput, outputs[0]) + "";
    }

    public String tekTest(double[] inputs) {
        for (int i = 0; i < inputCount; i++) {
            inputs[i] = MinMax(maksimumlar[i], minimumlar[i], inputs[i]);
        }
        NeuralNetwork nn = NeuralNetwork.createFromFile("model.nnet");
        nn.setInput(inputs);
        nn.calculate();
        return Sonuc(nn.getOutput());
    }

    public double getHata() {
        return bp.getTotalNetworkError();
    }

    public DataSet getEgitimVeriSeti() {
        return egitimVeriSeti;
    }

    public DataSet getTestVeriSeti() {
        return dataset[1];
    }

    public double Test() {
        NeuralNetwork nn = NeuralNetwork.createFromFile("model.nnet");
        double toplamHata = 0;
        for (DataSetRow row : getTestVeriSeti()) {
            nn.setInput(row.getInput());
            nn.calculate();
            toplamHata += MSE(new double[]{DeMinMax(maksimumOutput, minimumOutput, row.getDesiredOutput()[0])}, new double[]{DeMinMax(maksimumOutput, minimumOutput, nn.getOutput()[0])});
        }
        return toplamHata / getTestVeriSeti().size();
    }

    private void egitimVeriSetiMaksMin() throws FileNotFoundException {
        Scanner oku = new Scanner(egitimDosya);
        while (oku.hasNext()) {
            for (int i = 0; i < inputCount; i++) {
                double d = oku.nextDouble();
                if (d > maksimumlar[i]) {
                    maksimumlar[i] = d;
                }
                if (d < minimumlar[i]) {
                    minimumlar[i] = d;
                }
            }
            double d = oku.nextDouble();
            if (d > maksimumOutput) {
                maksimumOutput = d;
            }
            if (d < minimumOutput) {
                minimumOutput = d;
            }
        }
    }

    private double MinMax(double max, double min, double x) {
        return (x - min) / (max - min);
    }

    private double DeMinMax(double max, double min, double x) {
        return x * (max - min) + min;
    }

    private DataSet EgitimVeriSeti() throws FileNotFoundException {
        Scanner oku = new Scanner(egitimDosya);
        DataSet egitim = new DataSet(inputCount, outputCount);

        while (oku.hasNextDouble()) {
            double input[] = new double[inputCount];
            for (int i = 0; i < inputCount; i++) {
                double d = oku.nextDouble();
                input[i] = MinMax(maksimumlar[i], minimumlar[i], d);
            }
            egitim.add(new DataSetRow(input, new double[]{MinMax(maksimumOutput, minimumOutput, oku.nextDouble())}));
        }
        return egitim;
    }
}