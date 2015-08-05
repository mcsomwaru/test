package gy.gov.gra.view.example.oauth;

/**
 * Verification code receiver.
 *
 * @author Yaniv Inbar
 */
public interface VerificationCodeReceiver {

  /** Returns the redirect URI. */
  String getRedirectUri() throws Exception;

  /** Waits for a verification code. */
  String waitForCode();

  /** Releases any resources and stops any processes started. */
  void stop() throws Exception;
}
