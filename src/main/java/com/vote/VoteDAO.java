package com.vote;

import com.config.ReadProperties;
import com.member.Member;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class VoteDAO {

    /**Connection
     * @auth Maxime
     * Connecte à la base de données
     * @return
     */
    private static Connection getConnection() {

        Connection connection = null;

        try {
            connection = DriverManager.getConnection(ReadProperties.getConfig("linkBDD"), ReadProperties.getConfig("loginBDD"), ReadProperties.getConfig("mdpBDD"));
        } catch (Exception e) {
            e.printStackTrace();
            if(e instanceof ClassNotFoundException)
                System.out.println("PostgreSQL JDBC driver not found.");
            if(e instanceof SQLException)
                System.out.println("Connection failure.");
        }

        return connection;
    }

    /**getAllVote
     * @auth Maxime
     * @return List de tous les votes
     */
    public static ArrayList<Vote> getAllVote() {

        ArrayList<Vote> votes = new ArrayList<Vote>();

        Connection connection = getConnection();

        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM public.vote");
            while (resultSet.next()) {
                votes.add(new Vote(resultSet.getInt(1), resultSet.getInt(2), resultSet.getInt(3)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return votes;
    }

    /**
     * @auth
     * @param member
     * @return Tous les votes d'un membre
     */
    public static ArrayList<Vote> getAllVoteForMember(Member member) {

        ArrayList<Vote> votes = new ArrayList<Vote>();

        Connection connection = getConnection();

        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM public.vote WHERE id_member="+member.getId());
            while (resultSet.next()) {
                votes.add(new Vote(resultSet.getInt(1), resultSet.getInt(2), resultSet.getInt(3)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return votes;
    }

    /**
     * @auth Maxime
     * @param member
     * @param id_arbre
     * Insere dans la base de données
     */
    public static void createNewVote(Member member, int id_arbre) {

        Connection connection = getConnection();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO public.vote VALUES (DEFAULT, ?, ?);");
            preparedStatement.setInt(1, member.getId());
            preparedStatement.setInt(2, id_arbre);
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



}
