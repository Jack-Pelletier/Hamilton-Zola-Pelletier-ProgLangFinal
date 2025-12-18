import org.junit.Test;

public class MatchTests extends LangTest
{
    /*
     * Basic matching: literals, wildcard, variable pattern.
     *
     * Notes based on your MatchNode:
     * - Patterns supported: LiteralPattern (int/bool/string), VarPattern, WildcardPattern (_)
     * - Non-exhaustive matches should throw an EvaluationException at runtime.
     * - All branches must type-check to the same type.
     */

    @Test
    public void matchIntLiteralFirstCase()
    {
        runTypeTest(
            "matchIntLiteralFirstCase","match 5 with | 5 -> 1 | _ -> 2;", "int"
        );
        runEvalTest(
            "matchIntLiteralFirstCase","match 5 with | 5 -> 1 | _ -> 2;","1"
        );
    }

    @Test
    public void matchIntLiteralFallsThroughToWildcard()
    {
        runTypeTest(
            "matchIntLiteralFallsThroughToWildcard","match 7 with | 5 -> 1 | _ -> 2;","int"
        );
        runEvalTest(
            "matchIntLiteralFallsThroughToWildcard","match 7 with | 5 -> 1 | _ -> 2;", "2"
        );
    }

    @Test
    public void matchBindsVarPattern()
    {
        runTypeTest(
            "matchBindsVarPattern","match 7 with | x -> x;","int"
        );
        runEvalTest(
            "matchBindsVarPattern","match 7 with | x -> x;","7"
        );
    }

    @Test
    public void matchBindsVarAndUsesInExpr()
    {
        runTypeTest(
            "matchBindsVarAndUsesInExpr","match 7 with | x -> x + 1;","int"
        );
        runEvalTest(
            "matchBindsVarAndUsesInExpr", "match 7 with | x -> x + 1;", "8"
        );
    }

    @Test
    public void matchStringLiteral()
    {
        runTypeTest(
            "matchStringLiteral","match \"hi\" with | \"hi\" -> 1 | _ -> 0;", "int"
        );
        runEvalTest(
            "matchStringLiteral","match \"hi\" with | \"hi\" -> 1 | _ -> 0;","1"
        );
    }

    @Test
    public void matchBoolLiteral()
    {
        runTypeTest(
            "matchBoolLiteral", "match true with | false -> 0 | true -> 1;","int"
        );
        runEvalTest(
            "matchBoolLiteral","match true with | false -> 0 | true -> 1;","1"
        );
    }

    /*
     * Order matters: earlier cases should win if multiple could match.
     * VarPattern and WildcardPattern always match, so they can “shadow” later cases.
     */
    @Test
    public void matchOrderVarBeforeLiteralWins()
    {
        runTypeTest(
            "matchOrderVarBeforeLiteralWins","match 5 with | x -> 99 | 5 -> 1;","int"
        );
        runEvalTest(
            "matchOrderVarBeforeLiteralWins","match 5 with | x -> 99 | 5 -> 1;","99"
        );
    }

    @Test
    public void matchOrderWildcardBeforeLiteralWins()
    {
        runTypeTest(
            "matchOrderWildcardBeforeLiteralWins","match 5 with | _ -> 99 | 5 -> 1;","int"
        );
        runEvalTest(
            "matchOrderWildcardBeforeLiteralWins","match 5 with | _ -> 99 | 5 -> 1;","99"
        );
    }

    /*
     * Matching inside let/val context (works with your env + type env expectations)
     *
     * IMPORTANT:
     * Your parser's let syntax is: let <id> := <expr> in <expr>
     * so these tests must use := not =
     */
    @Test
    public void matchInsideLet()
    {
        runTypeTest(
            "matchInsideLet","let x := 10 in match x with | 10 -> 1 | _ -> 2;", "int"
        );
        runEvalTest(
            "matchInsideLet","let x := 10 in match x with | 10 -> 1 | _ -> 2;",  "1"
        );
    }

    /*
     * Also test it inside val, since your project requires new features
     * to work with val/let.
     */
    @Test
    public void matchAfterVal()
    {
        runTypeTest(
            "matchAfterVal","val x := 10; match x with | 10 -> 1 | _ -> 2;","int"
        );
        runEvalTest(
            "matchAfterVal","val x := 10; match x with | 10 -> 1 | _ -> 2;","1"
        );
    }
}
