/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tools;
import java.io.FileWriter;
import java.io.File;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
/**
 *
 * @author Simon
 */
public class PrimitiveLogger {

    public synchronized static void log(String filename, String message)
    {
        try {
            File file = new File(filename);
            file.createNewFile(); //only creates if it's not there already
            FileWriter fstream = new FileWriter(filename, true);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(message + "\n");
            out.close();
            fstream.close();
            //cm.dispose();
        }   catch (Exception e) {
            e.printStackTrace(); //try -> catch it! if it's just caught normally then it'll be logged during this... infinite loop
        }
    }

    public static void logScriptTimeout(String path)
    {
        log("faultyscripts.log", "Script timeout on NPC " + path + ".");
    }

    public static void logException(Exception e)
    {
       log("exceptions.log", getStackTrace(e));
    }

    public static void logException(Throwable t)
    {
       log("exceptions.log", getStackTrace(t));
    }

    public static void logExceptionCustomName(String name, Throwable t)
    {
       log(name, getStackTrace(t));
    }

    public static void logNPCException(String NPC, Exception e)
    {
        log("npcexceptions.log", "NPC Script error with NPC " + NPC + ": \n " + getStackTrace(e));
    }

    public static void logCommandException(String command, Exception e)
    {
        log("commandexceptions.log", "Command error with command " + command + ": \n " + getStackTrace(e));
    }

    public static void logClientError(int errorNum, byte[] faultyPacket)
    {
        log("clienterrors.log", "Client error code: " + errorNum + "\nData: " + byteArrayToString(faultyPacket));
    }

    private static String byteArrayToString(byte[] toConvert)
    {
        StringBuilder res = new StringBuilder();
        for (byte B : toConvert)
        {
            res.append(Byte.toString(B));
        }
        return res.toString();
    }


    private static String getStackTrace(Exception e)
    {
        try
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw, true);
            e.printStackTrace(pw);
            pw.flush();
            sw.flush();
            pw.close();
            sw.close();
            return sw.toString();
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    private static String getStackTrace(Throwable t)
    {
        try
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw, true);
            t.printStackTrace(pw);
            pw.flush();
            sw.flush();
            pw.close();
            sw.close();
            return sw.toString();
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

}
