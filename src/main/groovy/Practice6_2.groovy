/**
 * @author yaroslav.yermilov
*/

import com.github.cyberwaste.muka.Huffman
import org.apache.commons.io.FileUtils

def fileFrom = "sample.txt.huffman"
def fileTo = "sample.txt.huffman.txt"

def fileContent = FileUtils.readLines(new File(fileFrom))

def huffman = new Huffman()
huffman.init fileContent
def decoded = huffman.decode fileContent[-1]

FileUtils.writeStringToFile(new File(fileTo), decoded)
