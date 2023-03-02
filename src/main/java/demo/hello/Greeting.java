package demo.hello;

public class Greeting {
  public String getMessage() {
    return message;
  }

  public void setMessage(final String pMessage) {
    message = pMessage;
  }

  private String message;

  public Greeting() {}

  public Greeting(final String pMessage) {
    this.message = pMessage;
  }

  public String toString() {
    return "Greeting: {" +
        "message: '" + this.message + "'" +
        "}";
  }


}
