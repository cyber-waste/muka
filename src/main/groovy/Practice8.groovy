import groovy.transform.ToString

/**
 * @author yaroslav.yermilov
 */
Random RANDOM = new Random()

int COUNT = 50


List booksPaths = getBooksPaths(COUNT)
List books = booksPaths.collect { path ->
    Book.loadFrom(path)
}

int clustersCount = Math.sqrt(books.size()) as int
List clusters = []

clustersCount.times {
    int index = RANDOM.nextInt(books.size())
    clusters << [ books[index] ]
    books.remove(index)
}

books.each { book ->
    def nearestCluster = clusters.min { cluster ->
        book.distanceTo(cluster[0])
    }
    nearestCluster << book
}

clusters.each { cluster ->
    println ("=" * 20)
    cluster.each { book ->
        println book.path
    }
    println ("=" * 20)
}

List getBooksPaths(int count) {
    Random RANDOM = new Random()
    File LIBRARY_ROOT = new File('F:\\Google Drive\\Library\\_calibre')

    List ALL_BOOKS_PATHS = []
    LIBRARY_ROOT.eachFileRecurse { path ->
        if (path.file && (path.absolutePath.endsWith('txt') || path.absolutePath.endsWith('fb2'))) {
            ALL_BOOKS_PATHS << path.absolutePath
        }
    }

    List result = []
    Math.min(count, ALL_BOOKS_PATHS.size()).times {
        int index = RANDOM.nextInt(ALL_BOOKS_PATHS.size())
        result << ALL_BOOKS_PATHS[index]
        ALL_BOOKS_PATHS.remove(index)
    }

    return result
}

@ToString
class Book {

    String path
    Map bagOfWords

    static Book loadFrom(String path) {
        Map bagOfWords = [:]
        List words = new File(path).text
                                    .split()
                                    .collect {
                                        it.toLowerCase().replaceAll('<p>', '').replaceAll('</p>', '')
                                    }
                                    .grep {
                                        !it.isEmpty() && !it.startsWith('<') && !it.endsWith('>') && it.length() < 20
                                    }
                                    .each {
                                        bagOfWords[it] = ((bagOfWords[it]?:0) + 1) as int
                                    }

        return new Book(path: path, bagOfWords: bagOfWords)
    }

    double distanceTo(Book other) {
        double thisSize = 0, otherSize = 0, scalar = 0

        this.bagOfWords.each { String word, int count ->
            if (!other.bagOfWords[word]) {
                other.bagOfWords[word] = 0
            }
            thisSize += count*count
        }

        other.bagOfWords.each { String word, int count ->
            if (!this.bagOfWords[word]) {
                this.bagOfWords[word] = 0
            }
            otherSize += count*count
        }

        this.bagOfWords.each { String word, int count ->
            scalar += count * other.bagOfWords[word]
        }

        return scalar / Math.sqrt(thisSize * otherSize)
    }
}