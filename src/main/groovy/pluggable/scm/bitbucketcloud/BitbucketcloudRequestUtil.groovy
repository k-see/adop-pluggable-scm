package pluggable.scm.bitbucketcloud;

import pluggable.scm.helpers.*;
import java.net.URL;
import java.io.IOException;
import java.io.DataOutputStream;
import java.nio.charset.Charset;
import groovy.json.JsonSlurper;
import groovy.json.JsonOutput;


public class BitbucketcloudRequestUtil {

  public static void isProjectAvailable(URL bitbucketcloudUrl, String username, String password, String projectKey){

    URL url = new URL(bitbucketcloudUrl, "/2.0/repositories/" + projectKey);
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
        throw new IOException("BitBucketCloud project with key: " + projectKey + " does not exist or BitBucket is not available!");
        break;
    }
  }

  public static String[] getProjectRepositorys(URL bitbucketcloudUrl, String username, String password, String projectKey){

    JsonSlurper jsonSlurper = new JsonSlurper();
    URL url = new URL(bitbucketcloudUrl, "/2.0/repositories/" + projectKey + "?limit=100");
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
          repositoryList.add(json.values[i].name)
        }
        break;
      case 401:
        Logger.log(LogLevel.ERROR, "Credentials are invalid.");
        break;
      case 404:
        throw new IOException("URI not found :" + bitbucketcloudUrl.toString() + "/2.0/repositories/" + projectKey);
        break;
      default:
        def json = jsonSlurper.parse(http.getInputStream())
        Logger.info(json.errors.message);
        break;
    }

    return repositoryList;
  }

  public static void createRepository(URL bitbucketcloudUrl, String username, String password, String projectKey, String repoName){

    JsonSlurper jsonSlurper = new JsonSlurper();
    URL url = new URL(bitbucketcloudUrl, "2.0/repositories/" + projectKey + "/" + repoName);
    def auth = "${username}:${password}".bytes.encodeBase64().toString();
    def body =  JsonOutput.toJson(["scm": "git"])
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

    DataOutputStream wr = new DataOutputStream( http.getOutputStream())
    wr.write( postData );
    wr.flush();
    wr.close();
	println("POST Sent");
	switch (http.getResponseCode()) {
		case 201:
			println("Repository created in Bitbucketcloud : " + projectKey + "/" + repoName + " "  + body);
			break;
		case 404:
			throw new IOException("URI not found: " + bitbucketcloudUrl.toString() + url + body);
			break;
		default:
			//def json = jsonSlurper.parse(http.getInputStream());
			//println(json.errors.message);
			println(http.getResponseMessage());
			break;
	}
  }
}
