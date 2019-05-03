package it.cnr.iit.usagecontrolframework.rest.jgiven.stages;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Duration.ONE_HUNDRED_MILLISECONDS;
import static org.awaitility.Duration.TWO_SECONDS;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import com.github.tomakehurst.wiremock.client.VerificationException;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;
import com.tngtech.jgiven.integration.spring.JGivenStage;

import it.cnr.iit.sessionmanagerdesktop.SessionManagerDesktop;
import it.cnr.iit.ucsinterface.sessionmanager.SessionInterface;

@JGivenStage
public class ThenMessage extends Stage<ThenMessage> {

    private static final Logger log = Logger.getLogger( ThenMessage.class.getName() );

    @ExpectedScenarioState
    WireMock wireMockContextHandler;

    @ExpectedScenarioState
    SessionManagerDesktop sessionManager;

    @ExpectedScenarioState
    String sessionId;

    List<SessionInterface> sessionsList;

    public ThenMessage the_asynch_post_request_for_$_with_decision_$_was_received_by_PEPRest( @Quoted String operation,
            @Quoted String decision ) {
        await().with().pollInterval( ONE_HUNDRED_MILLISECONDS )
            .and().with().pollDelay( TWO_SECONDS )
            .until( postRequestWasVerified( operation, decision ) );
        return self();
    }

    private Callable<Boolean> postRequestWasVerified( String operation, String decision ) {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    wireMockContextHandler.verifyThat( postRequestedFor( urlEqualTo( operation ) )
                        .withRequestBody( containing( "\"decision\":\"" + decision + "\"" ) )
                        .withHeader( "Content-Type", equalTo( "application/json" ) ) );
                } catch( VerificationException e ) {
                    log.warning( "POST request is not yet received. Polling with 100ms interval for 10 seconds." );
                    return false;
                } catch( Exception e ) {
                    fail( e.getLocalizedMessage() );
                }
                return true;
            }
        };
    }

    public ThenMessage an_entry_for_session_with_status_$_is_persisted( @Quoted String status ) {
        await().with().pollInterval( ONE_HUNDRED_MILLISECONDS )
            .and().with().pollDelay( TWO_SECONDS )
            .until( sessionForStatusCreated( status ) );

        assertNotNull( sessionsList );
        SessionInterface sessionInterface = sessionsList.get( 0 );
        assertTrue( sessionInterface.getStatus().equalsIgnoreCase( status ) );
        sessionId = sessionInterface.getId();
        return self();
    }

    private Callable<Boolean> sessionForStatusCreated( String status ) {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    sessionsList = sessionManager.getSessionsForStatus( status );
                    if( sessionsList.isEmpty() ) {
                        log.warning( "Session for status is not yet created. Polling with 100ms interval for 10 seconds." );
                        return false;
                    }
                } catch( Exception e ) {
                    fail( e.getLocalizedMessage() );
                }
                return true;
            }
        };
    }

    public ThenMessage the_session_entry_status_is_updated_to_$( @Quoted String status ) {
        assertNotNull( sessionId );
        await().with().pollInterval( ONE_HUNDRED_MILLISECONDS )
            .and().with().pollDelay( TWO_SECONDS )
            .until( sessionForIdUpdated( status ) );
        return self();
    }

    private Callable<Boolean> sessionForIdUpdated( String status ) {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    Optional<SessionInterface> sessionInterface = sessionManager.getSessionForId( sessionId );
                    assertTrue( sessionInterface.isPresent() );
                    if( !sessionInterface.get().getStatus().equalsIgnoreCase( status ) ) {
                        log.warning( "Session Id is not yet updated. Polling with 100ms interval for 10 seconds." );
                        return false;
                    }
                } catch( Exception e ) {
                    fail( e.getLocalizedMessage() );
                }
                return true;
            }
        };
    }
}
