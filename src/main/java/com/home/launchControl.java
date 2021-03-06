package com.home;

import com.activity.Activity;
import com.activity.ActivityDAO;
import com.association.Association;
import com.association.AssociationDAO;
import com.association.Finance;
import com.association.FinanceDAO;
import com.cotisation.Cotisation;
import com.cotisation.CotisationDAO;
import com.mail.SendMail;
import com.member.Member;

import com.member.MemberDAO;

import com.opencsv.bean.CsvToBeanBuilder;

import com.tree.Tree;
import com.visite.Visite;
import com.visite.VisiteDAO;
import com.vote.Vote;
import com.vote.VoteDAO;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.member.MemberDAO.getAllMemberThatAreDonor;
import static com.vote.VoteDAO.getAllVote;
import static com.vote.VoteDAO.getAllVoteForMember;
import static java.lang.Integer.parseInt;
import static java.util.Collections.frequency;

public class launchControl {

    private static Member currentMember;
    private static Association associationMember;
    private static String treeFileName;

    public static void menu(Member memberRecu, String treeFileNameRecu) {

        //Initialisation des variable static member et association
        currentMember = memberRecu;
        associationMember = AssociationDAO.getAssociationByMember(currentMember);
        treeFileName = treeFileNameRecu;

        boolean programLife = true;
        while(programLife) {
            if (currentMember.isMember()) {

                System.out.print("\n" + "Menu (membre) : " + "\n" +
                        "1 - Payer ma cotisation" + "\n" +
                        "2 - Extraire mes RGPD" + "\n" +
                        "3 - Voter pour un arbre" + "\n" +
                        "4 - Supprimer mon compte membre" + "\n" +
                        "5 - Enregistrer une visite d'arbre" + "\n" +
                        "6 - Afficher toute les visites" + "\n" +
                        "0 - Quitter le programme" + "\n" +
                        "------ PARTIE TECHNIQUE (pour tester) ------" + "\n" +
                        "10 - Exraire le rapport d'activit?? \n" +
                        "11 - Revoquer les membres n'ayant pas pay?? leur cotisation \n" +
                        "12 - Faire une demande de don" + "\n" +
                        "13 - Faire une demande de subvention" + "\n" +
                        "14 - Resultats des votes\n" +
                        "15 - Payer une facture \n" +
                        "Votre choix : ");
                Scanner s = new Scanner(System.in);
                switch (s.nextInt()) {
                    case 0:
                        programLife = false;
                        break;
                    case 1:
                        payCotisation();
                        break;
                    case 2:
                        displayRGPD();
                        break;
                    case 3:
                        vote();
                        break;
                    case 4:
                        deleteAccount();
                        break;
                    case 5:
                        planVisit();
                        break;
                    case 6:
                        displayVisit();
                        break;
                    case 10:
                        getActivityReport();
                        break;
                    case 11:
                        checkMemberCotisation();
                        break;
                    case 12:
                        askForDonation();
                        break;
                    case 13:
                        askForSubvention();
                        break;
                    case 14:
                        resultVote();
                        break;
                    case 15:
                        payBill();
                        break;
                }
            }
            if (!currentMember.isMember()) {
                System.out.print("\n" + "Menu (doneur) : " + "\n" +
                        "1 - Verser un don" + "\n" +
                        "2 - Extraire mes RGPD" + "\n" +
                        "3 - Supprimer mon compte doneur" + "\n" +
                        "0 - Quitter le programme" + "\n" +
                        "Votre choix : ");
                switch (new Scanner(System.in).nextInt()) {
                    case 0:
                        programLife = false;
                        break;
                    case 1:
                        payCotisation();
                        break;
                    case 2:
                        displayRGPD();
                        break;
                    case 3:
                        deleteAccount();
                        break;
                }
            }
        }

        exitProgram();
    }

    /**@auth Maxime
     * Stoppe le programme
     */
    public static void exitProgram() {
        System.out.println("\n" + "Merci d'avoir utilis?? le programme. Au revoir !");
        System.exit(0);
    }

