package mwgrid.middleware.distributedobject;

public interface Variable {
    /**
     * @return (Class<?>) fType
     */
    Class<?> getType();
    
    /**
     * @return (int) ID
     */
    int getVariableId();
    
    /**
     * @return (String) name
     */
    String getName();
}
