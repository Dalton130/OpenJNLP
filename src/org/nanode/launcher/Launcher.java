/*
 * The contents of this file are subject to the Mozilla Public License Version 1.1 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.mozilla.org/MPL/ 
 *
 * Software distributed under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
 * ANY KIND, either express or implied. See the License for the specific language governing rights and
 * limitations under the License.
 *
 * The Original Code is openjnlp.nanode.org code.
 *
 * The Initial Developer of the Original Code is Nanode LLC. Portions created by Nanode are
 * Copyright (C) 2001-2002 Nanode LLC. All Rights Reserved.
 *
 * Contributor(s):
 *    Kevin Herrboldt <kevin@nanode.org>
 */
package org.nanode.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.nanode.launcher.cache.CacheEntry;
import org.nanode.launcher.cache.FileCache;
import org.nanode.launcher.cache.FileCacheEntry;
import org.nanode.launcher.cache.FileCachedResource;


/**
 * This class provides a mechanism for managing and invoking cached resources.
 *
 * @author Kevin Herrboldt (kevin@nanode.org)
 */
public class Launcher {
    private static Version launcherVersion;

    static {
        configEnvironment();
    }
    
    private static class OSSetting {
       public String sOSName;
       public String sArch;
       public String sLibPathSettingCommand;
       public String sPreCommand;
       public String sPreCommand2;       
       public String sPostCommand;
       
       public OSSetting(String sOSName, 
                        String sArch, 
                        String sLibPathSettingCommand, 
                        String sPreCommand,
                        String sPreCommand2,
                        String sPostCommand)
       {
          this.sOSName = sOSName;
          this.sArch = sArch;
          this.sLibPathSettingCommand = sLibPathSettingCommand;
          this.sPreCommand = sPreCommand;
          this.sPreCommand2 = sPreCommand2;          
          this.sPostCommand = sPostCommand;
       }
    }
    private OSSetting[] oSSettings = {
             new OSSetting("win", "x86", null, null, null, null),
             new OSSetting("aix", "power", "LIBPATH={0};export LIBPATH;", "sh", "-c", null),
             new OSSetting("aix", "ppc", "LIBPATH={0}:$LIBPATH;export LIBPATH;", "sh", "-c", null),                
             new OSSetting("solaris", "sparc", "LD_LIBRARY_PATH={0}:$LD_LIBRARY_PATH;export LD_LIBRARY_PATH;", "sh",  "-c", null),
             new OSSetting("sunos", "sparc", "LD_LIBRARY_PATH={0}:$LD_LIBRARY_PATH;export LD_LIBRARY_PATH;", "sh", "-c", null),                
             new OSSetting("hp-ux", "pa-risc", "chmod -R +x {0};SHLIB_PATH={0};export SHLIB_PATH;", "sh", "-c", null),
             new OSSetting("hp-ux", "pa_risc", "chmod -R +x {0};SHLIB_PATH={0};export SHLIB_PATH;", "sh", "-c", null),
             new OSSetting("linux", "x86", "LD_LIBRARY_PATH={0}:$LD_LIBRARY_PATH;export LD_LIBRARY_PATH;", "sh", "-c", null),
             new OSSetting("linux", "i386", "LD_LIBRARY_PATH={0}:$LD_LIBRARY_PATH;export LD_LIBRARY_PATH;", "sh", "-c", null),                
          };

    public static final String LAUNCH_CLASSDIR = "classdir";
    public static final String LAUNCH_COMMAND = "cmd";
    public static final String LAUNCH_MAIN = "main";

    private static final String LAUNCHER_DEFAULTS = "/lib/launcher.properties";
    private static final String LAUNCH_PREFIX = "launch.";

    private static final String VERSION_FILE = "version.txt";

    private static ThreadGroup launchGroup;			// all launched apps are in this group
    private static Properties launchProperties;		// properties for launching externally
    private static File launchDir;					// where launching jars are for external launching
    private static Logger defaultLogger;
    private static Logger logger;

