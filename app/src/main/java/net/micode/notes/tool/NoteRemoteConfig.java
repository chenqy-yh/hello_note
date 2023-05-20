package net.micode.notes.tool;

public class NoteRemoteConfig {

    //private
//    public static final String VERIFICATION_HOST = "192.168.206.123";
    public static final String VERIFICATION_HOST = "120.26.58.94";
    private static final int VERIFICATION_PORT = 3000;
    private static final String PROTOCOL = "http";
    private static final String GLOBAL_PREFIX = "/api";
    private static final String verification_url = PROTOCOL + "://" + VERIFICATION_HOST + ":" + VERIFICATION_PORT+ GLOBAL_PREFIX;


    //public
    public static final int RESPONSE_SUCCESS = 200;
    public static final int RESPONSE_FAIL = 400;

    public static String generateUrl(String endpoint) {
        return verification_url + endpoint;
    }

}
