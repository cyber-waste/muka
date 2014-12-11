import groovy.transform.ToString

/**
 * @author yaroslav.yermilov
 */
double K1 = 0.5
double B = 0.5
int COUNT = 10

//String query = "собака рыба"
//String query = "космос"
String query = "робот"


List documentsPaths = getDocumentsPaths(COUNT)
List documents = documentsPaths.collect { path ->
    Document.loadFrom(path)
}

List ranged = documents.sort { Document document ->
    query.split().sum { String term ->
        double q1 = Math.log((Document.TOTAL_WORD_COUNT + 1.0) / (Document.COMMON_BAG[term]?:0 + 1.0))
        double q2 = (K1 + 1) * (document.bagOfWords[term]?:0 as int)
        double q3 = K1 * ((1 -  B) + B * (document.length / (Document.TOTAL_WORD_COUNT / Document.DOCUMENT_COUNT))) + (document.bagOfWords[term]?:0)

        - q1 * q2 / q3
    }
}
ranged.each{ Document document ->
    println document.path
}


List getDocumentsPaths(int count) {
    Random RANDOM = new Random()
    File LIBRARY_ROOT = new File('F:\\Google Drive\\Library\\_calibre')

    List ALL_DOCUMENTS_PATHS = []
    LIBRARY_ROOT.eachFileRecurse { path ->
        if (path.file && (path.absolutePath.endsWith('txt') || path.absolutePath.endsWith('fb2'))) {
            ALL_DOCUMENTS_PATHS << path.absolutePath
        }
    }

    List result = []
    Math.min(count, ALL_DOCUMENTS_PATHS.size()).times {
        int index = RANDOM.nextInt(ALL_DOCUMENTS_PATHS.size())
        result << ALL_DOCUMENTS_PATHS[index]
        ALL_DOCUMENTS_PATHS.remove(index)
    }

    return result
}

@ToString
class Document {

    static int TOTAL_WORD_COUNT = 0
    static int DOCUMENT_COUNT = 0
    static Map COMMON_BAG = [:]

    String path
    Map bagOfWords
    int length

    static Document loadFrom(String path) {
        Map bagOfWords = [:]
        List words = new File(path).text
                                    .split()
                                    .collect {
                                        it.toLowerCase().replaceAll('<p>', '').replaceAll('</p>', '')
                                    }
                                    .grep {
                                        !it.isEmpty() && !it.startsWith('<') && !it.endsWith('>') && it.length() < 20
                                    }

        int length = words.size()
        DOCUMENT_COUNT++

        words.each {
            bagOfWords[it] = ((bagOfWords[it]?:0) + 1) as int
            COMMON_BAG[it] = ((COMMON_BAG[it]?:0) + 1) as int
            TOTAL_WORD_COUNT++
        }

        return new Document(path: path, bagOfWords: bagOfWords, length: length)
    }
}