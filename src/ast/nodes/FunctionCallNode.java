package ast.nodes;

import ast.Closure;
import ast.EvaluationException;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.FunType;
import ast.typesystem.types.Type;
import environment.Environment;
import environment.TypeEnvironment;

/**
 * Represents a function application.
 *
 * Example:
 * f(x)
 */
public final class FunctionCallNode extends SyntaxNode {
    private final SyntaxNode function;
    private final SyntaxNode argument;

    /**
     * Constructs a function call node.
     *
     * @param function the function expression
     * @param argument the argument expression
     * @param line     the line number
     */
    public FunctionCallNode(SyntaxNode function, SyntaxNode argument, long line) {
        super(line);
        this.function = function;
        this.argument = argument;
    }

    /**
     * Evaluate the function call.
     */
    @Override
    public Object evaluate(Environment env) throws EvaluationException {
        Object funVal = function.evaluate(env);
        Object argVal = argument.evaluate(env);

        if (!(funVal instanceof Closure)) {
            logError("Attempted to call a non-function value.");
            throw new EvaluationException();
        }

        Closure closure = (Closure) funVal;

        // Create a new environment for the function call
        Environment callEnv = closure.getEnv().copy();
        callEnv.updateEnvironment(closure.getVar(), argVal);

        // Evaluate the body of the function in the new environment
        return closure.getBody().evaluate(callEnv);
    }

    /**
     * Determine the type of the function call.
     */
    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer)
            throws TypeException {
        Type funType = function.typeOf(tenv, inferencer);
        Type argType = argument.typeOf(tenv, inferencer);

        Type resultType = tenv.getTypeVariable(); // fresh type variable for result
        inferencer.unify(funType, new FunType(argType, resultType), "Unifying function type in FunctionCallNode");

        return inferencer.getSubstitutions().apply(resultType);
    }

    /**
     * Display the AST subtree.
     */
    @Override
    public void displaySubtree(int indentAmt) {
        printIndented("Call(", indentAmt);
        function.displaySubtree(indentAmt + 2);
        argument.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }
}
