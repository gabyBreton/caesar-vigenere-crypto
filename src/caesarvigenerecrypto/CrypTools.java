package caesarvigenerecrypto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class provides methods to encrypt/decrypt using Caesar and Vigenere 
 * ciphers.
 * 
 * @author Gabriel Breton - 43397
 */
public class CrypTools {

    private final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

    /**
     * Encrypt with Vigenere cipher.
     *
     * @param text the original text.
     * @param key the key for the encryption.
     * @return the text encrypted.
     */
    String vigenereEncrypt(String text, String key) {
        char[] cryptedText = new char[text.length()];
        int txtCharPos, keyCharPos;
        
        for (int i = 0; i < text.length(); i++) {
            txtCharPos = getTxtCharsAlphabetPos(text.charAt(i));
            keyCharPos = getKeyCharsAlphabetPos(i, key);
            cryptedText[i] = ALPHABET.charAt((txtCharPos + keyCharPos) % 26);
        }

        return new String(cryptedText);
    }

    /**
     * Decrypt Vigenere cipher with the key.
     * 
     * @param text the encrypted text.
     * @param key the key that encrypted the text.
     * @return the decrypted text.
     */
    String vigenereDecryptWithKey(String text, String key) {
        char[] decryptedText = new char[text.length()];
        int txtCharPos, keyCharPos;
        
        for (int i = 0; i < text.length(); i++) {
            txtCharPos = getTxtCharsAlphabetPos(text.charAt(i));
            keyCharPos = getKeyCharsAlphabetPos(i, key);
            decryptedText[i] = ALPHABET.charAt(((26 + txtCharPos) - keyCharPos) % 26);
        }

        return new String(decryptedText);        
    }
    
    /**
     * Decrypt the Vigenere cipher without the key.
     * 
     * @param text the encrypted text.
     * @return the decrypted text.
     */
    void vigenereDecrypt(String text) {
        char [] cryptedText = text.toCharArray();
        
//----- Find trigrams that matches and calculate the space between them.        
        ArrayList<Integer>[] matchesSpacing = trigramsSpaceLengths(cryptedText);
        
//----- Find the greatest common divisor of all the spaces length.        
        ArrayList<Integer> allGCD = computesGCDs(matchesSpacing);

//----- Calculate the frequency of each common divisors.
        Map<Integer, Integer> frequenciesGCD = computeFrequenciesGCD(allGCD);
        printGCDFrequencies(frequenciesGCD);

//----- Find the most frequent GCD.
        int maxGCD = computeMostFrequentGCD(frequenciesGCD);

//----- Assume that the key length is probably the most frequent GCD.      
        
        // Works in most of the case !
        // But ...
        // More big is the key, more trigrams we have to compute to get good
        // GCDs stats and be able to assume one of them is the key length.
        // Also calculating GCD only between the same trigrams is not the most
        // efficient. 
        
        int keyLength = maxGCD;        
        printKeyLength(keyLength, "GCD");

//----- Find the key value.
        String key = "";
        key = findKeyValue(keyLength, text);
        printKeyValue(key, "GCD");
        
        
//----- Compute the IC.
        double[] indexCoincidenceSubTxt = getCoincidenceIndex(text);
        
        System.out.println("\nComputing the index of coincidence:");
        System.out.println("-----------------------------------");        
        printAllIC(indexCoincidenceSubTxt);

//----- Find the nearest IC to 0.074 (french IC) and assume it is the key length        
        
        // Does not works often but sometimes it works better than GCD method.
        
        int keyLengthIC = findNearestIC(indexCoincidenceSubTxt);
        printKeyLength(keyLengthIC, "IC");
        
//----- Find key value        
        String keyIC = "";
        keyIC = findKeyValue(keyLengthIC, text);
        printKeyValue(keyIC, "IC");
        
//----- Decrypt the text using the both keys.  
        System.out.println("\nDecrypted text with key based on GCD:");
        System.out.println("-------------------------------------");
        System.out.println(vigenereDecryptWithKey(text, key));

        System.out.println("\nDecrypted text with key based on IC:");
        System.out.println("------------------------------------");
        System.out.println(vigenereDecryptWithKey(text, keyIC));
    }