    /**@auth Maxime
     * Permet ?? l'utilisateur de planifier une visite vers un arbre, de se faire rembourser les frais et d'??crire son compte rendu
     */
    public static void planVisit() {

        if(VisiteDAO.getAllVisitesForMember(currentMember).size() > 5 ) {
            System.out.println("D??sol??, Vous ??tes limit?? ?? 5 visites par an.");
            return;
        }

        int id_arbre = -1;
        while(id_arbre <= 0) {
            try{
                System.out.print("\n" + "Veuillez renseigner le num??ro de l'arbre visit?? (n'importe quelle num??ro pour tester si besoin) : ");
                id_arbre = new Scanner(System.in).nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Erreur, vous devez rentrer un nombre entier.");
            }
            if(id_arbre <= 0) {
                System.out.println("Veuillez faire attention ?? ce que votre choix soit sup??rieur ?? 0.");
            }
        }

        int prix = 6; //Defraiement = 6 (en dur)

        System.out.print("Veuillez ??crire votre compte rendu au sujet de cette visite : ");
        String compte_rendu = new Scanner(System.in).nextLine();

        VisiteDAO.createNewVisite(currentMember, id_arbre, prix, compte_rendu);

        //Actualisation des d??penses de l'association et de la variable statique association.
        AssociationDAO.updateDepense(associationMember, prix);
        associationMember = AssociationDAO.getAssociationByMember(currentMember);

        System.out.println("\n" + "Voici le r??sum?? de votre visite : ");
        System.out.println(VisiteDAO.getLastVisite().getInfo());
    }

    /**@auth Maxime
     * Affiche la liste de toute les visites de l'utilisateur
     */
    public static void displayVisit() {

        System.out.println("");
        for(Visite visite : VisiteDAO.getAllVisitesForMember(currentMember)) {
            System.out.println(visite.getInfo());
            System.out.println("--------------------");
        }

    }

    /**
     * @auth Martin
     * Envoie un mail de demande de subvention
     */
    private static void askForSubvention() {
        System.out.print("Veuillez renseigner l'adresse email du contact auquel demander une subvention : ");
        String mailTo = new Scanner(System.in).next();

        String message = initAskFor("subvention");

        SendMail.main(("Demande de subvention pour "+associationMember.getName()), message, mailTo);
    }

    /**
     * @auth Martin
     * Envoie un mail de demande de donnation ?? tous les donnateurs
     */
    private static void askForDonation() {
        String message = initAskFor("donation");

        List<Member> donors = getAllMemberThatAreDonor();
        for(int i=0; i<donors.size();i++) {
            SendMail.main(("Demande de donnation pour "+associationMember.getName()), message, donors.get(i).getLogin());
        }
    }

    /**
     * @auth Martin
     * @param type type de demande
     * @return message du meil
     */
    private static String initAskFor(String type) {
        //G??naration des rapports d'activit??es des deux derni??res ann??es
        String year = DateTimeFormatter.ofPattern("yyyy").format(LocalDateTime.now());
        String lastYear = Integer.toString(parseInt(year)-1);
        activityReportForYear(year);
        activityReportForYear(lastYear);

        String message = "";
        try {
            message = "Bonjour madame, monsieur.\n" +
                    "Afin de pr??server tous les arbres qui font la beaut?? de Paris, nous faisons appel ?? vous dans l'objectif d'obtenir une "+type+".\n" +
                    "Voici nos rapports d'activit??s des deux derni??res ann??es contenant chacun une synth??se : \n\n\n"+
                    Files.readString(Path.of(associationMember.getName()+"_activity_report_for_year_" + year + ".txt"))+"\n________________________________________________________\n"+
                    Files.readString(Path.of(associationMember.getName()+"_activity_report_for_year_" + lastYear + ".txt"));
        } catch (IOException e) {
            System.out.println("Erreur, un probl??me est survenue lors de la g??n??ration du mail.");
            e.printStackTrace();
        }

        return message;
    }

