### Things to remember while setting up KeyStore
* The server must use VM settings: 
    * `-Djavax.net.ssl.keyStore=$MODULE_DIR$/../../keystore/serverkeystore.jks`
    * `-Djavax.net.ssl.keyStorePassword=password`
* The client must use VM settings:
    * `-Djavax.net.ssl.trustStore=$MODULE_DIR$/../../keystore/clienttruststore.jks`
    * `-Djavax.net.ssl.trustStorePassword=password`

Follow this guide: https://www.baeldung.com/java-ssl-handshake-failures.