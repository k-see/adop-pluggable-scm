package pluggable.scm.bitbucketcloud;

import pluggable.scm.SCMProvider;
import pluggable.configuration.EnvVarProperty;
import pluggable.scm.helpers.*;
import java.util.Properties;
import java.net.URL;

/**
* This class implements the Bitbucketcloud SCM Provider.
*/
public class BitbucketcloudSCMProvider implements SCMProvider {


  // SCM specific variables.
  private final int scmPort;
  private final BitbucketcloudSCMProtocol scmProtocol;

  // Bitbucketcloud specific variables.
  private final String bitbucketcloudEndpoint;
  private final String bitbucketcloudEndpointContext;
  private final int bitbucketcloudPort;
  private final BitbucketcloudSCMProtocol bitbucketcloudProtocol;
  private String bitbucketcloudUsername;
  private String bitbucketcloudPassword;

  /**
  * Constructor for class BitbucketcloudSCMProvider.
  *
  * @param scmPort scm port
  * @param scmProtocol scm clone protocol
  * @param bitbucketcloudEndpoint host url e.g. 10.0.0.1, innersource.accenture.com
  * @param bitbucketcloudEndpointContext bitbucketcloud host endpoint context.
  * @param bitbucketcloudProtocol protocol which will be used for HTTP requests.
  * @param bitbucketcloudPort bitbucketcloud API port.
  */
  public BitbucketcloudSCMProvider(int scmPort,
                              BitbucketcloudSCMProtocol scmProtocol,
                              String bitbucketcloudEndpoint,
                              String bitbucketcloudEndpointContext,
                              BitbucketcloudSCMProtocol bitbucketcloudProtocol,
                              int bitbucketcloudPort){

      this.scmPort = scmPort;
      this.scmProtocol = scmProtocol;
      this.bitbucketcloudEndpoint = bitbucketcloudEndpoint;
      this.bitbucketcloudEndpointContext = bitbucketcloudEndpointContext;
      this.bitbucketcloudPort = bitbucketcloudPort;
      this.bitbucketcloudProtocol = bitbucketcloudProtocol;

      // If not it will thorw IllegalArgumentException.
      BitbucketcloudSCMProtocol.isProtocolSupported(this.bitbucketcloudProtocol);
  }

  /**
  * Return Bitbucketcloud SCM URL.
  * @return SCM url for the provider.
  *     e.g. Bitbucketcloud-SSH  ssh://git@10.0.0.0:22/
  *          Bitbucketcloud-HTTP http://10.0.0.0:80/scm/
  *          Bitbucketcloud-HTTPS http://10.0.0.0:443/scm/
  *
  * @throws IllegalArgumentException
  *           If the SCM protocol type is not supported.
  **/
  public String getScmUrl(){

      StringBuffer url = new StringBuffer("")

      url.append(this.scmProtocol);
      url.append("://");
      switch(this.scmProtocol){
        case BitbucketcloudSCMProtocol.SSH:
          url.append("git@");
          break;
        case BitbucketcloudSCMProtocol.HTTP:
        case BitbucketcloudSCMProtocol.HTTPS:
          break;
        default:
          throw new IllegalArgumentException("SCM Protocol type not supported.");
          break;
      }

      url.append(this.bitbucketcloudEndpoint);
      url.append(":");
      url.append(this.scmPort);
      url.append(this.bitbucketcloudEndpointContext);

      if(!url.toString().endsWith("/")){
        url.append("/")
      }

      if((this.scmProtocol == BitbucketcloudSCMProtocol.HTTP) ||
         (this.scmProtocol == BitbucketcloudSCMProtocol.HTTPS)
      ){
        url.append("scm/");
      }

      return url;
  }

  /**
  * Return a url encoded value
  * @param value string to encode.
  * @return a url encoded value.
  */
  private String urlEncode(String value){
      return URLEncoder.encode(value)
  }