    /**
     * @auth Martin & Maxime
     * Demande ?? l'utlisateur de payer une cotisation ou une donation en fonction de son type
     */
    private static void payCotisation() {

        if(currentMember.isMember()) {

            if(CotisationDAO.getCotisationForMemberByDate(currentMember, DateTimeFormatter.ofPattern("dd/MM/yyyy").format(LocalDateTime.now())) == null) {

                int montant = 30; //Montant en dur
                String answer = "";
                while(!answer.equals("Y") && !answer.equals("N")) {
                    System.out.print("Voulez vous payer votre cotisation de "+montant+" euros (Y/N) : ");
                    answer = new Scanner(System.in).next();
                    if(!answer.equals("Y") && !answer.equals("N")) {
                        System.out.println("Veuillez faire attention ?? r??pondre par oui (Y) ou par non (N).");
                    }
                }

                if(answer.equals("Y")) {
                    CotisationDAO.createNewCotisation(currentMember, 30);
                    ActivityDAO.createNewActivity(associationMember, associationMember.getName()+" ?? re??u une cotisation de "+montant+" euros de la part de "+currentMember.getName()+".");
                    System.out.println("\n" + "Voici le r??sum?? de votre cotisation : " + "\n");
                    System.out.println(CotisationDAO.getLastCotisation().getInfo());
                }
                if(answer.equals("N")) {
                    return;
                }

            } else { System.out.println("Vous avez d??j?? r??gl?? votre cotisation cette ann??e."); }

        } else {

            int montant = -1;
            while (montant <= 0) {
                try{
                    System.out.print("\n" + "Veuillez renseigner le montant de votre donation : ");
                    montant = new Scanner(System.in).nextInt();
                } catch (InputMismatchException e) {
                    System.out.println("Erreur, vous devez rentrer un nombre entier.");
                }
                if(montant <= 0) {
                    System.out.println("Veuillez faire attention ?? ce que le montant renseign?? soit sup??rieur ?? 0.");
                }
            }

            CotisationDAO.createNewCotisation(currentMember, montant);
            ActivityDAO.createNewActivity(associationMember, associationMember.getName()+" ?? re??u une donation de "+montant+" de la part de "+currentMember.getName()+".");
            System.out.println("\n" + "Voici le r??sum?? de votre donation : " + "\n");
            System.out.println(CotisationDAO.getLastCotisation().getInfo());
        }

        //Update de la recette de l'association (et de sa variable statique)
        AssociationDAO.updateRecette(associationMember);
        associationMember = AssociationDAO.getAssociationByMember(currentMember);
    }

    /**
     * @auth Martin & Maxime
     * Cr??e un fichier contenant les informations de l'utilisateur
     */
    private static void displayRGPD() {
        String fileName = currentMember.getName()+"_"+ currentMember.getId()+"_data.txt";
        String encoding = "UTF-8";

        try{
            PrintWriter writer = new PrintWriter(fileName, encoding);
            writer.println(currentMember.getInfo());
            writer.println("--------------------");
            for(Cotisation cotisation : CotisationDAO.getAllCotisationForMember(currentMember)) {
                writer.println(cotisation.getInfo());
                writer.println("\n");
            }
            writer.close();

            System.out.println("Un fichier de format txt ?? ??t?? imprim?? dans java project.");
        }
        catch (IOException e){
            e.printStackTrace();
            System.out.println("Une erreur durant l'impression du fichier RGPD ?? eu lieu.");
        }
    }

    /**
     * @auth Bastien
     * Demande ?? l'utilisateur de saisir une ann??e et d??bute l'impression d'un rapport d'activit?? de l'association
     */
    private static void getActivityReport() {
        int year = 0;
        boolean condition = false;

        while (!condition) {
            try{
                System.out.print("\n" + "Veuillez indiquer l'ann??e du rapport : ");
                year = new Scanner(System.in).nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Erreur, vous devez rentrer une ann??e valide.");
            }

            condition = true;
            if(year < 1900 || year > 2100) {
                System.out.println("Erreur, vous devez rentrer une ann??e valide.");
                condition = false;
            }
        }

        activityReportForYear(String.valueOf(year));
    }

    /**
     * Cr??e un fichier contenant le rapport d'activit?? de l'association pour une ann??e
     * @auth Bastien
     * @param year
     */
    private static void activityReportForYear(String year) {
        ArrayList<Activity> activities = ActivityDAO.getAllActivitiesByYear(year);

        String fileName = associationMember.getName()+"_activity_report_for_year_" + year + ".txt";
        String encoding = "UTF-8";

        try{
            PrintWriter writer = new PrintWriter(fileName, encoding);

            //Finances
            writer.println("Etats des finances pour l'ann??e "+year+" : ");
            Finance finance = FinanceDAO.getFinanceForYear(associationMember, year);
            if (finance == null) {
                writer.println("Aucune informations sur cette ann??e.");
            } else {
                writer.println("D??pense : " +finance.getDepense());
                writer.println("Recette : " +finance.getRecette());
            }

            //Activit??
            writer.println("Synth??se des activit??es pour l'ann??e "+year+" : ");
            if (activities.size() == 0)
            {
                writer.println("Aucune activit??es concernant cette ann??e.");
            }
            else {
                for (Activity activity : activities) {
                    writer.println(activity.info());
                    writer.println("--------------------");
                }
            }
            writer.close();

            System.out.println("Un fichier de format txt ?? ??t?? imprim?? dans java project.");
        }
        catch (IOException e){
            e.printStackTrace();
            System.out.println("Une erreur durant l'impression du rapport d'activit?? ?? eu lieu.");
        }

    }

