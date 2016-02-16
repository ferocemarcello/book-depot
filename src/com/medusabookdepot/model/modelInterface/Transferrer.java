/**
 * 
 */
package com.medusabookdepot.model.modelInterface;

import javafx.beans.property.StringProperty;

/**
 * This interface represent a generic transferrer
 * @author Marcello_Feroce
 */
public interface Transferrer {

    /**
     * 
     * @return the name of the Transferrer object
     */
    public String getName();
    /**
     * 
     * @return the name of the Transferrer object
     */
    public StringProperty nameProperty();
    /**
     * 
     * @param name is the name I want to give to the Transferrer Object
     */
    public void setName(String name);
    /**
     * 
     * @return true if the transferrer is a Depot
     */
    public boolean isADepot();

}
