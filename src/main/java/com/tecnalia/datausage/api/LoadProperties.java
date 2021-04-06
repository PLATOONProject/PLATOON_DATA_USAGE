/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tecnalia.datausage.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author root
 */
public class LoadProperties {
    
    
    public Properties prop =new Properties();
    public LoadProperties() throws IOException{
         
        try(InputStream resourceStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("datausage.properties")) {
            prop.load(resourceStream);
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
               
    }
    
    public String getString(String key){
        
        return prop.getProperty(key);
    }
            
    
}