    /**
     * @auth Maxime
     * Demande ?? l'utilisateur si il souhaite supprimer son compte, et si oui, le supprime et quitte le programme
     */
    private static void deleteAccount() {
        String answer = "";
        while(!answer.equals("Y") && !answer.equals("N")) {
            System.out.print("\n" + "??tes vous sur de vouloir supprimer votre compte (Y/N) : ");
            answer = new Scanner(System.in).next();
            if(!answer.equals("Y") && !answer.equals("N")) {
                System.out.println("Veuillez faire attention ?? r??pondre par oui (Y) ou par non (N).");
            }
        }

        if(answer.equals("Y")) {
            MemberDAO.deleteMember(currentMember);
            exitProgram();
        }
        if (answer.equals("N")) {
            return;
        }
    }

    /**
     * @auth Martin
     * @return Une liste comprenant les arbres du fichier CSV
     */
    private static List<Tree> treesCSV() {
        List<Tree> arbres = new ArrayList<>();
        try {
            arbres = new CsvToBeanBuilder(new FileReader(treeFileName))
                    .withType(Tree.class)
                    .withSeparator(';')
                    .build()
                    .parse();
        } catch(FileNotFoundException e) {
            System.out.println("Erreur, le fichier 'les-arbres.csv' n'a pas ??t?? trouv??.");
            e.printStackTrace();
        }
        return arbres;
    }

    /**
     * @auth Martin
     * Imprime la liste de tous les abres dans la console (fonction lente)
     */
    public static void printTree() {
        List<Tree> liste = treesCSV();
        for (int i = 0; i<liste.size();i++){
            System.out.println(liste.get(i).showMe());
        }
    }

    /**
     * @auth Martin
     * @param lieu Arrondissement/d??partement
     * Imprime les arbres d'un certain arrondissement dans la console (fonction lente)
     */
    public static void printTree(String lieu) {
        List<Tree> liste = treesCSV();
        for (int i = 0; i<liste.size();i++){
            if(Objects.equals(lieu, liste.get(i).getLocation())) {
                System.out.println(liste.get(i).showMe());
            }
        }
    }

