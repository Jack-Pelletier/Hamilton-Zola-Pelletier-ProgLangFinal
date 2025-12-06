
/*
 *   Copyright (C) 2022 -- 2025  Zachary A. Kissel
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import ast.EvaluationException;
import ast.SyntaxTree;
import ast.typesystem.TypeException;
import environment.Environment;
import environment.TypeEnvironment;
import merrimackutil.cli.LongOption;
import merrimackutil.cli.OptionParser;
import merrimackutil.util.Tuple;
import parser.ParseException;
import parser.MFLParser;

/**
 * This provides a simple front end to a recursive descent parser for the 
 * Merrimack Functional Language (MFL).
 * 
 * @author Zach Kissel
 */
public class Interpreter
{
    private static boolean doHelp = false;       // Perform the help option.
    private static boolean doTracing = false;    // Turn on parser tracing.
    private static boolean doFile = false;       // Run program in the file.
    private static String fileName = null;       // File containing the program.
    private static boolean displayAST = false;   // Display the AST resulting from parsing.

    /**
     * Show the license message to the screen.
     */
    public static void showLicense()
    {
        System.out.println("Copyright (C) 2021 -- 2025 Zachary Kissel");
        System.out.println("This program comes with ABSOLUTELY NO WARRANTY.");
        System.out.println(
                "This is free software, and you are welcome to redistribute it");
        System.out.println("under certain conditions.");
    }

    /**
     * Prints a usage message to the screen and exits.
     */
    public static void usage()
    {
        System.err.println("usage:");
        System.err.println("   mfl [--trace] [--ast] --file <filename>");
        System.err.println("   mfl [--trace] [--ast]");
        System.err.println("   mfl --help");
        System.err.println("options:");
        System.err.println("--trace, -t \t\tTurn on interpreter tracing.");
        System.err.println("--file, -f \t\tInterpret the file.");
        System.err.println("--ast,-a \t\tDisplay the abstract syntax tree.");
        System.err.println("--help, -h \t\tDisplay this message");
        System.exit(1);
    }

    /**
     * Runs the interactive mode version of the interpreter.
     */
    public static void runInteractive()
    {
        String line = "";
        Scanner scan = new Scanner(System.in);
        boolean exit = false;
        MFLParser parse; // The MFL parser which builds the AST.
        SyntaxTree ast; // The AST we will use to evealuate the file.
        Environment env = null; // For the first line, mark that we don't have
                                // an environment.
        TypeEnvironment tenv = null;    // The type environment to keep around.
        
        showLicense();
        System.out.println();
        System.out.println("MFL interactive mode. Enter .quit to exit.");
        while (!exit)
        {
            System.out.print("mfl> ");
            line = scan.nextLine();

            // Interpret the line if needed.
            line.trim();
            if (!line.isEmpty() && !line.equals(".quit"))
            {
                // Try to interpret the program.
                parse = new MFLParser(line);

                // Determine if we should turn on tracing.
                if (doTracing)
                    parse.toggleTracing();

                try {
                    ast = parse.parse();
                } catch (ParseException e) {
                    System.out.println(e.getMessage());
                    continue;
                }
                
                if (displayAST)
                    ast.printTree();

                if (env != null)
                    ast.setEnvironment(env);
                    
                if (tenv != null)
                    ast.setTypeEnvironment(tenv);
                try
                {
                    String type = ast.getType();
                    Object res = ast.evaluate();
                    System.out.println(res + " : " + type);

                    // We potentially need to update the environment, as we
                    // may have defined a new named value.
                    env = ast.getEnvironment();
                    tenv = ast.getTypeEnvironment();
                }
                catch (EvaluationException ex)
                {
                    System.out.println(ex.getMessage());
                }
                catch (TypeException tex)
                {
                    System.out.println("Type Error " + tex.getMessage());
                }
            }       
            else if (line.equals(".quit"))
                exit = true;
        }
        scan.close();
    }

    /**
     * Interprets a file (non-interactive mode.)
     */
    public static void interpretFile()
    {
        MFLParser parse; // The MFL parser which builds the AST.
        SyntaxTree ast; // The AST we will use to evealuate the file.

        // Try to interpret the program.
        try
        {
            parse = new MFLParser(new File(fileName));

            // Determine if we should turn on tracing.
            if (doTracing)
                parse.toggleTracing();

            try {
                ast = parse.parse();
            } catch (ParseException e) {
                System.out.println(e.getMessage());
                return;
            }
           
            if (displayAST)
                ast.printTree();

            try
            {
                if (!ast.typeCheck())
                    System.exit(1);
                Object res = ast.evaluate();
                System.out.println(res + " : " + ast.getType());
            }
            catch (EvaluationException ex)
            {
                System.out.println(ex.getMessage());
            }
            catch (TypeException tex)
            {   
                // This should *never* happen.
                System.out.println(tex.getMessage());
            }
          
        }
        catch (FileNotFoundException ex)
        {
            System.err.println(ex);
            System.exit(1);
        }
    }

    /**
     * Process the command line arguments.
     * 
     * @param args the array of command line arguments.
     */
    public static void processArgs(String[] args)
    {
        OptionParser parser;

        LongOption[] opts = new LongOption[4];
        opts[0] = new LongOption("help", false, 'h');
        opts[1] = new LongOption("file", true, 'f');
        opts[2] = new LongOption("trace", false, 't');
        opts[3] = new LongOption("ast", false, 'a');

        Tuple<Character, String> currOpt;

        parser = new OptionParser(args);
        parser.setLongOpts(opts);
        parser.setOptString("hf:ta");

        while (parser.getOptIdx() != args.length)
        {
            currOpt = parser.getLongOpt(false);

            switch (currOpt.getFirst())
            {
            case 'h':
                doHelp = true;
                break;
            case 't':
                doTracing = true;
                break;
            case 'f':
                doFile = true;
                fileName = currOpt.getSecond();
                break;
            case 'a':
                displayAST = true;
                break;
            case '?':
                usage();
                break;
            }
        }

        // Verify the options are not conflicting.
        if (doFile && doHelp || doTracing && doHelp || displayAST && doHelp)
            usage();
    }

    /**
     * The entry point.
     * 
     * @param args the array of strings that represnt the command line
     *             arguments.
     */
    public static void main(String[] args)
    {
        // Determine if we are looking at file or command line.
        if (args.length > 4)
            usage();

        // Determine what the user requested.
        processArgs(args);

        // Perform the correct action.
        if (doFile)
            interpretFile();
        else if (doHelp)
            usage();
        else
            runInteractive();
    }
}
