/**
 * 
 */
package mkm.install;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Updates the installer file
 *
 * @author Kenny
 * @since 10.04.2016
 */
public class ConfigureInstaller
{

	public static void udpateInstallFile() throws IOException
	{
		String newLine = "\r\n";

		File javaBinDir = new File(System.getProperty("java.library.path").split(System.getProperty("path.separator"))[0]);

		System.out.println("Searching library path for java/bin dir: ");
		if (!javaBinDir.getAbsolutePath().endsWith("bin"))
		{
			for (String s : System.getProperty("java.library.path").split(System.getProperty("path.separator")))
			{
				javaBinDir = new File(s);

				if (javaBinDir.getAbsolutePath().endsWith("bin") && javaBinDir.getAbsolutePath().toLowerCase().indexOf("sun") == -1
						&& javaBinDir.getAbsolutePath().toLowerCase().indexOf("java") > -1)
				{
					break;
				}
				else
				{
					System.out.println("skippuing: " + javaBinDir.getAbsolutePath());
				}
			}
		}

		System.out.println("javaBinDir: " + javaBinDir);

		if (javaBinDir.exists())
		{
			String javaCert = javaBinDir.getParentFile().getAbsolutePath().replace('\\', '/');

			String progRoot = new File("").getAbsolutePath().replace('\\', '/');

			File installFileSource = new File("install_cert.bat");
			File installFileTarget = new File("install_cert_modified.bat");

			if (installFileSource.exists())
			{
				if (installFileTarget.exists())
				{
					installFileTarget.delete();
				}

				installFileTarget.createNewFile();

				RandomAccessFile source = null, target = null;
				try
				{
					source = new RandomAccessFile(installFileSource, "rw");
					target = new RandomAccessFile(installFileTarget, "rw");

					String line = source.readLine();
					while (line != null)
					{
						if (line.startsWith("SET JAVA_ROOT="))
						{
							target.write(("SET JAVA_ROOT=" + javaCert + newLine).getBytes());
						}
						else
						{
							target.write((line.replace("D:/mkm_connector", progRoot) + newLine).getBytes());
						}

						line = source.readLine();
					}

					System.out.println("successfully written : " + installFileTarget.getAbsolutePath());
					System.out.println("run this file in mode to install the mkm-certificate.");
					System.out.println("ADMIN MODE!!! ADMIN");
				}
				finally
				{
					if (source != null)
						source.close();
					if (target != null)
						target.close();
				}
			}
		}
	}

}
