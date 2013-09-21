package yabby.util;



import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import yabby.evolution.alignment.*;
import yabby.evolution.datatype.DataType;
import yabby.evolution.tree.TraitSet;
import yabby.evolution.tree.Tree;




//TODO: handle taxon sets
//begin sets;
//taxset junk1 = P._articulata P._gracilis P._fimbriata;
//taxset junk2 = P._robusta;
//end;


/**
 * parses nexus file and grabs alignment and calibration from the file *
 */
public class NexusParser {
    /**
     * keep track of nexus file line number, to report when the file does not parse *
     */
    int lineNr;

    /**
     * Beast II objects reconstructed from the file*
     */
    public Alignment m_alignment;
    public List<Alignment> filteredAlignments = new ArrayList<Alignment>();
    public TraitSet traitSet;

    public List<String> taxa;
    public List<Tree> trees;

    static Set<String> g_sequenceIDs;

    static {
        g_sequenceIDs = new HashSet<String>();
    }

    public List<TaxonSet> taxonsets = new ArrayList<TaxonSet>();

    private List<NexusParserListener> listeners = new ArrayList<NexusParserListener>();

    public void addListener(NexusParserListener listener) {
        listeners.add(listener);
    }

    /**
     * Try to parse BEAST 2 objects from the given file
     *
     * @param file the file to parse.
     */
    public void parseFile(File file) throws Exception {
        String fileName = file.getName().replaceAll(".*[\\/\\\\]", "").replaceAll("\\..*", "");

        parseFile(fileName, new FileReader(file));
    }

