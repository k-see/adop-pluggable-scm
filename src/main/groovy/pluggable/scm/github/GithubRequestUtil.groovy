package pluggable.scm.github;

import pluggable.scm.helpers.*;
import java.net.URL;
import java.io.IOException;
import java.io.DataOutputStream;
import java.nio.charset.Charset;
import groovy.json.JsonSlurper;
import groovy.json.JsonOutput;


public class GithubRequestUtil {

  public static void isProjectAvailable(URL githubUrl, String username, String password, String projectKey){

    URL url = new URL(githubUrl, "/users/" + projectKey + "/repos");
    def auth = "${username}:${password}".bytes.encodeBase64().toString();

    HttpURLConnection http = (HttpURLConnection) url.openConnection();
    http.setRequestMethod("GET");
    http.setRequestProperty ("Authorization", "Basic ${auth}");

    switch (http.getResponseCode()) {
      case 200:
        Logger.info("Project ${projectKey} found.");
        break;
      case 401:
        Logger.log(LogLevel.ERROR, "Credentials are invalid.");
        break;
      case {it > 401}:
        throw new IOException("Github project with key: " + projectKey + " does not exist or github is not available!");
        break;
    }
  }

  public static String[] getProjectRepositorys(URL githubUrl, String username, String password, String projectKey){

    JsonSlurper jsonSlurper = new JsonSlurper();
    URL url = new URL(githubUrl, "/users/" + projectKey + "/repos" + "?limit=100");
    def auth = "${username}:${password}".bytes.encodeBase64().toString();
    List<String> repositoryList = [];

    HttpURLConnection http = (HttpURLConnection) url.openConnection();
    http.setRequestMethod("GET");
    http.setRequestProperty ("Authorization", "Basic ${auth}");
    http.setRequestProperty("Content-Type", "application/json");

    switch (http.getResponseCode()) {
      case 200:
        def json = jsonSlurper.parse(http.getInputStream())
        for(int i = 0; i < json.size; i++){
          repositoryList.add(json.name[i])
        }
        break;
      case 401:
        Logger.log(LogLevel.ERROR, "Credentials are invalid.");
        break;
      case 404:
        throw new IOException("URI not found :" + githubUrl.toString() + "/users/" + projectKey + "/repos");
        break;
      default:
        def json = jsonSlurper.parse(http.getInputStream())
        Logger.info(json.errors.message);
        break;
    }

    return repositoryList;
  }

  public static void createRepository(URL githubUrl, String username, String password, String projectKey, String repoName){

    JsonSlurper jsonSlurper = new JsonSlurper();
    URL url = new URL(githubUrl, "/user/repos");
    def auth = "${username}:${password}".bytes.encodeBase64().toString();
    def body =  JsonOutput.toJson(["name": repoName ])
    byte[] postData = body.getBytes(Charset.forName("UTF-8"));
    println("DEBUG: URL: " + url);
    println("DEBUG: BODY: " + body);
    HttpURLConnection http = (HttpURLConnection) url.openConnection();
    http.setRequestMethod("POST");
    http.setDoOutput(true);
    http.setInstanceFollowRedirects(false);
    http.setRequestProperty("Authorization", "Basic ${auth}");
    http.setRequestProperty("Content-Type", "application/json");
    http.setRequestProperty("charset", "utf-8");
    http.setRequestProperty("Content-Length", postData.length.toString());
    http.setUseCaches(false);
	println("DEBUG: http after props: " + http);

    DataOutputStream wr = new DataOutputStream( http.getOutputStream())
    wr.write( postData );
	println("DEBUG: post data: " + body);
    wr.flush();
    wr.close();
    println("POST Sent");
    switch (http.getResponseCode()) {
        case 201:
            println("Repository created in github : " + projectKey + "/" + repoName + " "  + body);
            break;
        case 404:
            throw new IOException("URI not found: " + url + body);
            break;
        default:
            //def json = jsonSlurper.parse(http.getInputStream());
            //println(json.errors.message);
            println(http.getResponseMessage());
            break;
    }
  }
}