    /**
     * @auth Martin
     * Permet ?? un membre de voter pour 5 arbres
     */
    public static void vote() {
        ArrayList<Vote> liste = getAllVoteForMember(currentMember);
        if(liste.size()<5) {
            System.out.println("Voulez vous  avoir la liste des arbres avant de voter (1) ou voter directement (2) ?\n");
            Scanner s = new Scanner(System.in);
            switch (s.nextInt()) {
                case 1:
                    System.out.println("De quel arrondissement/d??partement voulez vous voir les arbres ?\n" +
                            "0 : Tous les arbres" + "\n" +
                            "1 : Paris, 1er arrondissement" + "\n" +
                            "2 : Paris, 2eme arrondissement" + "\n" +
                            "3 : Paris, 3eme arrondissement" + "\n" +
                            "4 : Paris, 4eme arrondissement" + "\n" +
                            "5 : Paris, 5eme arrondissement" + "\n" +
                            "6 : Paris, 6eme arrondissement" + "\n" +
                            "7 : Paris, 7eme arrondissement" + "\n" +
                            "8 : Paris, 8eme arrondissement" + "\n" +
                            "9 : Paris, 9eme arrondissement" + "\n" +
                            "10 : Paris, 10eme arrondissement" + "\n" +
                            "11 : Paris, 11eme arrondissement" + "\n" +
                            "12 : Paris, 12eme arrondissement" + "\n" +
                            "13 : Paris, 13eme arrondissement" + "\n" +
                            "14 : Paris, 14eme arrondissement" + "\n" +
                            "15 : Paris, 15eme arrondissement" + "\n" +
                            "16 : Paris, 16eme arrondissement" + "\n" +
                            "17 : Paris, 17eme arrondissement" + "\n" +
                            "18 : Paris, 18eme arrondissement" + "\n" +
                            "19 : Paris, 19eme arrondissement" + "\n" +
                            "20 : Paris, 20eme arrondissement" + "\n" +
                            "21 : Bois de Boulogne" + "\n" +
                            "22 : Bois de Vincennes" + "\n" +
                            "23 : Hauts-de-Seine" + "\n" +
                            "24 : Seine-Saint-Denis" + "\n" +
                            "25 : Val-de-Marne");
                    Scanner c = new Scanner(System.in);
                    int num = c.nextInt();
                    if (num == 0) {
                        printTree();
                    } else if (num <= 20) {
                        printTree("PARIS " + num + "E ARRDT");
                    } else {
                        switch (num) {
                            case 21:
                                printTree("BOIS DE BOULOGNE");
                                break;
                            case 22:
                                printTree("BOIS DE VINCENNES");
                                break;
                            case 23:
                                ;
                                printTree("HAUTS-DE-SEINE");
                                break;
                            case 24:
                                ;
                                printTree("SEINE-SAINT-DENIS");
                                break;
                            case 25:
                                ;
                                printTree("VAL-DE-MARNE");
                                break;
                        }
                    }
                case 2:
                    System.out.println("Veuillez rentrer les id des 5 arbres (l'ordre n'a pas d'importance)");
                    Scanner s1 = new Scanner(System.in);
                    VoteDAO.createNewVote(currentMember, s1.nextInt());
                    Scanner s2 = new Scanner(System.in);
                    VoteDAO.createNewVote(currentMember, s2.nextInt());
                    Scanner s3 = new Scanner(System.in);
                    VoteDAO.createNewVote(currentMember, s3.nextInt());
                    Scanner s4 = new Scanner(System.in);
                    VoteDAO.createNewVote(currentMember, s4.nextInt());
                    Scanner s5 = new Scanner(System.in);
                    VoteDAO.createNewVote(currentMember, s5.nextInt());
                    break;
            }
        }
        else{
            System.out.println("Vous avez d??j?? vot??");
        }
    }

