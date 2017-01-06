
package pluggable.configuration;

import java.util.regex.*;

/**
* A singleton class representing a EnvVarProperty object.
* This class is responsible for storing injected/binded
* environment variables to be used by the SCM pluggable library.
*/
public class EnvVarProperty {

  private def bindings = null;

  private static final Object lock = new Object();
  private static EnvVarProperty singleton = null;

  /**
  * A singleton method for the EnvVarProperty object.
  * @return an instance of an EnvVarProperty.
  */
  public static EnvVarProperty getInstance(){
    if(EnvVarProperty.singleton == null){
      synchronized(lock) {
        EnvVarProperty.singleton = new EnvVarProperty();
      }
    }

    return EnvVarProperty.singleton;
  }

  /**
  * Provate constructor for class EnvVarProperty as it's a singleton.
  */
  private EnvVarProperty(){ }

  /**
  * A setter method used to inject variables to store.
  *
  * @param variables - variables to store.
  */
  public void setVariableBindings(def variables){

      if (variables == null){
          throw new IllegalArgumentException("Binding variables cannot be null.");
      }

      this.bindings = variables;
  }

  /**
  * Return a string representation of the SCM provider's pluggable search
  * path.
  *
  * @return a string representation of the SCM provider's pluggable search
  * path.
  */
  public String getPluggablePath(){

      if (!this.hasProperty("SCM_PROVIDER_PLUGGABLE_PATH"))
        throw new IllegalArgumentException(
          "Property SCM_PROVIDER_PLUGGABLE_PATH does not exist.");


      if(!this.checkDirectoryExists(this.bindings.SCM_PROVIDER_PLUGGABLE_PATH)){
        throw new IllegalArgumentException(
          "Invalid environment variable - SCM_PROVIDER_PLUGGABLE_PATH must be a valid directory.");
      }

      return this.bindings.SCM_PROVIDER_PLUGGABLE_PATH
  }

  /**
  * Return a string representation of the SCM provider's properties file search
  * path.
  *
  * @return a string representation of the SCM provider's properties file search
  * path.
  */
  public String getPropertiesPath(){

      if (!this.hasProperty("SCM_PROVIDER_PROPERTIES_PATH"))
      throw new IllegalArgumentException(
        "Property SCM_PROVIDER_PROPERTIES_PATH does not exist.");

      if(!this.checkDirectoryExists(this.bindings.SCM_PROVIDER_PROPERTIES_PATH)){
        throw new IllegalArgumentException(
          "Invalid environment variable - SCM_PROVIDER_PROPERTIES_PATH value must be a valid directory.");
      }

      return this.bindings.SCM_PROVIDER_PROPERTIES_PATH;
  }

  /**
  * Returns a string where all environment variable keys pre-pended with "$" are replaced
  * with their literal values
  *
  * @return a string with environment variable keys replaced with values.
  */
  public String returnValue(String keyString){

      String intermediateString = keyString;

      String pattern1 = '${';
      String pattern2 = '}';
      String pattern3 = '$';

      Pattern p1 = Pattern.compile(Pattern.quote(pattern1) + "([A-Za-z0-9_-]+)" + Pattern.quote(pattern2));
      Matcher m1 = p1.matcher(intermediateString);
      if (m1.find()) {
        String temp = this.bindings.get(m1.group(1))
        intermediateString = intermediateString.replace(pattern1 + m1.group(1) + pattern2, temp)
      }

      String tokenisedString = intermediateString;

      Pattern p2 = Pattern.compile(Pattern.quote(pattern3) + "([A-Za-z0-9_-]+)");
      Matcher m2 = p2.matcher(tokenisedString);
      if (m2.find()) {
        String temp = this.bindings.get(m2.group(1))
        tokenisedString = tokenisedString.replace(pattern3 + m2.group(1), temp)
      }

      return tokenisedString;
  }

  /**
  * Return true if the env var property exists, else false if bindings is null
  * or the property does not exist.
  *
  * @param propertyName - the name of the property to check exists.
  * @return true if the env var property exists, else false if bindings is null
  * or the property does not exist.
  */
  public boolean hasProperty(String propertyName){
    return this.bindings != null && this.bindings.containsKey(propertyName)
  }

  /**
  * Return STDOUT logger.
  *
  * @return STDOUT logger.
  */
  public PrintStream getLogger(){
    return this.bindings.out;
  }

  /**
  * Return true if the specified path is a directory and exists.
  *
  * @param directoryPath - the file path to the directory.
  * @return true if path is a directory and exists.
  */
  private boolean checkDirectoryExists(String directoryPath){
      File directory = new File(directoryPath);

      if(directory.exists() && directory.isDirectory()) {
          return true;
      }else{
          return false;
      }
  }
}
