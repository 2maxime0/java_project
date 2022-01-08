package com.home;

import com.activity.Activity;
import com.activity.ActivityDAO;
import com.association.Association;
import com.association.AssociationDAO;
import com.config.*;
import com.cotisation.Cotisation;
import com.cotisation.CotisationDAO;
import com.member.*;

import java.util.ArrayList;

public class main {

    public static void main(String[] args) {

        /*
        //Exemple de récupération d'un lien depuis le fichier de config
        System.out.println(ReadProperties.getConfig("linkBDD"));
        System.out.println(ReadProperties.getConfig("loginBDD"));
        System.out.println(ReadProperties.getConfig("mdpBDD"));
        System.out.println("\n");
         */

        /*
        //Exemple de récupération de tout les membres
        for (Member member : MemberDAO.getAllMember()) {
           System.out.println(member.toString());
            System.out.println("------------------");
        }
        System.out.println("\n");

        //Exemple de récupération de toute les associations
        for (Association association : AssociationDAO.getAllAssociation()) {
            System.out.println(association.toString());
            System.out.println("------------------");
        }
        System.out.println("\n");

        //Exemple de récupération de toute les activités
        for (Activity activity : ActivityDAO.getAllActivities()) {
            System.out.println(activity.toString());
            System.out.println("------------------");
        }
        System.out.println("\n");

        //Exemple de récupération de toute les cotisations
        for (Cotisation cotisation : CotisationDAO.getAllCotisation()) {
            System.out.println(cotisation.toString());
            System.out.println("------------------");
        }
        System.out.println("\n");
         */

        //Changer le numéro pour changer de membre
        //Dans les données de bases de la base, 1 = Maxime, 2 = Martin et 3 = Bastien
        Member memberTest = MemberDAO.getMemberById(2);

        launchControl.menu(memberTest);

    }

}
