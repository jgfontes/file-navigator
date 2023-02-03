package br.edu.utfpr.sistemarquivos;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public enum Command {

    LIST() {
        @Override
        boolean accept(String command) {
            final var commands = command.split(" ");
            return commands.length > 0 && commands[0].startsWith("LIST") || commands[0].startsWith("list");
        }

        @Override
        Path execute(Path path) throws IOException {
            Arrays.stream(path.toFile().listFiles())
                    .map(File::getName)
                    .forEach(System.out::println);

            return path;
        }
    },
    SHOW() {
        private String[] parameters = new String[]{};

        @Override
        void setParameters(String[] parameters) {
            this.parameters = parameters;
        }

        @Override
        boolean accept(String command) {
            final var commands = command.split(" ");
            return commands.length > 0 && commands[0].startsWith("SHOW") || commands[0].startsWith("show");
        }

        @Override
        Path execute(Path path) throws IOException {
            FileReader fileReader = new FileReader();
            if(parameters.length <= 1) {
                throw new UnsupportedOperationException("SHOW Command needs an input. Example: 'OPEN teste.txt'. Try again.");
            }
            String fileName = parameters[1];

            //Validate if file has extension and if directory contains the file
            if(getFileExtension(fileName).isEmpty() || !directoryHasFile(path, fileName)) {
                throw new UnsupportedOperationException("The file cannot be a folder and should be contained in the directory. Try again.");
            }

            //

            fileReader.read(Paths.get(path.toString() + File.separatorChar + fileName));

            return path;
        }
    },
    BACK() {
        @Override
        boolean accept(String command) {
            final var commands = command.split(" ");
            return commands.length > 0 && commands[0].startsWith("BACK") || commands[0].startsWith("back");
        }

        @Override
        Path execute(Path path) {

            //Validate if application is already on ROOT
            if(path.toString().equals(Application.ROOT)) {
                throw new UnsupportedOperationException("Cannot go back! The application is already on its root.");
            }
            System.out.println("Printing new path: " + path.getParent());
            return path.getParent();
        }
    },
    OPEN() {
        private String[] parameters = new String[]{};

        @Override
        void setParameters(String[] parameters) {
            this.parameters = parameters;
        }

        @Override
        boolean accept(String command) {
            final var commands = command.split(" ");
            return commands.length > 0 && commands[0].startsWith("OPEN") || commands[0].startsWith("open");
        }

        @Override
        Path execute(Path path) {
            if(parameters.length <= 1) {
                throw new UnsupportedOperationException("OPEN Command needs an input. Example: 'OPEN teste.txt'. Try again.");
            }
            String folderName = parameters[1];

            //Validate if file has extension and if directory contains the file
            if(!getFileExtension(folderName).isEmpty() || !directoryHasFile(path, folderName)) {
                throw new UnsupportedOperationException("The destination cannot be a file, should be a folder contained in the actual directory. Try again.");
            }
            path = Paths.get(path.toString() + File.separatorChar + folderName);
            System.out.println(path.toString());
            return path;
        }
    },
    DETAIL() {
        private String[] parameters = new String[]{};

        @Override
        void setParameters(String[] parameters) {
            this.parameters = parameters;
        }

        @Override
        boolean accept(String command) {
            final var commands = command.split(" ");
            return commands.length > 0 && commands[0].startsWith("DETAIL") || commands[0].startsWith("detail");
        }

        @Override
        Path execute(Path path) {
            if(parameters.length <= 1) {
                throw new UnsupportedOperationException("SHOW Command needs an input. Example: 'OPEN teste.txt'. Try again.");
            }
            String fileName = parameters[1];

            //Validate if file has extension and if directory contains the file
            if(!directoryHasFile(path, fileName)) {
                throw new UnsupportedOperationException("The file/folder should exist in the current directory. Try again.");
            }

            BasicFileAttributeView view = Files.getFileAttributeView(
                    Path.of(path.toString() + File.separatorChar + fileName),
                    BasicFileAttributeView.class
            );
            try {
                BasicFileAttributes attrs = view.readAttributes();
                System.out.println("Is directory: " + attrs.isDirectory());
                System.out.println("Size: " + attrs.size());
                System.out.println("Created on: " + attrs.creationTime().toString());
                System.out.println("Last access time: " + attrs.lastAccessTime().toString());
                System.out.println();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return path;
        }
    },
    EXIT() {
        @Override
        boolean accept(String command) {
            final var commands = command.split(" ");
            return commands.length > 0 && commands[0].startsWith("EXIT") || commands[0].startsWith("exit");
        }

        @Override
        Path execute(Path path) {
            System.out.print("Saindo...");
            return path;
        }

        @Override
        boolean shouldStop() {
            return true;
        }
    };

    abstract Path execute(Path path) throws IOException;

    abstract boolean accept(String command);

    void setParameters(String[] parameters) {
    }

    boolean shouldStop() {
        return false;
    }

    protected Optional<String> getFileExtension(String fileName) {
        String[] fileNameArray = fileName.split("\\.");

        //Check if array has only one element
        if(Integer.compare(fileNameArray.length, 1) == 0) {
            return Optional.empty();
        }

        String extension = fileNameArray[fileNameArray.length-1];

        //Declare regex
        boolean regexStatement = Pattern.compile("[a-zA-Z]{3}").matcher(extension).matches();
        if(regexStatement) {
            return Optional.ofNullable(extension);
        }
        return Optional.empty();
    }

    protected boolean directoryHasFile(Path path, String fileName) {
        boolean match = Arrays
                .stream(path.toFile().listFiles())
                .map(File::getName)
                .anyMatch(fileName::equals);
        return match ? true:false;
    }

    public static Command parseCommand(String commandToParse) {

        if (commandToParse.isBlank()) {
            throw new UnsupportedOperationException("Type something...");
        }

        final var possibleCommands = values();

        for (Command possibleCommand : possibleCommands) {
            if (possibleCommand.accept(commandToParse)) {
                possibleCommand.setParameters(commandToParse.split(" "));
                return possibleCommand;
            }
        }

        throw new UnsupportedOperationException("Can't parse command [%s]".formatted(commandToParse));
    }
}
