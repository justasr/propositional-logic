/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package propositionallogic;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author Justinas Ruika
 */
public class PropositionalLogic {
    
    private static final String FILES_PATH = ""; // path to data.txt directory 
    
    private static ArrayList<ArrayList<String>> db_data = new ArrayList<ArrayList<String>>();
    private static String ending = new String();
    
    
    public static void main(String[] args) throws IOException {
        
        readFile("data.txt");
        boolean status = resolution(); 
        writeFile(true,"output.txt");
        
        System.in.read();
    }
    
    private static void readFile(String filename)
    {
        String line;
        String file_path = FILES_PATH;
        Path path = Paths.get(file_path, filename); 
        Charset charset = Charset.forName("UTF-8");

        try (BufferedReader reader = Files.newBufferedReader(path , charset)) {
          while ((line = reader.readLine()) != null ) {
           String[] lineVariables = line.split(" "); 
           ArrayList<String> list = new ArrayList<String>(lineVariables.length);  
           for (String s : lineVariables) {  
               list.add(s);  
           }    
           db_data.add(list);
          }
        } catch (IOException e) {
            System.err.println(e);
        }
    }
    
    private static void writeFile(boolean status,String filename) throws FileNotFoundException, UnsupportedEncodingException
    {
        PrintWriter writer = new PrintWriter(FILES_PATH + filename, "UTF-8");
        if( status )
            writer.println("Pavyko rasti sprendima " + ending );
        else
            writer.println("Nepavyko rasti sprendima");
        
        for (ArrayList<String> sentances : db_data) {
            String toFile = "";
            for( String statment: sentances )
                toFile = toFile + " " + statment;
            writer.println( toFile );
        }
        writer.close();
    }
    
    private static boolean resolution() {
        int sentence_number = 0;
        while( sentence_number < db_data.size() /* Kol neviršijam masyvo ribų */ )  {
            for (int i = 0; i < sentence_number; i++) {
                boolean status = tryResolution(i,sentence_number);
                /* Paneigta, sėkminga pabaiga */
                if( status == false )
                    return true;
                
            }
            sentence_number++;
        }
        /* Visi sakiniai išskleisti, paneigimas nerastas */
        return false;
    }
    
    private static boolean tryResolution(int sentence_1, int sentence_2) {
        
        for (int i = 0; i < db_data.get(sentence_1).size(); i++) {
            for (int j = 0; j < db_data.get(sentence_2).size(); j++) {
                String element_1 = db_data.get(sentence_1).get(i);
                String element_2 = db_data.get(sentence_2).get(j);
                
                /* Ieskoma beta */
                /* 1. Patikriname ar kuris nors yra paneigtas */
                if( (checkNegative( element_1 ) &&  checkPositive( element_2 )) ||
                      (checkNegative(element_2) &&  checkPositive(element_1))  ) {
                    /* 2. Tikriname ar teiginiai lygūs */
                    /* 2.1 Nuimame neigimą */
                    element_1 = stripNegative(element_1);
                    element_2 = stripNegative(element_2);
                    
                    if( element_1.equals(element_2) )
                    {
                       /* Kuriamas naujas sakinys */
                       /* 1. Sujungiami sakiniai pagal JAVA logika */
                       ArrayList<String> new_sentence_1 = cloneList( db_data.get(sentence_1) );
                       ArrayList<String> new_sentence_2 = cloneList( db_data.get(sentence_2) );
                       /* 2. Išmetamas beta */
                       new_sentence_1.remove(i);
                       new_sentence_2.remove(j);
                       /* 3. Sujungiami du sakiniai */
                       new_sentence_1.addAll(new_sentence_2);
                       /* 4. Pervadinama nes niekam nerupi */
                       ArrayList<String> new_sentence = cloneList( new_sentence_1 );
                                                                    
                       /* Tikrinama ar galima pridėti sakinį i DB */
                       if( check_DB_insertion( new_sentence ) ) 
                           db_data.add(new_sentence);
                       
                       
                       /* Tikrinam ar ne pabaiga */
                       if( new_sentence.size() == 0 ) {
                           ending = db_data.get(sentence_1).get(i) + " " + db_data.get(sentence_2).get(j);
                           return false;
                       }
                    }
                }
                
            } 
        }
        
        return true;
    }
    
    /* Tikrina ar teiginys paneigtas */
    private static boolean checkNegative( String element ) {
        if( element.contains("-") )
            return true;   
        return false;
    }
    
    /* Tikrina ar teiginys paneigtas */
    private static boolean checkPositive(String element ) {
        if( !element.contains("-") )
            return true;   
        return false;
    }
    
    /* Panaikinamas neigimas */
    private static String stripNegative( String element ) {
        if( element.contains("-") )
            return element.substring( 1 );
        return element;
    }
    
    private static boolean check_DB_insertion( ArrayList<String> new_sentence ) {    
        
        /* Sakinio prastymas */        
        Set setItems = new LinkedHashSet(new_sentence);
        new_sentence.clear();
        new_sentence.addAll(setItems);
               
        /* Tikrinam ar pabaiga */
        if(new_sentence.size() == 0)
            return false;
        
        
        for (ArrayList<String> sentance_from_db: db_data) {
            /* Tikrinama ar toks sakinys jau egzistuoja duomenų bazėje */
            if( checkEquals(new_sentence,sentance_from_db) )
                return false;
        }
        
        return true;
    }
    
    private static ArrayList<String> cloneList(ArrayList<String> list) {
        ArrayList<String> clone = new ArrayList<String>(list.size());
        for (String string : list) 
            clone.add(string);

        return clone;
    }
       
     
   
    /* Patikrina ar du masyvai lygus neziurint elementu indeksu */
    private static boolean checkEquals( ArrayList<String> arr1, ArrayList<String> arr2 ) {
        for (String e_arr1 : arr1) {
            boolean exits = false;
            for (String e_arr2 : arr2) {
               if( e_arr2.equals(e_arr1) )
                   exits = true;
            }
            
            if(!exits)
                return false;
        }
        
        if(arr1.size() == arr2.size())
            return true;
            
        return false;
    }
   
}