    private static boolean launcherUpToDate = false;


    Descriptor launchDescriptor;
    String[] cmdArray;


    // this is a singleton, don't let it be instantiated
    private Launcher(Descriptor des) {
        launchDescriptor = des;

        defineCommand();
    }

    private void defineCommand() {
        String key = Gestalt.platformKey();
        String propCmd = LAUNCH_PREFIX + key + "." + LAUNCH_COMMAND;
        String propMain = LAUNCH_PREFIX + key + "." + LAUNCH_MAIN;

        List cmd = new ArrayList();
        cmd.add(launchProperties.getProperty(propCmd));		// every platform starts with the command

        switch (Gestalt.osPlatform()) {
            case Gestalt.OSPLATFORM_MACOSX:
                // Mac OS X launch adds special -X flags for integrating with the Finder.
                cmd.add("-Xdock:name=" + launchDescriptor.getInformation().getTitle());
                cmd.add("-Xdock:icon=Resources/jnlp.icns");			// XXX kdh - default, should be app-specific

                break;
            default:
                // default launching for non-special case platforms
                break;
        }

        cmd.add("-Djava.ext.dirs=" + launchDir);
        cmd.add(launchProperties.getProperty(propMain));
        cmd.add("-internal");
        cmd.add(launchDescriptor.getSource().getURL().toString());

        cmdArray = (String[]) cmd.toArray(new String[0]);
    }

    
    private OSSetting getOSSetting()
   {
      String sOSName = System.getProperty("os.name", "unknown").toLowerCase();
      String sArchName = System.getProperty("os.arch", "unknown").toLowerCase();
      
      System.out.println("Operating system: " + sOSName + "/" + sArchName);
      for (int i = 0; i < oSSettings.length; i++)
      {
         if ((sOSName.startsWith(oSSettings[i].sOSName))
                  && (sArchName.startsWith(oSSettings[i].sArch)))
         {
            return (oSSettings[i]);
         }
      }
      return (null);
   }
    /**
     * 
     * @param setLibPath [in] if true, the system library path is set to the jnlp library directory
     * 
     * @throws IOException
     */
    private void execute(boolean setLibPath)
            throws IOException
   {
       
      Process proc = null;

      if (setLibPath)
      {
         StringBuffer sbCmd = new StringBuffer();

         OSSetting oSSpecs = getOSSetting();

         CacheEntry cacheEntry = launchDescriptor.cache
                  .entryFromDescriptorURL(launchDescriptor.getSource().getURL());

         FileCacheEntry fc = (FileCacheEntry) cacheEntry;

         if ((null != oSSpecs) && (null != oSSpecs.sLibPathSettingCommand))
         {
            sbCmd.append(MessageFormat.format(oSSpecs.sLibPathSettingCommand,
                     new Object[] { fc.getLibraryDir() }));
         }

         for (int i = 0; i < cmdArray.length; i++)
         {
            if (0 != i)
            {
               sbCmd.append(" ");
            }
            sbCmd.append(cmdArray[i]);
         }

         String[] asExec = getOSExecArray(oSSpecs, sbCmd.toString());

//         System.out.print("Cmd: ");
//         for (int i = 0; i < asExec.length; i++)
//         {
//            System.out.print(asExec[i] + " ");
//         }
//         System.out.println();
         proc = Runtime.getRuntime().exec(asExec, null,
                  new File(System.getProperty("user.dir")));
      }
      else
      {
         proc = Runtime.getRuntime().exec(cmdArray, null,
                  new File(System.getProperty("user.dir")));
      }

      AppContext appContext = new AppContext(launchDescriptor, proc);

      ThreadGroup appGroup = new ThreadGroup(getLaunchGroup(), launchDescriptor
               .getInformation().getDefaultTitle());
      //appGroup.setDaemon(true);

      Thread appThread = new Thread(appGroup, appContext, "main");
      appThread.start();
   }

