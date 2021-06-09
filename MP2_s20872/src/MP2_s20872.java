import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.nio.file.FileAlreadyExistsException;
import java.util.*;

public class MP2_s20872 {

        public static String group0 = "";
        public static String group1 = "";
        public static double[] initialWeightVector = new double[0];
        public static double threshold = randomDouble(-5,5);

    public static void main(String[] args) throws FileNotFoundException {



        Scanner inputScanner = new Scanner(System.in);

        System.out.println("Wprowadź a: ");
        double a = inputScanner.nextDouble();
        inputScanner.nextLine();

        System.out.println("Podaj plik treningowy: ");
        String trainSet = inputScanner.nextLine();

        System.out.println("Podaj plik testowy: ");
        String testSet = inputScanner.nextLine();


            File trainFile = new File(trainSet);
            File testFile = new File(testSet);


        //Wczytywanie pierwszej linii pliku treningowego w celu utworzenia wektora początkowego wag

        Scanner trainScanner = new Scanner(trainFile);
        String firstLine = trainScanner.nextLine();
        String[] firstLineTokens = firstLine.split(";");
        initialWeightVector = new double[firstLineTokens.length-1];

        for(int i = 0; i < initialWeightVector.length; i++)
        {
            initialWeightVector[i] = randomDouble(-5,5);
        }

        trainScanner.reset();

        List<String[]> trainList = new ArrayList<>();

        //Wczytywanie elementów pliku treningowego do listy i wymieszanie danych

        while(trainScanner.hasNextLine())
        {
            String line = trainScanner.nextLine();
            String[] tokens = line.split(";");
            trainList.add(tokens);
            if(group0.equals("")) group0 = tokens[tokens.length-1];
            else if (group1.equals("") && !tokens[tokens.length-1].equals(group0)) group1 = tokens[tokens.length-1];
        }

        Collections.shuffle(trainList);

        // Nauczanie

        for(String[] trainElement : trainList)
        {


            double net = countNet(trainElement,initialWeightVector);
            String output = checkOutputGroup(net,threshold);
            String correctOutput = trainElement[trainElement.length-1];
            int expectedY = getY(correctOutput);
            int y = getY(output);

            //Wykonanie reguły delta
            if(!output.equals(correctOutput))
            {

                initialWeightVector = updateWeightVector(trainElement, initialWeightVector, a, expectedY, y);
                threshold = updateThreshold(threshold, expectedY, y, a);
            }
        }

        // Sprawdzanie
        Scanner testScanner = new Scanner(testFile);

        int allClassifications = 0;
        int correctClassifications = 0;
        int correctGroup0Classifications = 0;
        int correctGroup1Classifications = 0;
        int allGroup0Classifications = 0;
        int allGroup1Classifications = 0;

        while (testScanner.hasNextLine())
        {
            String[] testElement = testScanner.nextLine().split(";");

            double net = countNet(testElement,initialWeightVector);
            String group = checkOutputGroup(net,threshold);

            String correctGroup = testElement[testElement.length-1];
            System.out.println("Poprawna: "+correctGroup+" Wyliczona: "+group);

            allClassifications++;
            if(correctGroup.equals(group))
            {
                correctClassifications++;

                if(group.equals(group0)) correctGroup0Classifications++;
                else correctGroup1Classifications++;
            }

            if(correctGroup.equals(group0)) allGroup0Classifications++;
            else allGroup1Classifications++;
        }
        System.out.println("Poprawność wszystkich klasyfikacji: "+ BigDecimal.valueOf(correctClassifications).divide(BigDecimal.valueOf(allClassifications),2,BigDecimal.ROUND_HALF_UP));
        System.out.println("Poprawność klasyfikacji dla "+group0+": "+BigDecimal.valueOf(correctGroup0Classifications ).divide(BigDecimal.valueOf(allGroup0Classifications),2,BigDecimal.ROUND_HALF_UP));
        System.out.println("Poprawność klasyfikacji dla "+group1+": "+BigDecimal.valueOf(correctGroup1Classifications ).divide(BigDecimal.valueOf(allGroup1Classifications),2,BigDecimal.ROUND_HALF_UP));

        //Wprowadzanie przez użytkownika
        showMenu();

    }

    public static double countNet(String[] testedVector, double[] initialWeightVector)
    {
        double net = 0;
        for(int i = 0; i < testedVector.length-1;i++)
        {
            net += Double.parseDouble(testedVector[i]) * initialWeightVector[i];
        }
        return net;
    }

    public static String checkOutputGroup(double y, double threshold)
    {
        if(y >= threshold) return group1;
        else return group0;

    }

    public static int getY(String output)
    {
        if (output.equals(group0)) return 0;
        else return 1;
    }

    public static double[] updateWeightVector(String[] trainVector, double[] weightVector, double a, int d,int y)
    {
        double[] newWeightVector = Arrays.copyOf(weightVector,weightVector.length);
        double[] tmpVector = new double[weightVector.length];
        for(int i = 0; i < weightVector.length; i++)
        {
            tmpVector[i] = (d-y) * a * Double.parseDouble(trainVector[i]);
        }

        for( int i = 0; i < newWeightVector.length; i++ )
        {
            newWeightVector[i] = weightVector[i]+tmpVector[i];
        }

        return newWeightVector;
    }

    public static double updateThreshold(double threshold,int d,int y, double a)
    {
        return threshold + (d-y) * a * (-1);
    }

    public static double randomDouble(double min, double max)
    {
        double tmp = (Math.random()*(max-min+1))+min;
        //(Math.random()*(5+5+1))-5
        return  changeDecimalPlaces(2,tmp);
    }

    public static double changeDecimalPlaces(int decimalPlaces,double x)
    {
        return (int)(x * Math.pow(10,decimalPlaces)) / Math.pow(10,decimalPlaces);
    }

    public static void showMenu()
    {
        boolean end = false;
        while(!end)
        {
            System.out.println("1. Wprowadź wektor");
            System.out.println("2. Zakończ");

            Scanner menuScanner = new Scanner(System.in);
            System.out.print("Podaj liczbe: ");
            int choice = menuScanner.nextInt();
            if(choice == 1)
            {
                loadVector();
            }
            else if(choice == 2) end = true;
            else showMenu();
        }
    }

    public static void loadVector()
    {
        Scanner sc = new Scanner(System.in);
        System.out.println("Wprowadź wektor: ");
        String[] tokens = sc.nextLine().split(";");
        if(tokens.length != initialWeightVector.length)
        {
            System.err.println("Niewłaściwy format danych");
            showMenu();
        }
        else
        {
            try
            {
                double net = countNet(tokens,initialWeightVector);
                String group = checkOutputGroup(net,threshold);
                System.out.println("Sklasyfikowano dla: "+group);
            }
            catch (NumberFormatException e)
            {
                System.err.println("Nieprawidłowy format danych");
                showMenu();
            }
        }
        showMenu();
    }

}
