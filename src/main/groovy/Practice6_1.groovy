/**
 * @author yaroslav.yermilov
 */

import com.github.cyberwaste.muka.Huffman
import org.apache.commons.io.FileUtils

def fileFrom = "sample.txt"
def fileTo = "sample.txt.huffman"

def fileContent = FileUtils.readFileToString(new File(fileFrom))

def huffman = new Huffman()
huffman.init fileContent
def encoded = huffman.encode fileContent

FileUtils.writeStringToFile(new File(fileTo), encoded)