   private String[] getOSExecArray(OSSetting oSSpecs, String sCmd)
   {
      if (null != oSSpecs)
      {
         if (null == oSSpecs.sPostCommand)
         {
            if (null != oSSpecs.sPreCommand)
            {
               if (null != oSSpecs.sPreCommand2)
               {
                  return (new String[] { oSSpecs.sPreCommand,
                           oSSpecs.sPreCommand2, sCmd });
               }
               else
               {
                  return (new String[] { oSSpecs.sPreCommand, sCmd });
               }
            }
         }
         else
         {
            if (null != oSSpecs.sPreCommand)
            {
               if (null != oSSpecs.sPreCommand2)
               {
                  return (new String[] { oSSpecs.sPreCommand,
                           oSSpecs.sPreCommand2, sCmd, oSSpecs.sPostCommand });
               }
               else
               {
                  return (new String[] { oSSpecs.sPreCommand, sCmd,
                           oSSpecs.sPostCommand });
               }
            }
            else
            {
               return (new String[] { sCmd, oSSpecs.sPostCommand });
            }

         }
      }
      return (new String[] { sCmd });
   }

   public static void setLaunchProperty(String prop, String value) {
        launchProperties.setProperty(LAUNCH_PREFIX + Gestalt.platformKey() + "." + prop, value);
    }

    private static void configEnvironment() {
        defaultLogger = new Logger();
        setLogger(defaultLogger);

        Properties defProps = new Properties();
        InputStream defStream = Launcher.class.getResourceAsStream(LAUNCHER_DEFAULTS);

        if (defStream != null) {
            try {
                defProps.load(defStream);
                defStream.close();
            } catch (IOException e) {
                // not a big deal if exception occurs
            }
        }

        // define launcher version
        launcherVersion = new Version(defProps.getProperty("launcher.version"));
        
        // create launchProperties with defaults
        launchProperties = new Properties(defProps);

        // special check, if on unix determine launch command based on java.home
        if (Gestalt.platformKey().equals(Gestalt.KEY_UNIX)) {
            File cmd = new File(System.getProperty("java.home"));
            cmd = new File(cmd, "bin");
            cmd = new File(cmd, "java");

            launchProperties.put(LAUNCH_PREFIX + Gestalt.platformKey() + "." + LAUNCH_COMMAND, cmd.toString());
        }
    }

    private static ThreadGroup getLaunchGroup() {
        // make sure the app thread group exists
        if (launchGroup == null) {
            synchronized (Launcher.class) {
                if (launchGroup == null) {
                    launchGroup = new ThreadGroup("jnlpApps");
                }
            }
        }

        return launchGroup;
    }