    /**
     * Gives the IC that is the nearest from 0.074 (french IC)
     * 
     * @param indexCoincidenceSubTxt the ICs of some subtext (here 30).
     * @return the offset that gives the nearest IC.
     */
    private int findNearestIC(double[] indexCoincidenceSubTxt) {
        double minimalDiff = Math.abs(0.074 -indexCoincidenceSubTxt[1]);
        int keyLengthIC = 1;
        for (int i = 2;i < indexCoincidenceSubTxt.length; i++) {
            if (Math.abs(0.074 -indexCoincidenceSubTxt[i]) < minimalDiff) {
                minimalDiff = Math.abs(0.074 -indexCoincidenceSubTxt[i]);
                keyLengthIC = i;
            }
        }
        return keyLengthIC;
    }

    /**
     * Prints all the ICs stored.
     * 
     * @param indexCoincidenceSubTxt the ICs of some subtext (here 30).
     */
    private void printAllIC(double[] indexCoincidenceSubTxt) {
        int j = 1;
        for (; j < indexCoincidenceSubTxt.length; j++) {
            System.out.println("# IC with a key of length " + j + " --> "
                    + indexCoincidenceSubTxt[j] );
        }
    }

    /**
     * Gives the IC of 30 subtexts (with a step from 1 to 29).
     * 
     * @param text the encrypted text.
     * @return the ICs of 30 subtexts.
     */
    private double[] getCoincidenceIndex(String text) {
        String subTxt;
        double[] indexCoincidenceSubTxt = new double [30];
        
        // Find the ic for the sub text of each possible key length
        for (int i = 1; i < indexCoincidenceSubTxt.length; i++){
            subTxt = "";
            // create a sub text with a specific step.
            for (int j = 0; j < (text.length() / i);) {
                subTxt += text.charAt(j);
                j += i;
            }
            
            int[] subTxtFreq = computeFrequencies(subTxt);
            indexCoincidenceSubTxt[i] = computeIndexCoincidence(subTxtFreq, subTxt.length());
        }
        return indexCoincidenceSubTxt;
    }

    /**
     * Calculates the IC based on the frequencies of a text.
     * 
     * @param txtFreq the frequencies of the text.
     * @param textLength the length of the text.
     * @return the IC of the text.
     */
    private double computeIndexCoincidence(int[] txtFreq, int textLength) {
        double indexCoincidence;
        double freqLettersSum = 0;
        
        for (Integer freq : txtFreq) {
            freqLettersSum += (freq * (freq - 1));
        }
        indexCoincidence = freqLettersSum / (textLength * (textLength - 1));
        
        return indexCoincidence;
    }

    /**
     * Gives all the frequencies for the letters of a text.
     * 
     * @param text the text to analyse.
     * @return the frequencies of the text.
     */
    private int[] computeFrequencies(String text) {
        int[] frequencies = new int [26];
        for (char ch : text.toCharArray()) {
            frequencies[getTxtCharsAlphabetPos(ch)] += 1;
        }
        return frequencies;
    }

    /**
     * Prints the key value.
     * 
     * @param key the key.
     */
    private void printKeyValue(String key, String method) {
        System.out.println("\nKey value, based on the " + method + ", should be:");
        System.out.println("---------------------------------------");
        System.out.println(key);
    }

    /**
     * Find the GCD that is the most frequent.
     * 
     * @param frequenciesGCD the frequencies of the GCDs
     * @return the most frequent GCD.
     */
    private int computeMostFrequentGCD(Map<Integer, Integer> frequenciesGCD) {
        int maxGCD = 0;
        int maxFrequency = 0;
        
        for (Integer key : frequenciesGCD.keySet()) {
            if (frequenciesGCD.get(key) > maxFrequency) {
                maxFrequency = frequenciesGCD.get(key);
                maxGCD = key;
            }
        }
        
        return maxGCD;
    }

