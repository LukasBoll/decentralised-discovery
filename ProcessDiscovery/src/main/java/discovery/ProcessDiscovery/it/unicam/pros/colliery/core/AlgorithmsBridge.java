package discovery.ProcessDiscovery.it.unicam.pros.colliery.core;

import au.edu.qut.bpmn.io.BPMNDiagramExporter;
import au.edu.qut.bpmn.io.impl.BPMNDiagramExporterImpl;
import au.edu.qut.processmining.miners.splitminer.SplitMiner;
import au.edu.qut.processmining.miners.splitminer.ui.dfgp.DFGPUIResult;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;

import java.io.File;
import java.util.Map;


public class AlgorithmsBridge {
    public AlgorithmsBridge() {
    }

    public static String discovery(String filePath, DiscoveryAlgorithm algo, Map<String, Object> miningParams) throws Exception {
        if(algo.equals(DiscoveryAlgorithm.SPLIT_MINER)){
            SplitMiner sM = new SplitMiner();
            XesXmlParser parser = new XesXmlParser();
            XLog log = parser.parse(new File(filePath)).get(0);
            XEventClassifier classifier = new XEventNameClassifier();
            BPMNDiagram model = sM.mineBPMNModel(log, classifier, 0.8, 0.5, DFGPUIResult.FilterType.NOF, false, false, false, null );
            BPMNDiagramExporter exp = new BPMNDiagramExporterImpl();

            return exp.exportBPMNDiagram(model);
        }
        return Pm4PyBridge.discovery(filePath, algo, miningParams);
    }
}