  /**
  * Creates relevant repositories defined by your cartridge in your chosen SCM provider
  * @param workspace Workspace of the cartridge loader job
  * @param repoNamespace Location in your SCM provider where your repositories will be created
  * @parma overwriteRepos
  **/
  public void createScmRepos(String workspace, String repoNamespace, String codeReviewEnabled, String overwriteRepos) {

    ExecuteShellCommand com = new ExecuteShellCommand()

    String cartHome = "/cartridge"
    String urlsFile = workspace + cartHome + "/src/urls.txt"

    // Create repositories
    String command1 = "cat " + urlsFile
    List<String> repoList = new ArrayList<String>();
    repoList = (com.executeCommand(command1).split("\\r?\\n"));

    // remove null or empty lines
    repoList.removeAll(Arrays.asList(null,""))

    if(!repoList.isEmpty()){

      EnvVarProperty envVarProperty = EnvVarProperty.getInstance();
      String filePath =  envVarProperty.getProperty("WORKSPACE")+ "@tmp/secretFiles/" +envVarProperty.getProperty("SCM_KEY")
      Properties fileProperties = PropertyUtils.getFileProperties(filePath)

      this.bitbucketcloudUsername = fileProperties.getProperty("SCM_USERNAME");
      this.bitbucketcloudPassword = fileProperties.getProperty("SCM_PASSWORD");

      URL bitbucketcloudUrl = new URL(BitbucketcloudSCMProtocol.HTTPS.toString(), this.bitbucketcloudEndpoint, this.bitbucketcloudPort, this.bitbucketcloudEndpointContext);

      BitbucketcloudRequestUtil.isProjectAvailable(bitbucketcloudUrl, this.bitbucketcloudUsername, this.bitbucketcloudPassword, repoNamespace);

      for(String repo: repoList) {
          // check if empty line
          if(repo.equals("")){
            break;
          }

          String repoName = repo.substring(repo.lastIndexOf("/") + 1, repo.indexOf(".git"));
          String target_repo_name = repoNamespace + "/" + repoName
          int repo_exists=0;

          List<String> bitbucketcloudRepoList = BitbucketcloudRequestUtil.getProjectRepositorys(bitbucketcloudUrl, this.bitbucketcloudUsername, this.bitbucketcloudPassword, repoNamespace);
          for(String bitbucketcloudRepo: bitbucketcloudRepoList) {
            if(bitbucketcloudRepo.trim().contains(repoName)) {
               Logger.info("Found: " + target_repo_name);
               repo_exists=1
               break
            }
          }

          if (repo_exists == 0) {
            BitbucketcloudRequestUtil.createRepository(bitbucketcloudUrl, this.bitbucketcloudUsername, this.bitbucketcloudPassword, repoNamespace, repoName);
          } else{
            Logger.info("Repository already exists, skipping create: " + target_repo_name);
          }

          // Populate repository
          String tempDir = workspace + "/tmp"

          def tempScript = new File(tempDir + '/shell_script.sh')

          tempScript << "git clone " + BitbucketcloudSCMProtocol.HTTPS.toString() + "://" + this.bitbucketcloudUsername + ":" + this.urlEncode(this.bitbucketcloudPassword) + "@" + this.bitbucketcloudEndpoint + "/scm/" + repoNamespace + "/" + repoName + ".git " + tempDir + "/" + repoName + "\n"
          def gitDir = "--git-dir=" + tempDir + "/" + repoName + "/.git"
          tempScript << "git " + gitDir + " remote add source " + repo + "\n"
          tempScript << "git " + gitDir + " fetch source" + "\n"

          if (overwriteRepos == "true"){
            tempScript << "git " + gitDir + " push origin +refs/remotes/source/*:refs/heads/*\n"
            Logger.info("Repository already exists, overwriting: " + target_repo_name);
          } else {
            tempScript << "git " + gitDir + " push origin refs/remotes/source/*:refs/heads/*\n"
          }

          com.executeCommand('chmod +x ' + tempDir + '/git_ssh.sh')
          com.executeCommand('chmod +x ' + tempDir + '/shell_script.sh')
          com.executeCommand(tempDir + '/shell_script.sh')

          tempScript.delete()
      }
    }
  }

  /**
    Return SCM section.

    @param projectName - name of the project.
    @param repoName  - name of the repository to clone.
    @param branchName - name of branch.
    @param credentialId - name of the credential in the Jenkins credential
            manager to use.
    @param extras - extra closures to add to the SCM section.
    @return a closure representation of the SCM providers SCM section.
  **/
  public Closure get(String projectName, String repoName, String branchName, String credentialId, Closure extras){
    if(extras == null) extras = {}
        return {
            git extras >> {
              remote{
                url(this.getScmUrl() + projectName + "/" + repoName + ".git")
                credentials(credentialId)
              }
              branch(branchName)
            }
        }
    }

    /**
    * Return a closure representation of the SCM providers trigger SCM section.
    *
    * @param projectName - project name.
    * @param repoName - repository name.
    * @param branchName - branch name to trigger.
    * @return a closure representation of the SCM providers trigger SCM section.
    */
    public Closure trigger(String projectName, String repoName, String branchName) {
        return {
              bitbucketcloudPush()
              scm('')
        }
    }
}
