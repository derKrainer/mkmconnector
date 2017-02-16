SET JAVA_ROOT=C:/Program Files/Java/jdk1.8.0_20/jre

::remove old
"%JAVA_ROOT%/bin/keytool" -delete -keystore "%JAVA_ROOT%/lib/security/cacerts" -storepass changeit -alias mkm_api_cert 	-file "E:/eclipseWorkspace/MkmConnector/cert/mkmapi.eu.crt"
"%JAVA_ROOT%/bin/keytool" -delete -keystore "%JAVA_ROOT%/lib/security/cacerts" -storepass changeit -alias mkm_cert 	-file "E:/eclipseWorkspace/MkmConnector/cert/cert.magickartenmarkt.de.crt"

::import new
"%JAVA_ROOT%/bin/keytool" -importcert -trustcacerts -keystore "%JAVA_ROOT%/lib/security/cacerts" -storepass changeit -alias mkm_api_cert 	-file "E:/eclipseWorkspace/MkmConnector/cert/mkmapi.eu.crt"
"%JAVA_ROOT%/bin/keytool" -importcert -trustcacerts -keystore "%JAVA_ROOT%/lib/security/cacerts" -storepass changeit -alias mkm_cert 	-file "E:/eclipseWorkspace/MkmConnector/cert/cert.magickartenmarkt.de.crt"

@pause
