/*
*   This File is part of the Colliery_source Project from PROSLabTeam
*   https://bitbucket.org/proslabteam/colliery_source/src/master/
*   Thanks to Lorenzo Rossi for granting permissions
* */

package discovery.ProcessDiscovery.it.unicam.pros.colliery;

import discovery.ProcessDiscovery.it.unicam.pros.colliery.core.CollaborationMiner;
import discovery.ProcessDiscovery.it.unicam.pros.colliery.core.DiscoveryAlgorithm;
import discovery.ProcessDiscovery.it.unicam.pros.colliery.core.Pm4PyBridge;

import javax.script.ScriptException;
import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Test {

    public static void main(String[] args) throws IOException {
//        BufferedReader bR = new BufferedReader(new FileReader(".\\logs\\log.xes"));
//        BufferedWriter bW = new BufferedWriter(new FileWriter(".\\logs\\logFresh.xes"));
//        String line = bR.readLine();
//        int i = 0;
//        while (line!=null){
//            i++;
//            line = line.replace("case_", "case_"+i);
//            bW.write(line);
//            line = bR.readLine();
//        }
//        bR.close();
//        bW.close();
        //Load XES files
        String[] logs = listFilesForFolder(new File("./logs"));
        String finalModel = null;
        try{
            finalModel = CollaborationMiner.mineLogs(logs, DiscoveryAlgorithm.SPLIT_MINER);
            stringToFile(new File("collaboration.bpmn"), finalModel);
        } catch (Exception e){
            e.printStackTrace();
        }


    }

    private static void stringToFile(File file, String xml) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(xml);
        writer.close();

    }

    public static String[] listFilesForFolder(final File folder) {
        List<String> files = new ArrayList<>();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                continue;//listFilesForFolder(fileEntry);
            } else {
                files.add(folder.getPath()+File.separator+fileEntry.getName());
            }
        }
        String[] ret = new String[files.size()];
        for(int i = 0; i<files.size(); i++) ret[i] = files.get(i);
        return ret;
    }

}
