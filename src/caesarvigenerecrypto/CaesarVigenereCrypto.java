package caesarvigenerecrypto;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.Normalizer;
import java.util.Scanner;

/**
 *
 * @author Gabriel Breton - 43397
 */
public class CaesarVigenereCrypto {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        int algoChoice;
        String encryptedTxt = "";
        CrypTools cryptools = new CrypTools();

        System.out.println("CAESAR & VIGENRE CIPHER TOOL");
        System.out.println("============================");
        
//----- Open the file and get the text in a String.
        System.out.println("\n...");        
        String decryptedTxt = getTxtFileAsString();;
        System.out.println("FILE SUCCESSFULLY OPEN !");
        System.out.println("...");
        
//----- Normalize and print the text.
        decryptedTxt = normalizeText(decryptedTxt);
        System.out.println("\nText successfully normalized: ");
        System.out.println("-----------------------------");
        System.out.println(decryptedTxt);        
        
//----- Switch between the two cipher algorithms.
        algoChoice = getAlgoChoice();
        switch (algoChoice) {
            
//--------- CAESAR
            case 1:
                int offset;
              
//------------- Get the offset and encrypt.
                System.out.println("\nEnter the offset length (between 1 and 25):");
                System.out.println("-------------------------------------------");
                offset = validateIntInput(25, 1);
                encryptedTxt = cryptools.caesarEncrypt(decryptedTxt, offset);

//------------- Print encrypted text.
                System.out.println("\nEncrypted text:");
                System.out.println("---------------");
                System.out.println(encryptedTxt);

//------------- Decrypt Caesar without the offset and print the text.
                System.out.println("\nDecrypted text:");
                System.out.println("---------------");
                System.out.println(cryptools.caesarDecrypt(encryptedTxt));
                
//------------- Brute force the cipher.
                System.out.println("\nBrute forcing the cipher:");
                System.out.println("-------------------------");
                for (int i = 1; i < 26; i++) {
                    System.out.println("# With offset == " + i);
                    System.out.println(cryptools.caesarDecWithOffset(encryptedTxt, i));
                }
                break;
                
//--------- VIGENERE    
            case 2:
                String key;
                Scanner keybd = new Scanner(System.in);

//------------- Ask for the key and encrypt.
                System.out.println("\nEnter the key (non accentued letters):");
                System.out.println("--------------------------------------");
                key = keybd.nextLine();
                encryptedTxt = cryptools.vigenereEncrypt(decryptedTxt, key);
                
//------------- Print encrypted text.
                System.out.println("\nEncrypted text:");
                System.out.println("---------------");
                System.out.println(encryptedTxt);                
                
//------------- Decrypt Vigenere.               
                System.out.println("\n...");
                System.out.println("STARTING TEXT DECRYPTION");
                System.out.println("...");
                cryptools.vigenereDecrypt(encryptedTxt);
                break;
        }
    }
    
    /**
     * Open a text file and return it as a String.
     * 
     * @return the text in a String.
     */
    private static String getTxtFileAsString() {
        String everything = "";
        String filePath = new File("").getAbsolutePath();
        
        try {
            BufferedReader buffRead = new BufferedReader(new FileReader(
                 filePath + "/src/caesarvigenerecrypto/logiciel_libre_fsf.txt"));
            
            StringBuilder stringBuild = new StringBuilder();
            String line = buffRead.readLine();
            
            while (line != null) {
                stringBuild.append(line);
                stringBuild.append(System.lineSeparator());
                line = buffRead.readLine();
            }
            everything = stringBuild.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return everything;
    }

    /**
     * Normalize a text. Transform accentued letters, remove spaces, numbers, 
     * punctuation, etc.
     * 
     * @param text the text to normalize.
     * @return the "cleaned" text.
     */
    private static String normalizeText(String text) {
        text = justAlphaChars(text);

        text = Normalizer.normalize(text, Normalizer.Form.NFD);
        text = text.replaceAll("[^\\p{ASCII}]", "");
        text = text.toLowerCase();

        return text;
    }

    /**
     * Remove all non alphabetic characters in a String.
     * 
     * @param text the text where remove.
     * @return the "cleaned" text.
     */
    private static String justAlphaChars(String text) {
        StringBuilder builder = new StringBuilder();

        for (char ch : text.toCharArray()) {
            if (Character.isAlphabetic(ch)) {
                builder.append(ch);
            }
        }
        text = builder.toString();

        return text;
    }

    /**
     * Gets the encryption algorithm choice.
     * 
     * @return the algorithm choice as an int.
     */
    private static int getAlgoChoice() {
        int algoChoice;

        System.out.println("\nChoose the encryption algorithm: ");
        System.out.println("--------------------------------");
        System.out.println("1) Caesar");
        System.out.println("2) Vigenere");
        System.out.print("Choice: ");
        algoChoice = validateIntInput(2, 1);

        return algoChoice;
    }

    /**
     * Gets and validates an integer input. While the input is not between the 
     * max and min specified bounds, or if the input is not an integer, the user
     * have to re-enter a value.
     * 
     * @param max the maximum integer the user can enter.
     * @param min the minimum integer the user can enter.
     * @return a valid integer input.
     */
    private static int validateIntInput(int max, int min) {
        Scanner keybd = new Scanner(System.in);
        int input = -1;

        while (input > max || input < min) {
            while (!keybd.hasNextInt()) {
                System.out.print("\nThat's not an integer. Retry : ");
                keybd.next();
            }

            input = keybd.nextInt();
            if (input > max || input < min) {
                System.out.println("\nThe number you entered is not between "
                        + min + " and " + max);
            }
        }

        return input;
    }
}
