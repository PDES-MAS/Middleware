package mwgrid.middleware.distributedobject;

public enum KernelVariables implements Variable {
    @PublicVariable
    CLASS(Integer.class), @PublicVariable
    MESSAGES(String.class), @PublicVariable
    LOCATION(Location.class), @PrivateVariable
    PREVIOUS(Location.class);
    private final Class<?> fType;
    
    /**
     * @param pType
     *            - type
     */
    private KernelVariables(final Class<?> pType) {
        this.fType = pType;
    }
    
    @Override
    public Class<?> getType() {
        return this.fType;
    }
    
    @Override
    public int getVariableId() {
        return this.ordinal() + 1;
    }
    
    @Override
    public String getName() {
        return this.name();
    }
}