    private static synchronized boolean updateLauncher() {
        if (launcherUpToDate) {
            return true;
        }

        // create sorted list of launcher files
        ArrayList list = new ArrayList();

        for (StringTokenizer st = new StringTokenizer(launchProperties.getProperty("launcher.files"), ","); st.hasMoreTokens();) {
            list.add(st.nextToken());
        }

        String[] names = (String[]) list.toArray(new String[0]);
        Arrays.sort(names);

        // locations of local and remote resource
        launchDir = new File(FileCache.defaultCacheDirectory(), "launcher");
        URL codebase = null;

        // determine codebase as either from file system (OpenJNLP running in distributed app) or network
        File libDir = new File(launchProperties.getProperty(LAUNCH_PREFIX + Gestalt.platformKey() + "." + LAUNCH_CLASSDIR));
        if (libDir.isDirectory()) {
            try {
                codebase = libDir.toURL();

                System.out.print("Updating external launcher");
                System.out.flush();
            } catch (MalformedURLException e) {
                System.err.println("malformed library dir URL: " + e.getMessage());
            }
        }

        if (codebase == null) {
            try {
                codebase = new URL(launchProperties.getProperty("launcher.codebase"));

                System.out.print("Updating external launcher from " + codebase.getHost());
                System.out.flush();
            } catch (MalformedURLException e) {
                System.err.println("launcher codebase is malformed: " + e.getMessage());
            }
        }

        if (codebase == null) {
            return false;				// failed to update launcher
        }

        // delete files not part of the launcher (if any)
        File[] files = launchDir.listFiles();

        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (Arrays.binarySearch(names, files[i].getName()) < 0) {
                    files[i].delete();
                }
            }
        }

        // update each cached resource for
        for (int i = 0; i < names.length; i++) {
            try {
                Reference ref = new Reference(new URL(codebase, names[i]));
                boolean stat = new FileCachedResource(ref, launchDir, null).update();

                System.out.print(".");
                System.out.flush();
            } catch (MalformedURLException e) {
                System.err.println("malformed launcher url: " + e.getMessage());
            }
        }

        System.out.println("updated");

        return (launcherUpToDate = true);
    }

    public static Version getVersion() {
        return launcherVersion;
    }

    /**
     * same as launchExternal(des, false);
     * @param des
     */
    public static void launchExternal(Descriptor des) {
       launchExternal(des, false);
    }
    
    /**
     * Launches a descriptor externally (a separate virtual machine). If the <code>Descriptor</code>
     * is null or does not have a defined source, no external virtual machine is started.
     * <p>
     * When launching in a separate virtual machine, the i/o streams from the <code>Process</code>
     * will be tied to an AppContext in the current virtual machine.
     * 
     * @param setLibPath [in] if true, the system library path is set to the jnlp library directory 
     */
    public static void launchExternal(Descriptor des, boolean setLibPath) {
        if (des == null) {
            return;
        }

        updateLauncher();		// make sure external launcher is up-to-date

        Launcher launcher = new Launcher(des);

        try {
            launcher.execute(setLibPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Launches a descriptor within the current virtual machine. If the <code>Descriptor</code>
     * is null or does not have a defined descriptor, nothing will be done.
     * <p>
     * This method creates the app context within the virtual machine. An app context currently is
     * defined as a classloader with a main thread within the group.
     * <p>
     * Launching depends on whatever eager resources are in the cache. They will not be updated
     * before launching. The classloader defines its own policy on updating lazy resources.
     *
     * @param des a launchable descriptor
     * @param setLibPath [in] if true, the system library path is set to the jnlp library directory   
     */
    public static void launchInternal(Descriptor des, boolean setLibPath) {
        // if nothing defined, no launching is possible
        if (des == null) {
            return;         // can't launch if undefined
        }

        AppContext appContext = new AppContext(des, null);

        ThreadGroup appGroup = new ThreadGroup(getLaunchGroup(), des.getInformation().getDefaultTitle());
        appGroup.setDaemon(true);

        Thread appThread = new Thread(appGroup, appContext, "main");
        appThread.setContextClassLoader(appContext.getAppLoader());
        appThread.start();
    }

    public static void logErr(Descriptor des, String msg) {
        logger.logErr(des, msg);
    }

    public static void logOut(Descriptor des, String msg) {
        logger.logOut(des, msg);
    }

    public static void setLogger(Logger l) {
        logger = (l != null) ? l : defaultLogger;
    }


    public static class Logger {
        PrintWriter stdout;
        PrintWriter stderr;


        protected Logger() {
            this(new PrintWriter(System.out), new PrintWriter(System.err));
        }

        protected Logger(PrintWriter out, PrintWriter err) {
            stdout = out;
            stderr = err;
        }

        public synchronized void logErr(Descriptor des, String msg) {
            if (des != null) {
                stderr.print("[");

                if (des.getInformation() != null) {
                    stderr.print(des.getInformation().getTitle());
                }

                stderr.print("] ");
            }

            stderr.println(msg);
        }

        public synchronized void logOut(Descriptor des, String msg) {
            if (des != null) {
                stdout.print("[");

                if (des.getInformation() != null) {
                    stdout.print(des.getInformation().getTitle());
                }

                stdout.print("] ");
            }

            stdout.println(msg);
        }
    }
}
