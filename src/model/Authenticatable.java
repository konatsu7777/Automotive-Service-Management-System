package model;
 
public interface Authenticatable {
    boolean authenticate(String password);
}