
/**
 * @author yaroslav.yermilov
 */
Random RANDOM = new Random()

List booksPaths = getBooksPaths()

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
        distance(book, cluster[0])
    }
    nearestCluster << book
}

clusters.each { cluster ->
    println ("=" * 20)
    cluster.each { book ->
        println book
    }
    println ("=" * 20)
}