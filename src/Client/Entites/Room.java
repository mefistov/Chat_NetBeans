/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.Entites;

import java.util.ArrayList;

/**
 *
 * @author Vlad
 */
public class Room {
    private final ArrayList<User> users;
    private String login;
    
    private Room(String login){
        users = new ArrayList<>();
        this.login = login;
    }
    public ArrayList<User> getUsers(){
        return users;
    }
    public void addUser(User user){
        users.add(user);
    }
    public void removeUser(User user){
        users.remove(user);
    }
    public int getUsersAmount(){
        return users.size();
    }
    public String getLogin(){
        return login;
    }
    public void setLogin(String login){
        this.login = login;
    }
}