    /**
     * try to reconstruct Beast II objects from the given reader
     *
     * @param id     a name to give to the parsed results
     * @param reader a reader to parse from
     */
    public void parseFile(String id, Reader reader) throws Exception {
        lineNr = 0;
        BufferedReader fin = null;
        if (reader instanceof BufferedReader) {
            fin = (BufferedReader) reader;
        } else {
            fin = new BufferedReader(reader);
        }
        try {
            while (fin.ready()) {
                String sStr = nextLine(fin);
                if (sStr == null) {
                    return;
                }
                if (sStr.toLowerCase().matches("^\\s*begin\\s+data;\\s*$") || sStr.toLowerCase().matches("^\\s*begin\\s+characters;\\s*$")) {
                    m_alignment = parseDataBlock(fin);
                    m_alignment.setID(id);
                } else if (sStr.toLowerCase().matches("^\\s*begin\\s+calibration;\\s*$")) {
                    traitSet = parseCalibrationsBlock(fin);
                } else if (sStr.toLowerCase().matches("^\\s*begin\\s+assumptions;\\s*$") ||
                        sStr.toLowerCase().matches("^\\s*begin\\s+sets;\\s*$") ||
                        sStr.toLowerCase().matches("^\\s*begin\\s+mrbayes;\\s*$")) {
                    parseAssumptionsBlock(fin);
                } else if (sStr.toLowerCase().matches("^\\s*begin\\s+taxa;\\s*$")) {
                    parseTaxaBlock(fin);
                } else if (sStr.toLowerCase().matches("^\\s*begin\\s+trees;\\s*$")) {
                    parseTreesBlock(fin);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Around line " + lineNr + "\n" + e.getMessage());
        }
    } // parseFile

    private void parseTreesBlock(BufferedReader fin) throws Exception {
        trees = new ArrayList<Tree>();
        // read to first non-empty line within trees block
        String sStr = fin.readLine().trim();
        while (sStr.equals("")) {
            sStr = fin.readLine().trim();
        }

        int origin = -1;

        Map<String,String> translationMap = null;
        // if first non-empty line is "translate" then parse translate block
        if (sStr.toLowerCase().indexOf("translate") >= 0) {
            translationMap = parseTranslateBlock(fin);
            origin = getIndexedTranslationMapOrigin(translationMap);
            if (origin != -1) {
                taxa = getIndexedTranslationMap(translationMap, origin);
            }
        }

        // read trees
        while (sStr != null) {
            if (sStr.toLowerCase().startsWith("tree ")) {
                int i = sStr.indexOf('(');
                if (i > 0) {
                    sStr = sStr.substring(i);
                }
                TreeParser treeParser = null;

                if (origin != -1) {
                    treeParser = new TreeParser(taxa, sStr, origin, false);
                } else {
                    try {
                        treeParser = new TreeParser(taxa, sStr, 0, false);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        treeParser = new TreeParser(taxa, sStr, 1, false);
                    }
                }
//                catch (NullPointerException e) {
//                    treeParser = new TreeParser(m_taxa, sStr, 1);
//                }

                for (NexusParserListener listener : listeners) {
                    listener.treeParsed(trees.size(), treeParser);
                }

                if (translationMap != null) treeParser.translateLeafIds(translationMap);

                trees.add(treeParser);

//				Node tree = treeParser.getRoot();
//				tree.sort();
//				tree.labelInternalNodes(nNrOfLabels);
            }
            sStr = fin.readLine();
            if (sStr != null) sStr = sStr.trim();
        }
    }

    private  List<String> getIndexedTranslationMap(Map<String, String> translationMap, int origin) {

        System.out.println("translation map size = " + translationMap.size());

        String[] taxa = new String[translationMap.size()];

        for (String key : translationMap.keySet()) {
            taxa[Integer.parseInt(key)-origin] = translationMap.get(key);
        }
        return Arrays.asList(taxa);
    }

    /**
     * @param translationMap
     * @return minimum key value if keys are a contiguous set of integers starting from zero or one, -1 otherwise
     */
    private int getIndexedTranslationMapOrigin(Map<String, String> translationMap) {

        SortedSet<Integer> indices = new TreeSet<Integer>();

        int count = 0;
        for (String key : translationMap.keySet()) {
            int index = Integer.parseInt(key);
            indices.add(index);
            count += 1;
        }
        if ((indices.last() - indices.first() == count - 1) && (indices.first() == 0 || indices.first() == 1)) {
            return indices.first();
        }
        return -1;
    }

    /**
     * @param reader a reader
     * @return a map of taxa translations, keys are generally integer node number starting from 1
     * whereas values are generally descriptive strings.
     * @throws IOException
     */
    private Map<String, String> parseTranslateBlock(BufferedReader reader) throws IOException {

        Map<String,String> translationMap = new HashMap<String, String>();

        String line = reader.readLine();
        StringBuilder translateBlock = new StringBuilder();
        while (line != null && !line.trim().toLowerCase().equals(";")) {
            translateBlock.append(line.trim());
            line = reader.readLine();
        }
        String[] taxaTranslations = translateBlock.toString().split(",");
        for (String taxaTranslation : taxaTranslations) {
            String[] translation = taxaTranslation.split("[\t ]+");
            if (translation.length == 2) {
                translationMap.put(translation[0],translation[1]);
//                System.out.println(translation[0] + " -> " + translation[1]);
            } else {
                System.err.println("Ignoring translation:" + Arrays.toString(translation));
            }
        }
        return translationMap;
    }

    private void parseTaxaBlock(BufferedReader fin) throws Exception {
        taxa = new ArrayList<String>();
        int nTaxaExpected = -1;
        String sStr;
        do {
            sStr = nextLine(fin);
            if (sStr.toLowerCase().matches("\\s*dimensions\\s.*")) {
                sStr = sStr.substring(sStr.toLowerCase().indexOf("ntax=") + 5);
                sStr = sStr.replaceAll(";", "");
                nTaxaExpected = Integer.parseInt(sStr.trim());
            } else if (sStr.toLowerCase().trim().equals("taxlabels")) {
                do {
                    sStr = nextLine(fin);
                    sStr = sStr.replaceAll(";", "");
                    sStr = sStr.trim();
                    if (sStr.length() > 0 && !sStr.toLowerCase().equals("end")) {
                        for (String taxon : sStr.split("\\s+")) {
                            taxa.add(taxon);
                        }
                    }
                } while (!sStr.toLowerCase().equals("end"));
            }
        } while (!sStr.toLowerCase().equals("end"));
        if (nTaxaExpected >= 0 && taxa.size() != nTaxaExpected) {
            throw new Exception("Taxa block: # taxa is not equal to dimension");
        }
    }

    /**
     * parse calibrations block and create TraitSet *
     */
    TraitSet parseCalibrationsBlock(BufferedReader fin) throws Exception {
        TraitSet traitSet = new TraitSet();
        traitSet.traitNameInput.setValue("date", traitSet);
        String sStr = null;
        do {
            sStr = nextLine(fin);
            if (sStr.toLowerCase().contains("options")) {
                String sScale = getAttValue("scale", sStr);
                if (sScale.endsWith("s")) {
                    sScale = sScale.substring(0, sScale.length() - 1);
                }
                traitSet.unitsInput.setValue(sScale, traitSet);
            }
        } while (sStr.toLowerCase().contains("tipcalibration"));

        String sText = "";
        while (true) {
            sStr = nextLine(fin);
            if (sStr.contains(";")) {
                break;
            }
            sText += sStr;
        }
        ;
        String[] sStrs = sText.split(",");
        sText = "";
        for (String sStr2 : sStrs) {
            String[] sParts = sStr2.split(":");
            String sDate = sParts[0].replaceAll(".*=\\s*", "");
            String[] sTaxa = sParts[1].split("\\s+");
            for (String sTaxon : sTaxa) {
                if (!sTaxon.matches("^\\s*$")) {
                    sText += sTaxon + "=" + sDate + ",\n";
                }
            }
        }
        sText = sText.substring(0, sText.length() - 2);
        traitSet.traitsInput.setValue(sText, traitSet);
        TaxonSet taxa = new TaxonSet();
        taxa.initByName("alignment", m_alignment);
        traitSet.taxaInput.setValue(taxa, traitSet);

        traitSet.initAndValidate();
        return traitSet;
    } // parseCalibrations


    /**
     * parse data block and create Alignment *
     */
    public Alignment parseDataBlock(BufferedReader fin) throws Exception {

        Alignment alignment = new Alignment();

        String sStr = null;
        int nTaxa = -1;
        int nChar = -1;
        int nTotalCount = 4;
        String sMissing = "?";
        String sGap = "-";
        // indicates character matches the one in the first sequence
        String sMatchChar = null;
        do {
            sStr = nextLine(fin);

            //dimensions ntax=12 nchar=898;
            if (sStr.toLowerCase().contains("dimensions")) {
                while (sStr.indexOf(';') < 0) {
                    sStr += nextLine(fin);
                }
                sStr = sStr.replace(";", " ");

                String sChar = getAttValue("nchar", sStr);
                if (sChar == null) {
                    throw new Exception("nchar attribute expected (e.g. 'dimensions char=123') expected, not " + sStr);
                }
                nChar = Integer.parseInt(sChar);
                String sTaxa = getAttValue("ntax", sStr);
                if (sTaxa != null) {
                    nTaxa = Integer.parseInt(sTaxa);
                }
            } else if (sStr.toLowerCase().contains("format")) {
                while (sStr.indexOf(';') < 0) {
                    sStr += nextLine(fin);
                }
                sStr = sStr.replace(";", " ");

                //format datatype=dna interleave=no gap=-;
                String sDataType = getAttValue("datatype", sStr);
                String sSymbols = getAttValue("symbols", sStr);
                if (sDataType == null) {
                    System.out.println("Warning: expected datatype (e.g. something like 'format datatype=dna;') not '" + sStr + "' Assuming integer dataType");
                    alignment.dataTypeInput.setValue("integer", alignment);
                } else if (sDataType.toLowerCase().equals("rna") || sDataType.toLowerCase().equals("dna") || sDataType.toLowerCase().equals("nucleotide")) {
                    alignment.dataTypeInput.setValue("nucleotide", alignment);
                    nTotalCount = 4;
                } else if (sDataType.toLowerCase().equals("aminoacid") || sDataType.toLowerCase().equals("protein")) {
                    alignment.dataTypeInput.setValue("aminoacid", alignment);
                    nTotalCount = 20;
                } else if (sDataType.toLowerCase().equals("standard") && sSymbols.equals("01")) {
                    alignment.dataTypeInput.setValue("binary", alignment);
                    nTotalCount = 2;
                } else {
                    alignment.dataTypeInput.setValue("integer", alignment);
                    if (sSymbols != null && (sSymbols.equals("01") || sSymbols.equals("012"))) {
                        nTotalCount = sSymbols.length();
                    }
                }
                String sMissingChar = getAttValue("missing", sStr);
                if (sMissingChar != null) {
                    sMissing = sMissingChar;
                }
                String sGapChar = getAttValue("gap", sStr);
                if (sGapChar != null) {
                    sGap = sGapChar;
                }
                sMatchChar = getAttValue("matchchar", sStr);
            }
        } while (!sStr.toLowerCase().contains("matrix"));

        // read character data
        Map<String, String> seqMap = new HashMap<String, String>();
        List<String> sTaxa = new ArrayList<String>();
        String sPrevTaxon = null;
        while (true) {
            sStr = nextLine(fin);
            if (sStr.contains(";")) {
                break;
            }

            int iStart = 0, iEnd = 0;
            String sTaxon;
            while (Character.isWhitespace(sStr.charAt(iStart))) {
                iStart++;
            }
            if (sStr.charAt(iStart) == '\'' || sStr.charAt(iStart) == '\"') {
                char c = sStr.charAt(iStart);
                iStart++;
                iEnd = iStart;
                while (sStr.charAt(iEnd) != c) {
                    iEnd++;
                }
                sTaxon = sStr.substring(iStart, iEnd);
                iEnd++;
            } else {
                iEnd = iStart;
                while (iEnd < sStr.length() && !Character.isWhitespace(sStr.charAt(iEnd))) {
                    iEnd++;
                }
                if (iEnd < sStr.length()) {
                    sTaxon = sStr.substring(iStart, iEnd);
                } else {
                    sTaxon = sPrevTaxon;
                    if (sTaxon == null) {
                        throw new Exception("Could not recognise taxon");
                    }
                    iEnd = iStart;
                }
            }
            sPrevTaxon = sTaxon;
            String sData = sStr.substring(iEnd);
            sData = sData.replaceAll("\\s", "");

//			String [] sStrs = sStr.split("\\s+");
//			String sTaxon = sStrs[0];
//			for (int k = 1; k < sStrs.length - 1; k++) {
//				sTaxon += sStrs[k];
//			}
//			sTaxon = sTaxon.replaceAll("'", "");
//			System.err.println(sTaxon);
//			String sData = sStrs[sStrs.length - 1];

            if (seqMap.containsKey(sTaxon)) {
                seqMap.put(sTaxon, seqMap.get(sTaxon) + sData);
            } else {
                seqMap.put(sTaxon, sData);
                sTaxa.add(sTaxon);
            }
        }
        if (nTaxa > 0 && sTaxa.size() > nTaxa) {
            throw new Exception("Wrong number of taxa. Perhaps a typo in one of the taxa: " + sTaxa);
        }
        for (String sTaxon : sTaxa) {
            String sData = seqMap.get(sTaxon);

            if (sData.length() != nChar) {
                throw new Exception(sStr + "\nExpected sequence of length " + nChar + " instead of " + sData.length() + " for taxon " + sTaxon);
            }
            // map to standard missing and gap chars
            sData = sData.replace(sMissing.charAt(0), DataType.MISSING_CHAR);
            sData = sData.replace(sGap.charAt(0), DataType.GAP_CHAR);

            // resolve matching char, if any
            if (sMatchChar != null && sData.contains(sMatchChar)) {
                char cMatchChar = sMatchChar.charAt(0);
                String sBaseData = seqMap.get(sTaxa.get(0));
                for (int i = 0; i < sData.length(); i++) {
                    if (sData.charAt(i) == cMatchChar) {
                        char cReplaceChar = sBaseData.charAt(i);
                        sData = sData.substring(0, i) + cReplaceChar + (i + 1 < sData.length() ? sData.substring(i + 1) : "");
                    }
                }
            }

            Sequence sequence = new Sequence();
            sequence.init(nTotalCount, sTaxon, sData);
            sequence.setID(generateSequenceID(sTaxon));
            alignment.sequenceInput.setValue(sequence, alignment);
        }
        alignment.initAndValidate();
        if (nTaxa > 0 && nTaxa != alignment.getNrTaxa()) {
            throw new Exception("dimensions block says there are " + nTaxa + " taxa, but there were " + alignment.getNrTaxa() + " taxa found");
        }
        return alignment;
    } // parseDataBlock


    /**
     * parse assumptions block
     * begin assumptions;
     * charset firsthalf = 1-449;
     * charset secondhalf = 450-898;
     * charset third = 1-457\3 662-896\3;
     * end;
     */
    void parseAssumptionsBlock(BufferedReader fin) throws Exception {
        String sStr;
        do {
            sStr = nextLine(fin);
            if (sStr.toLowerCase().matches("\\s*charset\\s.*")) {
                sStr = sStr.replaceAll("^\\s+", "");
                sStr = sStr.replaceAll(";", "");
                String[] sStrs = sStr.split("\\s+");
                String sID = sStrs[1];
                String sRange = "";
                for (int i = 3; i < sStrs.length; i++) {
                    sRange += sStrs[i] + " ";
                }
                sRange = sRange.trim().replace(' ', ',');
                FilteredAlignment alignment = new FilteredAlignment();
                alignment.setID(sID);
                alignment.alignmentInput.setValue(m_alignment, alignment);
                alignment.filterInput.setValue(sRange, alignment);
                alignment.initAndValidate();
                filteredAlignments.add(alignment);
            }
        } while (!sStr.toLowerCase().contains("end;"));
    }

    /**
     * parse sets block
     * BEGIN Sets;
     * TAXSET 'con' = 'con_SL_Gert2' 'con_SL_Tran6' 'con_SL_Tran7' 'con_SL_Gert6';
     * TAXSET 'spa' = 'spa_138a_Cerb' 'spa_JB_Eyre1' 'spa_JB_Eyre2';
     * END; [Sets]
     */
    void parseSetsBlock(BufferedReader fin) throws Exception {
        String sStr;
        do {
            sStr = nextLine(fin);
            if (sStr.toLowerCase().matches("\\s*taxset\\s.*")) {
                sStr = sStr.replaceAll("^\\s+", "");
                sStr = sStr.replaceAll(";", "");
                String[] sStrs = sStr.split("\\s+");
                String sID = sStrs[1];
                sID = sID.replaceAll("'\"", "");
                TaxonSet set = new TaxonSet();
                set.setID(sID);
                for (int i = 3; i < sStrs.length; i++) {
                    sID = sStrs[i];
                    sID = sID.replaceAll("'\"", "");
                    Taxon taxon = new Taxon();
                    taxon.setID(sID);
                    set.taxonsetInput.setValue(taxon, set);
                }
                taxonsets.add(set);
            }
        } while (!sStr.toLowerCase().contains("end;"));
    }

    private String generateSequenceID(String sTaxon) {
        String sID = "seq_" + sTaxon;
        int i = 0;
        while (g_sequenceIDs.contains(sID + (i > 0 ? i : ""))) {
            i++;
        }
        sID = sID + (i > 0 ? i : "");
        g_sequenceIDs.add(sID);
        return sID;
    }

    /**
     * read line from nexus file *
     */
    String readLine(BufferedReader fin) throws Exception {
        if (!fin.ready()) {
            return null;
        }
        lineNr++;
        return fin.readLine();
    }

    /**
     * read next line from nexus file that is not a comment and not empty *
     */
    String nextLine(BufferedReader fin) throws Exception {
        String sStr = readLine(fin);
        if (sStr == null) {
            return null;
        }
        if (sStr.contains("[")) {
            int iStart = sStr.indexOf('[');
            int iEnd = sStr.indexOf(']', iStart);
            while (iEnd < 0) {
                sStr += readLine(fin);
                iEnd = sStr.indexOf(']', iStart);
            }
            sStr = sStr.substring(0, iStart) + sStr.substring(iEnd + 1);
            if (sStr.matches("^\\s*$")) {
                return nextLine(fin);
            }
        }
        if (sStr.matches("^\\s*$")) {
            return nextLine(fin);
        }
        return sStr;
    }

    /**
     * return attribute value as a string *
     */
    String getAttValue(String sAttribute, String sStr) {
        Pattern pattern = Pattern.compile(".*" + sAttribute + "\\s*=\\s*([^\\s;]+).*");
        Matcher matcher = pattern.matcher(sStr.toLowerCase());
        if (!matcher.find()) {
            return null;
        }
        String sAtt = matcher.group(1);
        if (sAtt.startsWith("\"") && sAtt.endsWith("\"")) {
            int iStart = matcher.start(1);
            sAtt = sStr.substring(iStart + 1, sStr.indexOf('"', iStart + 1));
        }
        return sAtt;
    }

    public static void main(String[] args) {
        try {
            NexusParser parser = new NexusParser();
            parser.parseFile(new File(args[0]));
            if (parser.taxa != null) {
                System.out.println(parser.taxa.size() + " taxa");
                System.out.println(Arrays.toString(parser.taxa.toArray(new String[0])));
            }
            if (parser.trees != null) {
                System.out.println(parser.trees.size() + " trees");
            }
            if (parser.m_alignment != null) {
                String sXML = new XMLProducer().toXML(parser.m_alignment);
                System.out.println(sXML);
            }
            if (parser.traitSet != null) {
                String sXML = new XMLProducer().toXML(parser.traitSet);
                System.out.println(sXML);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    } // main

} // class NexusParser
