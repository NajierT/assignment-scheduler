package io.github.najiert.assignmentscheduler.service;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class uses most of the code from the Google Calendar API quickStart to
 * authenticate the user's credentials and return the calendar service. There is a
 * modification to delete the existing tokens in the case of a TokenResponseException
 * @author Google and Najier Torrence
 *
 */
public class CalendarService {
  /**
   * Application name.
   */
  private static final String APPLICATION_NAME = "Assignment Scheduler API Manager";
  /**
   * Global instance of the JSON factory.
   */
  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
  /**
   * Directory to store authorization tokens for this application.
   */
  private static final String TOKENS_DIRECTORY_PATH = "tokens";

  /**
   * Global instance of the scopes required by this quickstart.
   * If modifying these scopes, delete your previously saved tokens/ folder.
   */
  private static final List<String> SCOPES =
		  List.of(CalendarScopes.CALENDAR_READONLY, CalendarScopes.CALENDAR_EVENTS);
  private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

  /**
   * Creates an authorized Credential object.
   *
   * @param HTTP_TRANSPORT The network HTTP Transport.
   * @return An authorized Credential object.
   * @throws IOException If the credentials.json file cannot be found.
   */
  private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
      throws IOException {
    // Load client secrets.
    InputStream in = CalendarService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
    if (in == null) {
      throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
    }
    GoogleClientSecrets clientSecrets =
        GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

    // Build flow and trigger user authorization request.
    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
        .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
        .setAccessType("offline")
        .setApprovalPrompt("force")
        .build();
    LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
    Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    //returns an authorized Credential object.
    if (credential.getAccessToken() == null) {
    	credential.refreshToken();
    }
    
    return credential;
  }
  
	/**
	 * Returns the list of calendars the user has
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static ArrayList<CalendarListEntry> getCalendarList(Calendar service) throws IOException, InterruptedException {
		try {
			CalendarList calendarList = service.calendarList().list().setShowHidden(true).execute();
			return (ArrayList<CalendarListEntry>) calendarList.getItems();
		}
		// delete credentials file and attempt to retry if invalid
		catch (TokenResponseException e) {
			File aboslute = new File(System.getProperty("user.dir"));
			File credentials = new File(aboslute, "/tokens/StoredCredential");
			System.out.println(credentials.delete());
			CalendarList calendarList = service.calendarList().list().setShowHidden(true).execute();
			return (ArrayList<CalendarListEntry>) calendarList.getItems();
		}
	}
  
  public static Calendar getUserCalendar() throws GeneralSecurityException,
  IOException, InterruptedException { 
	 try {
		 final NetHttpTransport HTTP_TRANSPORT =  GoogleNetHttpTransport.newTrustedTransport();
		 Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY,getCredentials(HTTP_TRANSPORT))
				 .setApplicationName(APPLICATION_NAME)
				 .build(); 
		 return service;
	 }
	// delete credentials file
	 catch (TokenResponseException e) {
		 File aboslute = new File(System.getProperty("user.dir"));
		 File credentials = new File(aboslute, "/tokens/StoredCredential");
		 System.out.println(credentials.delete());
		 final NetHttpTransport HTTP_TRANSPORT =  GoogleNetHttpTransport.newTrustedTransport();
		 Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY,getCredentials(HTTP_TRANSPORT))
				 .setApplicationName(APPLICATION_NAME)
				 .build(); 
		 return service;
	}
  }
}