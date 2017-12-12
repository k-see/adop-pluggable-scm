package pluggable.scm.github;

import pluggable.scm.SCMProvider;
import pluggable.scm.SCMProviderFactory;
import pluggable.scm.SCMProviderInfo;

/**
* The github SCM factory class is responsible for parsing the
* providers properties and instantiating a githubSCMProvider.
*/
@SCMProviderInfo(type="github")
public class GithubSCMProviderFactory implements SCMProviderFactory {

  /**
  * A factory method which return an SCM Provider instantiated with the
  * the provided properties.
  *
  * @param scmProviderProperties - properties for the SCM provider.
  * @return SCMProvider configured from the provided SCM properties.
  **/
  public SCMProvider create(Properties scmProviderProperties){
    String scmProtocol = scmProviderProperties.getProperty("scm.protocol");
    int scmPort = Integer.parseInt(scmProviderProperties.getProperty("scm.port"));

    // Env variables for github REST
    String githubEndpoint = scmProviderProperties.getProperty("github.endpoint");
    String githubURL = scmProviderProperties.getProperty("github.url");
    String githubEndpointContext = scmProviderProperties.getProperty("github.endpoint.context");
    String githubProtocol = scmProviderProperties.getProperty("github.protocol");
    int githubPort = Integer.parseInt(scmProviderProperties.getProperty("github.port"));

    return new GithubSCMProvider(
            scmPort,
            GithubSCMProtocol.valueOf(this.validateProperties("scm.protocol", scmProtocol.toUpperCase())),
            this.validateProperties("github.endpoint", githubEndpoint),
            this.validateProperties("github.url", githubURL),
            githubEndpointContext,
            GithubSCMProtocol.valueOf(this.validateProperties("github.protocol", githubProtocol.toUpperCase())),
            githubPort
    );
  }

  /**
  * Return valid value.
  * @param key
  * @param value
  * @return Valid value
  * @throw IllegalArgumentException
  *           If the value ir null or empty without those params scripts can't work.
  */
  public String validateProperties(String key, String value){
    if(value == null || value.equals("")){
        throw new IllegalArgumentException("Please make sure " + key + " exist and have valid value.");
    }
    return value;
  }
}
