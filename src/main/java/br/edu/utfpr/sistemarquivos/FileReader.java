package br.edu.utfpr.sistemarquivos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileReader {

    public void read(Path path) throws IOException{
        // TODO implementar a leitura dos arquivos do PATH aqui
        Files.readAllLines(path).stream().forEach(System.out::println);
    }
}
