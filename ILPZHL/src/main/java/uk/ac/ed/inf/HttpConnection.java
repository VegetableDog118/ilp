package uk.ac.ed.inf;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * @author zhuhaolin s1930356
 * This class establish the connection between client and the sever
 */
public class HttpConnection {

  //HttpClient can access the web server, it is shared by all the Requests.
  private static final HttpClient httpClient = HttpClient.newHttpClient();

  //jsonContent store the json content we read from the webserver
  private String jsonContent;
  //machine name for the server for example: localhost if the program run on our own machine
  private String machineName;
  //the port number
  private String port;

  /**
   * connect to server by using the given url and get the data
   * @param url the url to connect the sever
   */
  public void getConnectToServer(String url){
    System.out.println("start connecting---");
    // create a http get request
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .build();
    try {
      System.out.println("here");
      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode()==200){
        // the connection is successful
        System.out.println("200,connection is successful");
        // assign response body to jsonContent
        setJsonContent(response.body());
      }else if(response.statusCode()==404){
        System.out.println("Error 404: content is not found");
        //exit the application
        System.exit(1);
      }else if(response.statusCode()==500){
        System.out.println("Error 500: there is an internal error in the server");
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

  }

  // getter
  public String getJsonContent() {
    return jsonContent;
  }

  //setter
  public void setJsonContent(String jsonContent) {
    this.jsonContent = jsonContent;
  }

  //getter
  public String getMachineName() {
    return machineName;
  }

  //setter
  public String getPort() {
    return port;
  }

  /**
   * Constructor
   * @param machineName the ip of the machine
   * @param port port number
   */
  public HttpConnection(String machineName, String port) {
    this.machineName = machineName;
    this.port = port;
  }
}
