package pluggable.scm.bitbucketcloud;

/**
* Set of named constants representing the supported Gerrit SCM protocols.
*/
enum BitbucketcloudSCMProtocol {
    SSH("ssh"),
    HTTP("http"),
    HTTPS("https")

    private final String protocol = "";

    /**
    * Constructor for class GerritSCMProtocol.
    *
    * @param protocal a string representation of the protocol e.g. ssh, https
    */
    public BitbucketcloudSCMProtocol(String protocol) {
        this.protocol = protocol;
    }

    public static void isProtocolSupported(BitbucketcloudSCMProtocol protocol){
      switch(protocol){
        case BitbucketcloudSCMProtocol.SSH:
        case BitbucketcloudSCMProtocol.HTTP:
        case BitbucketcloudSCMProtocol.HTTPS:
          break;
        default:
          throw new IllegalArgumentException("SCM Protocol type not supported.");
          break;
      }
    }

    /**
    * Return a string representation of the SCM protocol.
    * @return a string representation of the SCM protocol.
    */
    @Override
    public String toString(){
      return this.protocol;
    }

}
