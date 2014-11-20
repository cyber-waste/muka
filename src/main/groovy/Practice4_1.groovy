import com.github.cyberwaste.muka.SuffixTree

/**
 * @author yaroslav.yermilov
 */

def suffixTree = new SuffixTree(documents: (1..1).collect { "text-${it}.txt" })
suffixTree.init()

println suffixTree.root