    /** resultVote
     * @auth Martin
     * Renvoie le r??sultat du vote des arbres ?? faire passen en arbre remarquable
      */
    private static void resultVote(){
        List<Vote> resultbeta = getAllVote();
        List<String> result = new ArrayList<String>();
        for(int i=0;i<resultbeta.size();i++){
            result.add(String.valueOf(resultbeta.get(i).getId_arbre()));
        }
        List<Tree> arbre = treesCSV();
        Tree first,second = new Tree("-1","-1","-1","-1","-1","-1","-1","-1"),third = new Tree("-1","-1","-1","-1","-1","-1","-1","-1"),fourth = new Tree("-1","-1","-1","-1","-1","-1","-1","-1"),fifth = new Tree("-1","-1","-1","-1","-1","-1","-1","-1");
        first=arbre.get(0);
        for (int i=1;i<arbre.size();i++){
            if(frequency(result, arbre.get(i).getId())>frequency(result, first.getId()) ||
                    frequency(result, arbre.get(i).getId())==frequency(result, first.getId()) && parseInt(arbre.get(i).getCir())>parseInt(first.getCir()) ||
                    frequency(result, arbre.get(i).getId())==frequency(result, first.getId()) && parseInt(arbre.get(i).getCir())==parseInt(first.getCir()) && parseInt(arbre.get(i).getHeight())>parseInt(first.getHeight())){
                fifth=fourth;
                fourth=third;
                third=second;
                second=first;
                first=arbre.get(i);
            }
            else if(frequency(result, arbre.get(i).getId())>frequency(result, second.getId()) ||
                    frequency(result, arbre.get(i).getId())==frequency(result, second.getId()) && parseInt(arbre.get(i).getCir())>parseInt(second.getCir()) ||
                    frequency(result, arbre.get(i).getId())==frequency(result, second.getId()) && parseInt(arbre.get(i).getCir())==parseInt(second.getCir()) && parseInt(arbre.get(i).getHeight())>parseInt(second.getHeight())) {
                fifth = fourth;
                fourth = third;
                third = second;
                second = arbre.get(i);
            }
            else if(frequency(result, arbre.get(i).getId())>frequency(result, third.getId()) ||
                    frequency(result, arbre.get(i).getId())==frequency(result, third.getId()) && parseInt(arbre.get(i).getCir())>parseInt(third.getCir()) ||
                    frequency(result, arbre.get(i).getId())==frequency(result, third.getId()) && parseInt(arbre.get(i).getCir())==parseInt(third.getCir()) && parseInt(arbre.get(i).getHeight())>parseInt(third.getHeight())) {
                fifth = fourth;
                fourth = third;
                third = arbre.get(i);
            }
            else if(frequency(result, arbre.get(i).getId())>frequency(result, fourth.getId()) ||
                    frequency(result, arbre.get(i).getId())==frequency(result, fourth.getId()) && parseInt(arbre.get(i).getCir())>parseInt(fourth.getCir()) ||
                    frequency(result, arbre.get(i).getId())==frequency(result, fourth.getId()) && parseInt(arbre.get(i).getCir())==parseInt(fourth.getCir()) && parseInt(arbre.get(i).getHeight())>parseInt(fourth.getHeight())) {
                fifth = fourth;
                fourth = arbre.get(i);
            }
            else if(frequency(result, arbre.get(i).getId())>frequency(result, fifth.getId()) ||
                    frequency(result, arbre.get(i).getId())==frequency(result, fifth.getId()) && parseInt(arbre.get(i).getCir())>parseInt(fifth.getCir()) ||
                    frequency(result, arbre.get(i).getId())==frequency(result, fifth.getId()) && parseInt(arbre.get(i).getCir())==parseInt(fifth.getCir()) && parseInt(arbre.get(i).getHeight())>parseInt(fifth.getHeight())) {
                fifth = arbre.get(i);
            }
        }
        System.out.println("Resultats :\n" +
                "1er - "+first.showMe()+", "+frequency(result, first.getId())+" votes\n" +
                " 2eme - "+second.showMe()+", "+frequency(result, second.getId())+" votes\n" +
                " 3eme - "+third.showMe()+", "+frequency(result, third.getId())+" votes\n" +
                " 4eme - "+fourth.showMe()+", "+frequency(result, fourth.getId())+" votes\n" +
                " 5eme - "+fifth.showMe()+", "+frequency(result, fifth.getId())+" votes\n");
    }

    /**
     * V??rifie que chaque membre aie pay?? sa cotisation. Ils sont radi??s sinon.
     * @auth Bastien
     */
    public static void  checkMemberCotisation(){
        boolean selfDeleteCheck = false;

        String date = DateTimeFormatter.ofPattern("dd/MM/yyyy").format(LocalDateTime.now());
        ArrayList<Member> members = MemberDAO.getAllMember();

        for(Member member : members) {
            if(CotisationDAO.getCotisationForMemberByDate(member, date) == null && member.isMember()) {
                System.out.println("Le membre " + member.getName() + " est radi?? pour non reglement de sa cotisation");
                MemberDAO.deleteMember(member);
                if (member.getId() == currentMember.getId()) {
                    selfDeleteCheck = true;
                }
            }
        }

        if (selfDeleteCheck) {
            exitProgram();
        }

    }

    /**
     * @auth Bastien
     * Permet de simuler le payement d'une facture par l'association
     */
    public static void payBill()
    {
        int montant = 0;
        boolean condition = false;

        while (!condition) {
            try{
                System.out.print("\n" + "Veuillez renseigner le montant de la facture : ");
                montant = new Scanner(System.in).nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Erreur, vous devez rentrer un nombre entier.");
            }

            condition = true;
            if(montant <= 0) {
                System.out.println("Veuillez faire attention ?? ce que le montant renseign?? soit sup??rieur ?? 0.");
                condition = false;
            }

        }
        System.out.print("Veuillez renseigner le motif de cette facture : ");
        String motif = new Scanner(System.in).nextLine();

        //Update de l'object statique associationMember
        AssociationDAO.updateDepense(associationMember, montant);
        associationMember = AssociationDAO.getAssociationByMember(currentMember);

        ActivityDAO.createNewActivity(associationMember, associationMember.getName() + " ?? pay?? une facture de "+montant+" euros pour le motif suivant : +"+motif);

        System.out.println("Vous avez pay?? une facture d'un montant de "+montant+" euros pour le motif suivant : "+motif+".");
    }

}
