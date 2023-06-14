/*
 *   This File is part of the Colliery_source Project from PROSLabTeam
 *   https://bitbucket.org/proslabteam/colliery_source/src/master/
 *   Thanks to Lorenzo Rossi for granting permissions
 * */


package discovery.ProcessDiscovery.it.unicam.pros.colliery.core;


import org.apache.commons.lang3.SystemUtils;

import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class Pm4PyBridge {

    public static String[] py = new String[]{"python", "/usr/local/bin/python3"};
    private static String python = "";

    public static void checkScripts() throws ScriptException {
        checkPython();
        System.out.println("Python is correctly installed.\n");
        for (DiscoveryAlgorithm a : DiscoveryAlgorithm.values()) {
            File f = new File(getScript(a));
            if (!f.exists()) throw new ScriptException("Missing script file! Please check ./script folder.");
        }
        System.out.println("Scripts are present.\n");
    }

    private static void checkPython() throws ScriptException {
        boolean ok = false;
        for (int i = 0; i< py.length; i++){
            python = py[i];
            System.out.println("Try... "+python);
            try {
                String[] reply = executeScript((python+" -V"));
            }catch (ScriptException e){
               continue;
            }
            ok = true;
            break;
        }

        if (!ok) throw new ScriptException("Please install python as system variable!");
    }

    private static String[] executeScript(String script) throws ScriptException {
        Process process = null;
        try {
            Runtime r = Runtime.getRuntime();
            process = r.exec(script);
        } catch (IOException e) {
            System.out.println(e.toString());
            throw new ScriptException("script "+(script)+" can't be ran.");
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errinput = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String line = null;
        try {
            line = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String answer = "";
        while (line != null) {
            answer+= line;
            try {
                line = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        line = null;
        try {
            line = errinput.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String error = "";
        while (line != null) {
            error+= line;
            try {
                line = errinput.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new String[]{answer, error};

    }

    public static String discovery(String filePath, DiscoveryAlgorithm algo, Map<String, Object> params) throws ScriptException {
        String parameters = "";

        double ind_tres = params == null ? 0.2 : (double) params.get("ind_tres");
        double heu_tres = params == null ? 0.5 :(double) params.get("heu_tres");
        double heu_and_tres = params == null ? 0.65 :(double) params.get("heu_and_tres");
        double heu_loop_tres = params == null ? 0.5 :(double) params.get("heu_loop_tres");

        switch (algo){
            case ALPHA:
                break;
            case ALPHA_PLUS:
                break;
            case INDUCTIVE:
                parameters = String.valueOf(ind_tres);
                break;
            case HEURISTICS:
                parameters = heu_tres+" "+heu_and_tres+" "+heu_loop_tres;
                break;
        }

        String script = getScript(algo);

        String[] reply = executeScript((python+" "+script+" "+filePath+" "+parameters));

        String xml = reply[0];
        if (xml == "") throw new ScriptException(reply[1]);

        xml = xml.replace("\\n", "\n").replace("\\t", "\t");

        // produce result for the program.
        return xml.substring(2,xml.length()-1);
    }

    private static String getScript(DiscoveryAlgorithm algo) {
        String script = "";
        if(SystemUtils.IS_OS_LINUX) script = System.getProperty("user.dir")+"/script/";
        if(SystemUtils.IS_OS_WINDOWS) script = System.getProperty("user.dir")+"\\script\\";
        if(SystemUtils.IS_OS_MAC) script = System.getProperty("user.dir")+"/script/";
        switch (algo){
            case ALPHA:
                script += "alpha.py";
                break;
            case ALPHA_PLUS:
                script += "alpha_plus.py";
                break;
            case INDUCTIVE:
                script += "inductive.py";
                break;
            case HEURISTICS:
                script += "heuristics.py";
                break;
        }
        return script;
    }
}


