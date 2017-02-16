SET JAVA_ROOT=C:\Program Files\Java\jre1.8.0_73

::remove old
"%JAVA_ROOT%/bin/keytool" -delete -keystore "%JAVA_ROOT%/lib/security/cacerts" -storepass changeit -alias mkm_api_cert 	-file "D:/mkm_connector/cert/mkmapi.eu.crt"
"%JAVA_ROOT%/bin/keytool" -delete -keystore "%JAVA_ROOT%/lib/security/cacerts" -storepass changeit -alias mkm_cert 	-file "D:/mkm_connector/cert/cert.magickartenmarkt.de.crt"

::import new
"%JAVA_ROOT%/bin/keytool" -importcert -trustcacerts -keystore "%JAVA_ROOT%/lib/security/cacerts" -storepass changeit -alias mkm_api_cert 	-file "D:/mkm_connector/cert/mkmapi.eu.crt"
"%JAVA_ROOT%/bin/keytool" -importcert -trustcacerts -keystore "%JAVA_ROOT%/lib/security/cacerts" -storepass changeit -alias mkm_cert 	-file "D:/mkm_connector/cert/cert.magickartenmarkt.de.crt"

@pause