    /**
     * Calculates and store the frequency of each GCD.
     * 
     * @param allGCD the list of the all GCDs.
     * @return a map of the GCDs and their frequencies.
     */
    private Map<Integer, Integer> computeFrequenciesGCD(ArrayList<Integer> allGCD) {
        Map<Integer, Integer> frequenciesGCD = new HashMap<>();
        for (Integer i : allGCD) {
            int prev = 0;
            
            // Get previous count.
            if(frequenciesGCD.get(i) != null) {
                prev = frequenciesGCD.get(i);
            }
            
            frequenciesGCD.put(i, prev + 1);
        }
        return frequenciesGCD;
    }

    /**
     * Calculates the GCDs for each trigrams.
     * 
     * @param matchesSpacing the spacings length of each trigrams.
     * @param allGCD 
     */
    private ArrayList<Integer> computesGCDs(ArrayList<Integer>[] matchesSpacing) {
        ArrayList<Integer> allGCD = new ArrayList<>();
        int currentGCD;
        
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < matchesSpacing[i].size() - 1; j++) {
                currentGCD = gcd(matchesSpacing[i].get(j), matchesSpacing[i].get(j+1)); 
                
                // To skip the GCD that are == 1.
                if (currentGCD != 1) {
                    allGCD.add(currentGCD);
                }
            }
        }
        
        return allGCD;
    }

    /**
     * Finds the value of the key.
     * 
     * @param keyLength the length of the key.
     * @param text the crypted text.
     * @return the key.
     */
    private String findKeyValue(int keyLength, String text) {
        String key = "";
        String subText;
        int j = 0;

        for (int i = 0; i < keyLength; i++) {
            subText = "";
            for (; j < text.length();) {
                subText += text.charAt(j);
                j += keyLength;
            }
            j = i + 1;            
            
            key += String.valueOf(ALPHABET.charAt(findCaesarOffset(subText)));
        }
        return key;
    }

    /**
     * Prints the key length.
     * 
     * @param keyLength the length of the key.
     */
    private void printKeyLength(int keyLength, String method) {
        System.out.println("\nKey length, based on the " + method + ", should be:");
        System.out.println("----------------------------------------");
        System.out.println(keyLength);
    }

    /**
     * Prints the frequencies of each GCDs.
     * 
     * @param frequenciesGCD the map that store the GCDs frequencies.
     */
    private void printGCDFrequencies(Map<Integer, Integer> frequenciesGCD) {
        System.out.println("\nGreatest Common Divisors frequencies {divisor=frequency}:");
        System.out.println("---------------------------------------------------------");
        System.out.println(frequenciesGCD);
    }

    /**
     * Find the matching trigrams and the space between each of them.
     * 
     * @param cryptedText the encrypted text.
     * @return an arraylist that contains all the space between trigrams.
     */
    private ArrayList<Integer>[] trigramsSpaceLengths(char[] cryptedText) {
        ArrayList<Integer>[] trigramsSpacing = initTrigramsSpacing();

        // To get the 3 first trigrams.
        System.out.println("\nLooking for the 3 first trigrams:");
        System.out.println("---------------------------------");
        findMatchingTrigrams(cryptedText, trigramsSpacing);

        return trigramsSpacing;
    }

    /**
     * Finds the trigrams that are matching.
     * 
     * @param cryptedText the encrypted text.
     * @param trigramsSpacing the list of the spaces between matching trigrams.
     */
    private void findMatchingTrigrams(char[] cryptedText, ArrayList<Integer>[] trigramsSpacing) {
        String currentTrigram = "";
        String comparedTrigram = "";
        
        for (int i = 0; i < 3; i++) {
            currentTrigram += cryptedText[i];
            currentTrigram += cryptedText[i + 1];
            currentTrigram += cryptedText[i + 2];
            
            // Looking for all the matchings in the whole text.
            System.out.println("\nTrigram " + (i + 1));
            System.out.println("---------");
            for (int j = 1; j < cryptedText.length - 2; j++) {
                comparedTrigram += cryptedText[j];
                comparedTrigram += cryptedText[j + 1];
                comparedTrigram += cryptedText[j + 2];
                
                addAndPrintMatchPositions(currentTrigram, comparedTrigram, i, j, trigramsSpacing);
                comparedTrigram = "";
            }
            currentTrigram = "";
        }
    }

    /**
     * Adds in a list all the space length between trigrams.
     * 
     * @param currentTrigram the current trigram.
     * @param comparedTrigram the compared trigram.
     * @param i the position of the current trigram.
     * @param j the position of the compared trigram.
     * @param trigramsSpacing the list that contains the spaces between each of them.
     */
    private void addAndPrintMatchPositions(String currentTrigram, 
                                           String comparedTrigram, 
                                           int i, int j, 
                                           ArrayList<Integer>[] trigramsSpacing) {

        // Adding all the space lengths.
        if (currentTrigram.equals(comparedTrigram)) {
            if (i != j) {
                trigramsSpacing[i].add(j - i);
            }
            System.out.println(currentTrigram + " at position " + i 
                               + " matches at position " + j);
        }
    }

    /**
     * Initializes the arraylist that will contains all the spaces between
     * matching trigrams.
     * 
     * @return an arraylist that contains the spaces between the matching 
     * trigrams.
     */
    private ArrayList<Integer>[] initTrigramsSpacing() {
        ArrayList<Integer>[] trigramsSpacing = new ArrayList[3];
        for (int i = 0; i < 3; i++) {
            trigramsSpacing[i] = new ArrayList<>();
        }
        return trigramsSpacing;
    }
    
    /**
     * Find the greatest common divisor of two numbers.
     * 
     * @param a one the numbers.
     * @param b one the numbers.
     * @return the GCD of the two given numbers.
     */
    private int gcd(int a, int b) {
        if (b == 0) {
            return a;
        }
     
        return gcd(b, a % b);
    }

    
    /**
     * Encrypt with the Caesar cipher.
     *
     * @param text the original text.
     * @param offset the offset for the encryption.
     * @return the text encrypted.
     */
    String caesarEncrypt(String text, int offset) {
        char[] cryptedText = new char[text.length()];
        int txtCharPos;
        
        for (int i = 0; i < text.length(); i++) {
            txtCharPos = getTxtCharsAlphabetPos(text.charAt(i));
            cryptedText[i] = ALPHABET.charAt((txtCharPos + offset) % 26);
        }
        
        return new String(cryptedText);
    }   
    
    /**
     * Decrypt the Caesar cipher without the offset.
     * 
     * @param text the encrypted text.
     * @return the decrypted text.
     */
    String caesarDecrypt(String text) {
        int offset = findCaesarOffset(text);
        return caesarDecWithOffset(text, offset);
    }

    private int findCaesarOffset(String text) {
        int[] lettersCount;
        int maxFrequence, maxLetterPos, offset;
        
        lettersCount = computeFrequencies(text);
        maxFrequence = lettersCount[0];
        maxLetterPos = 0;
        
        for (int i = 1; i < 26; i++) {
            if (lettersCount[i] > maxFrequence) {
                maxFrequence = lettersCount[i];
                maxLetterPos = i;
            }
        }
        offset = (maxLetterPos + 22) % 26;
        return offset;
    }

    /**
     * Decrypt the Caesar cipher with the offset given.
     * 
     * @param text the encrypted text.
     * @param offset the offset of the cipher.
     * @return the decrypted text.
     */
    String caesarDecWithOffset(String text, int offset) {
        return caesarEncrypt(text, 26 - offset);
    }     

    /**
     * Gives the position in the alphabet of a character.
     *
     * @param character the character to find the position.
     * @return the alphabetic position of the given character.
     */
    private int getTxtCharsAlphabetPos(char character)  {
        return ALPHABET.indexOf(String.valueOf(character));
    }

    /**
     * Gives the position in the alphabet of a letter of the key.
     * 
     * @param position the position of the character in the key.
     * @param key the key used to encrypt the text.
     * @return the alphabetic position of the specified key letter.
     */
    private int getKeyCharsAlphabetPos(int position, String key) {
        char character = key.charAt(position % key.length());
        return ALPHABET.indexOf(character);
    }    
}
