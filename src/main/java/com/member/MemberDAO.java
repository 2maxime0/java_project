package com.member;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import com.association.Association;
import com.config.*;

public class MemberDAO {
    /**
     * @auth Maxime
     * @return Connecte à la base de donnée
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

    /**
     * @auth Maxime
     * @return tous les membres
     */
    public static ArrayList<Member> getAllMember() {

        ArrayList<Member> members = new ArrayList<Member>();

        Connection connection = getConnection();

        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM public.member");
            while (resultSet.next()) {
                members.add(new Member(resultSet.getInt(1), resultSet.getInt(2), resultSet.getBoolean(3), resultSet.getString(4),
                        resultSet.getString(5), resultSet.getString(6), resultSet.getString(7), resultSet.getString(8), resultSet.getString(9)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return members;
    }

    /**
     * @auth Maxime
     * @return Tous les membre donneurs
     */
    public static ArrayList<Member> getAllMemberThatAreDonor() {

        ArrayList<Member> members = new ArrayList<Member>();

        Connection connection = getConnection();

        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM public.member WHERE type=false;");
            while (resultSet.next()) {
                members.add(new Member(resultSet.getInt(1), resultSet.getInt(2), resultSet.getBoolean(3), resultSet.getString(4),
                        resultSet.getString(5), resultSet.getString(6), resultSet.getString(7), resultSet.getString(8), resultSet.getString(9)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return members;
    }

    /**
     * @aut Maxime
     * @param id
     * @return un membre en fonction de son id
     */
    public static Member getMemberById(int id) {
        
        Member member = null;
        
        Connection connection = getConnection();

        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM public.member WHERE id="+id);
            if(resultSet.next())
                member = new Member(resultSet.getInt(1), resultSet.getInt(2), resultSet.getBoolean(3), resultSet.getString(4),
                        resultSet.getString(5), resultSet.getString(6), resultSet.getString(7), resultSet.getString(8), resultSet.getString(9));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return member;
    }

    /**
     * @auth Maxime
     * @param login
     * @return recupère un membre en fonction de son login
     */
    public static Member getMemberByLogin(String login) {

        Member member = null;

        Connection connection = getConnection();

        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM public.member WHERE login='"+login+"'");
            if(resultSet.next())
                member = new Member(resultSet.getInt(1), resultSet.getInt(2), resultSet.getBoolean(3), resultSet.getString(4),
                        resultSet.getString(5), resultSet.getString(6), resultSet.getString(7), resultSet.getString(8), resultSet.getString(9));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return member;
    }

    /**
     * @auth Maxime
     * @return récupère le dernier membre
     */
    public static Member getLastMember() {

        Member member = null;

        Connection connection = getConnection();

        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM public.member ORDER BY id DESC LIMIT 1");
            if(resultSet.next())
                member = new Member(resultSet.getInt(1), resultSet.getInt(2), resultSet.getBoolean(3), resultSet.getString(4),
                        resultSet.getString(5), resultSet.getString(6), resultSet.getString(7), resultSet.getString(8), resultSet.getString(9));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return member;
    }

    /**
     * @auth Maxime
     * @param association
     * @param type
     * @param login
     * @param mdp
     * @param name
     * @param birth
     * @param adress
     * Cree un membre dans la base de donnée
     */
    public static void createNewMember(Association association, boolean type, String login, String mdp, String name, String birth, String adress) {

        Connection connection = getConnection();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO public.member VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?);");
            preparedStatement.setInt(1, association.getId());
            preparedStatement.setBoolean(2, type);
            preparedStatement.setString(3, login);
            preparedStatement.setString(4, mdp);
            preparedStatement.setString(5, name);
            preparedStatement.setString(6, birth);
            preparedStatement.setString(7, adress);
            preparedStatement.setString(8, DateTimeFormatter.ofPattern("dd/MM/yyyy").format(LocalDate.now()));
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @auth Maxime
     * @param member
     * supprime un memebre dde la base de donnée
     */
    public static void deleteMember(Member member) {

        Connection connection = getConnection();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM public.member WHERE id=?;");
            preparedStatement.setInt(1, member.getId());
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
