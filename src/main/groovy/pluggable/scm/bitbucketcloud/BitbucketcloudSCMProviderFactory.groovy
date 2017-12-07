package pluggable.scm.bitbucketcloud;

import pluggable.scm.SCMProvider;
import pluggable.scm.SCMProviderFactory;
import pluggable.scm.SCMProviderInfo;

/**
* The Bitbucketcloud SCM factory class is responsible for parsing the
* providers properties and instantiating a BitbucketcloudSCMProvider.
*/
@SCMProviderInfo(type="bitbucketcloud")
public class BitbucketcloudSCMProviderFactory implements SCMProviderFactory {

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

    // Env variables for Bitbucketcloud REST
    String bitbucketcloudEndpoint = scmProviderProperties.getProperty("bitbucketcloud.endpoint");
    String bitbucketcloudURL = scmProviderProperties.getProperty("bitbucketcloud.url");
    String bitbucketcloudEndpointContext = scmProviderProperties.getProperty("bitbucketcloud.endpoint.context");
    String bitbucketcloudProtocol = scmProviderProperties.getProperty("bitbucketcloud.protocol");
    int bitbucketcloudPort = Integer.parseInt(scmProviderProperties.getProperty("bitbucketcloud.port"));

    return new BitbucketcloudSCMProvider(
            scmPort,
            BitbucketcloudSCMProtocol.valueOf(this.validateProperties("scm.protocol", scmProtocol.toUpperCase())),
            this.validateProperties("bitbucketcloud.endpoint", bitbucketcloudEndpoint),
            this.validateProperties("bitbucketcloud.url", bitbucketcloudURL),
            bitbucketcloudEndpointContext,
            BitbucketcloudSCMProtocol.valueOf(this.validateProperties("bitbucketcloud.protocol", bitbucketcloudProtocol.toUpperCase())),
            bitbucketcloudPort
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
