import de.vandermeer.asciitable.AsciiTable;
import java.util.Scanner;
import java.util.HashSet;
import java.util.Arrays;
import java.util.HexFormat;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Main {    
    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException,
                                                  UnsupportedEncodingException {
        var argSet = new HashSet<>(Arrays.asList(args));
        if ((args.length%2==0)||(args.length<3)||(args.length!=argSet.size())){
            System.out.print("Error! Arguments must be unique! The number of arguments must be odd and greater than 2");
            System.exit(0);
        }
        while(true){
            SecureRandom secureInt = new SecureRandom();         
            int pcId = secureInt.nextInt(args.length); 
            String pcArg = args[pcId];
            String[] dk = Hmacgen.genHmac(pcArg);
            showMenu(args, dk);
            int uId = getUserMove(args);
            showResult(args, dk, uId, pcId);
        }
    }

    public static void showMenu(String[] args, String[] dk) {
        System.out.println("HMAC: " + dk[0]);
        System.out.print("Available moves: \n");
        for (int i = 0; i < args.length; i++) {
            System.out.printf("%d - %s \n", i+1, args[i]);
        }
        System.out.println("0 - exit");
        System.out.println("? - help");
    }

    public static void showResult(String[] args, String[] dk, int uId, int pcId) {
        System.out.printf("Your move: %s \n", args[uId]);
        System.out.printf("Computer move: %s \n", args[pcId]);
        String r = Winner.getWinner(args, uId, pcId);
        String result = r.equals("Draw")?"Draw!":(r.equals("Win")?"You win!":"You lose!");
        System.out.println(result);
        System.out.printf("HMAC key: %s \n\n\n", dk[1]);
    }

    public static Integer getUserMove(String[] args) {  
        boolean running = true;
        int uId=-1;
        String regex = "[0-9]";
        do {
            System.out.print("Enter your move: "); 
            Scanner in = new Scanner(System.in);
            String move = in.nextLine();
            if (move.equals("0")) {
                System.exit(0);
            }
            if (move.equals("?")) {
                Table.genTable(args);
                continue;
            }
            if (move.matches(regex)) {
                uId = Integer.parseInt(move)-1;
            }
            if (0<=uId && uId<args.length) {
                running = false;
                break;
            }
            System.out.print("Incorrect input!\n");
        }   
        while (running);
        return uId;     
    }
}

class Hmacgen {
    public static String[] genHmac(String pcMove) throws NoSuchAlgorithmException, InvalidKeyException,
                                                         UnsupportedEncodingException {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecureRandom secureKey = new SecureRandom();
        byte[] keyBytes = new byte[16];   
        secureKey.nextBytes(keyBytes); 
        SecretKeySpec k = new SecretKeySpec(keyBytes, "HmacSHA256");
        mac.init(k);
        byte[] d = mac.doFinal(pcMove.getBytes("UTF-8"));
        String digest = HexFormat.of().formatHex(d); 
        String key = HexFormat.of().formatHex(keyBytes);  
        String[] dk = {digest, key};
        return dk;
    }
}

class Winner {
    public static String getWinner(String[] args, int uId, int pcId) {
        int start = pcId + 1;
        HashSet<Integer> winners = new HashSet<Integer>();                                                           
        for (int i = 0; i < ((args.length-1)/2); i++) {
            winners.add((start + i) % args.length);
        }
        String result = (pcId == uId)? "Draw":(winners.contains(uId)?"Win":"Lose");
        return result;
    }
}

class Table {
    public static void genTable(String[] args) {
        AsciiTable at = new AsciiTable();
        String[] title = new String[args.length+1];
        title[0] = "User \\ Pc";
        for (int i = 0; i < args.length; i++) {
            title[i+1] = args[i];
        }
        at.addRow(title);
        for (int i = 0; i < args.length; i++) {
           at.addRule();
           String[] columns = new String[args.length+1];
           for (int j = 0; j < args.length; j++) {
               columns[0] = args[i];
               columns[j+1] = Winner.getWinner(args, i, j);
           }
        at.addRow(columns);
        }
        String rend = at.render();
        System.out.println(rend + "\n");
    }
}