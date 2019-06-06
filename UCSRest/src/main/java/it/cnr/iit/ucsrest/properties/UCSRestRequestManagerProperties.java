package it.cnr.iit.ucsrest.properties;

import org.springframework.beans.factory.annotation.Value;

import it.cnr.iit.ucs.properties.components.RequestManagerProperties;

public class UCSRestRequestManagerProperties implements RequestManagerProperties {

    @Value( "${class-name}" )
    private String className;

    @Value( "${communication-type}" )
    private String communicationType;

    @Value( "${api-remote-response}" )
    private String apiRemoteResponse;

    @Value( "${active}" )
    private String active;

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public String getCommunicationType() {
        return communicationType;
    }

    @Override
    public String getApiRemoteResponse() {
        return apiRemoteResponse;
    }

    public void setApiRemoteResponse( String apiRemoteResponse ) {
        this.apiRemoteResponse = apiRemoteResponse;
    }

    public void setClassName( String className ) {
        this.className = className;
    }

    public void setCommunicationType( String communicationType ) {
        this.communicationType = communicationType;
    }

    public void setActive( String active ) {
        this.active = active;
    }

    @Override
    public boolean isActive() {
        if( active == null ) {
            return true;
        }
        return Boolean.parseBoolean( active );
    }

}
