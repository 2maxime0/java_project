package com.tree;

import java.io.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

public class Tree {
    private int id, cir, height;
    private String location,name,type, speicies;
    boolean remarquable;
    public Tree(int idcsv,int circsv,int heightcsv,String loccsv,String nom, String ty, String spe, boolean remcsv){
        id=idcsv;
        cir=circsv;
        height=heightcsv;
        location=loccsv;
        name=nom;
        type=ty;
        speicies=spe;
        remarquable=remcsv;
    }

    private static ArrayList<Tree> treesCSV() throws FileNotFoundException {
        File getCSVFile = new File("java_project/src/main/ressources/les-arbres.csv");
        Scanner sc = new Scanner(getCSVFile);
        sc.useDelimiter(";");
        ArrayList<Tree> Retour = new ArrayList<Tree>();
        while (sc.hasNext()){
            int id= Integer.parseInt(sc.next());
            sc.next();
            sc.next();
            String loc=sc.next();
            sc.next();
            sc.next();
            sc.next();
            sc.next();
            String name = sc.next();
            String type = sc.next();
            String spe = sc.next();
            sc.next();
            int cir = Integer.parseInt(sc.next());
            int hei = Integer.parseInt(sc.next());
            sc.next();
            String rem = sc.next();
            boolean remar = !Objects.equals(rem, "NON");
            sc.next();
            Retour.add(new Tree(id,cir,hei,loc,name,type,spe,remar));
        }
        Retour.remove(0);
        return Retour;
    }

    public String showMe() {
        return  "n°" + id +" - " + name +"/genre : " + type +
                "/espece : " + speicies+" - "+
                "circonférence (en cm) : " + cir +

                " - hauteur (en m) : " + height +
                " - Quartier : " + location +" | "
                 ;
    }

    public static void printTree() throws FileNotFoundException {
        ArrayList<Tree> liste = treesCSV();
        for (int i = 0; i<liste.size();i++){
            liste.get(i).showMe();
        }
    }

    public static void orintTree(String lieu) throws FileNotFoundException {
        ArrayList<Tree> liste = treesCSV();
        for (int i = 0; i<liste.size();i++){
            if(lieu==liste.get(i).location) {
                liste.get(i).showMe();
            }
        }
    }
}
